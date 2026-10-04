package com.example.data.api

import android.util.Log
import com.example.BuildConfig
import com.example.data.telegram.ParsedSignal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import kotlin.math.abs

data class AiAnalysisResult(
    val approved: Boolean,
    val confidence: Int, // 0 to 100
    val risk: String, // "low", "medium", "high"
    val reason: String,
    val riskRewardRatio: Double = 0.0
)

object GeminiApiClient {
    private const val TAG = "GeminiApiClient"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent"

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun analyzeSignal(signal: ParsedSignal, customApiKey: String? = null): AiAnalysisResult = withContext(Dispatchers.IO) {
        val apiKey = when {
            !customApiKey.isNullOrBlank() -> customApiKey
            BuildConfig.GEMINI_API_KEY.isNotBlank() && BuildConfig.GEMINI_API_KEY != "MY_GEMINI_API_KEY" -> BuildConfig.GEMINI_API_KEY
            else -> ""
        }

        // Calculate quantitative Risk Reward Ratio
        val primaryTp = signal.takeProfits.firstOrNull() ?: signal.entryPrice
        val tpDistance = abs(primaryTp - signal.entryPrice)
        val slDistance = abs(signal.entryPrice - signal.stopLoss)
        val rrr = if (slDistance > 0) String.format("%.2f", tpDistance / slDistance).toDoubleOrNull() ?: 1.5 else 1.0

        if (apiKey.isBlank()) {
            // Local Quantitative AI Fallback Evaluator
            return@withContext performLocalQuantitativeAnalysis(signal, rrr)
        }

        try {
            val prompt = """
                You are a professional crypto futures risk manager.
                Analyze the following trading signal extracted from Telegram:
                - Symbol: ${signal.symbol}
                - Direction: ${signal.side}
                - Entry Price: ${signal.entryPrice}
                - Stop Loss: ${signal.stopLoss}
                - Take Profit(s): ${signal.takeProfits.joinToString(", ")}
                - Leverage: ${signal.leverage}x
                - Calculated Risk Reward Ratio: 1:$rrr
                - Raw Message:
                "${signal.rawText}"

                Perform the following checks:
                1. Format correctness (Are SL and TP prices logically positioned relative to Entry for ${signal.side}?)
                2. Risk-Reward Ratio (Ideal is >= 1:1.5)
                3. High leverage risk (Leverage > 50x is high risk)
                4. Scam indicators (e.g. guarantee of 1000% profit, pump and dump patterns, absurd unrealistic targets)
                5. Provide an overall confidence score from 0 to 100 and risk assessment.

                Respond ONLY with valid JSON in this exact structure:
                {
                  "approved": true,
                  "confidence": 92,
                  "risk": "low",
                  "reason": "Good RR ratio (1:$rrr) with logical SL and conservative leverage"
                }
            """.trimIndent()

            val requestJson = JSONObject().apply {
                val contents = JSONArray().apply {
                    val contentObj = JSONObject().apply {
                        val parts = JSONArray().apply {
                            put(JSONObject().apply { put("text", prompt) })
                        }
                        put("parts", parts)
                    }
                    put(contentObj)
                }
                put("contents", contents)

                val generationConfig = JSONObject().apply {
                    put("responseMimeType", "application/json")
                    put("temperature", 0.2)
                }
                put("generationConfig", generationConfig)
            }

            val requestBody = requestJson.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url("$BASE_URL?key=$apiKey")
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                Log.w(TAG, "Gemini API failed with code ${response.code}, falling back to local model")
                return@withContext performLocalQuantitativeAnalysis(signal, rrr)
            }

            val responseBody = response.body?.string() ?: ""
            val jsonResponse = JSONObject(responseBody)
            val candidates = jsonResponse.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val content = firstCandidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val responseText = parts?.optJSONObject(0)?.optString("text") ?: ""

            // Parse returned JSON from Gemini
            val resultJson = JSONObject(responseText)
            val approved = resultJson.optBoolean("approved", true)
            val confidence = resultJson.optInt("confidence", 85)
            val risk = resultJson.optString("risk", "low")
            val reason = resultJson.optString("reason", "Analyzed by Gemini Flash AI")

            AiAnalysisResult(
                approved = approved,
                confidence = confidence.coerceIn(0, 100),
                risk = risk,
                reason = reason,
                riskRewardRatio = rrr
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error executing Gemini API call: ${e.message}", e)
            performLocalQuantitativeAnalysis(signal, rrr)
        }
    }

    private fun performLocalQuantitativeAnalysis(signal: ParsedSignal, rrr: Double): AiAnalysisResult {
        var confidence = 85
        val reasons = mutableListOf<String>()

        // Check direction logic
        val isSlLogical = if (signal.side == "LONG") {
            signal.stopLoss < signal.entryPrice
        } else {
            signal.stopLoss > signal.entryPrice
        }

        if (!isSlLogical) {
            return AiAnalysisResult(
                approved = false,
                confidence = 20,
                risk = "high",
                reason = "Invalid Stop Loss placement: SL violates ${signal.side} position bounds.",
                riskRewardRatio = rrr
            )
        }

        // RRR check
        if (rrr >= 2.0) {
            confidence += 8
            reasons.add("Excellent R:R ratio (1:$rrr)")
        } else if (rrr >= 1.5) {
            confidence += 4
            reasons.add("Good R:R ratio (1:$rrr)")
        } else if (rrr < 1.0) {
            confidence -= 20
            reasons.add("Poor R:R ratio (<1.0)")
        }

        // Leverage check
        val riskLevel = when {
            signal.leverage > 50 -> {
                confidence -= 15
                reasons.add("High leverage (${signal.leverage}x)")
                "high"
            }
            signal.leverage > 25 -> {
                confidence -= 5
                reasons.add("Moderate leverage (${signal.leverage}x)")
                "medium"
            }
            else -> {
                confidence += 5
                reasons.add("Safe leverage (${signal.leverage}x)")
                "low"
            }
        }

        // Scam words filter
        val rawLower = signal.rawText.lowercase()
        if (rawLower.contains("100% win") || rawLower.contains("guaranteed") || rawLower.contains("pump") || rawLower.contains("all in")) {
            confidence -= 35
            reasons.add("Potential scam keywords detected")
        }

        val finalConfidence = confidence.coerceIn(10, 98)
        val approved = finalConfidence >= 65

        return AiAnalysisResult(
            approved = approved,
            confidence = finalConfidence,
            risk = riskLevel,
            reason = reasons.joinToString(". "),
            riskRewardRatio = rrr
        )
    }
}

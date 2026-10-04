package com.example.data.repository

import android.content.Context
import com.example.data.api.AiAnalysisResult
import com.example.data.api.BybitApiClient
import com.example.data.api.BybitOrderResult
import com.example.data.api.BybitPosition
import com.example.data.api.BybitWalletBalance
import com.example.data.api.GeminiApiClient
import com.example.data.local.CryptoDatabase
import com.example.data.local.entity.BotSettings
import com.example.data.local.entity.SignalHistory
import com.example.data.local.entity.TradeHistory
import com.example.data.security.KeystoreHelper
import com.example.data.telegram.ParsedSignal
import com.example.data.telegram.TelegramSignalParser
import com.example.service.NotificationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class TraderRepository(private val context: Context) {
    private val database = CryptoDatabase.getInstance(context)
    private val signalDao = database.signalDao()
    private val tradeDao = database.tradeDao()
    private val settingsDao = database.settingsDao()

    val allSignals: Flow<List<SignalHistory>> = signalDao.getAllSignals()
    val allTrades: Flow<List<TradeHistory>> = tradeDao.getAllTrades()
    val openTrades: Flow<List<TradeHistory>> = tradeDao.getOpenTrades()
    val settingsFlow: Flow<BotSettings?> = settingsDao.getSettingsFlow()

    suspend fun getSettings(): BotSettings {
        return settingsDao.getSettings() ?: BotSettings().also {
            settingsDao.saveSettings(it)
        }
    }

    suspend fun saveSettings(settings: BotSettings) = withContext(Dispatchers.IO) {
        settingsDao.saveSettings(settings)
    }

    suspend fun saveEncryptedBybitKeys(apiKey: String, secretKey: String, isTestnet: Boolean) = withContext(Dispatchers.IO) {
        val current = getSettings()
        val encryptedSecret = KeystoreHelper.encrypt(secretKey)
        val updated = current.copy(
            bybitApiKey = apiKey,
            bybitApiSecret = encryptedSecret,
            bybitTestnet = isTestnet,
            bybitConnected = apiKey.isNotBlank()
        )
        settingsDao.saveSettings(updated)
    }

    suspend fun saveEncryptedTelegram(apiId: String, apiHash: String, phone: String, channel: String) = withContext(Dispatchers.IO) {
        val current = getSettings()
        val encryptedHash = KeystoreHelper.encrypt(apiHash)
        val updated = current.copy(
            telegramApiId = apiId,
            telegramApiHash = encryptedHash,
            telegramPhone = phone,
            telegramChannel = channel,
            telegramConnected = true
        )
        settingsDao.saveSettings(updated)
    }

    suspend fun getDecryptedBybitClient(): BybitApiClient = withContext(Dispatchers.IO) {
        val settings = getSettings()
        val decryptedSecret = KeystoreHelper.decrypt(settings.bybitApiSecret)
        BybitApiClient(
            apiKey = settings.bybitApiKey,
            apiSecret = decryptedSecret,
            isTestnet = settings.bybitTestnet
        )
    }

    suspend fun fetchWalletBalance(): BybitWalletBalance = withContext(Dispatchers.IO) {
        val client = getDecryptedBybitClient()
        client.getWalletBalance()
    }

    suspend fun fetchLivePrice(symbol: String): Double = withContext(Dispatchers.IO) {
        val client = getDecryptedBybitClient()
        client.getTickerPrice(symbol)
    }

    // Process incoming Telegram Signal message
    suspend fun processIncomingTelegramMessage(
        rawMessage: String,
        channel: String = "@CryptoVipSignals"
    ): SignalHistory? = withContext(Dispatchers.IO) {
        val parsed = TelegramSignalParser.parse(rawMessage) ?: return@withContext null
        val settings = getSettings()

        // 1. Initial Signal Record
        val signal = SignalHistory(
            symbol = parsed.symbol,
            side = parsed.side,
            entry = parsed.entryPrice,
            tp = parsed.takeProfits.joinToString(", "),
            sl = parsed.stopLoss,
            leverage = if (settings.leverage > 0) settings.leverage else parsed.leverage,
            rawText = rawMessage,
            channelSource = channel,
            status = "NEW"
        )
        val signalId = signalDao.insertSignal(signal)

        // 2. Analyze with Gemini Flash AI
        val aiResult = GeminiApiClient.analyzeSignal(parsed, settings.geminiApiKeyOverride)

        val meetsConfidence = aiResult.confidence >= settings.minimumConfidence
        val newStatus = when {
            !aiResult.approved -> "REJECTED_BY_AI"
            !meetsConfidence -> "LOW_CONFIDENCE"
            settings.manualApproval -> "PENDING_APPROVAL"
            settings.autoTrade && !settings.emergencyStop -> "APPROVED"
            else -> "APPROVED"
        }

        val updatedSignal = signal.copy(
            id = signalId,
            aiConfidence = aiResult.confidence,
            aiRisk = aiResult.risk,
            aiReason = aiResult.reason,
            status = newStatus
        )
        signalDao.updateSignal(updatedSignal)

        // Notify user of new parsed signal & AI evaluation
        NotificationHelper.sendSignalAlert(
            context = context,
            symbol = parsed.symbol,
            side = parsed.side,
            confidence = aiResult.confidence,
            status = newStatus
        )

        // 3. Auto Execute if eligible
        if (newStatus == "APPROVED" && settings.autoTrade && !settings.emergencyStop) {
            executeTradeForSignal(updatedSignal)
        }

        updatedSignal
    }

    suspend fun executeTradeForSignal(signal: SignalHistory): BybitOrderResult = withContext(Dispatchers.IO) {
        val settings = getSettings()
        val client = getDecryptedBybitClient()
        val balance = client.getWalletBalance()

        // Calculate Position Size
        // Margin = Available Balance * (riskPercent / 100)
        // Position Size = Margin * Leverage
        val riskFraction = (settings.riskPercent / 100.0).coerceIn(0.01, 1.0)
        val marginUsdt = balance.totalAvailableBalance * riskFraction
        val totalPositionUsdt = marginUsdt * signal.leverage
        val entryPrice = if (signal.entry > 0) signal.entry else client.getTickerPrice(signal.symbol)
        val coinQty = if (entryPrice > 0) totalPositionUsdt / entryPrice else 0.01

        val primaryTp = signal.tp.split(",").firstOrNull()?.trim()?.toDoubleOrNull() ?: 0.0

        val orderResult = client.openFuturesOrder(
            symbol = signal.symbol,
            side = signal.side,
            qty = coinQty,
            leverage = signal.leverage,
            stopLoss = signal.sl,
            takeProfit = primaryTp,
            marginMode = settings.marginMode
        )

        if (orderResult.success) {
            signalDao.updateStatus(signal.id, "EXECUTED")

            val trade = TradeHistory(
                symbol = signal.symbol,
                side = signal.side,
                entryPrice = if (orderResult.executedPrice > 0) orderResult.executedPrice else entryPrice,
                amountUsdt = totalPositionUsdt,
                leverage = signal.leverage,
                marginMode = settings.marginMode,
                orderId = orderResult.orderId,
                signalId = signal.id,
                status = "OPEN"
            )
            tradeDao.insertTrade(trade)

            NotificationHelper.sendOrderSuccess(
                context = context,
                symbol = signal.symbol,
                side = signal.side,
                orderId = orderResult.orderId
            )
        } else {
            signalDao.updateStatus(signal.id, "EXECUTION_FAILED")
        }

        orderResult
    }

    suspend fun closeActiveTrade(trade: TradeHistory): BybitOrderResult = withContext(Dispatchers.IO) {
        val client = getDecryptedBybitClient()
        val currentPrice = client.getTickerPrice(trade.symbol)
        val qty = if (trade.entryPrice > 0) trade.amountUsdt / trade.entryPrice else 0.01

        val result = client.closePosition(trade.symbol, trade.side, qty)
        if (result.success) {
            // Calculate final PnL
            val priceDiff = if (trade.side.equals("LONG", true)) {
                currentPrice - trade.entryPrice
            } else {
                trade.entryPrice - currentPrice
            }
            val pnlPercent = if (trade.entryPrice > 0) (priceDiff / trade.entryPrice) * 100.0 * trade.leverage else 0.0
            val profitUsdt = (trade.amountUsdt / trade.leverage) * (pnlPercent / 100.0)

            tradeDao.closeTrade(
                id = trade.id,
                exitPrice = currentPrice,
                profit = if (profitUsdt >= 0) profitUsdt else null,
                loss = if (profitUsdt < 0) -profitUsdt else null,
                pnlPercent = pnlPercent,
                closeTimestamp = System.currentTimeMillis()
            )
        }
        result
    }

    suspend fun emergencyHalt() = withContext(Dispatchers.IO) {
        val current = getSettings()
        settingsDao.saveSettings(current.copy(emergencyStop = true, autoTrade = false))
    }

    suspend fun resumeTrading() = withContext(Dispatchers.IO) {
        val current = getSettings()
        settingsDao.saveSettings(current.copy(emergencyStop = false, autoTrade = true))
    }

    // Export database to JSON string for backup
    suspend fun exportDataJson(): String = withContext(Dispatchers.IO) {
        val signals = signalDao.getAllSignals().firstOrNull() ?: emptyList()
        val trades = tradeDao.getAllTrades().firstOrNull() ?: emptyList()

        val root = JSONObject()
        val signalsArray = JSONArray()
        for (s in signals) {
            signalsArray.put(JSONObject().apply {
                put("symbol", s.symbol)
                put("side", s.side)
                put("entry", s.entry)
                put("tp", s.tp)
                put("sl", s.sl)
                put("leverage", s.leverage)
                put("aiConfidence", s.aiConfidence)
                put("status", s.status)
                put("timestamp", s.timestamp)
            })
        }
        val tradesArray = JSONArray()
        for (t in trades) {
            tradesArray.put(JSONObject().apply {
                put("symbol", t.symbol)
                put("side", t.side)
                put("entryPrice", t.entryPrice)
                put("exitPrice", t.exitPrice ?: 0.0)
                put("profit", t.profit ?: 0.0)
                put("loss", t.loss ?: 0.0)
                put("pnlPercent", t.pnlPercent ?: 0.0)
                put("status", t.status)
            })
        }
        root.put("signals", signalsArray)
        root.put("trades", tradesArray)
        root.put("exportTime", System.currentTimeMillis())
        root.toString(2)
    }

    suspend fun clearHistory() = withContext(Dispatchers.IO) {
        signalDao.clearAll()
        tradeDao.clearAll()
    }
}

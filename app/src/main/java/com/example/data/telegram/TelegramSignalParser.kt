package com.example.data.telegram

import java.util.Locale

data class ParsedSignal(
    val symbol: String, // e.g. "BTCUSDT"
    val side: String, // "LONG" or "SHORT"
    val entryPrice: Double,
    val takeProfits: List<Double>,
    val stopLoss: Double,
    val leverage: Int = 20,
    val rawText: String = "",
    val isValid: Boolean = true,
    val parseError: String? = null
)

object TelegramSignalParser {

    fun parse(message: String): ParsedSignal? {
        val clean = message.trim()
        if (clean.length < 5) return null

        val lines = clean.lines().map { it.trim() }.filter { it.isNotEmpty() }
        val upperText = clean.uppercase(Locale.ROOT)

        // 1. Detect Direction
        val isLong = upperText.contains("LONG") || upperText.contains("BUY") || upperText.contains("CALL")
        val isShort = upperText.contains("SHORT") || upperText.contains("SELL") || upperText.contains("PUT")

        if (!isLong && !isShort) {
            return null // Not an actionable signal
        }
        val side = if (isLong) "LONG" else "SHORT"

        // 2. Extract Symbol (e.g., BTC, BTCUSDT, ETH, ETH/USDT, SOL, SOLUSDT)
        val symbolRegex = Regex("""([A-Z0-9]{2,10})([\/\-_]?(USDT|BUSD|USD))?""", RegexOption.IGNORE_CASE)
        val knownCoins = listOf("BTC", "ETH", "SOL", "BNB", "XRP", "ADA", "DOGE", "AVAX", "NEAR", "SUI", "APT", "PEPE", "LINK", "DOT", "MATIC", "POL")

        var extractedSymbol = ""
        // Check for common coin names first
        for (coin in knownCoins) {
            if (upperText.contains(coin)) {
                extractedSymbol = "${coin}USDT"
                break
            }
        }

        if (extractedSymbol.isEmpty()) {
            val symbolMatch = Regex("""#?([A-Z]{2,10})(USDT|\/USDT)?""").find(upperText)
            if (symbolMatch != null) {
                var sym = symbolMatch.groupValues[1]
                if (!sym.endsWith("USDT")) sym += "USDT"
                extractedSymbol = sym
            } else {
                extractedSymbol = "BTCUSDT"
            }
        }

        // 3. Extract Entry Price
        var entryPrice = 0.0
        val entryRegex = Regex("""(?:ENTRY|ENTER|BUY\s*ZONE|OPEN|PRICE)[\s:=-]+([0-9]+(?:\.[0-9]+)?)""", RegexOption.IGNORE_CASE)
        val entryMatch = entryRegex.find(clean)
        if (entryMatch != null) {
            entryPrice = entryMatch.groupValues[1].toDoubleOrNull() ?: 0.0
        } else {
            // Check lines following "Entry:"
            for (i in lines.indices) {
                if (lines[i].contains("Entry", ignoreCase = true) && i + 1 < lines.size) {
                    val nextLinePrice = Regex("""[0-9]+(?:\.[0-9]+)?""").find(lines[i + 1])
                    if (nextLinePrice != null) {
                        entryPrice = nextLinePrice.value.toDoubleOrNull() ?: 0.0
                        break
                    }
                }
            }
        }

        // 4. Extract Take Profits (TP)
        val takeProfits = mutableListOf<Double>()
        val tpMatches = Regex("""(?:TP|TARGET|TAKE\s*PROFIT)[\s\d:=-]+([0-9]+(?:\.[0-9]+)?)""", RegexOption.IGNORE_CASE).findAll(clean)
        for (m in tpMatches) {
            val tpVal = m.groupValues[1].toDoubleOrNull()
            if (tpVal != null && tpVal > 0) {
                takeProfits.add(tpVal)
            }
        }

        if (takeProfits.isEmpty()) {
            // Check multi-line TP block
            var inTpBlock = false
            for (line in lines) {
                if (line.contains("TP", ignoreCase = true) || line.contains("Target", ignoreCase = true)) {
                    inTpBlock = true
                    val p = Regex("""[0-9]+(?:\.[0-9]+)?""").find(line)
                    if (p != null) p.value.toDoubleOrNull()?.let { takeProfits.add(it) }
                    continue
                }
                if (inTpBlock) {
                    if (line.contains("SL", ignoreCase = true) || line.contains("Leverage", ignoreCase = true)) {
                        inTpBlock = false
                    } else {
                        val p = Regex("""[0-9]+(?:\.[0-9]+)?""").find(line)
                        if (p != null) {
                            p.value.toDoubleOrNull()?.let { takeProfits.add(it) }
                        }
                    }
                }
            }
        }

        // 5. Extract Stop Loss (SL)
        var stopLoss = 0.0
        val slRegex = Regex("""(?:SL|STOP|STOP\s*LOSS)[\s:=-]+([0-9]+(?:\.[0-9]+)?)""", RegexOption.IGNORE_CASE)
        val slMatch = slRegex.find(clean)
        if (slMatch != null) {
            stopLoss = slMatch.groupValues[1].toDoubleOrNull() ?: 0.0
        } else {
            for (i in lines.indices) {
                if (lines[i].contains("SL", ignoreCase = true) || lines[i].contains("Stop", ignoreCase = true)) {
                    if (i + 1 < lines.size) {
                        val nextLineSl = Regex("""[0-9]+(?:\.[0-9]+)?""").find(lines[i + 1])
                        if (nextLineSl != null) {
                            stopLoss = nextLineSl.value.toDoubleOrNull() ?: 0.0
                            break
                        }
                    }
                }
            }
        }

        // 6. Extract Leverage
        var leverage = 20
        val levMatch = Regex("""([0-9]{1,3})x""", RegexOption.IGNORE_CASE).find(clean)
        if (levMatch != null) {
            leverage = levMatch.groupValues[1].toIntOrNull()?.coerceIn(1, 125) ?: 20
        } else {
            val levWordMatch = Regex("""(?:LEVERAGE|LEV)[\s:=-]+([0-9]{1,3})""", RegexOption.IGNORE_CASE).find(clean)
            if (levWordMatch != null) {
                leverage = levWordMatch.groupValues[1].toIntOrNull()?.coerceIn(1, 125) ?: 20
            }
        }

        // Auto-fix or sanitize entry and SL if entry was 0
        if (entryPrice <= 0.0) {
            // Find numbers in text
            val allNumbers = Regex("""[0-9]+(?:\.[0-9]+)?""").findAll(clean)
                .mapNotNull { it.value.toDoubleOrNull() }
                .filter { it > 1.0 && it != leverage.toDouble() }
                .toList()
            if (allNumbers.isNotEmpty()) {
                entryPrice = allNumbers.first()
            }
        }

        if (stopLoss <= 0.0 && entryPrice > 0) {
            // Calculate a default 1.5% SL
            stopLoss = if (side == "LONG") entryPrice * 0.985 else entryPrice * 1.015
        }

        if (takeProfits.isEmpty() && entryPrice > 0) {
            val defaultTp = if (side == "LONG") entryPrice * 1.03 else entryPrice * 0.97
            takeProfits.add(defaultTp)
        }

        return ParsedSignal(
            symbol = extractedSymbol,
            side = side,
            entryPrice = entryPrice,
            takeProfits = takeProfits,
            stopLoss = stopLoss,
            leverage = leverage,
            rawText = clean,
            isValid = entryPrice > 0.0
        )
    }
}

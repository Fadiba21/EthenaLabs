package com.example.data.api

data class BybitWalletBalance(
    val totalWalletBalance: Double = 12500.0,
    val totalAvailableBalance: Double = 9840.50,
    val unrealisedPnl: Double = 345.20,
    val coin: String = "USDT"
)

data class BybitTicker(
    val symbol: String,
    val lastPrice: Double,
    val price24hPcnt: Double,
    val highPrice24h: Double,
    val lowPrice24h: Double
)

data class BybitPosition(
    val symbol: String,
    val side: String, // "Buy" (Long) or "Sell" (Short)
    val size: Double,
    val entryPrice: Double,
    val markPrice: Double,
    val unrealisedPnl: Double,
    val pnlPercentage: Double,
    val leverage: Int,
    val marginMode: String // "Cross" or "Isolated"
)

data class BybitOrderResult(
    val success: Boolean,
    val orderId: String,
    val message: String,
    val executedPrice: Double = 0.0,
    val executedQty: Double = 0.0
)

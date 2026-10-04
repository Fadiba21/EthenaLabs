package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trades")
data class TradeHistory(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val symbol: String,
    val side: String, // "LONG" or "SHORT"
    val entryPrice: Double,
    val exitPrice: Double? = null,
    val currentPrice: Double = entryPrice,
    val amountUsdt: Double = 0.0,
    val profit: Double? = null,
    val loss: Double? = null,
    val pnlPercent: Double? = null,
    val leverage: Int = 20,
    val marginMode: String = "Cross",
    val orderId: String = "",
    val signalId: Long? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val closeTimestamp: Long? = null,
    val status: String = "OPEN" // "OPEN", "CLOSED", "CANCELLED"
)

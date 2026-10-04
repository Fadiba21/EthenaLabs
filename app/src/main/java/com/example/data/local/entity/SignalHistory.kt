package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "signals")
data class SignalHistory(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val symbol: String,
    val side: String, // "LONG" or "SHORT"
    val entry: Double,
    val tp: String, // Comma separated or single target
    val sl: Double,
    val leverage: Int = 20,
    val aiConfidence: Int = 0, // 0-100
    val aiRisk: String = "low", // "low", "medium", "high"
    val aiReason: String = "",
    val rawText: String = "",
    val channelSource: String = "@CryptoVipSignals",
    val status: String = "NEW" // "NEW", "APPROVED", "EXECUTED", "REJECTED_BY_AI", "CANCELLED"
)

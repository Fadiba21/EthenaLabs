package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bot_settings")
data class BotSettings(
    @PrimaryKey
    val id: Int = 1,
    // Telegram Configuration
    val telegramApiId: String = "",
    val telegramApiHash: String = "",
    val telegramPhone: String = "",
    val telegramChannel: String = "@CryptoVipSignals",
    val telegramSession: String = "",
    val telegramConnected: Boolean = true, // Default to demo-connected ready state
    
    // Bybit Configuration
    val bybitApiKey: String = "",
    val bybitApiSecret: String = "",
    val bybitTestnet: Boolean = true,
    val bybitConnected: Boolean = true,

    // Trading Rules
    val autoTrade: Boolean = true,
    val marginMode: String = "Cross", // "Cross" or "Isolated"
    val riskPercent: Float = 20f, // 1% - 100%
    val leverage: Int = 20, // 1x - 125x
    val minimumConfidence: Int = 75, // 50% - 100%
    val manualApproval: Boolean = false,
    val trailingStop: Boolean = false,
    val autoBreakeven: Boolean = true,
    val emergencyStop: Boolean = false,

    // AI Configuration
    val geminiApiKeyOverride: String = "",

    // Security
    val pinCode: String = "",
    val biometricEnabled: Boolean = false,
    val isAppLocked: Boolean = false
)

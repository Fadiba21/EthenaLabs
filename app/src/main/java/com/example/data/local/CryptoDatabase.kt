package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.SettingsDao
import com.example.data.local.dao.SignalDao
import com.example.data.local.dao.TradeDao
import com.example.data.local.entity.BotSettings
import com.example.data.local.entity.SignalHistory
import com.example.data.local.entity.TradeHistory

@Database(
    entities = [
        SignalHistory::class,
        TradeHistory::class,
        BotSettings::class
    ],
    version = 1,
    exportSchema = false
)
abstract class CryptoDatabase : RoomDatabase() {
    abstract fun signalDao(): SignalDao
    abstract fun tradeDao(): TradeDao
    abstract fun settingsDao(): SettingsDao

    companion object {
        @Volatile
        private var INSTANCE: CryptoDatabase? = null

        fun getInstance(context: Context): CryptoDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CryptoDatabase::class.java,
                    "crypto_signal_trader.db"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

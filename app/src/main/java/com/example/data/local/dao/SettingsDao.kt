package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.BotSettings
import kotlinx.coroutines.flow.Flow

@Dao
interface SettingsDao {
    @Query("SELECT * FROM bot_settings WHERE id = 1")
    fun getSettingsFlow(): Flow<BotSettings?>

    @Query("SELECT * FROM bot_settings WHERE id = 1")
    suspend fun getSettings(): BotSettings?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSettings(settings: BotSettings)

    @Update
    suspend fun updateSettings(settings: BotSettings)
}

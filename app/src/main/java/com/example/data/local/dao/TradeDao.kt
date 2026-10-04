package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.TradeHistory
import kotlinx.coroutines.flow.Flow

@Dao
interface TradeDao {
    @Query("SELECT * FROM trades ORDER BY timestamp DESC")
    fun getAllTrades(): Flow<List<TradeHistory>>

    @Query("SELECT * FROM trades WHERE status = 'OPEN' ORDER BY timestamp DESC")
    fun getOpenTrades(): Flow<List<TradeHistory>>

    @Query("SELECT * FROM trades WHERE id = :id")
    suspend fun getTradeById(id: Long): TradeHistory?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrade(trade: TradeHistory): Long

    @Update
    suspend fun updateTrade(trade: TradeHistory)

    @Query("UPDATE trades SET status = 'CLOSED', exitPrice = :exitPrice, profit = :profit, loss = :loss, pnlPercent = :pnlPercent, closeTimestamp = :closeTimestamp WHERE id = :id")
    suspend fun closeTrade(id: Long, exitPrice: Double, profit: Double?, loss: Double?, pnlPercent: Double?, closeTimestamp: Long)

    @Query("DELETE FROM trades WHERE id = :id")
    suspend fun deleteTrade(id: Long)

    @Query("DELETE FROM trades")
    suspend fun clearAll()
}

package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.SignalHistory
import kotlinx.coroutines.flow.Flow

@Dao
interface SignalDao {
    @Query("SELECT * FROM signals ORDER BY timestamp DESC")
    fun getAllSignals(): Flow<List<SignalHistory>>

    @Query("SELECT * FROM signals WHERE id = :id")
    suspend fun getSignalById(id: Long): SignalHistory?

    @Query("SELECT * FROM signals WHERE status = 'NEW' ORDER BY timestamp DESC")
    fun getPendingSignals(): Flow<List<SignalHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSignal(signal: SignalHistory): Long

    @Update
    suspend fun updateSignal(signal: SignalHistory)

    @Query("UPDATE signals SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String)

    @Query("DELETE FROM signals WHERE id = :id")
    suspend fun deleteSignal(id: Long)

    @Query("DELETE FROM signals")
    suspend fun clearAll()
}

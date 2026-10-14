package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.RateAlertEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RateAlertDao {
    @Query("SELECT * FROM rate_alerts ORDER BY createdAt DESC")
    fun getAllAlerts(): Flow<List<RateAlertEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(alert: RateAlertEntity): Long

    @Query("DELETE FROM rate_alerts WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("UPDATE rate_alerts SET isActive = :isActive WHERE id = :id")
    suspend fun updateStatus(id: Long, isActive: Boolean)
}

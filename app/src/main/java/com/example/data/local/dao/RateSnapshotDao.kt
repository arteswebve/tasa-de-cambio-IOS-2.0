package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.RateSnapshotEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RateSnapshotDao {
    @Query("SELECT * FROM rate_snapshots WHERE currencyCode = :currencyCode ORDER BY timestamp ASC")
    fun getSnapshotsForCurrency(currencyCode: String): Flow<List<RateSnapshotEntity>>

    @Query("SELECT COUNT(*) FROM rate_snapshots")
    suspend fun getCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(snapshot: RateSnapshotEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(snapshots: List<RateSnapshotEntity>)

    @Query("DELETE FROM rate_snapshots")
    suspend fun clearAll()
}

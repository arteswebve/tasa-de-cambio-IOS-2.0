package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "rate_snapshots")
data class RateSnapshotEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val currencyCode: String, // "USD", "EUR", "USDT"
    val rateValue: Double,
    val dateStr: String, // e.g. "13 Oct" or "2026-10-13"
    val timestamp: Long = System.currentTimeMillis()
)

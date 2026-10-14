package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "rate_alerts")
data class RateAlertEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val currencyCode: String, // "USD", "EUR", "USDT"
    val targetRate: Double,
    val condition: String, // "ABOVE" (mayor que) or "BELOW" (menor que)
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

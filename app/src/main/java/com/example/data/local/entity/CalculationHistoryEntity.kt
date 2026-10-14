package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "calculation_history")
data class CalculationHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val fromCurrency: String,
    val toCurrency: String,
    val inputAmount: Double,
    val rateApplied: Double,
    val resultAmount: Double,
    val timestamp: Long = System.currentTimeMillis()
)

package com.example.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CurrencyFormatUtils {
    private val venezuelanSymbols = DecimalFormatSymbols(Locale("es", "VE")).apply {
        decimalSeparator = ','
        groupingSeparator = '.'
    }

    private val standardFormatter = DecimalFormat("#,##0.00", venezuelanSymbols)
    private val compactFormatter = DecimalFormat("#,##0", venezuelanSymbols)

    fun formatAmount(amount: Double): String {
        return standardFormatter.format(amount)
    }

    fun formatCompact(amount: Double): String {
        return compactFormatter.format(amount)
    }

    fun parseAmount(input: String): Double? {
        val sanitized = input.trim()
            .replace(".", "") // remove thousand separators
            .replace(",", ".") // replace comma with decimal point
        return sanitized.toDoubleOrNull()
    }

    fun formatTimestamp(millis: Long): String {
        val format = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale("es", "VE"))
        return format.format(Date(millis))
    }

    fun formatShortTime(millis: Long): String {
        val format = SimpleDateFormat("hh:mm a", Locale("es", "VE"))
        return format.format(Date(millis))
    }
}

package com.example.data.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.CurrencyEurAmber
import com.example.ui.theme.CurrencyUsdGreen
import com.example.ui.theme.CurrencyUsdtCyan
import com.example.ui.theme.CurrencyVesBlue

enum class CurrencyType(
    val code: String,
    val title: String,
    val symbol: String,
    val flagEmoji: String,
    val accentColor: Color
) {
    USD(
        code = "USD",
        title = "Dólar BCV",
        symbol = "$",
        flagEmoji = "🇺🇸",
        accentColor = CurrencyUsdGreen
    ),
    EUR(
        code = "EUR",
        title = "Euro BCV",
        symbol = "€",
        flagEmoji = "🇪🇺",
        accentColor = CurrencyEurAmber
    ),
    USDT(
        code = "USDT",
        title = "USDT Binance",
        symbol = "₮",
        flagEmoji = "🪙",
        accentColor = CurrencyUsdtCyan
    ),
    VES(
        code = "VES",
        title = "Bolívar VES",
        symbol = "Bs",
        flagEmoji = "🇻🇪",
        accentColor = CurrencyVesBlue
    );

    companion object {
        fun fromCode(code: String): CurrencyType {
            return entries.find { it.code.equals(code, ignoreCase = true) } ?: USD
        }
    }
}

data class CurrentRates(
    val usdBcv: Double = 876.80,
    val eurBcv: Double = 982.00,
    val usdtBinance: Double = 1008.35,
    val lastUpdatedMillis: Long = System.currentTimeMillis(),
    val lastUpdatedFormatted: String = "Hoy, 08:30 AM",
    val sourceName: String = "dolarvzla.com (BCV)"
) {
    val spreadPercentage: Double
        get() = if (usdBcv > 0) ((usdtBinance - usdBcv) / usdBcv) * 100 else 0.0

    fun getRate(currency: CurrencyType): Double {
        return when (currency) {
            CurrencyType.USD -> usdBcv
            CurrencyType.EUR -> eurBcv
            CurrencyType.USDT -> usdtBinance
            CurrencyType.VES -> 1.0
        }
    }
}

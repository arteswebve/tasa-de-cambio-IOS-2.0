package com.example

import com.example.data.model.CurrencyType
import com.example.data.model.CurrentRates
import com.example.util.CurrencyFormatUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testCurrencyFormatting() {
        val formatted = CurrencyFormatUtils.formatAmount(1250.50)
        assertEquals("1.250,50", formatted)

        val parsed = CurrencyFormatUtils.parseAmount("1.250,50")
        assertNotNull(parsed)
        assertEquals(1250.50, parsed!!, 0.001)
    }

    @Test
    fun testSpreadCalculation() {
        val rates = CurrentRates(usdBcv = 48.25, usdtBinance = 58.40)
        val spread = rates.spreadPercentage
        assertTrue(spread > 20.0)
    }

    @Test
    fun testCurrencyTypeDefaults() {
        val defaultCurrency = CurrencyType.fromCode("USD")
        assertEquals(CurrencyType.USD, defaultCurrency)
        assertEquals("Dólar BCV", defaultCurrency.title)
        assertEquals("🇺🇸", defaultCurrency.flagEmoji)
    }
}

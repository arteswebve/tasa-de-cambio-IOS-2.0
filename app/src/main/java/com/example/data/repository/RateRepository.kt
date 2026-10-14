package com.example.data.repository

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.local.entity.CalculationHistoryEntity
import com.example.data.local.entity.RateAlertEntity
import com.example.data.local.entity.RateSnapshotEntity
import com.example.data.model.CurrentRates
import com.example.data.remote.DolarVzlaApiClient
import com.example.widget.RateAppWidgetProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.random.Random

class RateRepository(private val context: Context) {
    private val database = AppDatabase.getDatabase(context)
    private val calculationDao = database.calculationHistoryDao()
    private val snapshotDao = database.rateSnapshotDao()
    private val alertDao = database.rateAlertDao()

    private val apiClient = DolarVzlaApiClient()
    private val sharedPrefs = context.getSharedPreferences("tasacambio_app_prefs", Context.MODE_PRIVATE)

    private val _currentRates = MutableStateFlow(
        CurrentRates(
            usdBcv = sharedPrefs.getFloat("rate_usd", 876.80f).toDouble(),
            eurBcv = sharedPrefs.getFloat("rate_eur", 982.00f).toDouble(),
            usdtBinance = sharedPrefs.getFloat("rate_usdt", 1008.35f).toDouble(),
            lastUpdatedMillis = sharedPrefs.getLong("rate_updated_time", System.currentTimeMillis()),
            lastUpdatedFormatted = sharedPrefs.getString("rate_updated_formatted", "Hoy, 08:30 AM") ?: "Hoy, 08:30 AM",
            sourceName = "dolarvzla.com (BCV)"
        )
    )
    val currentRates = _currentRates.asStateFlow()

    private val _isRewardUnlocked = MutableStateFlow(
        sharedPrefs.getBoolean("widget_reward_unlocked", false)
    )
    val isRewardUnlocked = _isRewardUnlocked.asStateFlow()

    init {
        // Initial sync to AppWidget
        val rates = _currentRates.value
        RateAppWidgetProvider.updateAllWidgets(context, rates.usdBcv, rates.eurBcv, rates.usdtBinance)
    }

    suspend fun initializeHistoricalDataIfNeeded() = withContext(Dispatchers.IO) {
        val count = snapshotDao.getCount()
        // Try fetching real 60-day historical rates from dolarvzla.com
        val realHistory = apiClient.fetchHistoricalRates60Days()
        if (realHistory.isNotEmpty()) {
            snapshotDao.clearAll()
            val snapshots = mutableListOf<RateSnapshotEntity>()
            // Insert in chronological order (oldest to newest)
            realHistory.reversed().forEach { entry ->
                snapshots.add(RateSnapshotEntity(currencyCode = "USD", rateValue = entry.usdBcv, dateStr = entry.dateFormatted, timestamp = entry.timestamp))
                snapshots.add(RateSnapshotEntity(currencyCode = "EUR", rateValue = entry.eurBcv, dateStr = entry.dateFormatted, timestamp = entry.timestamp))
                snapshots.add(RateSnapshotEntity(currencyCode = "USDT", rateValue = entry.usdtRate, dateStr = entry.dateFormatted, timestamp = entry.timestamp))
            }
            snapshotDao.insertAll(snapshots)
        } else if (count < 50) {
            val snapshots = mutableListOf<RateSnapshotEntity>()
            val dateFormat = SimpleDateFormat("dd MMM", Locale("es", "VE"))

            // Fallback seed: 60 days based around current real market values (~730 to 878 Bs)
            val currentUsd = _currentRates.value.usdBcv
            val currentEur = _currentRates.value.eurBcv
            val currentUsdt = _currentRates.value.usdtBinance

            for (i in 60 downTo 0) {
                val cal = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_YEAR, -i)
                }
                val dateStr = dateFormat.format(cal.time)
                val time = cal.timeInMillis

                val usdVal = (currentUsd - (i * 2.35) + Random.nextDouble(-1.2, 1.2))
                val eurVal = (currentEur - (i * 2.30) + Random.nextDouble(-1.3, 1.3))
                val usdtVal = (currentUsdt - (i * 2.70) + Random.nextDouble(-1.5, 1.5))

                snapshots.add(RateSnapshotEntity(currencyCode = "USD", rateValue = Math.round(usdVal * 100.0) / 100.0, dateStr = dateStr, timestamp = time))
                snapshots.add(RateSnapshotEntity(currencyCode = "EUR", rateValue = Math.round(eurVal * 100.0) / 100.0, dateStr = dateStr, timestamp = time))
                snapshots.add(RateSnapshotEntity(currencyCode = "USDT", rateValue = Math.round(usdtVal * 100.0) / 100.0, dateStr = dateStr, timestamp = time))
            }
            snapshotDao.insertAll(snapshots)
        }
    }

    suspend fun refreshRates(): CurrentRates = withContext(Dispatchers.IO) {
        val fetched = apiClient.fetchCurrentRates()

        val now = System.currentTimeMillis()
        val nowFormatted = "Hoy, " + SimpleDateFormat("hh:mm a", Locale("es", "VE")).format(Date(now))

        val newUsd: Double
        val newEur: Double
        val newUsdt: Double

        if (fetched != null && fetched.usdBcv > 0) {
            newUsd = fetched.usdBcv
            newEur = fetched.eurBcv
            newUsdt = fetched.usdtRate
        } else {
            // Graceful offline fallback with current state
            val current = _currentRates.value
            newUsd = current.usdBcv
            newEur = current.eurBcv
            newUsdt = current.usdtBinance
        }

        sharedPrefs.edit()
            .putFloat("rate_usd", newUsd.toFloat())
            .putFloat("rate_eur", newEur.toFloat())
            .putFloat("rate_usdt", newUsdt.toFloat())
            .putLong("rate_updated_time", now)
            .putString("rate_updated_formatted", nowFormatted)
            .apply()

        val updated = CurrentRates(
            usdBcv = newUsd,
            eurBcv = newEur,
            usdtBinance = newUsdt,
            lastUpdatedMillis = now,
            lastUpdatedFormatted = nowFormatted,
            sourceName = "dolarvzla.com (BCV)"
        )
        _currentRates.value = updated

        // Record snapshot in Room
        val dayFormat = SimpleDateFormat("dd MMM", Locale("es", "VE"))
        val dayStr = dayFormat.format(Date(now))
        snapshotDao.insert(RateSnapshotEntity(currencyCode = "USD", rateValue = newUsd, dateStr = dayStr, timestamp = now))
        snapshotDao.insert(RateSnapshotEntity(currencyCode = "EUR", rateValue = newEur, dateStr = dayStr, timestamp = now))
        snapshotDao.insert(RateSnapshotEntity(currencyCode = "USDT", rateValue = newUsdt, dateStr = dayStr, timestamp = now))

        // Update home screen widget
        RateAppWidgetProvider.updateAllWidgets(context, newUsd, newEur, newUsdt)

        updated
    }

    fun unlockReward() {
        sharedPrefs.edit().putBoolean("widget_reward_unlocked", true).apply()
        _isRewardUnlocked.value = true
    }

    // Calculations
    fun getCalculationHistory(): Flow<List<CalculationHistoryEntity>> = calculationDao.getAllHistory()

    suspend fun saveCalculation(history: CalculationHistoryEntity) = withContext(Dispatchers.IO) {
        calculationDao.insert(history)
    }

    suspend fun deleteCalculation(id: Long) = withContext(Dispatchers.IO) {
        calculationDao.deleteById(id)
    }

    suspend fun clearHistory() = withContext(Dispatchers.IO) {
        calculationDao.clearAll()
    }

    // Trends & Snapshots
    fun getSnapshots(currencyCode: String): Flow<List<RateSnapshotEntity>> =
        snapshotDao.getSnapshotsForCurrency(currencyCode)

    // Alerts
    fun getAlerts(): Flow<List<RateAlertEntity>> = alertDao.getAllAlerts()

    suspend fun saveAlert(alert: RateAlertEntity) = withContext(Dispatchers.IO) {
        alertDao.insert(alert)
    }

    suspend fun deleteAlert(id: Long) = withContext(Dispatchers.IO) {
        alertDao.delete(id)
    }

    suspend fun toggleAlertStatus(id: Long, isActive: Boolean) = withContext(Dispatchers.IO) {
        alertDao.updateStatus(id, isActive)
    }
}

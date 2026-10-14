package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.CalculationHistoryEntity
import com.example.data.local.entity.RateAlertEntity
import com.example.data.local.entity.RateSnapshotEntity
import com.example.data.model.CurrencyType
import com.example.data.model.CurrentRates
import com.example.data.repository.RateRepository
import com.example.util.CurrencyFormatUtils
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CalculatorUiState(
    val selectedCurrency: CurrencyType = CurrencyType.USD, // Default MUST BE Dólar BCV!
    val isInverseDirection: Boolean = false, // false: Divisa -> VES, true: VES -> Divisa
    val inputAmountText: String = "100", // Start with 100 so user immediately sees real calculated values
    val parsedInputAmount: Double = 100.0,
    val currentRate: Double = 48.25,
    val calculatedResult: Double = 4825.0,
    val lastSavedCalculationId: Long? = null
)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = RateRepository(application)

    // Current Market Rates
    val currentRates: StateFlow<CurrentRates> = repository.currentRates
    val isRewardUnlocked: StateFlow<Boolean> = repository.isRewardUnlocked

    // Refresh state
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    // Calculator State
    private val _calculatorState = MutableStateFlow(CalculatorUiState())
    val calculatorState = _calculatorState.asStateFlow()

    // History Flow from Room
    val historyList: StateFlow<List<CalculationHistoryEntity>> = repository.getCalculationHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Alerts Flow from Room
    val alertsList: StateFlow<List<RateAlertEntity>> = repository.getAlerts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Trends Selection
    private val _trendCurrency = MutableStateFlow(CurrencyType.USD)
    val trendCurrency = _trendCurrency.asStateFlow()

    private val _trendPeriodDays = MutableStateFlow(15) // 7, 15, 30
    val trendPeriodDays = _trendPeriodDays.asStateFlow()

    // Dynamic Snapshots Flow
    val trendSnapshots: StateFlow<List<RateSnapshotEntity>> = combine(
        _trendCurrency,
        _trendPeriodDays
    ) { currency, days ->
        Pair(currency, days)
    }.combine(repository.getSnapshots("USD")) { pair, _ ->
        pair
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Pair(CurrencyType.USD, 15))
        .let { combinedPairFlow ->
            // Flow for snapshots based on selected currency
            MutableStateFlow<List<RateSnapshotEntity>>(emptyList())
        }

    // Rewarded Modal State
    private val _showRewardModal = MutableStateFlow(false)
    val showRewardModal = _showRewardModal.asStateFlow()

    private val _isVideoPlaying = MutableStateFlow(false)
    val isVideoPlaying = _isVideoPlaying.asStateFlow()

    private val _rewardProgress = MutableStateFlow(0f)
    val rewardProgress = _rewardProgress.asStateFlow()

    private val _countdownSeconds = MutableStateFlow(5)
    val countdownSeconds = _countdownSeconds.asStateFlow()

    // Debounce job for auto-saving history
    private var autoSaveJob: Job? = null

    init {
        viewModelScope.launch {
            repository.initializeHistoricalDataIfNeeded()
            loadTrendSnapshots(_trendCurrency.value, _trendPeriodDays.value)
        }

        // Listen to rate updates to keep currentRate & calculation synced
        viewModelScope.launch {
            repository.currentRates.collect { rates ->
                val current = _calculatorState.value
                val newRate = rates.getRate(current.selectedCurrency)
                val calc = computeResult(current.parsedInputAmount, newRate, current.isInverseDirection)
                _calculatorState.value = current.copy(
                    currentRate = newRate,
                    calculatedResult = calc
                )
            }
        }
    }

    private val _currentSnapshots = MutableStateFlow<List<RateSnapshotEntity>>(emptyList())
    val currentSnapshots = _currentSnapshots.asStateFlow()

    fun setTrendCurrency(currency: CurrencyType) {
        _trendCurrency.value = currency
        loadTrendSnapshots(currency, _trendPeriodDays.value)
    }

    fun setTrendPeriodDays(days: Int) {
        _trendPeriodDays.value = days
        loadTrendSnapshots(_trendCurrency.value, days)
    }

    private fun loadTrendSnapshots(currency: CurrencyType, days: Int) {
        viewModelScope.launch {
            repository.getSnapshots(currency.code).collect { list ->
                val filtered = if (list.size > days) list.takeLast(days) else list
                _currentSnapshots.value = filtered
            }
        }
    }

    // Rate Ticker & Currency Card Selection
    fun selectCurrency(currency: CurrencyType) {
        if (currency == CurrencyType.VES) return
        val rates = currentRates.value
        val rate = rates.getRate(currency)
        val current = _calculatorState.value
        val calc = computeResult(current.parsedInputAmount, rate, current.isInverseDirection)

        _calculatorState.value = current.copy(
            selectedCurrency = currency,
            currentRate = rate,
            calculatedResult = calc
        )
        scheduleAutoSave()
    }

    fun toggleDirection() {
        val current = _calculatorState.value
        val newInverse = !current.isInverseDirection
        val calc = computeResult(current.parsedInputAmount, current.currentRate, newInverse)

        _calculatorState.value = current.copy(
            isInverseDirection = newInverse,
            calculatedResult = calc
        )
        scheduleAutoSave()
    }

    fun setInputAmountText(input: String) {
        val parsed = CurrencyFormatUtils.parseAmount(input) ?: 0.0
        val current = _calculatorState.value
        val calc = computeResult(parsed, current.currentRate, current.isInverseDirection)

        _calculatorState.value = current.copy(
            inputAmountText = input,
            parsedInputAmount = parsed,
            calculatedResult = calc
        )

        if (parsed > 0) {
            scheduleAutoSave()
        }
    }

    fun setQuickAmount(amount: Double) {
        val text = if (amount % 1.0 == 0.0) amount.toInt().toString() else amount.toString()
        setInputAmountText(text)
    }

    private fun computeResult(amount: Double, rate: Double, isInverse: Boolean): Double {
        if (amount <= 0.0 || rate <= 0.0) return 0.0
        return if (!isInverse) {
            // Divisa -> VES (e.g. 100 USD * 48.25 = 4825.00 VES)
            amount * rate
        } else {
            // VES -> Divisa (e.g. 4825 VES / 48.25 = 100.00 USD)
            amount / rate
        }
    }

    private fun scheduleAutoSave() {
        autoSaveJob?.cancel()
        autoSaveJob = viewModelScope.launch {
            delay(1200) // Debounce auto-save to history
            val state = _calculatorState.value
            if (state.parsedInputAmount > 0) {
                val fromCode = if (!state.isInverseDirection) state.selectedCurrency.code else "VES"
                val toCode = if (!state.isInverseDirection) "VES" else state.selectedCurrency.code

                repository.saveCalculation(
                    CalculationHistoryEntity(
                        fromCurrency = fromCode,
                        toCurrency = toCode,
                        inputAmount = state.parsedInputAmount,
                        rateApplied = state.currentRate,
                        resultAmount = state.calculatedResult
                    )
                )
            }
        }
    }

    fun refreshRates() {
        if (_isRefreshing.value) return
        viewModelScope.launch {
            _isRefreshing.value = true
            delay(600) // Smooth refresh animation
            val updated = repository.refreshRates()
            val current = _calculatorState.value
            val newRate = updated.getRate(current.selectedCurrency)
            val calc = computeResult(current.parsedInputAmount, newRate, current.isInverseDirection)

            _calculatorState.value = current.copy(
                currentRate = newRate,
                calculatedResult = calc
            )
            loadTrendSnapshots(_trendCurrency.value, _trendPeriodDays.value)
            _isRefreshing.value = false
        }
    }

    // Rewarded Modal & Simulation
    fun openRewardModal() {
        _showRewardModal.value = true
    }

    fun dismissRewardModal() {
        if (!_isVideoPlaying.value) {
            _showRewardModal.value = false
        }
    }

    fun startRewardSimulation() {
        if (_isVideoPlaying.value) return
        viewModelScope.launch {
            _isVideoPlaying.value = true
            _rewardProgress.value = 0f
            _countdownSeconds.value = 5

            val totalSteps = 50
            for (step in 1..totalSteps) {
                delay(100)
                _rewardProgress.value = step / totalSteps.toFloat()
                _countdownSeconds.value = Math.max(0, 5 - (step / 10))
            }

            _isVideoPlaying.value = false
            repository.unlockReward()
        }
    }

    // History actions
    fun reuseCalculation(item: CalculationHistoryEntity) {
        val currency = if (item.fromCurrency != "VES") {
            CurrencyType.fromCode(item.fromCurrency)
        } else {
            CurrencyType.fromCode(item.toCurrency)
        }
        val isInverse = (item.fromCurrency == "VES")
        val amount = item.inputAmount
        val text = if (amount % 1.0 == 0.0) amount.toInt().toString() else amount.toString()

        val rate = currentRates.value.getRate(currency)
        val calc = computeResult(amount, rate, isInverse)

        _calculatorState.value = CalculatorUiState(
            selectedCurrency = currency,
            isInverseDirection = isInverse,
            inputAmountText = text,
            parsedInputAmount = amount,
            currentRate = rate,
            calculatedResult = calc
        )
    }

    fun deleteCalculation(id: Long) {
        viewModelScope.launch {
            repository.deleteCalculation(id)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    // Alert actions
    fun addAlert(currency: CurrencyType, targetRate: Double, condition: String) {
        viewModelScope.launch {
            repository.saveAlert(
                RateAlertEntity(
                    currencyCode = currency.code,
                    targetRate = targetRate,
                    condition = condition,
                    isActive = true
                )
            )
        }
    }

    fun deleteAlert(id: Long) {
        viewModelScope.launch {
            repository.deleteAlert(id)
        }
    }

    fun toggleAlert(id: Long, isActive: Boolean) {
        viewModelScope.launch {
            repository.toggleAlertStatus(id, isActive)
        }
    }
}

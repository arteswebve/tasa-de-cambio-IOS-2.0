package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.MainViewModel
import com.example.ui.components.AppHeader
import com.example.ui.components.BottomNavBar
import com.example.ui.components.InfoDisclaimerDialog
import com.example.ui.components.IosCodeViewerDialog
import com.example.ui.components.NavTab
import com.example.ui.components.RateTickerHeader
import com.example.ui.components.RewardedWidgetDialog
import com.example.ui.screens.AlertsScreen
import com.example.ui.screens.CalculatorScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.TrendsScreen
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.TasaCambioTheme
import com.example.util.AdMobBannerView
import com.example.util.AdMobManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        AdMobManager.initialize(this)
        AdMobManager.preloadRewardedAd(this)
        setContent {
            TasaCambioTheme {
                MainApp()
            }
        }
    }
}

@Composable
fun MainApp(viewModel: MainViewModel = viewModel()) {
    var selectedTab by rememberSaveable { mutableStateOf(NavTab.CALCULATOR) }
    var showInfoModal by rememberSaveable { mutableStateOf(false) }
    var showIosCodeModal by rememberSaveable { mutableStateOf(false) }

    // Initial rates refresh from dolarvzla.com on launch
    LaunchedEffect(Unit) {
        viewModel.refreshRates()
    }

    // Reactive State Collection
    val currentRates by viewModel.currentRates.collectAsStateWithLifecycle()
    val calculatorState by viewModel.calculatorState.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val isRewardUnlocked by viewModel.isRewardUnlocked.collectAsStateWithLifecycle()
    val historyList by viewModel.historyList.collectAsStateWithLifecycle()
    val alertsList by viewModel.alertsList.collectAsStateWithLifecycle()
    val trendCurrency by viewModel.trendCurrency.collectAsStateWithLifecycle()
    val trendPeriodDays by viewModel.trendPeriodDays.collectAsStateWithLifecycle()
    val currentSnapshots by viewModel.currentSnapshots.collectAsStateWithLifecycle()

    val showRewardModal by viewModel.showRewardModal.collectAsStateWithLifecycle()
    val isVideoPlaying by viewModel.isVideoPlaying.collectAsStateWithLifecycle()
    val rewardProgress by viewModel.rewardProgress.collectAsStateWithLifecycle()
    val countdownSeconds by viewModel.countdownSeconds.collectAsStateWithLifecycle()

    // Handle back button to return to Calculator tab
    BackHandler(enabled = selectedTab != NavTab.CALCULATOR) {
        selectedTab = NavTab.CALCULATOR
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkBackground)
                    .statusBarsPadding()
            ) {
                AppHeader(
                    isRewardUnlocked = isRewardUnlocked,
                    onRewardClick = { viewModel.openRewardModal() },
                    onIosCodeClick = { showIosCodeModal = true }
                )
                RateTickerHeader(
                    rates = currentRates,
                    selectedCurrency = calculatorState.selectedCurrency,
                    isRefreshing = isRefreshing,
                    onCurrencySelected = { currency ->
                        viewModel.selectCurrency(currency)
                        selectedTab = NavTab.CALCULATOR
                    },
                    onRefreshClick = { viewModel.refreshRates() }
                )
            }
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkBackground)
                    .navigationBarsPadding()
            ) {
                AdMobBannerView()
                BottomNavBar(
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it },
                    historyCount = historyList.size,
                    alertsCount = alertsList.count { it.isActive }
                )
            }
        },
        containerColor = DarkBackground,
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(DarkBackground)
        ) {
            when (selectedTab) {
                NavTab.CALCULATOR -> {
                    CalculatorScreen(
                        state = calculatorState,
                        onInputChanged = { viewModel.setInputAmountText(it) },
                        onQuickAmountClick = { viewModel.setQuickAmount(it) },
                        onToggleDirection = { viewModel.toggleDirection() },
                        onInfoClick = { showInfoModal = true }
                    )
                }

                NavTab.TRENDS -> {
                    TrendsScreen(
                        selectedCurrency = trendCurrency,
                        selectedPeriodDays = trendPeriodDays,
                        snapshots = currentSnapshots,
                        onCurrencySelected = { viewModel.setTrendCurrency(it) },
                        onPeriodSelected = { viewModel.setTrendPeriodDays(it) }
                    )
                }

                NavTab.HISTORY -> {
                    HistoryScreen(
                        historyList = historyList,
                        onReuseCalculation = {
                            viewModel.reuseCalculation(it)
                            selectedTab = NavTab.CALCULATOR
                        },
                        onDeleteCalculation = { viewModel.deleteCalculation(it) },
                        onClearAllHistory = { viewModel.clearAllHistory() }
                    )
                }

                NavTab.ALERTS -> {
                    AlertsScreen(
                        currentRates = currentRates,
                        alertsList = alertsList,
                        onAddAlert = { currency, rate, cond -> viewModel.addAlert(currency, rate, cond) },
                        onDeleteAlert = { viewModel.deleteAlert(it) },
                        onToggleAlert = { id, active -> viewModel.toggleAlert(id, active) }
                    )
                }
            }
        }
    }

    // Modal Rewarded Video Dialog
    if (showRewardModal) {
        RewardedWidgetDialog(
            rates = currentRates,
            isUnlocked = isRewardUnlocked,
            isVideoPlaying = isVideoPlaying,
            progress = rewardProgress,
            countdownSeconds = countdownSeconds,
            onStartSimulation = { viewModel.startRewardSimulation() },
            onDismiss = { viewModel.dismissRewardModal() }
        )
    }

    // Modal Legal Disclaimer & Info Dialog
    if (showInfoModal) {
        InfoDisclaimerDialog(
            onDismiss = { showInfoModal = false }
        )
    }

    // Modal iOS SwiftUI Code Viewer & Exporter
    if (showIosCodeModal) {
        IosCodeViewerDialog(
            onDismiss = { showIosCodeModal = false }
        )
    }
}

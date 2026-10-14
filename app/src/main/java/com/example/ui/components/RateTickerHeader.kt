package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CurrencyType
import com.example.data.model.CurrentRates
import com.example.ui.theme.IosLabelPrimary
import com.example.ui.theme.IosLabelSecondary
import com.example.ui.theme.IosLabelTertiary
import com.example.ui.theme.IosQuaternarySystemBackground
import com.example.ui.theme.IosSecondarySystemBackground
import com.example.ui.theme.IosSeparator
import com.example.ui.theme.IosSystemYellow
import com.example.ui.theme.IosTertiarySystemBackground
import com.example.util.CurrencyFormatUtils

@Composable
fun RateTickerHeader(
    rates: CurrentRates,
    selectedCurrency: CurrencyType,
    isRefreshing: Boolean,
    onCurrencySelected: (CurrencyType) -> Unit,
    onRefreshClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val rotation by animateFloatAsState(
        targetValue = if (isRefreshing) 360f else 0f,
        animationSpec = tween(durationMillis = 600, easing = LinearEasing),
        label = "refresh_rotation"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 2.dp)
    ) {
        // Ticker top compact info bar: Last updated, Spread, Source and Refresh button (iOS style)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = rates.lastUpdatedFormatted,
                    color = IosLabelTertiary,
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.width(6.dp))
                // Brecha cambiaria pill badge
                Surface(
                    color = IosTertiarySystemBackground,
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(0.5.dp, IosSeparator)
                ) {
                    Text(
                        text = "Brecha: +${String.format(java.util.Locale.US, "%.1f", rates.spreadPercentage)}%",
                        color = IosSystemYellow,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.5.dp)
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "• dolarvzla.com",
                    color = IosLabelTertiary,
                    fontSize = 10.sp
                )
            }

            IconButton(
                onClick = onRefreshClick,
                enabled = !isRefreshing,
                modifier = Modifier
                    .size(28.dp)
                    .testTag("refresh_rates_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refrescar tasas",
                    tint = if (isRefreshing) IosSystemYellow else IosLabelSecondary,
                    modifier = Modifier
                        .size(16.dp)
                        .rotate(rotation)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // 3 ultra-compact interactive Cupertino cards in a row: USD BCV, EUR BCV, USDT Binance
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            CompactIosRateCard(
                currency = CurrencyType.USD,
                rateValue = rates.usdBcv,
                isSelected = selectedCurrency == CurrencyType.USD,
                onClick = { onCurrencySelected(CurrencyType.USD) },
                modifier = Modifier.weight(1f)
            )

            CompactIosRateCard(
                currency = CurrencyType.EUR,
                rateValue = rates.eurBcv,
                isSelected = selectedCurrency == CurrencyType.EUR,
                onClick = { onCurrencySelected(CurrencyType.EUR) },
                modifier = Modifier.weight(1f)
            )

            CompactIosRateCard(
                currency = CurrencyType.USDT,
                rateValue = rates.usdtBinance,
                isSelected = selectedCurrency == CurrencyType.USDT,
                onClick = { onCurrencySelected(CurrencyType.USDT) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun CompactIosRateCard(
    currency: CurrencyType,
    rateValue: Double,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) IosSystemYellow else IosSeparator
    val backgroundColor = if (isSelected) IosTertiarySystemBackground else IosSecondarySystemBackground

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = BorderStroke(if (isSelected) 1.5.dp else 0.5.dp, borderColor),
        modifier = modifier
            .clickable(onClick = onClick)
            .testTag("rate_card_${currency.code.lowercase()}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top row: Flag + Currency Code + Active Dot indicator
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = currency.flagEmoji,
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = currency.code,
                    color = if (isSelected) IosSystemYellow else IosLabelSecondary,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold
                )
                if (isSelected) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .background(IosSystemYellow, CircleShape)
                    )
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Price value
            Text(
                text = "Bs. ${CurrencyFormatUtils.formatAmount(rateValue)}",
                color = currency.accentColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }
    }
}

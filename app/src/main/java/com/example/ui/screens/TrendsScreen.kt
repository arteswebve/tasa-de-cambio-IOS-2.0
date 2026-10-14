package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.RateSnapshotEntity
import com.example.data.model.CurrencyType
import com.example.ui.components.CupertinoSegmentedControl
import com.example.ui.theme.IosLabelPrimary
import com.example.ui.theme.IosLabelSecondary
import com.example.ui.theme.IosLabelTertiary
import com.example.ui.theme.IosQuaternarySystemBackground
import com.example.ui.theme.IosSecondarySystemBackground
import com.example.ui.theme.IosSeparator
import com.example.ui.theme.IosSystemGreen
import com.example.ui.theme.IosSystemRed
import com.example.ui.theme.IosSystemYellow
import com.example.ui.theme.IosTertiarySystemBackground
import com.example.util.CurrencyFormatUtils

@Composable
fun TrendsScreen(
    selectedCurrency: CurrencyType,
    selectedPeriodDays: Int,
    snapshots: List<RateSnapshotEntity>,
    onCurrencySelected: (CurrencyType) -> Unit,
    onPeriodSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    val rates = snapshots.map { it.rateValue }
    val minRate = rates.minOrNull() ?: 0.0
    val maxRate = rates.maxOrNull() ?: 0.0
    val firstRate = rates.firstOrNull() ?: 0.0
    val lastRate = rates.lastOrNull() ?: 0.0
    val deltaAmount = lastRate - firstRate
    val deltaPercent = if (firstRate > 0) (deltaAmount / firstRate) * 100 else 0.0

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        Text(
            text = "Evolución y Tendencias Históricas",
            color = IosLabelPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Monitoreo del tipo de cambio en el mercado venezolano",
            color = IosLabelSecondary,
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Cupertino Segmented Control for Currency
        Text(
            text = "DIVISA",
            color = IosLabelTertiary,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.8.sp,
            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
        )
        CupertinoSegmentedControl(
            items = listOf(CurrencyType.USD, CurrencyType.EUR, CurrencyType.USDT),
            selectedItem = selectedCurrency,
            onItemSelected = onCurrencySelected,
            labelProvider = { "${it.flagEmoji} ${it.code}" },
            testTagPrefix = "trend_currency"
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Cupertino Segmented Control for Period
        Text(
            text = "INTERVALO DE TIEMPO",
            color = IosLabelTertiary,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.8.sp,
            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
        )
        CupertinoSegmentedControl(
            items = listOf(7, 15, 30, 60),
            selectedItem = selectedPeriodDays,
            onItemSelected = onPeriodSelected,
            labelProvider = { "$it días" },
            testTagPrefix = "trend_period"
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Stats Summary Cards (Cupertino Inset Grouped)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Variation Card
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = IosSecondarySystemBackground),
                border = BorderStroke(0.5.dp, IosSeparator),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(text = "Variación (${selectedPeriodDays}d)", color = IosLabelTertiary, fontSize = 10.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    val isPositive = deltaPercent >= 0
                    val changeColor = if (isPositive) IosSystemRed else IosSystemGreen
                    val sign = if (isPositive) "+" else ""
                    Text(
                        text = "$sign${String.format(java.util.Locale.US, "%.2f", deltaPercent)}%",
                        color = changeColor,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$sign${CurrencyFormatUtils.formatAmount(deltaAmount)} Bs",
                        color = changeColor,
                        fontSize = 11.sp
                    )
                }
            }

            // Min Rate Card
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = IosSecondarySystemBackground),
                border = BorderStroke(0.5.dp, IosSeparator),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(text = "Tasa Mínima", color = IosLabelTertiary, fontSize = 10.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Bs. ${CurrencyFormatUtils.formatAmount(minRate)}",
                        color = IosSystemGreen,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(text = "Suelo del periodo", color = IosLabelTertiary, fontSize = 10.sp)
                }
            }

            // Max Rate Card
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = IosSecondarySystemBackground),
                border = BorderStroke(0.5.dp, IosSeparator),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(text = "Tasa Máxima", color = IosLabelTertiary, fontSize = 10.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Bs. ${CurrencyFormatUtils.formatAmount(maxRate)}",
                        color = IosSystemYellow,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(text = "Techo del periodo", color = IosLabelTertiary, fontSize = 10.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Custom High-Res Interactive Canvas Chart (iOS Stocks style)
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = IosSecondarySystemBackground),
            border = BorderStroke(0.5.dp, IosSeparator),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("trend_chart_card")
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Curva de Cotización (${selectedCurrency.code})",
                        color = IosLabelPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Bs. ${CurrencyFormatUtils.formatAmount(lastRate)}",
                        color = selectedCurrency.accentColor,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (snapshots.size >= 2) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height

                            val paddingY = 20f
                            val chartHeight = h - (paddingY * 2)

                            val spread = (maxRate - minRate).coerceAtLeast(0.1)

                            val points = snapshots.mapIndexed { index, snapshot ->
                                val x = (index.toFloat() / (snapshots.size - 1)) * w
                                val normalizedY = ((snapshot.rateValue - minRate) / spread).toFloat()
                                val y = (h - paddingY) - (normalizedY * chartHeight)
                                Offset(x, y)
                            }

                            // Draw horizontal guide lines
                            val gridColor = IosSeparator.copy(alpha = 0.5f)
                            drawLine(gridColor, Offset(0f, paddingY), Offset(w, paddingY), strokeWidth = 1f)
                            drawLine(gridColor, Offset(0f, h / 2f), Offset(w, h / 2f), strokeWidth = 1f)
                            drawLine(gridColor, Offset(0f, h - paddingY), Offset(w, h - paddingY), strokeWidth = 1f)

                            // Area Gradient Path
                            val areaPath = Path().apply {
                                moveTo(points.first().x, h)
                                lineTo(points.first().x, points.first().y)
                                for (i in 1 until points.size) {
                                    val prev = points[i - 1]
                                    val curr = points[i]
                                    val cx = (prev.x + curr.x) / 2f
                                    cubicTo(cx, prev.y, cx, curr.y, curr.x, curr.y)
                                }
                                lineTo(points.last().x, h)
                                close()
                            }

                            val fillBrush = Brush.verticalGradient(
                                colors = listOf(
                                    selectedCurrency.accentColor.copy(alpha = 0.30f),
                                    Color.Transparent
                                ),
                                startY = paddingY,
                                endY = h
                            )
                            drawPath(areaPath, fillBrush)

                            // Line Path
                            val linePath = Path().apply {
                                moveTo(points.first().x, points.first().y)
                                for (i in 1 until points.size) {
                                    val prev = points[i - 1]
                                    val curr = points[i]
                                    val cx = (prev.x + curr.x) / 2f
                                    cubicTo(cx, prev.y, cx, curr.y, curr.x, curr.y)
                                }
                            }

                            drawPath(
                                path = linePath,
                                color = selectedCurrency.accentColor,
                                style = Stroke(width = 3.dp.toPx())
                            )

                            // End point glowing dot
                            val lastPoint = points.last()
                            drawCircle(
                                color = selectedCurrency.accentColor,
                                radius = 5.dp.toPx(),
                                center = lastPoint
                            )
                            drawCircle(
                                color = Color.White,
                                radius = 2.dp.toPx(),
                                center = lastPoint
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Date labels row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = snapshots.firstOrNull()?.dateStr ?: "", color = IosLabelTertiary, fontSize = 10.sp)
                        val midIndex = snapshots.size / 2
                        Text(text = snapshots.getOrNull(midIndex)?.dateStr ?: "", color = IosLabelTertiary, fontSize = 10.sp)
                        Text(text = snapshots.lastOrNull()?.dateStr ?: "", color = IosLabelTertiary, fontSize = 10.sp)
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Registrando cotizaciones históricas...", color = IosLabelSecondary, fontSize = 12.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Inset Grouped Historical Rates List
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Historial de Cotizaciones",
                    color = IosLabelPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${snapshots.size} cotizaciones registradas",
                    color = IosLabelSecondary,
                    fontSize = 11.sp
                )
            }

            Surface(
                color = IosTertiarySystemBackground,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(0.5.dp, IosSeparator)
            ) {
                Text(
                    text = "${selectedPeriodDays}d",
                    color = IosSystemYellow,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        val reversedList = snapshots.reversed()
        reversedList.forEachIndexed { index, snap ->
            val prevRate = if (index < reversedList.size - 1) reversedList[index + 1].rateValue else snap.rateValue
            val diff = snap.rateValue - prevRate
            val isDiffPositive = diff > 0.001
            val isDiffNegative = diff < -0.001

            Surface(
                color = IosSecondarySystemBackground,
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(0.5.dp, IosSeparator),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 3.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 9.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = snap.dateStr,
                            color = IosLabelPrimary,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (index == 0) {
                            Text(
                                text = "Última oficial",
                                color = IosSystemYellow,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isDiffPositive || isDiffNegative) {
                            Text(
                                text = if (isDiffPositive) "+${String.format(java.util.Locale.US, "%.2f", diff)}" else String.format(java.util.Locale.US, "%.2f", diff),
                                color = if (isDiffPositive) IosSystemRed else IosSystemGreen,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }

                        Text(
                            text = "Bs. ${CurrencyFormatUtils.formatAmount(snap.rateValue)}",
                            color = selectedCurrency.accentColor,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

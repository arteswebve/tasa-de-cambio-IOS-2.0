package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.CalculationHistoryEntity
import com.example.data.model.CurrencyType
import com.example.ui.theme.IosLabelPrimary
import com.example.ui.theme.IosLabelSecondary
import com.example.ui.theme.IosLabelTertiary
import com.example.ui.theme.IosSecondarySystemBackground
import com.example.ui.theme.IosSeparator
import com.example.ui.theme.IosSystemRed
import com.example.ui.theme.IosSystemYellow
import com.example.ui.theme.IosTertiarySystemBackground
import com.example.util.CurrencyFormatUtils

@Composable
fun HistoryScreen(
    historyList: List<CalculationHistoryEntity>,
    onReuseCalculation: (CalculationHistoryEntity) -> Unit,
    onDeleteCalculation: (Long) -> Unit,
    onClearAllHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // iOS Header with Clear Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Historial de Conversiones",
                    color = IosLabelPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${historyList.size} cálculos guardados localmente",
                    color = IosLabelSecondary,
                    fontSize = 11.sp
                )
            }

            if (historyList.isNotEmpty()) {
                OutlinedButton(
                    onClick = onClearAllHistory,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, IosSystemRed.copy(alpha = 0.6f)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = IosSystemRed
                    ),
                    modifier = Modifier.testTag("clear_history_button")
                ) {
                    Icon(imageVector = Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Limpiar", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (historyList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 60.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Surface(
                        color = IosTertiarySystemBackground,
                        shape = RoundedCornerShape(50.dp),
                        modifier = Modifier.size(72.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                tint = IosSystemYellow,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Sin conversiones recientes",
                        color = IosLabelPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "A medida que realices cálculos en la calculadora, se guardarán aquí automáticamente para reutilizarlos en cualquier momento.",
                        color = IosLabelSecondary,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 17.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(historyList, key = { it.id }) { item ->
                    IosHistoryItemCard(
                        item = item,
                        onReuse = { onReuseCalculation(item) },
                        onDelete = { onDeleteCalculation(item.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun IosHistoryItemCard(
    item: CalculationHistoryEntity,
    onReuse: () -> Unit,
    onDelete: () -> Unit
) {
    val fromCurr = CurrencyType.fromCode(item.fromCurrency)
    val toCurr = CurrencyType.fromCode(item.toCurrency)

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = IosSecondarySystemBackground),
        border = BorderStroke(0.5.dp, IosSeparator),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("history_item_${item.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Currency pair pill
                Surface(
                    color = IosTertiarySystemBackground,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(0.5.dp, IosSeparator)
                ) {
                    Text(
                        text = "${fromCurr.flagEmoji} ${item.fromCurrency} ➔ ${toCurr.flagEmoji} ${item.toCurrency}",
                        color = IosSystemYellow,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                Text(
                    text = CurrencyFormatUtils.formatTimestamp(item.timestamp),
                    color = IosLabelTertiary,
                    fontSize = 10.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Main conversion values
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${fromCurr.symbol} ${CurrencyFormatUtils.formatAmount(item.inputAmount)}",
                        color = IosLabelSecondary,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "${toCurr.symbol} ${CurrencyFormatUtils.formatAmount(item.resultAmount)}",
                        color = IosLabelPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Action buttons: Reuse & Delete
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedButton(
                        onClick = onReuse,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, IosSystemYellow),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = IosSystemYellow
                        ),
                        modifier = Modifier.testTag("reuse_button_${item.id}")
                    ) {
                        Icon(imageVector = Icons.Default.Replay, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reutilizar", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Eliminar",
                            tint = IosLabelTertiary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Tasa aplicada: Bs. ${CurrencyFormatUtils.formatAmount(item.rateApplied)}",
                color = IosLabelTertiary,
                fontSize = 10.sp
            )
        }
    }
}

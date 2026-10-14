package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAlert
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.RateAlertEntity
import com.example.data.model.CurrencyType
import com.example.data.model.CurrentRates
import com.example.ui.components.CupertinoSegmentedControl
import com.example.ui.theme.IosLabelPrimary
import com.example.ui.theme.IosLabelSecondary
import com.example.ui.theme.IosLabelTertiary
import com.example.ui.theme.IosQuaternarySystemBackground
import com.example.ui.theme.IosSecondarySystemBackground
import com.example.ui.theme.IosSeparator
import com.example.ui.theme.IosSystemGreen
import com.example.ui.theme.IosSystemYellow
import com.example.ui.theme.IosTertiarySystemBackground
import com.example.util.CurrencyFormatUtils

@Composable
fun AlertsScreen(
    currentRates: CurrentRates,
    alertsList: List<RateAlertEntity>,
    onAddAlert: (CurrencyType, Double, String) -> Unit,
    onDeleteAlert: (Long) -> Unit,
    onToggleAlert: (Long, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var selectedCurrency by remember { mutableStateOf(CurrencyType.USD) }
    var selectedCondition by remember { mutableStateOf("ABOVE") } // "ABOVE" or "BELOW"
    var targetRateText by remember { mutableStateOf("") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "Alertas de Cotización",
                color = IosLabelPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Recibe avisos visuales cuando la tasa cruce tus objetivos de compra o venta",
                color = IosLabelSecondary,
                fontSize = 12.sp
            )
        }

        // New Alert Creation Card (Cupertino Inset Grouped)
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = IosSecondarySystemBackground),
                border = BorderStroke(0.5.dp, IosSeparator),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("create_alert_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Configurar Nueva Alerta",
                        color = IosSystemYellow,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Currency Selector with Cupertino Segmented Control
                    Text(
                        text = "DIVISA",
                        color = IosLabelTertiary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.8.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    CupertinoSegmentedControl(
                        items = listOf(CurrencyType.USD, CurrencyType.EUR, CurrencyType.USDT),
                        selectedItem = selectedCurrency,
                        onItemSelected = { currency ->
                            selectedCurrency = currency
                            val current = currentRates.getRate(currency)
                            if (targetRateText.isEmpty()) {
                                targetRateText = String.format(java.util.Locale.US, "%.2f", current + 1.0)
                            }
                        },
                        labelProvider = { "${it.flagEmoji} ${it.code}" },
                        testTagPrefix = "alert_currency"
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Condition Selector with Cupertino Segmented Control
                    Text(
                        text = "CONDICIÓN DE DISPARO",
                        color = IosLabelTertiary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.8.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    CupertinoSegmentedControl(
                        items = listOf("ABOVE", "BELOW"),
                        selectedItem = selectedCondition,
                        onItemSelected = { selectedCondition = it },
                        labelProvider = { if (it == "ABOVE") "📈 Suba de (≥)" else "📉 Baje de (≤)" },
                        testTagPrefix = "alert_condition"
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Target Rate Input
                    Text(
                        text = "PRECIO OBJETIVO EN BOLÍVARES (VES)",
                        color = IosLabelTertiary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.8.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    OutlinedTextField(
                        value = targetRateText,
                        onValueChange = { targetRateText = it },
                        placeholder = { Text("Ejemplo: 50.00", color = IosLabelTertiary) },
                        leadingIcon = {
                            Text("Bs.", color = IosSystemYellow, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 10.dp))
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = IosTertiarySystemBackground,
                            unfocusedContainerColor = IosTertiarySystemBackground,
                            focusedBorderColor = IosSystemYellow,
                            unfocusedBorderColor = IosSeparator,
                            focusedTextColor = IosLabelPrimary,
                            unfocusedTextColor = IosLabelPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("target_rate_input")
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Create Button (Cupertino Solid Button)
                    Button(
                        onClick = {
                            val rate = CurrencyFormatUtils.parseAmount(targetRateText)
                            if (rate != null && rate > 0) {
                                onAddAlert(selectedCurrency, rate, selectedCondition)
                                targetRateText = ""
                                Toast.makeText(context, "¡Alerta creada con éxito!", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Ingresa un precio objetivo válido", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = IosSystemYellow,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("create_alert_button")
                    ) {
                        Icon(imageVector = Icons.Default.AddAlert, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Crear Alerta", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Active Alerts Header
        item {
            Text(
                text = "Tus Alertas Activas (${alertsList.size})",
                color = IosLabelPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }

        if (alertsList.isEmpty()) {
            item {
                Surface(
                    color = IosSecondarySystemBackground,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(0.5.dp, IosSeparator),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsNone,
                            contentDescription = null,
                            tint = IosLabelTertiary,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No tienes alertas creadas",
                            color = IosLabelSecondary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Agrega una alerta arriba para saber cuándo comprar o vender divisas.",
                            color = IosLabelTertiary,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(alertsList, key = { it.id }) { alert ->
                val currency = CurrencyType.fromCode(alert.currencyCode)
                val currentLiveRate = currentRates.getRate(currency)
                val isTriggered = if (alert.condition == "ABOVE") {
                    currentLiveRate >= alert.targetRate
                } else {
                    currentLiveRate <= alert.targetRate
                }

                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = IosSecondarySystemBackground),
                    border = BorderStroke(if (isTriggered && alert.isActive) 1.dp else 0.5.dp, if (isTriggered && alert.isActive) IosSystemYellow else IosSeparator),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("alert_item_${alert.id}")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = currency.flagEmoji, fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${currency.code} ${if (alert.condition == "ABOVE") "≥" else "≤"} Bs. ${CurrencyFormatUtils.formatAmount(alert.targetRate)}",
                                    color = IosLabelPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "Tasa actual: Bs. ${CurrencyFormatUtils.formatAmount(currentLiveRate)}",
                                color = currency.accentColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )

                            if (isTriggered && alert.isActive) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "🔔 ¡Condición cumplida ahora!",
                                    color = IosSystemYellow,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Cupertino Green Switch
                            Switch(
                                checked = alert.isActive,
                                onCheckedChange = { checked -> onToggleAlert(alert.id, checked) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = IosSystemGreen,
                                    uncheckedThumbColor = IosLabelTertiary,
                                    uncheckedTrackColor = IosTertiarySystemBackground
                                ),
                                modifier = Modifier.testTag("alert_switch_${alert.id}")
                            )

                            IconButton(
                                onClick = { onDeleteAlert(alert.id) },
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
                }
            }
        }
    }
}

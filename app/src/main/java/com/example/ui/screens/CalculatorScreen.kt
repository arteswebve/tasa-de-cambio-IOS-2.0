package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CurrencyType
import com.example.ui.CalculatorUiState
import com.example.ui.components.ArtesWebVeFooter
import com.example.ui.components.CupertinoSegmentedControl
import com.example.ui.theme.IosLabelPrimary
import com.example.ui.theme.IosLabelSecondary
import com.example.ui.theme.IosLabelTertiary
import com.example.ui.theme.IosQuaternarySystemBackground
import com.example.ui.theme.IosSecondarySystemBackground
import com.example.ui.theme.IosSeparator
import com.example.ui.theme.IosSystemBackground
import com.example.ui.theme.IosSystemYellow
import com.example.ui.theme.IosTertiarySystemBackground
import com.example.util.CurrencyFormatUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(
    state: CalculatorUiState,
    onInputChanged: (String) -> Unit,
    onQuickAmountClick: (Double) -> Unit,
    onToggleDirection: () -> Unit,
    onInfoClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val sourceCurrency = if (!state.isInverseDirection) state.selectedCurrency else CurrencyType.VES
    val targetCurrency = if (!state.isInverseDirection) CurrencyType.VES else state.selectedCurrency

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        // Section 1: iOS Inset Grouped - Direction Card
        Text(
            text = "CONVERSIÓN DE DIVISAS",
            color = IosLabelTertiary,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.8.sp,
            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
        )

        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = IosSecondarySystemBackground),
            border = BorderStroke(0.5.dp, IosSeparator),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Source badge
                CurrencyBadge(currency = sourceCurrency)

                // Direction invert button (iOS Circular Action)
                Surface(
                    shape = CircleShape,
                    color = IosSystemYellow,
                    modifier = Modifier.size(40.dp)
                ) {
                    IconButton(
                        onClick = onToggleDirection,
                        modifier = Modifier.testTag("swap_direction_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapVert,
                            contentDescription = "Invertir dirección de conversión",
                            tint = Color.Black,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                // Target badge
                CurrencyBadge(currency = targetCurrency)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Section 2: Amount Input Field (Real-time calculation as user types)
        Text(
            text = "MONTO A CALCULAR (${sourceCurrency.code})",
            color = IosLabelTertiary,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.8.sp,
            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
        )

        OutlinedTextField(
            value = state.inputAmountText,
            onValueChange = { newValue ->
                val filtered = newValue.filter { it.isDigit() || it == '.' || it == ',' }
                onInputChanged(filtered)
            },
            placeholder = { Text("0,00", color = IosLabelTertiary) },
            leadingIcon = {
                Text(
                    text = sourceCurrency.symbol,
                    color = IosSystemYellow,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 12.dp, end = 6.dp)
                )
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = IosTertiarySystemBackground,
                unfocusedContainerColor = IosSecondarySystemBackground,
                focusedBorderColor = IosSystemYellow,
                unfocusedBorderColor = IosSeparator,
                focusedTextColor = IosLabelPrimary,
                unfocusedTextColor = IosLabelPrimary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("amount_input_field")
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Quick amount chips (Cupertino Inset style)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val quickAmounts = if (!state.isInverseDirection) {
                listOf(5.0, 10.0, 20.0, 50.0, 100.0)
            } else {
                listOf(200.0, 500.0, 1000.0, 2500.0, 5000.0)
            }

            quickAmounts.forEach { amount ->
                val label = if (!state.isInverseDirection) {
                    "${sourceCurrency.symbol}${amount.toInt()}"
                } else {
                    "Bs. ${amount.toInt()}"
                }

                FilterChip(
                    selected = false,
                    onClick = { onQuickAmountClick(amount) },
                    label = { Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = IosTertiarySystemBackground,
                        labelColor = IosSystemYellow
                    ),
                    border = BorderStroke(0.5.dp, IosSeparator),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.testTag("quick_chip_${amount.toInt()}")
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Section 3: Detailed Result Breakdown Card (Apple Inset Grouped)
        Text(
            text = "TOTAL CALCULADO",
            color = IosLabelTertiary,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.8.sp,
            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
        )

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = IosSecondarySystemBackground),
            border = BorderStroke(1.dp, IosSystemYellow),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("result_card")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TOTAL A RECIBIR",
                        color = IosSystemYellow,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )

                    Surface(
                        color = IosTertiarySystemBackground,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(0.5.dp, IosSeparator)
                    ) {
                        Text(
                            text = "Tasa: Bs. ${CurrencyFormatUtils.formatAmount(state.currentRate)}",
                            color = IosLabelSecondary,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Big Formatted Result
                Text(
                    text = "${targetCurrency.symbol} ${CurrencyFormatUtils.formatAmount(state.calculatedResult)}",
                    color = IosLabelPrimary,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Breakdown description
                Text(
                    text = "Conversión de ${CurrencyFormatUtils.formatAmount(state.parsedInputAmount)} ${sourceCurrency.code} a ${targetCurrency.code}",
                    color = IosLabelTertiary,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // iOS Action Buttons: Copy & Share
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            val textToCopy = "${sourceCurrency.symbol} ${CurrencyFormatUtils.formatAmount(state.parsedInputAmount)} = ${targetCurrency.symbol} ${CurrencyFormatUtils.formatAmount(state.calculatedResult)} (Tasa: Bs. ${CurrencyFormatUtils.formatAmount(state.currentRate)} - TasaCambio 2.0 iOS)"
                            copyToClipboard(context, textToCopy)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = IosTertiarySystemBackground,
                            contentColor = IosLabelPrimary
                        ),
                        border = BorderStroke(0.5.dp, IosSeparator),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("copy_result_button")
                    ) {
                        Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Copiar", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = {
                            val shareText = "💱 *Cálculo de TasaCambio 2.0 iOS*\n\n" +
                                    "• Monto: ${sourceCurrency.symbol} ${CurrencyFormatUtils.formatAmount(state.parsedInputAmount)} ${sourceCurrency.code}\n" +
                                    "• Tasa aplicada: Bs. ${CurrencyFormatUtils.formatAmount(state.currentRate)}\n" +
                                    "• Total a recibir: *${targetCurrency.symbol} ${CurrencyFormatUtils.formatAmount(state.calculatedResult)} ${targetCurrency.code}*\n\n" +
                                    "Desarrollada por ArtesWebVE | https://arteswebve.com/"
                            shareCalculation(context, shareText)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = IosSystemYellow,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("share_result_button")
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Compartir", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Footer Section
        ArtesWebVeFooter(onInfoClick = onInfoClick)

        Spacer(modifier = Modifier.height(14.dp))
    }
}

@Composable
private fun CurrencyBadge(
    currency: CurrencyType,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Text(text = currency.flagEmoji, fontSize = 22.sp)
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(text = currency.title, color = IosLabelPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Text(text = currency.code, color = currency.accentColor, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("TasaCambio 2.0 iOS", text)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, "¡Cálculo copiado al portapapeles!", Toast.LENGTH_SHORT).show()
}

private fun shareCalculation(context: Context, text: String) {
    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, text)
        type = "text/plain"
    }
    val shareIntent = Intent.createChooser(sendIntent, "Compartir cálculo TasaCambio 2.0 iOS")
    context.startActivity(shareIntent)
}

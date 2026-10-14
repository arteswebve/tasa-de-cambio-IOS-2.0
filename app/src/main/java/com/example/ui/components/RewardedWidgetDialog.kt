package com.example.ui.components

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.CurrentRates
import com.example.ui.theme.BrandGoldDark
import com.example.ui.theme.BrandYellow
import com.example.ui.theme.CurrencyEurAmber
import com.example.ui.theme.CurrencyUsdGreen
import com.example.ui.theme.CurrencyUsdtCyan
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.GreenSuccess
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.CurrencyFormatUtils
import com.example.widget.RateAppWidgetProvider

@Composable
fun RewardedWidgetDialog(
    rates: CurrentRates,
    isUnlocked: Boolean,
    isVideoPlaying: Boolean,
    progress: Float,
    countdownSeconds: Int,
    onStartSimulation: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = BorderStroke(1.dp, BrandYellow),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("rewarded_widget_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top row with title & close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = BrandYellow.copy(alpha = 0.2f),
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Widgets,
                                    contentDescription = null,
                                    tint = BrandYellow,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isUnlocked) "¡Widget Desbloqueado!" else "¡Obtén tu Recompensa!",
                            color = TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (!isVideoPlaying) {
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cerrar",
                                tint = TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = if (isUnlocked)
                        "Ya tienes acceso exclusivo al widget de tasas en tu pantalla de inicio."
                    else
                        "Mira un video patrocinado de sólo 5 segundos y desbloquea el Widget interactivo para tu pantalla de inicio sin abrir la app.",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 17.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Interactive Pixel-Perfect Preview of the Widget
                Text(
                    text = "VISTA PREVIA DEL WIDGET",
                    color = BrandYellow,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(6.dp))

                Surface(
                    color = Color(0xFF0F172A),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, DarkBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // USD
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "🇺🇸 BCV", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            Text(text = CurrencyFormatUtils.formatAmount(rates.usdBcv), color = CurrencyUsdGreen, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }

                        Box(modifier = Modifier.width(1.dp).height(24.dp).background(DarkBorder))

                        // EUR
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "🇪🇺 EUR", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            Text(text = CurrencyFormatUtils.formatAmount(rates.eurBcv), color = CurrencyEurAmber, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }

                        Box(modifier = Modifier.width(1.dp).height(24.dp).background(DarkBorder))

                        // USDT
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "🪙 USDT", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            Text(text = CurrencyFormatUtils.formatAmount(rates.usdtBinance), color = CurrencyUsdtCyan, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Video Simulation Box or Completed Actions
                if (isVideoPlaying) {
                    Surface(
                        color = DarkSurfaceElevated,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, BrandYellow),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Reproduciendo anuncio patrocinado ArtesWebVE...",
                                color = BrandYellow,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            LinearProgressIndicator(
                                progress = { progress },
                                color = BrandYellow,
                                trackColor = DarkBorder,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Tu recompensa se desbloqueará en $countdownSeconds segundos",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                } else if (!isUnlocked) {
                    val activity = context as? android.app.Activity
                    val hasAdMob = com.example.util.AdMobManager.hasRewardedAd()

                    Button(
                        onClick = {
                            if (activity != null && hasAdMob) {
                                com.example.util.AdMobManager.showRewardedAd(
                                    activity = activity,
                                    onRewardEarned = { onStartSimulation() },
                                    onAdClosedOrFailed = { onStartSimulation() }
                                )
                            } else {
                                onStartSimulation()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BrandYellow,
                            contentColor = DarkBackground
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("start_reward_video_button")
                    ) {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (hasAdMob) "Ver Video AdMob / Desbloquear" else "Ver Video Recompensado (5s)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    // Unlocked State with Pin Button
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = GreenSuccess,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "¡Recompensa activa con éxito!",
                                color = GreenSuccess,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                pinAppWidgetToHomeScreen(context)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BrandYellow,
                                contentColor = DarkBackground
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("pin_widget_button")
                        ) {
                            Icon(imageVector = Icons.Default.AddCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Añadir Widget a Pantalla de Inicio",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Instructions
                        Surface(
                            color = DarkSurfaceElevated,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "📌 Pasos manuales:",
                                    color = BrandYellow,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "1. Ve a la pantalla de inicio de tu Android.\n2. Mantén presionado en un espacio vacío.\n3. Selecciona 'Widgets' y busca 'TasaCambio VE'.\n4. Arrástralo a tu pantalla principal.",
                                    color = TextSecondary,
                                    fontSize = 10.sp,
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun pinAppWidgetToHomeScreen(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val provider = ComponentName(context, RateAppWidgetProvider::class.java)

        if (appWidgetManager.isRequestPinAppWidgetSupported) {
            appWidgetManager.requestPinAppWidget(provider, null, null)
            Toast.makeText(context, "Solicitando añadir widget al lanzador...", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(
                context,
                "Tu lanzador no admite fijado directo. Por favor usa el menú de widgets de tu pantalla de inicio.",
                Toast.LENGTH_LONG
            ).show()
        }
    } else {
        Toast.makeText(
            context,
            "Mantén presionada tu pantalla de inicio y añade el widget desde la lista.",
            Toast.LENGTH_LONG
        ).show()
    }
}

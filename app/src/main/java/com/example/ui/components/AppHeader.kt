package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PhoneIphone
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.IosLabelPrimary
import com.example.ui.theme.IosLabelSecondary
import com.example.ui.theme.IosQuaternarySystemBackground
import com.example.ui.theme.IosSecondarySystemBackground
import com.example.ui.theme.IosSeparator
import com.example.ui.theme.IosSystemBackground
import com.example.ui.theme.IosSystemGreen
import com.example.ui.theme.IosSystemYellow
import com.example.ui.theme.IosTertiarySystemBackground

@Composable
fun AppHeader(
    isRewardUnlocked: Boolean,
    onRewardClick: () -> Unit,
    onIosCodeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Surface(
        color = IosSystemBackground,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // App Title & ArtesWebVE branding with Cupertino styling
            Column(
                modifier = Modifier
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        openArtesWebVe(context)
                    }
                    .testTag("app_brand_header")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "TasaCambio",
                        color = IosLabelPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Surface(
                        color = IosSystemYellow,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "2.0 iOS",
                            color = IosSystemBackground,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                        )
                    }
                }

                Text(
                    text = "por ArtesWebVE ↗",
                    color = IosSystemYellow,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Top-right Action Buttons: iOS Swift Code Viewer & Reward Button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Button to open Swift / iOS Code viewer
                Surface(
                    color = IosTertiarySystemBackground,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, IosSeparator),
                    modifier = Modifier.clickable { onIosCodeClick() }.testTag("view_ios_code_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhoneIphone,
                            contentDescription = "Código iOS",
                            tint = IosSystemYellow,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "iOS Swift",
                            color = IosLabelPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                if (!isRewardUnlocked) {
                    ElevatedButton(
                        onClick = onRewardClick,
                        colors = ButtonDefaults.elevatedButtonColors(
                            containerColor = IosSystemYellow,
                            contentColor = IosSystemBackground
                        ),
                        shape = RoundedCornerShape(16.dp),
                        contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
                        modifier = Modifier.testTag("reward_cta_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CardGiftcard,
                            contentDescription = "Recompensa",
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Widget",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    OutlinedButton(
                        onClick = onRewardClick,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = IosSystemGreen
                        ),
                        border = BorderStroke(1.dp, IosSeparator),
                        modifier = Modifier.testTag("widget_active_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Widget Activo",
                            tint = IosSystemGreen,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Activo",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

fun openArtesWebVe(context: Context) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://arteswebve.com/")).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    } catch (_: Exception) {
    }
}

package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.IosLabelSecondary
import com.example.ui.theme.IosSecondarySystemBackground
import com.example.ui.theme.IosSeparator
import com.example.ui.theme.IosSystemBackground
import com.example.ui.theme.IosSystemRed
import com.example.ui.theme.IosSystemYellow

enum class NavTab(
    val title: String,
    val icon: ImageVector,
    val testTag: String
) {
    CALCULATOR("Calculadora", Icons.Default.Calculate, "tab_calculator"),
    TRENDS("Tendencias", Icons.Default.ShowChart, "tab_trends"),
    HISTORY("Historial", Icons.Default.History, "tab_history"),
    ALERTS("Alertas", Icons.Default.Notifications, "tab_alerts")
}

/**
 * Authentic Apple iOS (Cupertino) Tab Bar.
 */
@Composable
fun BottomNavBar(
    selectedTab: NavTab,
    onTabSelected: (NavTab) -> Unit,
    historyCount: Int,
    alertsCount: Int,
    modifier: Modifier = Modifier
) {
    Surface(
        color = IosSecondarySystemBackground,
        border = BorderStroke(0.5.dp, IosSeparator),
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavTab.entries.forEach { tab ->
                val isSelected = tab == selectedTab
                val tint = if (isSelected) IosSystemYellow else IosLabelSecondary
                val badgeCount = when (tab) {
                    NavTab.HISTORY -> historyCount
                    NavTab.ALERTS -> alertsCount
                    else -> 0
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable { onTabSelected(tab) }
                        .testTag(tab.testTag),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        BadgedBox(
                            badge = {
                                if (badgeCount > 0) {
                                    Badge(
                                        containerColor = IosSystemRed,
                                        contentColor = Color.White
                                    ) {
                                        Text(
                                            text = if (badgeCount > 99) "99+" else badgeCount.toString(),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.title,
                                tint = tint,
                                modifier = Modifier.size(21.dp)
                            )
                        }

                        Text(
                            text = tab.title,
                            color = tint,
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

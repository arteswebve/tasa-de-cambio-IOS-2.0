package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// ==========================================
// Apple iOS Cupertino System Colors (Dark Mode)
// ==========================================

// True iOS OLED Black Background
val IosSystemBackground = Color(0xFF000000)
// iOS Grouped Background (Settings / Inset Lists)
val IosSecondarySystemBackground = Color(0xFF1C1C1E)
// iOS Grouped Secondary (Card Interiors / Fill)
val IosTertiarySystemBackground = Color(0xFF2C2C2E)
// iOS Elevated Surface / Hover Fill
val IosQuaternarySystemBackground = Color(0xFF3A3A3C)
// iOS Hairline Separator / Border (0.5dp)
val IosSeparator = Color(0xFF38383A)
val IosOpaqueSeparator = Color(0xFF48484A)

// iOS System Accent Colors
val IosSystemYellow = Color(0xFFFFD60A) // iOS Native Yellow / Gold
val IosSystemGoldDark = Color(0xFFD4AF37)
val IosSystemBlue = Color(0xFF0A84FF)   // iOS Native System Blue
val IosSystemGreen = Color(0xFF30D158)  // iOS Native System Green
val IosSystemOrange = Color(0xFFFF9F0A) // iOS Native System Orange
val IosSystemRed = Color(0xFFFF453A)    // iOS Native System Red
val IosSystemCyan = Color(0xFF64D2FF)   // iOS Native System Teal/Cyan
val IosSystemIndigo = Color(0xFF5E5CE6) // iOS Native System Indigo

// iOS Typography Label Hierarchy
val IosLabelPrimary = Color(0xFFFFFFFF)
val IosLabelSecondary = Color(0xFF8E8E93)
val IosLabelTertiary = Color(0xFF636366)
val IosLabelQuaternary = Color(0xFF48484A)

// ==========================================
// Compatibility Aliases for Existing Architecture
// ==========================================
val DarkBackground = IosSystemBackground
val DarkSurface = IosSecondarySystemBackground
val DarkSurfaceElevated = IosTertiarySystemBackground
val DarkBorder = IosSeparator
val DarkBorderHighlight = IosOpaqueSeparator

val BrandYellow = IosSystemYellow
val BrandGoldDark = IosSystemGoldDark
val BrandYellowLight = Color(0xFFFFE066)

val CurrencyUsdGreen = IosSystemGreen
val CurrencyEurAmber = IosSystemOrange
val CurrencyUsdtCyan = IosSystemCyan
val CurrencyVesBlue = IosSystemBlue

val TextPrimary = IosLabelPrimary
val TextSecondary = IosLabelSecondary
val TextMuted = IosLabelTertiary

val RedDanger = IosSystemRed
val GreenSuccess = IosSystemGreen

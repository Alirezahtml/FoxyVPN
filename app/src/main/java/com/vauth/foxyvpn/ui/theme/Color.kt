package com.vauth.foxyvpn.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

val FoxSeed = Color(0xFFFF5722)

// Luxury Obsidian Theme Colors
val LuxuryDarkBg = Color(0xFF0C0C10)
val LuxuryDarkCard = Color(0xFF16161D)
val LuxuryDarkCardBorder = Color(0xFF262634)
val LuxuryDarkDock = Color(0xFF13131A)
val LuxuryOrange = Color(0xFFFF5722)
val LuxuryOrangeLight = Color(0xFFFF7A45)
val LuxuryOrangeGlow = Color(0xFFFF3D00)
val LuxuryEmerald = Color(0xFF00E676)
val LuxuryAmber = Color(0xFFFFAB00)

val md_theme_light_primary = Color(0xFFFF5722)
val md_theme_light_onPrimary = Color(0xFFFFFFFF)
val md_theme_light_primaryContainer = Color(0xFFFFDBCB)
val md_theme_light_onPrimaryContainer = Color(0xFF351000)
val md_theme_light_inversePrimary = Color(0xFFFFB59B)

val md_theme_light_secondary = Color(0xFF765749)
val md_theme_light_onSecondary = Color(0xFFFFFFFF)
val md_theme_light_secondaryContainer = Color(0xFFFFDBCB)
val md_theme_light_onSecondaryContainer = Color(0xFF2B160C)

val md_theme_light_tertiary = Color(0xFF64612E)
val md_theme_light_onTertiary = Color(0xFFFFFFFF)
val md_theme_light_tertiaryContainer = Color(0xFFEBE7A6)
val md_theme_light_onTertiaryContainer = Color(0xFF1E1D00)

val md_theme_light_error = Color(0xFFBA1A1A)
val md_theme_light_onError = Color(0xFFFFFFFF)
val md_theme_light_errorContainer = Color(0xFFFFDAD6)
val md_theme_light_onErrorContainer = Color(0xFF410002)

val md_theme_light_background = Color(0xFF0C0C10)
val md_theme_light_onBackground = Color(0xFFEDEDF4)
val md_theme_light_surface = Color(0xFF14141B)
val md_theme_light_onSurface = Color(0xFFEDEDF4)
val md_theme_light_surfaceVariant = Color(0xFF21212B)
val md_theme_light_onSurfaceVariant = Color(0xFFA5A5B8)
val md_theme_light_surfaceTint = md_theme_light_primary

val md_theme_light_surfaceContainerLowest = Color(0xFF0C0C10)
val md_theme_light_surfaceContainerLow = Color(0xFF14141B)
val md_theme_light_surfaceContainer = Color(0xFF191922)
val md_theme_light_surfaceContainerHigh = Color(0xFF22222E)
val md_theme_light_surfaceContainerHighest = Color(0xFF2B2B3A)
val md_theme_light_surfaceBright = Color(0xFF22222E)
val md_theme_light_surfaceDim = Color(0xFF0C0C10)

val md_theme_light_outline = Color(0xFF3B3B4D)
val md_theme_light_outlineVariant = Color(0xFF262634)
val md_theme_light_inverseSurface = Color(0xFFEDEDF4)
val md_theme_light_inverseOnSurface = Color(0xFF14141B)
val md_theme_light_scrim = Color(0xFF000000)

val md_theme_dark_primary = Color(0xFFFF5722)
val md_theme_dark_onPrimary = Color(0xFFFFFFFF)
val md_theme_dark_primaryContainer = Color(0xFF4A1806)
val md_theme_dark_onPrimaryContainer = Color(0xFFFFDBCB)
val md_theme_dark_inversePrimary = Color(0xFFA03A00)

val md_theme_dark_secondary = Color(0xFFE6BEAF)
val md_theme_dark_onSecondary = Color(0xFF442A20)
val md_theme_dark_secondaryContainer = Color(0xFF382319)
val md_theme_dark_onSecondaryContainer = Color(0xFFFFDBCB)

val md_theme_dark_tertiary = Color(0xFFCFCB8D)
val md_theme_dark_onTertiary = Color(0xFF343205)
val md_theme_dark_tertiaryContainer = Color(0xFF4B4919)
val md_theme_dark_onTertiaryContainer = Color(0xFFEBE7A6)

val md_theme_dark_error = Color(0xFFFFB4AB)
val md_theme_dark_onError = Color(0xFF690005)
val md_theme_dark_errorContainer = Color(0xFF93000A)
val md_theme_dark_onErrorContainer = Color(0xFFFFDAD6)

val md_theme_dark_background = Color(0xFF0C0C10)
val md_theme_dark_onBackground = Color(0xFFEDEDF4)
val md_theme_dark_surface = Color(0xFF14141B)
val md_theme_dark_onSurface = Color(0xFFEDEDF4)
val md_theme_dark_surfaceVariant = Color(0xFF21212B)
val md_theme_dark_onSurfaceVariant = Color(0xFFA5A5B8)
val md_theme_dark_surfaceTint = md_theme_dark_primary

val md_theme_dark_surfaceContainerLowest = Color(0xFF0C0C10)
val md_theme_dark_surfaceContainerLow = Color(0xFF14141B)
val md_theme_dark_surfaceContainer = Color(0xFF191922)
val md_theme_dark_surfaceContainerHigh = Color(0xFF22222E)
val md_theme_dark_surfaceContainerHighest = Color(0xFF2B2B3A)
val md_theme_dark_surfaceBright = Color(0xFF22222E)
val md_theme_dark_surfaceDim = Color(0xFF0C0C10)

val md_theme_dark_outline = Color(0xFF3B3B4D)
val md_theme_dark_outlineVariant = Color(0xFF262634)
val md_theme_dark_inverseSurface = Color(0xFFEDEDF4)
val md_theme_dark_inverseOnSurface = Color(0xFF14141B)
val md_theme_dark_scrim = Color(0xFF000000)

data class FoxyStatusColors(
    val connected: Color,
    val connecting: Color,
    val disconnected: Color,
)

val LightStatusColors = FoxyStatusColors(
    connected = Color(0xFF00E676),
    connecting = Color(0xFFFFAB00),
    disconnected = Color(0xFF6B6B7F),
)

val DarkStatusColors = FoxyStatusColors(
    connected = Color(0xFF00E676),
    connecting = Color(0xFFFFAB00),
    disconnected = Color(0xFF6B6B7F),
)

val LocalFoxyStatusColors = staticCompositionLocalOf { LightStatusColors }

val ConnectedGreen = LightStatusColors.connected
val ConnectingAmber = LightStatusColors.connecting

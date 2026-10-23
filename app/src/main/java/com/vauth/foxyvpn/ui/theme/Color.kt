package com.vauth.foxyvpn.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

val FoxSeed = Color(0xFFE06236)

// Neumorphic Light Palette
val NeuLightBg = Color(0xFFE8ECF2)
val NeuLightSurface = Color(0xFFE8ECF2)
val NeuLightSurfaceElevated = Color(0xFFEFF3F8)
val NeuLightShadowLight = Color(0xFFFFFFFF)
val NeuLightShadowDark = Color(0xFFAAB8CC)

val md_theme_light_primary = Color(0xFFD65625)
val md_theme_light_onPrimary = Color(0xFFFFFFFF)
val md_theme_light_primaryContainer = Color(0xFFFFDCD0)
val md_theme_light_onPrimaryContainer = Color(0xFF381000)
val md_theme_light_inversePrimary = Color(0xFFFFB59C)

val md_theme_light_secondary = Color(0xFF626E7E)
val md_theme_light_onSecondary = Color(0xFFFFFFFF)
val md_theme_light_secondaryContainer = Color(0xFFDCE2EC)
val md_theme_light_onSecondaryContainer = Color(0xFF1A222B)

val md_theme_light_tertiary = Color(0xFF4C756B)
val md_theme_light_onTertiary = Color(0xFFFFFFFF)
val md_theme_light_tertiaryContainer = Color(0xFFCCE8DF)
val md_theme_light_onTertiaryContainer = Color(0xFF07201A)

val md_theme_light_error = Color(0xFFBA1A1A)
val md_theme_light_onError = Color(0xFFFFFFFF)
val md_theme_light_errorContainer = Color(0xFFFFDAD6)
val md_theme_light_onErrorContainer = Color(0xFF410002)

val md_theme_light_background = NeuLightBg
val md_theme_light_onBackground = Color(0xFF1C222A)
val md_theme_light_surface = NeuLightSurface
val md_theme_light_onSurface = Color(0xFF1C222A)
val md_theme_light_surfaceVariant = Color(0xFFDFE4ED)
val md_theme_light_onSurfaceVariant = Color(0xFF556070)
val md_theme_light_surfaceTint = md_theme_light_primary

val md_theme_light_surfaceContainerLowest = Color(0xFFFFFFFF)
val md_theme_light_surfaceContainerLow = Color(0xFFF0F3F8)
val md_theme_light_surfaceContainer = NeuLightSurface
val md_theme_light_surfaceContainerHigh = Color(0xFFE2E7EE)
val md_theme_light_surfaceContainerHighest = Color(0xFFDBE0E8)
val md_theme_light_surfaceBright = Color(0xFFF7F9FC)
val md_theme_light_surfaceDim = Color(0xFFD4DAE3)

val md_theme_light_outline = Color(0xFFB5C0CE)
val md_theme_light_outlineVariant = Color(0xFFCAD2DC)
val md_theme_light_inverseSurface = Color(0xFF2C323B)
val md_theme_light_inverseOnSurface = Color(0xFFF0F4FA)
val md_theme_light_scrim = Color(0xFF000000)

// Neumorphic Dark Palette
val NeuDarkBg = Color(0xFF191C22)
val NeuDarkSurface = Color(0xFF1E2229)
val NeuDarkSurfaceElevated = Color(0xFF252A33)
val NeuDarkShadowLight = Color(0xFF2A313C)
val NeuDarkShadowDark = Color(0xFF0F1115)

val md_theme_dark_primary = Color(0xFFF47B4B)
val md_theme_dark_onPrimary = Color(0xFF4A1804)
val md_theme_dark_primaryContainer = Color(0xFF6E2A0E)
val md_theme_dark_onPrimaryContainer = Color(0xFFFFDCD0)
val md_theme_dark_inversePrimary = Color(0xFFD65625)

val md_theme_dark_secondary = Color(0xFFA5B2C4)
val md_theme_dark_onSecondary = Color(0xFF1B232C)
val md_theme_dark_secondaryContainer = Color(0xFF333D4A)
val md_theme_dark_onSecondaryContainer = Color(0xFFD6DFEC)

val md_theme_dark_tertiary = Color(0xFF86BAAD)
val md_theme_dark_onTertiary = Color(0xFF072A22)
val md_theme_dark_tertiaryContainer = Color(0xFF23443B)
val md_theme_dark_onTertiaryContainer = Color(0xFFCCE8DF)

val md_theme_dark_error = Color(0xFFFFB4AB)
val md_theme_dark_onError = Color(0xFF690005)
val md_theme_dark_errorContainer = Color(0xFF93000A)
val md_theme_dark_onErrorContainer = Color(0xFFFFDAD6)

val md_theme_dark_background = NeuDarkBg
val md_theme_dark_onBackground = Color(0xFFECF0F6)
val md_theme_dark_surface = NeuDarkSurface
val md_theme_dark_onSurface = Color(0xFFECF0F6)
val md_theme_dark_surfaceVariant = Color(0xFF292F39)
val md_theme_dark_onSurfaceVariant = Color(0xFF99A6B6)
val md_theme_dark_surfaceTint = md_theme_dark_primary

val md_theme_dark_surfaceContainerLowest = Color(0xFF121419)
val md_theme_dark_surfaceContainerLow = Color(0xFF16191F)
val md_theme_dark_surfaceContainer = NeuDarkSurface
val md_theme_dark_surfaceContainerHigh = Color(0xFF242931)
val md_theme_dark_surfaceContainerHighest = Color(0xFF2B313B)
val md_theme_dark_surfaceBright = Color(0xFF363E4A)
val md_theme_dark_surfaceDim = Color(0xFF171A20)

val md_theme_dark_outline = Color(0xFF4B5565)
val md_theme_dark_outlineVariant = Color(0xFF303743)
val md_theme_dark_inverseSurface = Color(0xFFECF0F6)
val md_theme_dark_inverseOnSurface = Color(0xFF1E2229)
val md_theme_dark_scrim = Color(0xFF000000)

data class FoxyStatusColors(
    val connected: Color,
    val connecting: Color,
    val disconnected: Color,
)

val LightStatusColors = FoxyStatusColors(
    connected = Color(0xFF2E7D32),
    connecting = Color(0xFFD97706),
    disconnected = Color(0xFF6B7280),
)

val DarkStatusColors = FoxyStatusColors(
    connected = Color(0xFF4ADE80),
    connecting = Color(0xFFFBBF24),
    disconnected = Color(0xFF94A3B8),
)

val LocalFoxyStatusColors = staticCompositionLocalOf { LightStatusColors }

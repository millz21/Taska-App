package com.example.ui.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

object TaskaThemeState {
    var isDark by mutableStateOf(false)
}

// Primary Taska Brand Palette (Adaptive for crisp contrast in Dark Mode)
val TaskaViolet: Color
    get() = if (TaskaThemeState.isDark) Color(0xFF7C63FF) else Color(0xFF5B3DF5)
val TaskaVioletSolid = Color(0xFF5B3DF5)
val TaskaVioletDark = Color(0xFF4326D9)
val TaskaVioletDeep = Color(0xFF28157A)
val TaskaVioletSoft: Color
    get() = if (TaskaThemeState.isDark) Color(0xFF28204D) else Color(0xFFEDE8FF)

val TaskaLime = Color(0xFFD4F358)
val TaskaLimeDark = Color(0xFFB4D62F)
val TaskaLimeSoft: Color
    get() = if (TaskaThemeState.isDark) Color(0xFF263012) else Color(0xFFF3F9D2)

// Fixed Constants for Dark/Light Overlays
val TaskaPureWhite = Color(0xFFFFFFFF)
val TaskaDeepInk = Color(0xFF14141F)

// Dynamic Theme-Reactive Backgrounds & Surfaces
val TaskaCream: Color
    get() = if (TaskaThemeState.isDark) Color(0xFF0B0A12) else Color(0xFFF7F6F2)

val TaskaCreamSecondary: Color
    get() = if (TaskaThemeState.isDark) Color(0xFF141222) else Color(0xFFEFEDE6)

val TaskaSurfaceAlt: Color
    get() = TaskaCreamSecondary

val TaskaWhite: Color
    get() = if (TaskaThemeState.isDark) Color(0xFF181629) else Color(0xFFFFFFFF)

val TaskaGlassWhite: Color
    get() = if (TaskaThemeState.isDark) Color(0xE61F1C35) else Color(0xEBFFFFFF)

val TaskaGlassBorder: Color
    get() = if (TaskaThemeState.isDark) Color(0x557C63FF) else Color(0xCCFFFFFF)

val TaskaGlassCardBrush: Brush
    get() = Brush.linearGradient(
        colors = if (TaskaThemeState.isDark) {
            listOf(Color(0xEE221E3A), Color(0xDD161326))
        } else {
            listOf(Color(0xF8FFFFFF), Color(0xEEF3F0FF))
        }
    )

val TaskaGlassHeaderBrush: Brush
    get() = Brush.verticalGradient(
        colors = if (TaskaThemeState.isDark) {
            listOf(Color(0xF2151324), Color(0xE60B0A12))
        } else {
            listOf(Color(0xF8FFFFFF), Color(0xF0F7F6F2))
        }
    )

val TaskaInk: Color
    get() = if (TaskaThemeState.isDark) Color(0xFFF5F4FC) else Color(0xFF14141F)

val TaskaCharcoal: Color
    get() = if (TaskaThemeState.isDark) Color(0xFF252238) else Color(0xFF1E1E2E)

val TaskaMutedText: Color
    get() = if (TaskaThemeState.isDark) Color(0xFFAFAAC9) else Color(0xFF5E5E72)

val TaskaBorder: Color
    get() = if (TaskaThemeState.isDark) Color(0xFF342E52) else Color(0xFFE5E3DC)

// Category Pastel Card Colors (Theme-Adaptive for Light & Dark Mode)
val CategoryLavender: Color
    get() = if (TaskaThemeState.isDark) Color(0xFF2C2354) else Color(0xFFE5DEFF)
val CategoryLavenderIcon: Color
    get() = if (TaskaThemeState.isDark) Color(0xFFB8A7FF) else Color(0xFF4E32E0)

val CategoryMint: Color
    get() = if (TaskaThemeState.isDark) Color(0xFF143B2B) else Color(0xFFD8F5E8)
val CategoryMintIcon: Color
    get() = if (TaskaThemeState.isDark) Color(0xFF6CE6B1) else Color(0xFF0E6B45)

val CategoryPeach: Color
    get() = if (TaskaThemeState.isDark) Color(0xFF42231A) else Color(0xFFFFE4D9)
val CategoryPeachIcon: Color
    get() = if (TaskaThemeState.isDark) Color(0xFFFFAD8F) else Color(0xFF9C3B16)

val CategoryButter: Color
    get() = if (TaskaThemeState.isDark) Color(0xFF3B3212) else Color(0xFFF5ECC6)
val CategoryButterIcon: Color
    get() = if (TaskaThemeState.isDark) Color(0xFFF5D969) else Color(0xFF6B530B)

val CategorySky: Color
    get() = if (TaskaThemeState.isDark) Color(0xFF172D4F) else Color(0xFFDCEBFF)
val CategorySkyIcon: Color
    get() = if (TaskaThemeState.isDark) Color(0xFF8CC0FF) else Color(0xFF164E9F)

val CategoryCoolGray: Color
    get() = if (TaskaThemeState.isDark) Color(0xFF252836) else Color(0xFFEAECEF)
val CategoryCoolGrayIcon: Color
    get() = if (TaskaThemeState.isDark) Color(0xFFD0D5E3) else Color(0xFF2B303C)

// Status & Accents
val VerifiedGreen: Color
    get() = if (TaskaThemeState.isDark) Color(0xFF34D389) else Color(0xFF129A5A)
val VerifiedGreenBg: Color
    get() = if (TaskaThemeState.isDark) Color(0xFF123626) else Color(0xFFE3F9EE)
val StarAmber = Color(0xFFF59E0B)
val StarGold = StarAmber
val CoralDot = Color(0xFFFF6E4A)

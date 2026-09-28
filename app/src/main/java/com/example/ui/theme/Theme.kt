package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.key
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

private val TaskaLightColorScheme = lightColorScheme(
    primary = Color(0xFF5B3DF5),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEDE8FF),
    onPrimaryContainer = TaskaVioletDeep,
    secondary = TaskaLime,
    onSecondary = Color(0xFF14141F),
    secondaryContainer = Color(0xFFF3F9D2),
    onSecondaryContainer = Color(0xFF14141F),
    tertiary = CoralDot,
    onTertiary = Color.White,
    background = Color(0xFFF7F6F2),
    onBackground = Color(0xFF14141F),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF14141F),
    surfaceVariant = Color(0xFFEFEDE6),
    onSurfaceVariant = Color(0xFF5E5E72),
    outline = Color(0xFFE5E3DC)
)

private val TaskaDarkColorScheme = darkColorScheme(
    primary = Color(0xFF8B72FF),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF2D274B),
    onPrimaryContainer = Color.White,
    secondary = TaskaLime,
    onSecondary = Color(0xFF14141F),
    secondaryContainer = Color(0xFF263012),
    onSecondaryContainer = Color(0xFFF3F9D2),
    background = Color(0xFF0B0A12),
    onBackground = Color(0xFFF5F4FC),
    surface = Color(0xFF181629),
    onSurface = Color(0xFFF5F4FC),
    surfaceVariant = Color(0xFF141222),
    onSurfaceVariant = Color(0xFFAFAAC9),
    outline = Color(0xFF342E52)
)

val TaskaShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    TaskaThemeState.isDark = darkTheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                WindowCompat.getInsetsController(window, view).apply {
                    isAppearanceLightStatusBars = !darkTheme
                    isAppearanceLightNavigationBars = !darkTheme
                }
            }
        }
    }

    MaterialTheme(
        colorScheme = if (darkTheme) TaskaDarkColorScheme else TaskaLightColorScheme,
        typography = Typography,
        shapes = TaskaShapes
    ) {
        key(darkTheme) {
            content()
        }
    }
}

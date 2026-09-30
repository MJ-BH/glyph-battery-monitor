package com.nothing.glyphbattery.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = NothingRed,
    onPrimary = NothingWhite,
    primaryContainer = NothingCardBorder,
    onPrimaryContainer = NothingWhite,
    secondary = NothingWhiteMuted,
    onSecondary = NothingBlack,
    background = NothingBlack,
    onBackground = NothingWhite,
    surface = NothingDarkSurface,
    onSurface = NothingWhite,
    surfaceVariant = NothingCardSurface,
    onSurfaceVariant = NothingWhiteMuted,
    outline = NothingCardBorder
)

private val LightColorScheme = lightColorScheme(
    primary = NothingRed,
    onPrimary = NothingWhite,
    primaryContainer = NothingWhiteMuted,
    onPrimaryContainer = NothingBlack,
    secondary = NothingBlack,
    onSecondary = NothingWhite,
    background = NothingWhite,
    onBackground = NothingBlack,
    surface = NothingWhite,
    onSurface = NothingBlack,
    surfaceVariant = NothingWhiteMuted,
    onSurfaceVariant = NothingBlack,
    outline = NothingCardBorder
)

@Composable
fun GlyphBatteryTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Defaulting to Signature Nothing Dark Theme for authentic OLED Glyph aesthetics
    val colorScheme = if (darkTheme) DarkColorScheme else DarkColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.background.toArgb()
                window.navigationBarColor = colorScheme.background.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}

package com.jrlabs.audiocity.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * AudioCity Theme - Basado en Theme.swift de iOS
 * Color primario: Coral (#FF5757)
 */

private val DarkColorScheme = darkColorScheme(
    primary = ACPrimaryDarkMode,
    onPrimary = ACTextInverted,
    primaryContainer = ACPrimarySurfaceDark,
    onPrimaryContainer = ACTextPrimaryDark,
    secondary = ACSecondary,
    onSecondary = ACTextInverted,
    secondaryContainer = ACSecondaryDark,
    onSecondaryContainer = ACTextInverted,
    tertiary = ACGold,
    onTertiary = ACTextPrimary,
    background = ACBackgroundDark,
    onBackground = ACTextPrimaryDark,
    surface = ACSurfaceDark,
    onSurface = ACTextPrimaryDark,
    surfaceVariant = ACSurfaceElevatedDark,
    onSurfaceVariant = ACTextSecondaryDark,
    outline = ACBorderDark,
    outlineVariant = ACDividerDark,
    error = ACError,
    onError = ACTextInverted,
    errorContainer = ACErrorLight,
    onErrorContainer = ACError
)

private val LightColorScheme = lightColorScheme(
    primary = ACPrimary,
    onPrimary = ACTextInverted,
    primaryContainer = ACPrimaryLight,
    onPrimaryContainer = ACPrimaryDark,
    secondary = ACSecondary,
    onSecondary = ACTextInverted,
    secondaryContainer = ACSecondaryLight,
    onSecondaryContainer = ACSecondaryDark,
    tertiary = ACGold,
    onTertiary = ACTextPrimary,
    background = ACBackground,
    onBackground = ACTextPrimary,
    surface = ACSurface,
    onSurface = ACTextPrimary,
    surfaceVariant = ACBackground,
    onSurfaceVariant = ACTextSecondary,
    outline = ACBorder,
    outlineVariant = ACDivider,
    error = ACError,
    onError = ACTextInverted,
    errorContainer = ACErrorLight,
    onErrorContainer = ACError
)

@Composable
fun AudioCityTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Disabled to use brand colors
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Status bar con color de fondo (no coral para no ser invasivo)
            window.statusBarColor = if (darkTheme) {
                ACBackgroundDark.toArgb()
            } else {
                ACBackground.toArgb()
            }
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

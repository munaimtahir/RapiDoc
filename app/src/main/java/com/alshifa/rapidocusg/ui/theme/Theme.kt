package com.alshifa.rapidocusg.ui.theme

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
import com.alshifa.rapidocusg.core.documentengine.AppearanceMode
import com.alshifa.rapidocusg.core.documentengine.AppSettings

private val LightColors = lightColorScheme(
    background = LightBackground,
    surface = LightSurface,
    primary = LightPrimary,
    onPrimary = LightSurface,
    onBackground = LightTextPrimary,
    onSurface = LightTextPrimary,
    outline = LightBorder,
    surfaceVariant = LightSurface,
    onSurfaceVariant = LightTextSecondary,
    onSecondaryContainer = LightTextPrimary,
    secondaryContainer = LightSurface
)

private val DarkColors = darkColorScheme(
    background = DarkBackground,
    surface = DarkSurface,
    primary = DarkPrimary,
    onPrimary = DarkTextPrimary,
    onBackground = DarkTextPrimary,
    onSurface = DarkTextPrimary,
    outline = DarkBorder,
    surfaceVariant = DarkSurface,
    onSurfaceVariant = DarkTextSecondary,
    onSecondaryContainer = DarkTextPrimary,
    secondaryContainer = DarkSurface
)

val StatusNormal: androidx.compose.ui.graphics.Color
    @Composable
    get() = if (isDarkThemeActive()) StatusNormalDark else StatusNormalLight

val StatusAbnormal: androidx.compose.ui.graphics.Color
    @Composable
    get() = if (isDarkThemeActive()) StatusAbnormalDark else StatusAbnormalLight

var currentAppearanceMode: AppearanceMode = AppearanceMode.SYSTEM

@Composable
fun isDarkThemeActive(): Boolean {
    return when (currentAppearanceMode) {
        AppearanceMode.LIGHT -> false
        AppearanceMode.DARK -> true
        AppearanceMode.SYSTEM -> isSystemInDarkTheme()
    }
}

@Composable
fun RapiDocTheme(
    settings: AppSettings = AppSettings(),
    content: @Composable () -> Unit
) {
    currentAppearanceMode = settings.appearanceMode
    val darkTheme = isDarkThemeActive()
    val colorScheme = if (darkTheme) DarkColors else LightColors
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}

package com.example.ui.theme

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
import com.example.models.MetalType
import com.example.settings.ThemeMode

private val DarkColorScheme = darkColorScheme(
    primary = GoldPrimaryDark,
    onPrimary = GoldOnPrimaryDark,
    primaryContainer = GoldPrimaryContainerDark,
    onPrimaryContainer = GoldOnPrimaryContainerDark,
    background = NeutralDarkBackground,
    surface = NeutralDarkSurface,
    surfaceVariant = NeutralDarkSurfaceVariant,
    onBackground = TextPrimaryDark,
    onSurface = TextPrimaryDark,
    onSurfaceVariant = TextSecondaryDark
)

private val LightColorScheme = lightColorScheme(
    primary = GoldPrimaryLight,
    onPrimary = GoldOnPrimaryLight,
    primaryContainer = GoldPrimaryContainerLight,
    onPrimaryContainer = GoldOnPrimaryContainerLight,
    background = NeutralLightBackground,
    surface = NeutralLightSurface,
    surfaceVariant = NeutralLightSurfaceVariant,
    onBackground = TextPrimaryLight,
    onSurface = TextPrimaryLight,
    onSurfaceVariant = TextSecondaryLight
)

private val PurpleDarkColorScheme = darkColorScheme(
    primary = PurplePrimaryDark,
    onPrimary = PurpleOnPrimaryDark,
    primaryContainer = PurplePrimaryContainerDark,
    onPrimaryContainer = PurpleOnPrimaryContainerDark,
    background = NeutralDarkBackground,
    surface = NeutralDarkSurface,
    surfaceVariant = NeutralDarkSurfaceVariant,
    onBackground = TextPrimaryDark,
    onSurface = TextPrimaryDark,
    onSurfaceVariant = TextSecondaryDark
)

private val PurpleLightColorScheme = lightColorScheme(
    primary = PurplePrimaryLight,
    onPrimary = PurpleOnPrimaryLight,
    primaryContainer = PurplePrimaryContainerLight,
    onPrimaryContainer = PurpleOnPrimaryContainerLight,
    background = NeutralLightBackground,
    surface = NeutralLightSurface,
    surfaceVariant = NeutralLightSurfaceVariant,
    onBackground = TextPrimaryLight,
    onSurface = TextPrimaryLight,
    onSurfaceVariant = TextSecondaryLight
)

@Composable
fun PeeyooshTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    metalTheme: MetalType? = null,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val colorScheme = when {
        metalTheme == MetalType.SILVER -> if (darkTheme) PurpleDarkColorScheme else PurpleLightColorScheme
        else -> if (darkTheme) DarkColorScheme else LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.background.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

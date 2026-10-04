package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = CryptoDarkBackground,
    primaryContainer = CryptoDarkCardElevated,
    onPrimaryContainer = NeonCyan,
    secondary = CryptoGreen,
    onSecondary = CryptoDarkBackground,
    secondaryContainer = CryptoGreenBg,
    onSecondaryContainer = CryptoGreen,
    tertiary = CryptoAmber,
    onTertiary = CryptoDarkBackground,
    background = CryptoDarkBackground,
    onBackground = TextPrimary,
    surface = CryptoDarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = CryptoDarkCard,
    onSurfaceVariant = TextSecondary,
    outline = CryptoDarkBorder,
    outlineVariant = CryptoDarkCardElevated,
    error = CryptoRed,
    onError = TextPrimary,
    errorContainer = CryptoRedBg,
    onErrorContainer = CryptoRed
)

@Composable
fun AICryptoSignalTraderTheme(
    darkTheme: Boolean = true, // Trading app is permanently sleek dark theme
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = CryptoDarkBackground.toArgb()
            window.navigationBarColor = CryptoDarkSurface.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

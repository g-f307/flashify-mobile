package com.example.flashify.view.ui.theme

import android.app.Activity
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

// Importe suas novas cores
import com.example.flashify.view.ui.theme.LightYellowBackground
import com.example.flashify.view.ui.theme.CardBackgroundLight
import com.example.flashify.view.ui.theme.TextColorLight
import com.example.flashify.view.ui.theme.YellowAccent // Certifique-se que YellowAccent está aqui
import com.example.flashify.view.ui.theme.TextSecondary
import com.example.flashify.view.ui.theme.DarkBackground
import com.example.flashify.view.ui.theme.CardBackground
import com.example.flashify.view.ui.theme.TextPrimary
// Se você criou YellowGradientStart, importe-o também

private val MyLightColorScheme = lightColorScheme(
    primary = YellowAccent,
    onPrimary = Color.Black,
    background = Color.White, // **MUDANÇA AQUI:** Fundo será branco ou quase
    surface = CardBackgroundLight,
    onBackground = TextColorLight,
    onSurface = TextColorLight,
    secondary = TextSecondary,
    onSecondary = TextColorLight
)

private val DarkColorScheme = darkColorScheme(
    primary = YellowAccent,
    background = DarkBackground,
    surface = CardBackground,
    onPrimary = DarkBackground,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    primaryContainer = CardBackground,
    onSurfaceVariant = TextSecondary
)

@Composable
fun FlashifyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> MyLightColorScheme // Usando o tema claro com fundo branco
    }
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
        typography = Typography,
        content = content
    )
}
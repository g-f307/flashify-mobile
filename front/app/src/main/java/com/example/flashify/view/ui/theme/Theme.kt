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

// Esquema de cores para o TEMA CLARO (inspirado na web)
private val MyLightColorScheme = lightColorScheme(
    // Cor primária - Amarelo vibrante
    primary = YellowAccent,
    onPrimary = Color.Black,

    // Cor secundária
    secondary = LightTextSecondary,
    onSecondary = LightTextPrimary,

    // Fundo geral do app - O gradiente é aplicado pelo GradientBackgroundScreen
    background = Color.White, // Branco como base (o gradiente sobrescreve)
    onBackground = LightTextPrimary,

    // Fundo dos cards e superfícies
    surface = CardBackgroundLight, // Branco puro
    onSurface = TextColorLight,

    // Variantes
    surfaceVariant = Color(0xFFF5F5F5), // Cinza muito claro
    onSurfaceVariant = LightTextSecondary,

    // Container primário (usado em alguns cards)
    primaryContainer = LightSurface,
    onPrimaryContainer = LightTextPrimary,

    // Outline (bordas)
    outline = Color(0xFFE0E0E0),
    outlineVariant = Color(0xFFF0F0F0),

    // Cores de erro
    error = Color(0xFFD32F2F),
    onError = Color.White,
    errorContainer = Color(0xFFFFCDD2),
    onErrorContainer = Color(0xFFB71C1C)
)

// Esquema de cores para o TEMA ESCURO
private val DarkColorScheme = darkColorScheme(
    // Cor primária - Amarelo vibrante
    primary = YellowAccent,
    onPrimary = DarkBackground,

    // Cor secundária
    secondary = TextSecondary,
    onSecondary = TextPrimary,

    // Fundo geral do app
    background = DarkBackground,
    onBackground = TextPrimary,

    // Fundo dos cards e superfícies
    surface = CardBackground,
    onSurface = TextPrimary,

    // Variantes
    surfaceVariant = Color(0xFF3C3C3E),
    onSurfaceVariant = TextSecondary,

    // Container primário
    primaryContainer = CardBackground,
    onPrimaryContainer = TextPrimary,

    // Outline (bordas)
    outline = Color(0xFF4A4A4A),
    outlineVariant = Color(0xFF3A3A3A),

    // Cores de erro
    error = Color(0xFFEF5350),
    onError = Color.Black,
    errorContainer = Color(0xFF8B0000),
    onErrorContainer = Color(0xFFFFCDD2)
)

@Composable
fun FlashifyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    // Escolhe o esquema de cores baseado no tema
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> MyLightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Define a cor da barra de status
            window.statusBarColor = colorScheme.background.toArgb()
            // Define se os ícones da barra devem ser escuros (tema claro) ou claros (tema escuro)
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
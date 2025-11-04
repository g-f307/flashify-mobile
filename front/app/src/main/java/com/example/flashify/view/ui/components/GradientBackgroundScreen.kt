package com.example.flashify.view.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

@Composable
fun GradientBackgroundScreen(content: @Composable () -> Unit) {
    val isDarkTheme = isSystemInDarkTheme()

    // Define o brush do gradiente
    val gradientBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFFFF7DC), // Amarelo pálido
            Color(0xFFEFEFEF),
            Color(0xFFEFEFEF)
        ),
        startY = 0f,
        endY = 1200f
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .then(
                // Aplica o gradiente SOMENTE se for tema claro
                if (!isDarkTheme) {
                    Modifier.background(gradientBrush)
                } else {
                    // Se for tema escuro, usa o fundo padrão do tema escuro
                    Modifier.background(MaterialTheme.colorScheme.background)
                }
            )
    ) {
        content()
    }
}
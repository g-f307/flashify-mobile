package com.example.flashify.view.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

@Composable
fun GradientBackgroundScreen(
    isDarkTheme: Boolean,
    content: @Composable () -> Unit
) {
    // Gradiente EXTREMAMENTE suave (Quase imperceptível)
    val gradientBrush = Brush.verticalGradient(
        colors = listOf(
            // Topo: Um tom "creme" muito pálido (Light Yellow 50)
            // Dá apenas um toque de calor ao topo da tela
            Color(0xFFFFFDE7),

            // Fundo: Branco puro
            // O gradiente funde-se completamente com o branco antes de chegar ao meio
            Color(0xFFFFFFFF)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .then(
                if (!isDarkTheme) {
                    Modifier.background(gradientBrush)
                } else {
                    Modifier.background(MaterialTheme.colorScheme.background)
                }
            )
    ) {
        content()
    }
}
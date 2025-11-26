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
    // Gradiente suave para tema claro (do amarelo pálido ao branco)
    val lightGradientBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFFFFDE7), // Light Yellow 50 - topo
            Color(0xFFFFFEF5), // Transição suave
            Color(0xFFFFFFFF)  // Branco puro - fundo
        ),
        startY = 0f,
        endY = 1000f // Transição mais suave
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .then(
                if (!isDarkTheme) {
                    Modifier.background(lightGradientBrush)
                } else {
                    // Tema escuro usa cor sólida do MaterialTheme
                    Modifier.background(MaterialTheme.colorScheme.background)
                }
            )
    ) {
        content()
    }
}
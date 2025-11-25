package com.example.flashify.view.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.flashify.model.data.NavItem
import com.example.flashify.view.ui.theme.TextSecondary
import com.example.flashify.view.ui.theme.YellowAccent

@Composable
fun NavegacaoBotaoAbaixo(
    modifier: Modifier = Modifier,
    navItems: List<NavItem>,
    selectedItem: Int,
    onItemSelected: (Int) -> Unit
) {
    // Container ocupando toda a largura
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .drawBehind {
                // Linha de acento no topo
                drawRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            YellowAccent.copy(alpha = 0.3f),
                            YellowAccent.copy(alpha = 0.6f),
                            YellowAccent.copy(alpha = 0.3f),
                            Color.Transparent
                        )
                    ),
                    size = size.copy(height = 2.dp.toPx())
                )
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .padding(top = 2.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            navItems.forEachIndexed { index, item ->
                NavBarItem(
                    icon = item.icon,
                    label = item.label,
                    isSelected = selectedItem == index,
                    onClick = { onItemSelected(index) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun NavBarItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Animações otimizadas
    val iconScale by animateFloatAsState(
        targetValue = if (isSelected) 1.08f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "iconScale"
    )

    val iconColor by animateColorAsState(
        targetValue = if (isSelected) YellowAccent else TextSecondary,
        animationSpec = tween(200),
        label = "iconColor"
    )

    val indicatorWidth by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "indicatorWidth"
    )

    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxHeight(),
        color = Color.Transparent
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Ícone
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .graphicsLayer {
                        scaleX = iconScale
                        scaleY = iconScale
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    modifier = Modifier.size(24.dp),
                    tint = iconColor
                )
            }

            Spacer(modifier = Modifier.height(3.dp))

            // Label
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = if (isSelected) {
                    androidx.compose.ui.text.font.FontWeight.Bold
                } else {
                    androidx.compose.ui.text.font.FontWeight.Medium
                },
                color = iconColor.copy(alpha = if (isSelected) 1f else 0.7f),
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(3.dp))

            // Indicador inferior
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.5f * indicatorWidth)
                    .height(2.5.dp)
                    .background(
                        if (isSelected) {
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    YellowAccent,
                                    Color.Transparent
                                )
                            )
                        } else {
                            Brush.horizontalGradient(colors = listOf(Color.Transparent))
                        }
                    )
            )
        }
    }
}
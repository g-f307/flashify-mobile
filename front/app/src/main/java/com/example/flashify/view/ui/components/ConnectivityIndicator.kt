package com.example.flashify.view.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.flashify.model.manager.ConnectivityState

/**
 * ✅ Banner de conectividade que respeita os insets do sistema
 */
@Composable
fun ConnectivityBanner(
    connectivityState: ConnectivityState,
    onSyncClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // ✅ Animação de entrada/saída
    AnimatedVisibility(
        visible = !connectivityState.isOnline || connectivityState.pendingSyncCount > 0,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
        modifier = modifier
    ) {
        Surface(
            color = when {
                !connectivityState.isOnline -> Color(0xFFFF9800) // Laranja para offline
                connectivityState.isSyncing -> Color(0xFF2196F3) // Azul para sincronizando
                connectivityState.pendingSyncCount > 0 -> Color(0xFFFFC107) // Amarelo para pendente
                else -> Color(0xFF4CAF50) // Verde para online
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            // ✅ Adicionar statusBarsPadding para não ficar atrás da barra de status
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding() // ✅ IMPORTANTE: respeita a barra de status
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .clickable(enabled = connectivityState.pendingSyncCount > 0) {
                        onSyncClick()
                    },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Ícone e Mensagem
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Ícone animado
                    when {
                        connectivityState.isSyncing -> {
                            // Ícone rotativo durante sincronização
                            val rotation by rememberInfiniteTransition(label = "rotation").animateFloat(
                                initialValue = 0f,
                                targetValue = 360f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(1000, easing = LinearEasing),
                                    repeatMode = RepeatMode.Restart
                                ),
                                label = "rotation"
                            )
                            Icon(
                                Icons.Default.Sync,
                                contentDescription = "Sincronizando",
                                tint = Color.White,
                                modifier = Modifier
                                    .size(20.dp)
                                    .graphicsLayer { rotationZ = rotation }
                            )
                        }
                        !connectivityState.isOnline -> {
                            Icon(
                                Icons.Default.CloudOff,
                                contentDescription = "Offline",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        connectivityState.pendingSyncCount > 0 -> {
                            Icon(
                                Icons.Default.CloudQueue,
                                contentDescription = "Pendente",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Column {
                        Text(
                            text = when {
                                connectivityState.isSyncing -> "Sincronizando..."
                                !connectivityState.isOnline -> "Modo Offline"
                                connectivityState.pendingSyncCount > 0 -> "Alterações Pendentes"
                                else -> "Online"
                            },
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )

                        if (connectivityState.pendingSyncCount > 0 && !connectivityState.isSyncing) {
                            Text(
                                text = "${connectivityState.pendingSyncCount} ${
                                    if (connectivityState.pendingSyncCount == 1) "item" else "itens"
                                } para sincronizar",
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 11.sp
                            )
                        }

                        if (!connectivityState.isOnline) {
                            Text(
                                text = "Suas alterações serão salvas localmente",
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                // Botão de sincronização manual
                if (connectivityState.isOnline &&
                    connectivityState.pendingSyncCount > 0 &&
                    !connectivityState.isSyncing) {
                    Surface(
                        color = Color.White.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Sincronizar",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * ✅ Indicador compacto de status de conectividade (para usar em AppBars)
 */
@Composable
fun CompactConnectivityIndicator(
    connectivityState: ConnectivityState,
    modifier: Modifier = Modifier
) {
    if (!connectivityState.isOnline || connectivityState.pendingSyncCount > 0) {
        Surface(
            color = when {
                !connectivityState.isOnline -> Color(0xFFFF9800).copy(alpha = 0.9f)
                connectivityState.pendingSyncCount > 0 -> Color(0xFFFFC107).copy(alpha = 0.9f)
                else -> Color.Transparent
            },
            shape = RoundedCornerShape(50),
            modifier = modifier
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = if (!connectivityState.isOnline)
                        Icons.Default.CloudOff
                    else
                        Icons.Default.CloudQueue,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )

                if (connectivityState.pendingSyncCount > 0) {
                    Text(
                        text = connectivityState.pendingSyncCount.toString(),
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * Badge de status para ser usado nos cards
 */
@Composable
fun SyncStatusBadge(
    isSynced: Boolean,
    modifier: Modifier = Modifier
) {
    if (!isSynced) {
        Surface(
            color = Color(0xFFFFC107).copy(alpha = 0.15f),
            shape = RoundedCornerShape(6.dp),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                Color(0xFFFFC107).copy(alpha = 0.3f)
            ),
            modifier = modifier
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    Icons.Default.CloudQueue,
                    contentDescription = null,
                    tint = Color(0xFFFFC107),
                    modifier = Modifier.size(12.dp)
                )
                Text(
                    "Pendente",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFFC107)
                )
            }
        }
    }
}
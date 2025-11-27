package com.example.flashify.view.ui.components

import android.util.Log
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
 *
 * Este banner aparece na parte superior da tela e exibe:
 * - Estado de conectividade (Online/Offline)
 * - Status de sincronização
 * - Número de itens pendentes
 * - Botão para sincronizar manualmente
 *
 * IMPORTANTE: Usa statusBarsPadding() para não ficar ofuscado pela barra de status
 */
@Composable
fun ConnectivityBanner(
    connectivityState: ConnectivityState,
    onSyncClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = !connectivityState.isOnline || connectivityState.pendingSyncCount > 0,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
        modifier = modifier
    ) {
        Surface(
            color = when {
                !connectivityState.isOnline -> Color(0xFFFF9800)
                connectivityState.isSyncing -> Color(0xFF2196F3)
                connectivityState.pendingSyncCount > 0 -> Color(0xFFFFC107)
                else -> Color(0xFF4CAF50)
            },
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // SEÇÃO: Ícone + Mensagem
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    when {
                        connectivityState.isSyncing -> {
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

                    Column(modifier = Modifier.weight(1f)) {
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
                                text = "Seus dados estão disponíveis offline",
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                // ✅ SEÇÃO: Botão de Sincronização (CORRIGIDO)
                // Mostrar botão SEMPRE que houver pendentes e estiver online
                if (connectivityState.pendingSyncCount > 0 &&
                    !connectivityState.isSyncing &&
                    connectivityState.isOnline) {

                    Button(
                        onClick = {
                            Log.d("ConnectivityBanner", "🔄 Botão Sincronizar clicado")
                            onSyncClick()
                        },
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .height(32.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White.copy(alpha = 0.25f),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier
                                .size(14.dp)
                                .padding(end = 4.dp)
                        )
                        Text(
                            text = "Sincronizar",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

/**
 * ✅ Indicador compacto de status de conectividade
 *
 * Versão reduzida do banner, ideal para usar em AppBars/Toolbars
 * quando o espaço é limitado
 *
 * Exibe apenas um ícone + número de itens pendentes (se houver)
 */
@Composable
fun CompactConnectivityIndicator(
    connectivityState: ConnectivityState,
    modifier: Modifier = Modifier
) {
    // Só mostra se estiver offline ou tiver items pendentes
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
                // Ícone
                Icon(
                    imageVector = if (!connectivityState.isOnline)
                        Icons.Default.CloudOff
                    else
                        Icons.Default.CloudQueue,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )

                // Número de items pendentes (se houver)
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
 * Badge de status para ser usado nos cards de deck/flashcard
 *
 * Exibe um pequeno badge indicando se o item está sincronizado ou pendente
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

/**
 * ✅ Indicador detalhado de sincronização
 *
 * Mostra informações completas sobre o status de sincronização
 * Ideal para telas de configuração ou debug
 */
@Composable
fun DetailedSyncIndicator(
    connectivityState: ConnectivityState,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Título
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = if (connectivityState.isOnline)
                        Icons.Default.CloudDone
                    else
                        Icons.Default.CloudOff,
                    contentDescription = null,
                    tint = if (connectivityState.isOnline)
                        Color(0xFF4CAF50)
                    else
                        Color(0xFFFF9800),
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = if (connectivityState.isOnline) "Online" else "Offline",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Status de sincronização
            if (connectivityState.isSyncing) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val rotation by rememberInfiniteTransition(label = "sync_rotation").animateFloat(
                        initialValue = 0f,
                        targetValue = 360f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1500, easing = LinearEasing),
                            repeatMode = RepeatMode.Restart
                        ),
                        label = "sync_rotation"
                    )
                    Icon(
                        Icons.Default.Sync,
                        contentDescription = null,
                        tint = Color(0xFF2196F3),
                        modifier = Modifier
                            .size(18.dp)
                            .graphicsLayer { rotationZ = rotation }
                    )
                    Text(
                        "Sincronizando dados...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Items pendentes
            if (connectivityState.pendingSyncCount > 0) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = Color(0xFFFFC107),
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        "${connectivityState.pendingSyncCount} item(ns) pendente(s) de sincronização",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Última sincronização
            if (connectivityState.lastSyncTimestamp > 0) {
                val lastSyncText = remember(connectivityState.lastSyncTimestamp) {
                    val diff = System.currentTimeMillis() - connectivityState.lastSyncTimestamp
                    when {
                        diff < 60000 -> "Agora mesmo"
                        diff < 3600000 -> "${diff / 60000} minuto(s) atrás"
                        diff < 86400000 -> "${diff / 3600000} hora(s) atrás"
                        else -> "${diff / 86400000} dia(s) atrás"
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        "Última sincronização: $lastSyncText",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Estado WiFi
            if (connectivityState.isOnline && connectivityState.isWifi) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Wifi,
                        contentDescription = null,
                        tint = Color(0xFF2196F3),
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        "Conectado via WiFi",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
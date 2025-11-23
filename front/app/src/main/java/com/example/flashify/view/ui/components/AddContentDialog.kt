package com.example.flashify.view.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.flashify.view.ui.theme.YellowAccent
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class DifficultyOption(
    val value: String,
    val label: String,
    val icon: ImageVector,
    val description: String
)

@Composable
fun AddContentDialog(
    title: String,
    description: String,
    contentType: String,
    currentCount: Int,
    maxLimit: Int,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (Int, String) -> Unit,
    // ✅ NOVO: Parâmetro para verificar limite
    hasGenerationLimit: Boolean = false
) {
    var quantity by remember { mutableStateOf(5) }
    var customQuantity by remember { mutableStateOf("") }
    var difficulty by remember { mutableStateOf("Média") }
    var loadingProgress by remember { mutableStateOf(0f) }
    var currentLoadingMessage by remember { mutableStateOf("") }

    val availableSlots = maxLimit - currentCount
    val isOverLimit = quantity > availableSlots || quantity < 1

    val quantityOptions = listOf(3, 5, 8, 10)
    val difficultyOptions = listOf(
        DifficultyOption("Fácil", "Fácil", Icons.Default.CheckCircle, "Conceitos básicos"),
        DifficultyOption("Média", "Média", Icons.Default.Brightness5, "Compreensão média"),
        DifficultyOption("Difícil", "Difícil", Icons.Default.Stars, "Análise crítica")
    )

    val loadingMessages = if (contentType == "Flashcards") {
        listOf(
            "Analisando conteúdo...",
            "Identificando conceitos...",
            "Criando flashcards...",
            "Revisando duplicatas...",
            "Finalizando..."
        )
    } else {
        listOf(
            "Analisando conteúdo...",
            "Identificando tópicos...",
            "Gerando perguntas...",
            "Criando alternativas...",
            "Finalizando..."
        )
    }

    val scope = rememberCoroutineScope()

    LaunchedEffect(isLoading) {
        if (isLoading) {
            loadingProgress = 0f
            var messageIndex = 0
            currentLoadingMessage = loadingMessages[0]

            scope.launch {
                while (loadingProgress < 95f) {
                    delay(800)
                    loadingProgress += (5..15).random().toFloat()
                    if (loadingProgress > 95f) loadingProgress = 95f

                    val newMessageIndex = ((loadingProgress / 100f) * loadingMessages.size).toInt()
                        .coerceIn(0, loadingMessages.size - 1)

                    if (newMessageIndex != messageIndex) {
                        messageIndex = newMessageIndex
                        currentLoadingMessage = loadingMessages[messageIndex]
                    }
                }
            }
        }
    }

    Dialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = !isLoading,
            dismissOnClickOutside = !isLoading
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.9f),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Header fixo
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(YellowAccent.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                if (contentType == "Flashcards") Icons.Default.Star else Icons.Default.TrendingUp,
                                null,
                                tint = YellowAccent,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = title,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = description,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                lineHeight = 16.sp
                            )
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                // Conteúdo scrollável
                if (isLoading) {
                    LoadingContent(
                        progress = loadingProgress,
                        message = currentLoadingMessage
                    )
                } else {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(18.dp)
                    ) {
                        StatusCard(
                            currentCount = currentCount,
                            maxLimit = maxLimit,
                            availableSlots = availableSlots,
                            contentType = contentType
                        )

                        QuantitySelector(
                            quantity = quantity,
                            customQuantity = customQuantity,
                            availableSlots = availableSlots,
                            quantityOptions = quantityOptions,
                            onQuantityChange = { quantity = it },
                            onCustomQuantityChange = {
                                customQuantity = it
                                it.toIntOrNull()?.let { num ->
                                    quantity = num.coerceIn(1, availableSlots)
                                }
                            }
                        )

                        DifficultySelector(
                            difficulty = difficulty,
                            options = difficultyOptions,
                            onDifficultyChange = { difficulty = it }
                        )

                        SummaryCard(
                            quantity = quantity,
                            currentCount = currentCount,
                            maxLimit = maxLimit,
                            contentType = contentType
                        )

                        // ✅ NOVO: Aviso se limite atingido
                        if (hasGenerationLimit) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFFF44336).copy(alpha = 0.1f)
                                ),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    Color(0xFFF44336).copy(alpha = 0.3f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = Color(0xFFF44336),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(Modifier.width(10.dp))
                                    Text(
                                        "Limite diário de gerações atingido! Aguarde o reset para criar mais conteúdo.",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // Footer com botões fixo
                if (!isLoading) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface)
                    ) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = onDismiss,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Cancelar", fontWeight = FontWeight.Medium, fontSize = 15.sp)
                            }
                            Button(
                                onClick = { onConfirm(quantity, difficulty) },
                                // ✅ DESABILITA se limite atingido OU over limit
                                enabled = !isOverLimit && !hasGenerationLimit,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (hasGenerationLimit) Color.Gray else YellowAccent,
                                    contentColor = Color.Black
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Adicionar", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ColumnScope.LoadingContent(progress: Float, message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val infiniteTransition = rememberInfiniteTransition(label = "sparkle")
            val scale by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = 1.15f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1500, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "scale"
            )

            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(YellowAccent.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Star,
                    null,
                    tint = YellowAccent,
                    modifier = Modifier.size(36.dp * scale)
                )
            }

            Text(
                text = "Gerando conteúdo com IA",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = message,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Column(
                modifier = Modifier.fillMaxWidth(0.9f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LinearProgressIndicator(
                    progress = { progress / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = YellowAccent,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
                Text(
                    text = "${progress.toInt()}% concluído",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(14.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Bolt,
                    null,
                    tint = YellowAccent,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Aguarde alguns segundos...",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

// StatusCard, QuantitySelector, DifficultySelector e SummaryCard permanecem iguais
@Composable
private fun StatusCard(currentCount: Int, maxLimit: Int, availableSlots: Int, contentType: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF6BDEF3).copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Status do Deck",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "$currentCount / $maxLimit",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6BDEF3)
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth((currentCount.toFloat() / maxLimit).coerceIn(0f, 1f))
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFF6BDEF3), Color(0xFF6BDEF3).copy(alpha = 0.8f))
                            )
                        )
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${contentType.replaceFirstChar { it.uppercase() }} atuais",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Text(
                    text = "$availableSlots disponíveis",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF6BDEF3)
                )
            }
        }
    }
}

@Composable
private fun QuantitySelector(
    quantity: Int,
    customQuantity: String,
    availableSlots: Int,
    quantityOptions: List<Int>,
    onQuantityChange: (Int) -> Unit,
    onCustomQuantityChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "Quantos deseja adicionar?",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            quantityOptions.forEach { qty ->
                val isDisabled = qty > availableSlots
                val isSelected = quantity == qty

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            when {
                                isDisabled -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                isSelected -> YellowAccent.copy(alpha = 0.15f)
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }
                        )
                        .border(
                            width = 2.dp,
                            color = if (isSelected) YellowAccent else Color.Transparent,
                            shape = RoundedCornerShape(10.dp)
                        )
                        .clickable(enabled = !isDisabled) { onQuantityChange(qty) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = qty.toString(),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            isDisabled -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                            isSelected -> YellowAccent
                            else -> MaterialTheme.colorScheme.onSurface
                        }
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
        )

        Text(
            text = "OU",
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = (-6).dp),
            textAlign = TextAlign.Center
        )

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "Digite um valor personalizado",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )
            OutlinedTextField(
                value = customQuantity,
                onValueChange = onCustomQuantityChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        "Ex: 7 (máx: $availableSlots)",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                textStyle = LocalTextStyle.current.copy(
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = YellowAccent,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                )
            )
            if (quantity < 1 && customQuantity.isNotEmpty()) {
                Text(
                    text = "⚠️ O valor deve ser no mínimo 1",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun DifficultySelector(
    difficulty: String,
    options: List<DifficultyOption>,
    onDifficultyChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "Nível de dificuldade",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            options.forEach { option ->
                val isSelected = difficulty == option.value

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (isSelected) YellowAccent.copy(alpha = 0.15f)
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                        .border(
                            width = 2.dp,
                            color = if (isSelected) YellowAccent else Color.Transparent,
                            shape = RoundedCornerShape(10.dp)
                        )
                        .clickable { onDifficultyChange(option.value) }
                        .padding(vertical = 14.dp, horizontal = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        option.icon,
                        null,
                        tint = if (isSelected) YellowAccent else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = option.label,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isSelected) YellowAccent else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        options.find { it.value == difficulty }?.let { selected ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Text(
                    text = selected.description,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(10.dp)
                )
            }
        }
    }
}

@Composable
private fun SummaryCard(quantity: Int, currentCount: Int, maxLimit: Int, contentType: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Resumo",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Após confirmar:",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = if (quantity < 1) "0" else quantity.toString(),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = YellowAccent
                    )
                    Text(
                        text = if (quantity == 1) contentType.dropLast(1) else contentType,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                thickness = 1.dp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Total final:",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Text(
                    text = "${if (quantity < 1) currentCount else currentCount + quantity} / $maxLimit",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
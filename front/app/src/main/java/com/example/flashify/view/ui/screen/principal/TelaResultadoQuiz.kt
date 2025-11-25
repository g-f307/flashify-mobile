package com.example.flashify.view.ui.screen.principal

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.flashify.model.manager.ThemeManager
import com.example.flashify.view.ui.components.*
import com.example.flashify.viewmodel.AddContentState
import com.example.flashify.viewmodel.DeckViewModel
import com.example.flashify.viewmodel.GenerationLimitState
import com.example.flashify.viewmodel.QuizViewModel
import kotlinx.coroutines.delay

@Composable
fun TelaResultadoQuiz(
    quizId: Int,
    documentId: Int,
    totalQuestions: Int,
    correctAnswers: Int,
    onRetry: () -> Unit,
    onFinish: () -> Unit,
    viewModel: QuizViewModel,
    deckViewModel: DeckViewModel
) {
    // --- THEME LOGIC ---
    val context = LocalContext.current
    val themeManager = remember { ThemeManager(context) }
    val isDarkTheme by themeManager.isDarkTheme.collectAsState(initial = isSystemInDarkTheme())
    val primaryColor = MaterialTheme.colorScheme.primary

    val score = if (totalQuestions > 0) (correctAnswers.toFloat() / totalQuestions) * 100 else 0f
    val accuracy = score / 100f
    val incorrectAnswers = totalQuestions - correctAnswers

    val addContentState by deckViewModel.addContentState.collectAsStateWithLifecycle()
    val generationLimitState by deckViewModel.generationLimitState.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }
    var shouldAutoRedirect by remember { mutableStateOf(false) }

    val hasGenerationLimit = remember(generationLimitState) {
        if (generationLimitState is GenerationLimitState.Success) {
            val info = (generationLimitState as GenerationLimitState.Success).info
            info.used >= info.limit
        } else {
            false
        }
    }

    LaunchedEffect(Unit) {
        viewModel.submitQuiz(quizId, score, correctAnswers, totalQuestions)
    }

    LaunchedEffect(addContentState) {
        when (val state = addContentState) {
            is AddContentState.Success -> {
                Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                showAddDialog = false
                shouldAutoRedirect = true
                delay(800)
                deckViewModel.resetAddContentState()
                onRetry()
            }
            is AddContentState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                deckViewModel.resetAddContentState()
                showAddDialog = false
            }
            else -> {}
        }
    }

    if (showAddDialog) {
        AddContentDialog(
            title = "Adicionar Perguntas",
            description = "Amplie seu quiz com novas perguntas geradas pela IA",
            contentType = "Perguntas",
            currentCount = totalQuestions,
            maxLimit = 15,
            isLoading = addContentState is AddContentState.Loading,
            hasGenerationLimit = hasGenerationLimit,
            onDismiss = {
                showAddDialog = false
                deckViewModel.resetAddContentState()
            },
            onConfirm = { qtd, difficulty ->
                if (!hasGenerationLimit) {
                    deckViewModel.addQuestionsToQuiz(documentId, qtd, difficulty)
                }
            }
        )
    }

    // ✅ Passing isDarkTheme to gradient
    GradientBackgroundScreen(isDarkTheme = isDarkTheme) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .systemBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Spacer(modifier = Modifier.weight(0.3f))

                MotivationalHeader(score = score)

                Spacer(modifier = Modifier.height(20.dp))

                // ✅ PERFORMANCE CARD WITH BORDER
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(8.dp, RoundedCornerShape(20.dp)),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    // Border added for definition in light mode
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularPerformanceChart(
                            percentage = accuracy,
                            color = if (score >= 70) Color(0xFF4CAF50)
                            else if (score >= 50) primaryColor
                            else Color(0xFFF44336)
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Divider(
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
                            thickness = 1.dp
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ResultStatItem(
                                icon = Icons.Default.CheckCircle,
                                label = "Corretas",
                                value = "$correctAnswers",
                                color = Color(0xFF4CAF50)
                            )

                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(40.dp)
                                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                            )

                            ResultStatItem(
                                icon = Icons.Default.Cancel,
                                label = "Incorretas",
                                value = "$incorrectAnswers",
                                color = Color(0xFFF44336)
                            )

                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(40.dp)
                                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                            )

                            // Blue or theme secondary color
                            val infoColor = if (isDarkTheme) Color(0xFF2196F3) else Color(0xFF0288D1)
                            ResultStatItem(
                                icon = Icons.Default.Help,
                                label = "Total",
                                value = "$totalQuestions",
                                color = infoColor
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onRetry,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = primaryColor,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(16.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                ) {
                    Icon(Icons.Default.Refresh, null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(10.dp))
                    Text("Tentar Novamente", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                if (totalQuestions < 15) {
                    Spacer(modifier = Modifier.height(12.dp))

                    val addColor = Color(0xFF26C6DA) // Cyan

                    OutlinedButton(
                        onClick = {
                            if (!hasGenerationLimit) {
                                showAddDialog = true
                            } else {
                                Toast.makeText(
                                    context,
                                    "Limite diário de gerações atingido!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        border = BorderStroke(1.5.dp, addColor),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = addColor),
                        shape = RoundedCornerShape(16.dp),
                        enabled = !hasGenerationLimit && addContentState !is AddContentState.Loading
                    ) {
                        if (addContentState is AddContentState.Loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = addColor,
                                strokeWidth = 2.dp
                            )
                            Spacer(Modifier.width(10.dp))
                            Text("Gerando...", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        } else {
                            Icon(Icons.Default.Add, null, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(10.dp))
                            Text(
                                if (hasGenerationLimit) "Limite Atingido" else "Adicionar Perguntas",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.height(20.dp))
            }

            IconButton(
                onClick = onFinish,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 12.dp, end = 12.dp)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f), CircleShape)
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f), CircleShape)
                    .size(40.dp)
            ) {
                Icon(
                    Icons.Default.Close,
                    "Fechar",
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}
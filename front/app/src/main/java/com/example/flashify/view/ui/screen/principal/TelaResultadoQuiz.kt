package com.example.flashify.view.ui.screen.principal

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.flashify.view.ui.components.*
import com.example.flashify.view.ui.theme.YellowAccent
import com.example.flashify.viewmodel.AddContentState
import com.example.flashify.viewmodel.DeckViewModel
import com.example.flashify.viewmodel.GenerationLimitState
import com.example.flashify.viewmodel.QuizViewModel

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
    val score = if (totalQuestions > 0) (correctAnswers.toFloat() / totalQuestions) * 100 else 0f
    val accuracy = score / 100f
    val incorrectAnswers = totalQuestions - correctAnswers

    val addContentState by deckViewModel.addContentState.collectAsStateWithLifecycle()
    val generationLimitState by deckViewModel.generationLimitState.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // ✅ VERIFICAR LIMITE DE GERAÇÕES
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

    // ✅ CORRIGIDO: Fluxo de redirecionamento após sucesso
    LaunchedEffect(addContentState) {
        when (val state = addContentState) {
            is AddContentState.Success -> {
                Toast.makeText(
                    context,
                    state.message,
                    Toast.LENGTH_SHORT
                ).show()

                // ✅ FECHA O DIÁLOGO IMEDIATAMENTE
                showAddDialog = false

                // ✅ AGUARDA 800MS E REDIRECIONA
                kotlinx.coroutines.delay(800)
                deckViewModel.resetAddContentState()
                onRetry() // ✅ REDIRECIONA AUTOMATICAMENTE
            }

            is AddContentState.Error -> {
                Toast.makeText(
                    context,
                    state.message,
                    Toast.LENGTH_LONG
                ).show()
                deckViewModel.resetAddContentState()
                showAddDialog = false
            }

            else -> {}
        }
    }

    // ✅ DIÁLOGO COM VERIFICAÇÃO DE LIMITE
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
                // ✅ DUPLA VERIFICAÇÃO antes de enviar
                if (!hasGenerationLimit) {
                    deckViewModel.addQuestionsToQuiz(documentId, qtd, difficulty)
                }
            }
        )
    }

    GradientBackgroundScreen {
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

                PerformanceCard {
                    CircularPerformanceChart(
                        percentage = accuracy,
                        color = if (score >= 70) Color(0xFF4CAF50)
                        else if (score >= 50) YellowAccent
                        else Color(0xFFF44336)
                    )

                    Divider(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                        thickness = 1.dp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StatItem(Icons.Default.CheckCircle, "Corretas", "$correctAnswers", Color(0xFF4CAF50))
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(48.dp)
                                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                        )
                        StatItem(Icons.Default.Cancel, "Incorretas", "$incorrectAnswers", Color(0xFFF44336))
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(48.dp)
                                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                        )
                        StatItem(Icons.Default.Help, "Total", "$totalQuestions", Color(0xFF2196F3))
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onRetry,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = YellowAccent,
                        contentColor = Color.Black
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

                    OutlinedButton(
                        onClick = {
                            // ✅ VERIFICAR LIMITE ANTES DE ABRIR DIÁLOGO
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
                        border = BorderStroke(1.5.dp, Color(0xFF26C6DA)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF26C6DA)),
                        shape = RoundedCornerShape(16.dp),
                        // ✅ BOTÃO DESABILITADO se limite atingido OU se está carregando
                        enabled = !hasGenerationLimit && addContentState !is AddContentState.Loading
                    ) {
                        if (addContentState is AddContentState.Loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = Color(0xFF26C6DA),
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
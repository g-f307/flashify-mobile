package com.example.flashify.view.ui.screen.principal

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.flashify.view.ui.theme.TextSecondary
import com.example.flashify.view.ui.theme.YellowAccent
import com.example.flashify.viewmodel.QuizSubmitState
import com.example.flashify.viewmodel.QuizViewModel

@Composable
fun TelaResultadoQuiz(
    quizId: Int,
    totalQuestions: Int,
    correctAnswers: Int,
    onRetry: () -> Unit,
    onFinish: () -> Unit,
    viewModel: QuizViewModel // Recebe o ViewModel injetado na tela pai
) {
    val submitState by viewModel.quizSubmitState.collectAsStateWithLifecycle()
    val score = if (totalQuestions > 0) {
        (correctAnswers.toFloat() / totalQuestions) * 100
    } else 0f

    val incorrectAnswers = totalQuestions - correctAnswers
    val accuracy = if (totalQuestions > 0) (correctAnswers.toFloat() / totalQuestions * 100).toInt() else 0

    // Submeter resultado automaticamente
    LaunchedEffect(Unit) {
        viewModel.submitQuiz(
            quizId = quizId,
            score = score,
            correctAnswers = correctAnswers,
            totalQuestions = totalQuestions
        )
    }

    AlertDialog(
        onDismissRequest = onFinish,
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = YellowAccent,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Quiz Concluído!",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "Você completou $totalQuestions perguntas",
                    textAlign = TextAlign.Center,
                    color = TextSecondary
                )
                Spacer(Modifier.height(16.dp))

                // Mensagem de "Progresso Salvo"
                if (submitState is QuizSubmitState.Success) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = null,
                            tint = Color.Green,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Seu progresso foi salvo!",
                            fontSize = 12.sp,
                            color = Color.Green
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                } else if (submitState is QuizSubmitState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    Spacer(Modifier.height(16.dp))
                }

                // Estatísticas
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "$correctAnswers",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4CAF50)
                        )
                        Text("Acertos", fontSize = 12.sp, color = TextSecondary)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "$incorrectAnswers",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF44336)
                        )
                        Text("Erros", fontSize = 12.sp, color = TextSecondary)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "$accuracy%",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = YellowAccent
                        )
                        Text("Precisão", fontSize = 12.sp, color = TextSecondary)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onFinish,
                colors = ButtonDefaults.buttonColors(
                    containerColor = YellowAccent,
                    contentColor = Color.Black
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Finalizar", fontWeight = FontWeight.Bold)
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}
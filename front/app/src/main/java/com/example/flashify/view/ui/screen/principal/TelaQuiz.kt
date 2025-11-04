package com.example.flashify.view.ui.screen.principal

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.flashify.model.data.AnswerResponse
import com.example.flashify.model.data.QuestionResponse
import com.example.flashify.model.data.QuizResponse
import com.example.flashify.view.ui.components.GradientBackgroundScreen
import com.example.flashify.view.ui.theme.TextSecondary
import com.example.flashify.view.ui.theme.YellowAccent
import com.example.flashify.viewmodel.AnswerCheckState
import com.example.flashify.viewmodel.QuizState
import com.example.flashify.viewmodel.QuizViewModel
import com.example.flashify.viewmodel.QuizSubmitState
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaQuiz(
    navController: NavController,
    documentId: Int,
    viewModel: QuizViewModel = viewModel()
) {
    val quizState by viewModel.quizState.collectAsStateWithLifecycle()
    // val quizSubmitState by viewModel.quizSubmitState.collectAsStateWithLifecycle() // Não é mais necessário aqui

    LaunchedEffect(documentId) {
        viewModel.loadQuiz(documentId)
    }

    // ▼▼▼ REMOVIDO ▼▼▼
    // O LaunchedEffect(quizSubmitState) foi removido.
    // Agora, a navegação de volta é controlada apenas pelo
    // botão "Finalizar" na TelaResultadoQuiz.
    // ▲▲▲ FIM DA REMOÇÃO ▲▲▲

    GradientBackgroundScreen {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text("Quiz", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, "Voltar", tint = MaterialTheme.colorScheme.onBackground)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = MaterialTheme.colorScheme.onBackground,
                        navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                    )
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                when (val state = quizState) {
                    is QuizState.Loading -> CircularProgressIndicator()
                    is QuizState.Error -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = state.message,
                                color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { navController.popBackStack() }) {
                                Text("Voltar")
                            }
                        }
                    }
                    is QuizState.Success -> {
                        QuizContent(
                            quiz = state.quiz,
                            viewModel = viewModel,
                            navController = navController
                        )
                    }
                    else -> {}
                }
            }
        }
    }
}

@Composable
fun QuizContent(
    quiz: QuizResponse,
    viewModel: QuizViewModel,
    navController: NavController
) {
    var currentQuestionIndex by remember { mutableStateOf(0) }
    var selectedAnswerId by remember { mutableStateOf<Int?>(null) }
    var correctAnswersCount by remember { mutableStateOf(0) }
    var answeredQuestions by remember { mutableStateOf(setOf<Int>()) }
    var showResult by remember { mutableStateOf(false) }

    val answerCheckState by viewModel.answerCheckState.collectAsStateWithLifecycle()

    val currentQuestion = quiz.questions.getOrNull(currentQuestionIndex)
    val progress = (currentQuestionIndex + 1).toFloat() / quiz.questions.size

    if (currentQuestion == null || showResult) {
        TelaResultadoQuiz(
            quizId = quiz.id,
            totalQuestions = quiz.questions.size,
            correctAnswers = correctAnswersCount,
            onRetry = {
                currentQuestionIndex = 0
                selectedAnswerId = null
                correctAnswersCount = 0
                answeredQuestions = setOf()
                showResult = false
                viewModel.resetAnswerCheckState()
                viewModel.resetSubmitState()
            },
            onFinish = {
                // ✅ Marca quiz como completado ao voltar
                navController.previousBackStackEntry
                    ?.savedStateHandle
                    ?.set("quiz_completed", true)
                navController.popBackStack()
            },
            viewModel = viewModel
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Barra de progresso
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${currentQuestionIndex + 1}/${quiz.questions.size}",
                    color = YellowAccent,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${(progress * 100).toInt()}% concluído",
                    color = TextSecondary,
                    fontSize = 14.sp
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = YellowAccent,
                trackColor = Color.Gray.copy(alpha = 0.3f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Pergunta
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Pergunta ${currentQuestionIndex + 1}",
                    fontSize = 14.sp,
                    color = YellowAccent,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = currentQuestion.text,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 26.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Respostas
        currentQuestion.answers.forEachIndexed { index, answer ->
            AnswerOption(
                answer = answer,
                index = index,
                isSelected = selectedAnswerId == answer.id,
                answerCheckState = answerCheckState,
                selectedAnswerId = selectedAnswerId,
                onClick = {
                    if (answerCheckState !is AnswerCheckState.Success) {
                        selectedAnswerId = answer.id
                    }
                }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Feedback e botão de próxima
        when (val checkState = answerCheckState) {
            is AnswerCheckState.Success -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (checkState.result.isCorrect)
                            Color.Green.copy(alpha = 0.2f)
                        else
                            Color.Red.copy(alpha = 0.2f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (checkState.result.isCorrect) Icons.Default.Check else Icons.Default.Close,
                                contentDescription = null,
                                tint = if (checkState.result.isCorrect) Color.Green else Color.Red
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (checkState.result.isCorrect) "Correto!" else "Incorreto",
                                fontWeight = FontWeight.Bold,
                                color = if (checkState.result.isCorrect) Color.Green else Color.Red
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = checkState.result.explanation,
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (currentQuestionIndex < quiz.questions.size - 1) {
                            currentQuestionIndex++
                            selectedAnswerId = null
                            viewModel.resetAnswerCheckState()
                        } else {
                            showResult = true
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = YellowAccent,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (currentQuestionIndex < quiz.questions.size - 1) "Próxima" else "Ver Resultado",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
            else -> {
                Button(
                    onClick = {
                        selectedAnswerId?.let { answerId ->
                            if (!answeredQuestions.contains(currentQuestion.id)) {
                                viewModel.checkAnswer(currentQuestion.id, answerId)
                                answeredQuestions = answeredQuestions + currentQuestion.id
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = selectedAnswerId != null && answerCheckState !is AnswerCheckState.Loading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = YellowAccent,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (answerCheckState is AnswerCheckState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.Black
                        )
                    } else {
                        Text(
                            text = "Verificar",
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }
            }
        }
    }

    // Atualizar contador de respostas corretas
    LaunchedEffect(answerCheckState) {
        if (answerCheckState is AnswerCheckState.Success) {
            val result = (answerCheckState as AnswerCheckState.Success).result
            if (result.isCorrect) {
                correctAnswersCount++
            }
        }
    }
}

@Composable
fun AnswerOption(
    answer: AnswerResponse,
    index: Int,
    isSelected: Boolean,
    answerCheckState: AnswerCheckState,
    selectedAnswerId: Int?,
    onClick: () -> Unit
) {
    val labels = listOf("A", "B", "C", "D", "E")
    val label = labels.getOrNull(index) ?: "${index + 1}"

    val isCorrectAnswer = if (answerCheckState is AnswerCheckState.Success) {
        answer.id == answerCheckState.result.correctAnswerId
    } else false

    val isWrongSelection = if (answerCheckState is AnswerCheckState.Success) {
        isSelected && !answerCheckState.result.isCorrect
    } else false

    val borderColor = when {
        isCorrectAnswer -> Color.Green
        isWrongSelection -> Color.Red
        isSelected -> YellowAccent
        else -> Color.Gray.copy(alpha = 0.3f)
    }

    val backgroundColor = when {
        isCorrectAnswer -> Color.Green.copy(alpha = 0.1f)
        isWrongSelection -> Color.Red.copy(alpha = 0.1f)
        isSelected -> YellowAccent.copy(alpha = 0.1f)
        else -> MaterialTheme.colorScheme.surface
    }

    OutlinedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(2.dp, borderColor),
        colors = CardDefaults.outlinedCardColors(
            containerColor = backgroundColor
        ),
        enabled = answerCheckState !is AnswerCheckState.Success
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Label (A, B, C, D, E)
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = borderColor.copy(alpha = 0.2f),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = label,
                        fontWeight = FontWeight.Bold,
                        color = borderColor
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Texto da resposta
            Text(
                text = answer.text,
                fontSize = 16.sp,
                lineHeight = 22.sp,
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.onSurface
            )

            // Ícone de correto/incorreto
            androidx.compose.animation.AnimatedVisibility(
                visible = answerCheckState is AnswerCheckState.Success && (isCorrectAnswer || isWrongSelection),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Icon(
                    imageVector = if (isCorrectAnswer) Icons.Default.Check else Icons.Default.Close,
                    contentDescription = null,
                    tint = if (isCorrectAnswer) Color.Green else Color.Red,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
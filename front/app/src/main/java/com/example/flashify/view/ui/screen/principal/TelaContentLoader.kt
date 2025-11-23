package com.example.flashify.view.ui.screen.principal

import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.flashify.model.util.BIBLIOTECA_SCREEN_ROUTE
import com.example.flashify.model.util.MAIN_SCREEN_ROUTE
import com.example.flashify.view.ui.components.GradientBackgroundScreen
import com.example.flashify.view.ui.theme.TextSecondary
import com.example.flashify.view.ui.theme.YellowAccent
import com.example.flashify.viewmodel.DeckViewModel
import com.example.flashify.viewmodel.DocumentProcessingState
import kotlinx.coroutines.delay

// Keywords mapeadas do backend
object ProcessingKeywords {
    const val START = "iniciando processamento"
    const val EXTRACT = "extraindo texto"
    const val AI_FLASHCARDS = "gerando flashcards com ia"
    const val PARSE_FLASHCARDS = "parsing flashcards"
    const val SAVE_FLASHCARDS = "salvando flashcards"
    const val AI_QUIZ = "gerando quiz com ia"
    const val PARSE_QUIZ = "parsing quiz"
    const val SAVE_QUIZ = "salvando quiz"
    const val DONE = "concluído"
}

// Modelo de passo de processamento
data class ProcessingStep(
    val name: String,
    val icon: ImageVector,
    val keyword: String
)

// Estados visuais de cada passo
enum class StepStatus {
    PENDING,
    ACTIVE,
    COMPLETED
}

@Composable
fun TelaContentLoader(
    navController: NavController,
    documentId: Int,
    generatesFlashcards: Boolean,
    generatesQuizzes: Boolean,
    viewModel: DeckViewModel = hiltViewModel()
) {
    val processingState by viewModel.documentProcessingState.collectAsStateWithLifecycle()

    LaunchedEffect(documentId) {
        viewModel.startDocumentPolling(documentId)
    }

    LaunchedEffect(processingState) {
        if (processingState is DocumentProcessingState.Completed) {
            delay(2000)
            navController.navigate(BIBLIOTECA_SCREEN_ROUTE) {
                popUpTo(MAIN_SCREEN_ROUTE)
            }
            viewModel.stopDocumentPolling()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopDocumentPolling()
        }
    }

    GradientBackgroundScreen {
        Scaffold(
            containerColor = Color.Transparent
        ) { innerPadding ->
            ContentLoaderContent(
                processingState = processingState,
                generatesFlashcards = generatesFlashcards,
                generatesQuizzes = generatesQuizzes,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

@Composable
fun ContentLoaderContent(
    processingState: DocumentProcessingState,
    generatesFlashcards: Boolean,
    generatesQuizzes: Boolean,
    modifier: Modifier = Modifier
) {
    val currentStepMessage = when (processingState) {
        is DocumentProcessingState.Processing -> processingState.currentStep ?: ""
        is DocumentProcessingState.Completed -> ProcessingKeywords.DONE
        is DocumentProcessingState.Error -> ""
        else -> ""
    }

    val currentStepMessageLower = currentStepMessage.lowercase()
    val isCompleted = currentStepMessageLower.contains(ProcessingKeywords.DONE)
    val isError = processingState is DocumentProcessingState.Error

    val processingSteps = remember(generatesFlashcards, generatesQuizzes) {
        buildList {
            add(ProcessingStep("Iniciando processamento", Icons.Default.AutoAwesome, ProcessingKeywords.START))
            add(ProcessingStep("Extraindo texto do arquivo", Icons.Default.Description, ProcessingKeywords.EXTRACT))

            if (generatesFlashcards) {
                add(ProcessingStep("Gerando seus flashcards", Icons.Default.Psychology, ProcessingKeywords.AI_FLASHCARDS))
                add(ProcessingStep("Estruturando seus flashcards", Icons.Default.AutoAwesome, ProcessingKeywords.PARSE_FLASHCARDS))
                add(ProcessingStep("Salvando seus flashcards", Icons.Default.Storage, ProcessingKeywords.SAVE_FLASHCARDS))
            }

            if (generatesQuizzes) {
                add(ProcessingStep("Gerando seu quiz", Icons.Default.Psychology, ProcessingKeywords.AI_QUIZ))
                add(ProcessingStep("Estruturando seu quiz", Icons.Default.Assignment, ProcessingKeywords.PARSE_QUIZ))
                add(ProcessingStep("Salvando seu quiz", Icons.Default.Storage, ProcessingKeywords.SAVE_QUIZ))
            }
        }
    }

    val activeStepIndex = processingSteps.indexOfLast { step ->
        currentStepMessageLower.contains(step.keyword)
    }

    val finalActiveIndex = if (activeStepIndex == -1 && !isCompleted) 0 else activeStepIndex

    val progress = if (isCompleted) {
        1f
    } else if (finalActiveIndex >= 0) {
        (finalActiveIndex + 1).toFloat() / processingSteps.size.toFloat()
    } else {
        0f
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .padding(top = 16.dp, bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        HeaderSection(
            generatesFlashcards = generatesFlashcards,
            generatesQuizzes = generatesQuizzes,
            isCompleted = isCompleted,
            progress = progress
        )

        Spacer(modifier = Modifier.height(20.dp))

        ProgressBar(progress = progress, isCompleted = isCompleted)

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            processingSteps.forEachIndexed { index, step ->
                val status = when {
                    isCompleted -> StepStatus.COMPLETED
                    index < finalActiveIndex -> StepStatus.COMPLETED
                    index == finalActiveIndex -> StepStatus.ACTIVE
                    else -> StepStatus.PENDING
                }

                ProcessingStepItem(
                    step = step,
                    status = status,
                    animationDelay = index * 60
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (isCompleted) {
            CompletedMessage()
        } else if (isError) {
            ErrorMessage((processingState as DocumentProcessingState.Error).message)
        } else {
            ProcessingMessage()
        }
    }
}

@Composable
fun HeaderSection(
    generatesFlashcards: Boolean,
    generatesQuizzes: Boolean,
    isCompleted: Boolean,
    progress: Float
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        val infiniteTransition = rememberInfiniteTransition(label = "pulse")
        val scale by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = if (isCompleted) 1f else 1.08f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "scale"
        )

        Box(
            modifier = Modifier
                .size(64.dp)
                .scale(if (isCompleted) 1f else scale)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            YellowAccent.copy(alpha = 0.2f),
                            YellowAccent.copy(alpha = 0.1f)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Default.Psychology,
                contentDescription = null,
                tint = if (isCompleted) Color.Green else YellowAccent,
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = if (isCompleted) "Concluído!" else getTitle(generatesFlashcards, generatesQuizzes),
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            lineHeight = 26.sp,
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = if (isCompleted)
                "Seu material está pronto!"
            else
                getSubtitle(generatesFlashcards, generatesQuizzes),
            fontSize = 13.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 18.sp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "${(progress * 100).toInt()}%",
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = YellowAccent
        )
    }
}

@Composable
fun ProgressBar(progress: Float, isCompleted: Boolean) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "progress"
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(animatedProgress)
                    .clip(RoundedCornerShape(3.dp))
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = if (isCompleted) {
                                listOf(Color.Green, Color.Green.copy(alpha = 0.7f))
                            } else {
                                listOf(YellowAccent, YellowAccent.copy(alpha = 0.7f))
                            }
                        )
                    )
            )
        }
    }
}

@Composable
fun ProcessingStepItem(
    step: ProcessingStep,
    status: StepStatus,
    animationDelay: Int
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(animationDelay.toLong())
        visible = true
    }

    val backgroundColor = when (status) {
        StepStatus.ACTIVE -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        StepStatus.COMPLETED -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        else -> Color.Transparent
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(300)) + slideInVertically(
            initialOffsetY = { 15 },
            animationSpec = tween(300, easing = FastOutSlowInEasing)
        )
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = backgroundColor
            ),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(
                defaultElevation = if (status == StepStatus.ACTIVE) 1.dp else 0.dp
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StepIcon(icon = step.icon, status = status)

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = step.name,
                        fontSize = 13.sp,
                        fontWeight = if (status == StepStatus.ACTIVE) FontWeight.SemiBold else FontWeight.Normal,
                        color = when (status) {
                            StepStatus.PENDING -> TextSecondary
                            StepStatus.ACTIVE -> MaterialTheme.colorScheme.onBackground
                            StepStatus.COMPLETED -> TextSecondary
                        },
                        lineHeight = 18.sp
                    )

                    if (status == StepStatus.ACTIVE) {
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "Em andamento...",
                            fontSize = 11.sp,
                            color = YellowAccent.copy(alpha = 0.8f)
                        )
                    }
                }

                StatusIndicator(status = status)
            }
        }
    }
}

@Composable
fun StatusIndicator(status: StepStatus) {
    when (status) {
        StepStatus.COMPLETED -> {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = Color.Green,
                modifier = Modifier.size(18.dp)
            )
        }
        StepStatus.ACTIVE -> {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                color = YellowAccent,
                strokeWidth = 2.dp
            )
        }
        StepStatus.PENDING -> {
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
        }
    }
}

@Composable
fun StepIcon(
    icon: ImageVector,
    status: StepStatus
) {
    val iconBackgroundColor = when (status) {
        StepStatus.ACTIVE -> YellowAccent.copy(alpha = 0.15f)
        StepStatus.COMPLETED -> Color.Green.copy(alpha = 0.15f)
        StepStatus.PENDING -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    }

    val iconTint = when (status) {
        StepStatus.ACTIVE -> YellowAccent
        StepStatus.COMPLETED -> Color.Green
        StepStatus.PENDING -> TextSecondary
    }

    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(iconBackgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(22.dp)
        )
    }
}

@Composable
fun CompletedMessage() {
    AnimatedVisibility(
        visible = true,
        enter = fadeIn(animationSpec = tween(500)) + scaleIn(
            initialScale = 0.85f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color.Green.copy(alpha = 0.12f)
            ),
            shape = RoundedCornerShape(14.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color.Green.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color.Green,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Processamento concluído!",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "Redirecionando para a biblioteca...",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
fun ErrorMessage(message: String) {
    AnimatedVisibility(
        visible = true,
        enter = fadeIn() + slideInVertically(initialOffsetY = { 15 })
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color.Red.copy(alpha = 0.12f)
            ),
            shape = RoundedCornerShape(14.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color.Red.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = null,
                        tint = Color.Red,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Erro no processamento",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = message,
                        fontSize = 12.sp,
                        color = TextSecondary,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
fun ProcessingMessage() {
    val infiniteTransition = rememberInfiniteTransition(label = "fade")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = null,
            tint = YellowAccent.copy(alpha = alpha),
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Essa ação pode demorar alguns minutos.\nPor favor, não feche esta página.",
            fontSize = 12.sp,
            color = TextSecondary.copy(alpha = alpha),
            textAlign = TextAlign.Center,
            lineHeight = 17.sp
        )
    }
}

private fun getTitle(generatesFlashcards: Boolean, generatesQuizzes: Boolean): String {
    return when {
        generatesFlashcards && generatesQuizzes -> "Gerando seu material de estudo"
        generatesFlashcards -> "Gerando seus flashcards"
        generatesQuizzes -> "Gerando seu quiz"
        else -> "Processando seu pedido"
    }
}

private fun getSubtitle(generatesFlashcards: Boolean, generatesQuizzes: Boolean): String {
    return when {
        generatesFlashcards && generatesQuizzes -> "Flashcards e quiz sendo criados pela IA"
        generatesFlashcards -> "Flashcards sendo criados pela IA"
        generatesQuizzes -> "Quiz sendo criado pela IA"
        else -> "O seu conteúdo está sendo processado"
    }
}
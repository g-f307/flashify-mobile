package com.example.flashify.view.ui.screen.principal

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.flashify.model.data.FlashcardResponse
import com.example.flashify.model.manager.ThemeManager
import com.example.flashify.view.ui.components.EditFlashcardDialog
import com.example.flashify.view.ui.components.GradientBackgroundScreen
import com.example.flashify.viewmodel.DeckViewModel
import com.example.flashify.viewmodel.FlashcardEditState
import com.example.flashify.viewmodel.StudyState
import com.example.flashify.viewmodel.StudyViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaEstudo(
    navController: NavController,
    deckId: Int,
    viewModel: StudyViewModel = hiltViewModel(),
    deckViewModel: DeckViewModel = hiltViewModel()
) {
    // --- LÓGICA DO TEMA ---
    val context = LocalContext.current
    val themeManager = remember { ThemeManager(context) }
    val isDarkTheme by themeManager.isDarkTheme.collectAsState(initial = isSystemInDarkTheme())
    val primaryColor = MaterialTheme.colorScheme.primary

    val uiState by viewModel.studyState.collectAsStateWithLifecycle()
    var elapsedTime by remember { mutableStateOf(0) }
    var isTimerRunning by remember { mutableStateOf(true) }

    LaunchedEffect(deckId) {
        viewModel.fetchFlashcards(deckId)
    }

    LaunchedEffect(isTimerRunning) {
        while (isTimerRunning) {
            delay(1000)
            elapsedTime++
        }
    }

    // ✅ Passamos isDarkTheme para o gradiente
    GradientBackgroundScreen(isDarkTheme = isDarkTheme) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                "Estudando",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                formatTime(elapsedTime),
                                fontSize = 12.sp,
                                color = primaryColor
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            isTimerRunning = false
                            navController.popBackStack()
                        }) {
                            Icon(
                                Icons.Default.ArrowBack,
                                "Voltar",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                when (val state = uiState) {
                    is StudyState.Loading -> CircularProgressIndicator(color = primaryColor)
                    is StudyState.Error -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                state.message,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(Modifier.height(16.dp))
                            Button(
                                onClick = { navController.popBackStack() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = primaryColor,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                )
                            ) {
                                Text("Voltar")
                            }
                        }
                    }
                    is StudyState.Success -> {
                        StudySession(
                            flashcards = state.flashcards,
                            deckId = deckId,
                            onLogStudy = { flashcardId, accuracy ->
                                viewModel.logStudyResult(flashcardId, accuracy)
                            },
                            onFinish = {
                                isTimerRunning = false
                                navController.popBackStack()
                            },
                            viewModel = viewModel,
                            deckViewModel = deckViewModel
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StudySession(
    flashcards: List<FlashcardResponse>,
    deckId: Int,
    onLogStudy: (Int, Float) -> Unit,
    onFinish: () -> Unit,
    viewModel: StudyViewModel,
    deckViewModel: DeckViewModel
) {
    var currentCardIndex by remember { mutableStateOf(0) }
    var isFlipped by remember { mutableStateOf(false) }
    var cardsCorrect by remember { mutableStateOf(0) }
    var cardsIncorrect by remember { mutableStateOf(0) }
    var isSessionFinished by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var shouldReloadFlashcards by remember { mutableStateOf(false) }

    val editState by viewModel.editState.collectAsStateWithLifecycle()
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.onSurfaceVariant

    LaunchedEffect(editState) {
        if (editState is FlashcardEditState.Success) {
            showEditDialog = false
            viewModel.resetEditState()
        }
    }

    LaunchedEffect(shouldReloadFlashcards) {
        if (shouldReloadFlashcards) {
            viewModel.fetchFlashcards(deckId)
            shouldReloadFlashcards = false
        }
    }

    if (isSessionFinished) {
        TelaResultadoEstudo(
            documentId = deckId,
            totalCards = flashcards.size,
            knownCards = cardsCorrect,
            learningCards = cardsIncorrect,
            onRestart = {
                shouldReloadFlashcards = true
                currentCardIndex = 0
                cardsCorrect = 0
                cardsIncorrect = 0
                isFlipped = false
                isSessionFinished = false
            },
            onFinish = onFinish,
            deckViewModel = deckViewModel
        )
        return
    }

    if (flashcards.isEmpty()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Default.Info, null, tint = primaryColor, modifier = Modifier.size(64.dp))
                Spacer(Modifier.height(16.dp))
                Text("Este deck não contém flashcards.", textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurface)
            }
        }
        return
    }

    val currentFlashcard = flashcards[currentCardIndex]
    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(500),
        label = "rotation"
    )
    val progress = (currentCardIndex + 1).toFloat() / flashcards.size

    fun handleAnswer(isCorrect: Boolean) {
        if (isCorrect) {
            cardsCorrect++
            onLogStudy(currentFlashcard.id, 1.0f)
        } else {
            cardsIncorrect++
            onLogStudy(currentFlashcard.id, 0.0f)
        }

        if (currentCardIndex < flashcards.size - 1) {
            isFlipped = false
            currentCardIndex++
        } else {
            isSessionFinished = true
        }
    }

    if (showEditDialog) {
        EditFlashcardDialog(
            currentFront = currentFlashcard.question,
            currentBack = currentFlashcard.answer,
            isLoading = editState is FlashcardEditState.Loading,
            onDismiss = {
                showEditDialog = false
                viewModel.resetEditState()
            },
            onConfirm = { newFront, newBack ->
                viewModel.updateFlashcard(
                    flashcardId = currentFlashcard.id,
                    newFront = newFront,
                    newBack = newBack
                )
            }
        )
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Progress bar
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "${currentCardIndex + 1}/${flashcards.size}",
                    color = primaryColor,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "${(progress * 100).toInt()}% concluído",
                    color = secondaryColor,
                    fontSize = 14.sp
                )
            }
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = primaryColor,
                trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            )
        }

        Spacer(Modifier.height(24.dp))

        // Stats
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatBadge(
                icon = Icons.Default.Check,
                value = cardsCorrect,
                label = "Acertos",
                color = Color(0xFF4CAF50)
            )
            StatBadge(
                icon = Icons.Default.Close,
                value = cardsIncorrect,
                label = "Erros",
                color = Color(0xFFF44336)
            )
        }

        Spacer(Modifier.weight(1f))

        // Flashcard com botão de edição
        FlashcardView(
            flashcard = currentFlashcard,
            isFlipped = isFlipped,
            rotation = rotation,
            onFlip = { isFlipped = !isFlipped },
            onEdit = { showEditDialog = true }
        )

        Spacer(Modifier.weight(1f))

        // Action buttons
        if (!isFlipped) {
            Button(
                onClick = { isFlipped = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = primaryColor,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Visibility, null)
                Spacer(Modifier.width(8.dp))
                Text("Revelar Resposta", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { handleAnswer(false) },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFFF44336)
                    ),
                    border = BorderStroke(1.dp, Color(0xFFF44336).copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Close, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Errei", fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = { handleAnswer(true) },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Check, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Acertei", fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
fun FlashcardView(
    flashcard: FlashcardResponse,
    isFlipped: Boolean,
    rotation: Float,
    onFlip: () -> Unit = {},
    onEdit: () -> Unit = {}
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val isDarkTheme = isSystemInDarkTheme()
    // Cor ciano para a resposta, adaptada ao tema
    val answerColor = if (isDarkTheme) Color(0xFF00BCD4) else Color(0xFF0097A7)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(350.dp)
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 12f * density
            }
            .clickable { onFlip() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        // ✅ Borda adicionada
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp)
        ) {
            // Botão de edição
            IconButton(
                onClick = onEdit,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(36.dp)
            ) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "Editar flashcard",
                    tint = primaryColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                if (rotation <= 90f) {
                    // Front (Question)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            "Pergunta",
                            fontSize = 14.sp,
                            color = primaryColor,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            flashcard.question,
                            fontSize = 20.sp,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 28.sp
                        )
                    }
                } else {
                    // Back (Answer)
                    Column(
                        modifier = Modifier.graphicsLayer { rotationY = 180f },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            "Resposta",
                            fontSize = 14.sp,
                            color = answerColor,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            flashcard.answer,
                            fontSize = 20.sp,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 28.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatBadge(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: Int,
    label: String,
    color: Color
) {
    val secondaryColor = MaterialTheme.colorScheme.onSurfaceVariant

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        // ✅ Borda adicionada (com a cor do status, ex: verde ou vermelho)
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
            }
            Column {
                Text("$value", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = color)
                Text(label, fontSize = 12.sp, color = secondaryColor)
            }
        }
    }
}

fun formatTime(seconds: Int): String {
    val minutes = seconds / 60
    val secs = seconds % 60
    return String.format("%02d:%02d", minutes, secs)
}
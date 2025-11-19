package com.example.flashify.view.ui.screen.principal

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.flashify.model.util.ESTUDO_SCREEN_ROUTE
import com.example.flashify.model.util.QUIZ_SCREEN_ROUTE
import com.example.flashify.view.ui.components.GradientBackgroundScreen
import com.example.flashify.view.ui.theme.TextSecondary
import com.example.flashify.view.ui.theme.YellowAccent
import com.example.flashify.viewmodel.DeckViewModel
import com.example.flashify.viewmodel.DeckListState
import com.example.flashify.viewmodel.DeckStatsState
import com.example.flashify.viewmodel.DeckActionState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaEscolhaModoEstudo(
    navController: NavController,
    deckId: Int,
    deckViewModel: DeckViewModel = viewModel()
) {
    val deckState by deckViewModel.deckListState.collectAsStateWithLifecycle()
    val statsState by deckViewModel.deckStatsState.collectAsStateWithLifecycle()
    val actionState by deckViewModel.deckActionState.collectAsStateWithLifecycle()

    var deck by remember { mutableStateOf<com.example.flashify.model.data.DeckResponse?>(null) }
    var shouldRefresh by remember { mutableStateOf(false) }

    // Estados separados para controlar o loading de cada card
    var isCreatingFlashcards by remember { mutableStateOf(false) }
    var isCreatingQuiz by remember { mutableStateOf(false) }

    // Guarda o estado anterior para detectar mudanças
    var previousHasQuiz by remember { mutableStateOf(deck?.hasQuiz ?: false) }
    var previousFlashcardsTotal by remember { mutableStateOf(0) }

    // SnackbarHost para mostrar mensagens
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // Observa se voltou de um quiz completado
    val navBackStackEntry = navController.currentBackStackEntry
    LaunchedEffect(navBackStackEntry) {
        val quizCompleted = navBackStackEntry
            ?.savedStateHandle
            ?.get<Boolean>("quiz_completed") ?: false

        if (quizCompleted) {
            shouldRefresh = true
            navBackStackEntry.savedStateHandle.remove<Boolean>("quiz_completed")
        }
    }

    // Carrega os dados do deck E as estatísticas na primeira vez
    LaunchedEffect(deckId) {
        println("🚀 INICIALIZANDO TelaEscolhaModoEstudo para deck $deckId")

        deckViewModel.resetStatsState()
        deckViewModel.fetchDeckStats(deckId)

        if (deckViewModel.deckListState.value !is DeckListState.Success) {
            deckViewModel.fetchDecks()
        }

        // Inicializa os valores anteriores após carregar os dados
        delay(1000)
        deck?.let {
            previousHasQuiz = it.hasQuiz
            println("   Inicializado previousHasQuiz = ${it.hasQuiz}")
        }

        val currentStatsState = deckViewModel.deckStatsState.value
        if (currentStatsState is DeckStatsState.Success) {
            previousFlashcardsTotal = currentStatsState.stats.flashcards.total
            println("   Inicializado previousFlashcardsTotal = $previousFlashcardsTotal")
        }
    }

    // Recarrega TUDO quando shouldRefresh é true
    LaunchedEffect(shouldRefresh) {
        if (shouldRefresh) {
            delay(500)
            deckViewModel.fetchDecks()
            deckViewModel.fetchDeckStats(deckId)
            shouldRefresh = false
        }
    }

    // Atualiza o deck quando o estado muda
    LaunchedEffect(deckState) {
        if (deckState is DeckListState.Success) {
            val updatedDeck = (deckState as DeckListState.Success).decks.find { it.id == deckId }
            if (updatedDeck != null) {
                val deckChanged = updatedDeck != deck

                // 🔴 ALTERADO: Lógica de parada simplificada e absoluta
                if (isCreatingQuiz && updatedDeck.hasQuiz) {
                    println("🎉 QUIZ DETECTADO! Parando loading do quiz...")
                    isCreatingQuiz = false
                }

                previousHasQuiz = updatedDeck.hasQuiz
                deck = updatedDeck
            } else {
                println("⚠️ Deck não encontrado na lista!")
            }
        }
    }

    // Força atualização quando stats mudam
    LaunchedEffect(statsState) {
        when (val currentStatsState = statsState) {
            is DeckStatsState.Success -> {
                val flashcardsTotal = currentStatsState.stats.flashcards.total
                val hasQuizInStats = currentStatsState.stats.quiz != null

                // 🔴 ALTERADO: Lógica de parada simplificada e absoluta
                if (isCreatingFlashcards && flashcardsTotal > 0) {
                    println("🎉 FLASHCARDS DETECTADOS! Parando loading dos flashcards...")
                    isCreatingFlashcards = false
                }

                previousFlashcardsTotal = flashcardsTotal
            }
            is DeckStatsState.Loading -> {
                println("📊 Stats: Loading...")
            }
            is DeckStatsState.Error -> {
                println("❌ Stats Error: ${currentStatsState.message}")
            }
            else -> {}
        }
    }

    // Observa o estado de ação e recarrega quando uma ação é bem-sucedida
    LaunchedEffect(actionState) {
        when (val currentActionState = actionState) {
            is DeckActionState.Success -> {
                println("=" .repeat(60))
                println("✅ AÇÃO BEM-SUCEDIDA!")
                println("   Mensagem: ${currentActionState.message}")

                val message = currentActionState.message

                // Inicia o polling: recarrega dados repetidamente até detectar mudança
                var attempts = 0
                val maxAttempts = 45 // 45 segundos (cada delay é aprox 1s no total com rede)

                while (attempts < maxAttempts) {
                    attempts++

                    // Se já detectou mudança (pelas variáveis observadas nos outros LaunchedEffect), sai
                    if (!isCreatingFlashcards && !isCreatingQuiz) {
                        println("\n✅ SUCESSO! Conteúdo detectado pelos observadores!")
                        break
                    }

                    println("\n🔄 POLLING SILENCIOSO - Tentativa $attempts/$maxAttempts")

                    // 🔴 ALTERADO: Usa showLoading = false para não piscar a tela
                    deckViewModel.fetchDecks(showLoading = false)
                    deckViewModel.fetchDeckStats(deckId, showLoading = false)

                    delay(2000)
                }

                // Timeout: força parar os loadings
                if (isCreatingFlashcards || isCreatingQuiz) {
                    println("\n⏱️ TIMEOUT ATINGIDO após $attempts tentativas")
                    isCreatingFlashcards = false
                    isCreatingQuiz = false

                    // Faz um último refresh VISÍVEL
                    deckViewModel.fetchDecks(showLoading = true)
                    deckViewModel.fetchDeckStats(deckId, showLoading = true)
                }

                println("\n🎉 Finalizando ação...")

                // Mostra mensagem de sucesso
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        message = message,
                        duration = SnackbarDuration.Short
                    )
                }

                deckViewModel.resetActionState()
            }
            is DeckActionState.Error -> {
                println("❌ ERRO NA AÇÃO: ${currentActionState.message}")

                // Para o loading em caso de erro
                isCreatingFlashcards = false
                isCreatingQuiz = false

                // Mostra mensagem de erro
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        message = currentActionState.message,
                        duration = SnackbarDuration.Long
                    )
                }

                deckViewModel.resetActionState()
            }
            is DeckActionState.Loading -> {
                println("⏳ Ação em andamento...")
            }
            else -> {}
        }
    }

    // Observa mudanças no deckState e statsState para atualizar o deck local
    LaunchedEffect(deckState, statsState) {
        if (deckState is DeckListState.Success) {
            val updatedDeck = (deckState as DeckListState.Success).decks.find { it.id == deckId }
            if (updatedDeck != null) {
                deck = updatedDeck
            }
        }
    }

    GradientBackgroundScreen {
        Scaffold(
            containerColor = Color.Transparent,
            snackbarHost = {
                SnackbarHost(
                    hostState = snackbarHostState,
                    snackbar = { snackbarData ->
                        Snackbar(
                            snackbarData = snackbarData,
                            containerColor = YellowAccent,
                            contentColor = Color.Black,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                )
            },
            topBar = {
                TopAppBar(
                    title = {},
                    navigationIcon = {
                        IconButton(
                            onClick = { navController.popBackStack() },
                            modifier = Modifier
                                .padding(8.dp)
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.3f))
                        ) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = "Voltar",
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
            ) {
                when {
                    (deckState is DeckListState.Loading || statsState is DeckStatsState.Loading) && deck == null -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = YellowAccent)
                        }
                    }
                    deckState is DeckListState.Error -> {
                        ErrorContent(
                            onRetry = { deckViewModel.fetchDecks() },
                            onBack = { navController.popBackStack() }
                        )
                    }
                    deck == null -> {
                        if (statsState is DeckStatsState.Error) {
                            ErrorContent(
                                onRetry = { deckViewModel.fetchDeckStats(deckId) },
                                onBack = { navController.popBackStack() }
                            )
                        } else {
                            DeckNotFoundContent(
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                    else -> {
                        val currentDeck = deck!!
                        MainContent(
                            deck = currentDeck,
                            stats = (statsState as? DeckStatsState.Success)?.stats,
                            navController = navController,
                            deckId = deckId,
                            deckViewModel = deckViewModel,
                            isCreatingFlashcards = isCreatingFlashcards,
                            isCreatingQuiz = isCreatingQuiz,
                            onCreateFlashcards = {
                                isCreatingFlashcards = true
                                deckViewModel.generateFlashcardsForDocument(deckId)
                            },
                            onCreateQuiz = {
                                isCreatingQuiz = true
                                deckViewModel.generateQuizForDocument(deckId)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MainContent(
    deck: com.example.flashify.model.data.DeckResponse,
    stats: com.example.flashify.model.data.DeckStatsResponse?,
    navController: NavController,
    deckId: Int,
    deckViewModel: DeckViewModel,
    isCreatingFlashcards: Boolean,
    isCreatingQuiz: Boolean,
    onCreateFlashcards: () -> Unit,
    onCreateQuiz: () -> Unit
) {
    val quizAttempts = stats?.quiz?.totalAttempts ?: 0
    val quizAverageScore = stats?.quiz?.averageScore ?: 0f
    val quizLastScore = stats?.quiz?.lastScore ?: 0f
    val hasQuizStats = quizAttempts > 0

    val flashcardProgress = stats?.flashcards?.progressPercentage ?: 0f
    val hasFlashcards = (stats?.flashcards?.total ?: 0) > 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Cabeçalho com ícone
        Box(
            modifier = Modifier
                .size(80.dp)
                .shadow(12.dp, CircleShape)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            YellowAccent.copy(alpha = 0.3f),
                            YellowAccent.copy(alpha = 0.15f)
                        )
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.LibraryBooks,
                contentDescription = null,
                tint = YellowAccent,
                modifier = Modifier.size(40.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = deck.filePath,
            fontSize = 26.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground,
            letterSpacing = 0.5.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Escolha sua atividade de estudo para este deck",
            fontSize = 15.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Card de Flashcards
        ModoEstudoCardMelhorado(
            icon = Icons.Default.Style,
            title = "Flashcards",
            description = "Veja e revise flashcards, otimizando seu aprendizado.",
            gradient = listOf(YellowAccent, YellowAccent.copy(alpha = 0.8f)),
            iconBackground = YellowAccent.copy(alpha = 0.2f),
            onClick = {
                navController.navigate("$ESTUDO_SCREEN_ROUTE/$deckId")
            },
            enabled = hasFlashcards,
            isLocked = !hasFlashcards,
            onCreateContent = onCreateFlashcards,
            isCreating = isCreatingFlashcards,
            contentType = "Flashcards"
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Card de Quiz
        ModoEstudoCardMelhorado(
            icon = Icons.Default.Quiz,
            title = "Quiz",
            description = "Teste os seus conhecimentos com perguntas de múltipla escolha geradas pela IA.",
            gradient = listOf(Color(0xFF00BCD4), Color(0xFF0097A7)),
            iconBackground = Color(0xFF00BCD4).copy(alpha = 0.2f),
            onClick = {
                navController.navigate("$QUIZ_SCREEN_ROUTE/$deckId")
            },
            enabled = deck.hasQuiz,
            isLocked = !deck.hasQuiz,
            onCreateContent = onCreateQuiz,
            isCreating = isCreatingQuiz,
            contentType = "Quiz"
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Estatísticas do Deck
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(6.dp, RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        Icons.Default.BarChart,
                        contentDescription = null,
                        tint = YellowAccent,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "Estatísticas",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    "Visão Geral",
                    fontSize = 13.sp,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Gráfico de barras comparativo
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Bottom
                ) {
                    // Barra de Flashcards
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            "${flashcardProgress.toInt()}%",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = YellowAccent
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .width(80.dp)
                                .height((flashcardProgress.toInt() * 1.2).dp.coerceAtMost(120.dp))
                                .background(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            YellowAccent,
                                            YellowAccent.copy(alpha = 0.7f)
                                        )
                                    ),
                                    shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                                )
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "Flashcards",
                            fontSize = 13.sp,
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.width(24.dp))

                    // Barra de Quiz
                    if (deck.hasQuiz) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            val displayScore = if (hasQuizStats) {
                                "${quizAverageScore.toInt()}%"
                            } else {
                                "0%"
                            }

                            Text(
                                displayScore,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF00BCD4)
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            val barHeight = if (hasQuizStats) {
                                (quizAverageScore.toInt() * 1.2).dp.coerceAtMost(120.dp)
                            } else {
                                8.dp
                            }

                            Box(
                                modifier = Modifier
                                    .width(80.dp)
                                    .height(barHeight)
                                    .background(
                                        brush = Brush.verticalGradient(
                                            colors = if (hasQuizStats) {
                                                listOf(
                                                    Color(0xFF00BCD4),
                                                    Color(0xFF00BCD4).copy(alpha = 0.7f)
                                                )
                                            } else {
                                                listOf(
                                                    Color.Gray.copy(alpha = 0.3f),
                                                    Color.Gray.copy(alpha = 0.2f)
                                                )
                                            }
                                        ),
                                        shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                                    )
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                if (hasQuizStats) "Quiz (Média)" else "Quiz",
                                fontSize = 13.sp,
                                color = TextSecondary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Detalhes de Flashcards
                Text(
                    "Flashcards",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StatItemCompact(
                        label = "Conhecidos",
                        value = stats?.flashcards?.known?.toString() ?: "0"
                    )
                    StatItemCompact(
                        label = "Ainda aprendendo",
                        value = stats?.flashcards?.learning?.toString() ?: "0"
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Barra de progresso detalhada
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Progresso",
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                        Text(
                            "${flashcardProgress.toInt()}% de acerto",
                            fontSize = 13.sp,
                            color = YellowAccent,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { flashcardProgress / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = YellowAccent,
                        trackColor = Color.Gray.copy(alpha = 0.2f)
                    )
                }

                // Estatísticas de Quiz
                if (deck.hasQuiz) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Divider(color = Color.Gray.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        "Quiz",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    if (hasQuizStats) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    "Última pontuação",
                                    fontSize = 13.sp,
                                    color = TextSecondary
                                )
                                Text(
                                    "${quizLastScore.toInt()}%",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    "Média",
                                    fontSize = 13.sp,
                                    color = TextSecondary
                                )
                                Text(
                                    "${quizAverageScore.toInt()}%",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Color(0xFF00BCD4).copy(alpha = 0.1f),
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                tint = Color(0xFF00BCD4),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "$quizAttempts tentativa${if (quizAttempts > 1) "s" else ""}",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                    } else {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Color(0xFF00BCD4).copy(alpha = 0.1f),
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                tint = Color(0xFF00BCD4),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                "Nenhuma tentativa realizada ainda.\nInicie o quiz para ver suas estatísticas!",
                                fontSize = 13.sp,
                                color = TextSecondary,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun ModoEstudoCardMelhorado(
    icon: ImageVector,
    title: String,
    description: String,
    gradient: List<Color>,
    iconBackground: Color,
    onClick: () -> Unit,
    enabled: Boolean,
    isLocked: Boolean = false,
    onCreateContent: () -> Unit = {},
    isCreating: Boolean = false,
    contentType: String = ""
) {
    val animatedProgress by animateFloatAsState(
        targetValue = if (isCreating) 1f else 0f,
        label = "progress"
    )

    Card(
        onClick = if (enabled) onClick else {{}},
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (enabled) 8.dp else 2.dp,
                shape = RoundedCornerShape(20.dp)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (enabled)
                MaterialTheme.colorScheme.surface
            else
                MaterialTheme.colorScheme.surface.copy(alpha = 0.4f)
        ),
        enabled = enabled
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Ícone com fundo colorido
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(iconBackground, RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (enabled) gradient[0] else Color.Gray,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (enabled)
                            MaterialTheme.colorScheme.onSurface
                        else
                            TextSecondary
                    )
                }

                if (enabled) {
                    Icon(
                        Icons.Default.ArrowForward,
                        contentDescription = null,
                        tint = gradient[0],
                        modifier = Modifier.size(24.dp)
                    )
                } else if (isLocked) {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = "Bloqueado",
                        tint = Color.Gray,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = description,
                fontSize = 14.sp,
                color = TextSecondary,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Mostra progresso se estiver criando
            AnimatedVisibility(visible = isCreating) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = gradient[0],
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Gerando $contentType...",
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = gradient[0],
                        trackColor = Color.Gray.copy(alpha = 0.2f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            // Botão de ação
            if (isLocked && !isCreating) {
                // Mostra botão de criar quando está bloqueado
                Button(
                    onClick = onCreateContent,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = gradient[0],
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Criar $contentType",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            } else if (!isCreating) {
                // Botão normal de iniciar
                Button(
                    onClick = if (enabled) onClick else {{}},
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    enabled = enabled,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (enabled) gradient[0] else Color.Gray,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (enabled) "Iniciar" else "Indisponível",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}

@Composable
fun StatItemCompact(
    label: String,
    value: String
) {
    Column {
        Text(
            text = label,
            fontSize = 13.sp,
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun ErrorContent(
    onRetry: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Error,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(64.dp)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            "Erro ao carregar deck",
            color = MaterialTheme.colorScheme.error,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp
        )
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(
                containerColor = YellowAccent,
                contentColor = Color.Black
            )
        ) {
            Text("Tentar Novamente")
        }
        Spacer(Modifier.height(8.dp))
        TextButton(onClick = onBack) {
            Text("Voltar", color = TextSecondary)
        }
    }
}

@Composable
fun DeckNotFoundContent(
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.SearchOff,
            contentDescription = null,
            tint = TextSecondary,
            modifier = Modifier.size(64.dp)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            "Deck não encontrado",
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Este deck pode ter sido excluído",
            color = TextSecondary,
            fontSize = 14.sp
        )
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onBack,
            colors = ButtonDefaults.buttonColors(
                containerColor = YellowAccent,
                contentColor = Color.Black
            )
        ) {
            Text("Voltar à Biblioteca")
        }
    }
}
package com.example.flashify.view.ui.screen.principal

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.flashify.model.data.DeckResponse
import com.example.flashify.model.data.NavItem
import com.example.flashify.model.manager.ThemeManager
import com.example.flashify.model.util.*
import com.example.flashify.view.ui.components.GradientBackgroundScreen
import com.example.flashify.view.ui.components.NavegacaoBotaoAbaixo
import com.example.flashify.viewmodel.*
import kotlinx.coroutines.launch

@Composable
fun TelaPrincipal(
    navController: NavController,
    deckViewModel: DeckViewModel = hiltViewModel(),
    homeViewModel: HomeViewModel = hiltViewModel()
) {
    // --- CONFIGURAÇÃO DE TEMA E ESTADO ---
    val context = LocalContext.current
    val themeManager = remember { ThemeManager(context) }
    val isDarkTheme by themeManager.isDarkTheme.collectAsState(initial = isSystemInDarkTheme())

    val deckState by deckViewModel.deckListState.collectAsStateWithLifecycle()
    val homeState by homeViewModel.uiState.collectAsState()
    val generationLimitState by deckViewModel.generationLimitState.collectAsStateWithLifecycle()

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                homeViewModel.refresh()
                deckViewModel.fetchDecks()
                deckViewModel.checkGenerationLimit()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(Unit) {
        deckViewModel.checkGenerationLimit()
    }

    var selectedItem by remember { mutableStateOf(0) }
    val navItems = listOf(
        NavItem("Início", Icons.Default.Home),
        NavItem("Biblioteca", Icons.Default.FormatListBulleted),
        NavItem("Criar", Icons.Default.Add),
        NavItem("Progresso", Icons.Default.TrendingUp),
        NavItem("Config", Icons.Default.Settings)
    )

    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = {
            Column(modifier = Modifier.navigationBarsPadding()) {
                NavegacaoBotaoAbaixo(
                    navItems = navItems,
                    selectedItem = selectedItem,
                    onItemSelected = { clickedIndex ->
                        selectedItem = clickedIndex
                        when (navItems[clickedIndex].label) {
                            "Início" -> { /* Já está aqui */ }
                            "Criar" -> navController.navigate(CREATE_FLASHCARD_ROUTE) { popUpTo(MAIN_SCREEN_ROUTE) }
                            "Biblioteca" -> navController.navigate(BIBLIOTECA_SCREEN_ROUTE) { popUpTo(MAIN_SCREEN_ROUTE) }
                            "Progresso" -> navController.navigate(PROGRESSO_SCREEN_ROUTE) { popUpTo(MAIN_SCREEN_ROUTE) }
                            "Config" -> navController.navigate(CONFIGURATION_SCREEN_ROUTE) { popUpTo(MAIN_SCREEN_ROUTE) }
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        GradientBackgroundScreen(isDarkTheme = isDarkTheme) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(Modifier.height(24.dp))

                // CABEÇALHO (HEADER)
                CabecalhoUsuario(
                    homeViewModel = homeViewModel,
                    settingsViewModel = hiltViewModel(),
                    generationLimitState = generationLimitState,
                    themeManager = themeManager,
                    isDarkTheme = isDarkTheme
                )

                Spacer(Modifier.height(32.dp))

                // STREAK
                SecaoStreak(state = homeState, isDarkTheme = isDarkTheme)

                Spacer(Modifier.height(28.dp))

                Text(
                    "Acesso Rápido",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.height(16.dp))

                // CARDS DE AÇÃO RÁPIDA
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CartaoSessaoEstudo(
                        modifier = Modifier.weight(1f),
                        navController = navController
                    )
                    CartaoProgresso(
                        modifier = Modifier.weight(1f),
                        state = homeState
                    )
                }

                Spacer(Modifier.height(32.dp))

                // DECKS RECENTES
                SecaoContinuarEstudando(
                    state = deckState,
                    navController = navController
                )

                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun CabecalhoUsuario(
    homeViewModel: HomeViewModel,
    settingsViewModel: SettingsViewModel,
    generationLimitState: GenerationLimitState,
    themeManager: ThemeManager,
    isDarkTheme: Boolean
) {
    val userState by settingsViewModel.userState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    // Cores da Paleta
    val primaryColor = MaterialTheme.colorScheme.primary
    val cyanColor = if (isDarkTheme) Color(0xFF00BCD4) else Color(0xFF0097A7)

    // Obtém informações do usuário
    val initial = when (val state = userState) {
        is UserState.Success -> state.user.username.firstOrNull()?.uppercaseChar()?.toString()
        else -> "U"
    }

    val fullName = when (val state = userState) {
        is UserState.Success -> {
            val parts = state.user.username.split(" ")
            if (parts.size > 1) "${parts.first()} ${parts.last()}" else state.user.username
        }
        is UserState.Loading -> "Carregando..."
        else -> "Estudante"
    }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Linha superior: Avatar e Ações
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // --- PERFIL (ESQUERDA) ---
            // Avatar com gradiente sutil
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                primaryColor.copy(alpha = 0.3f),
                                primaryColor.copy(alpha = 0.15f)
                            )
                        )
                    )
                    .border(2.dp, primaryColor.copy(alpha = 0.4f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initial ?: "U",
                    fontWeight = FontWeight.ExtraBold,
                    color = primaryColor,
                    fontSize = 28.sp
                )
            }

            // --- AÇÕES (DIREITA) ---
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // BADGE DE ENERGIA (GERAÇÕES) COM BARRA DE CONSUMO
                if (generationLimitState is GenerationLimitState.Success) {
                    val info = (generationLimitState as GenerationLimitState.Success).info
                    val isLimitReached = info.used >= info.limit
                    // Calcula o progresso de CONSUMO (quanto já foi usado)
                    val consumptionPercentage = if (info.limit > 0) info.used.toFloat() / info.limit.toFloat() else 0f

                    // Cores ajustadas para modo claro e escuro
                    val badgeColor = if (isLimitReached) {
                        MaterialTheme.colorScheme.error
                    } else {
                        if (isDarkTheme) Color(0xFF00BCD4) else Color(0xFF00796B) // Cyan escuro/Verde-azulado para modo claro
                    }
                    val badgeBg = if (isDarkTheme) {
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    } else {
                        Color(0xFFE0E0E0) // Cinza claro com mais contraste no modo claro
                    }

                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Texto com ícone - mostra quantos foram USADOS
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Bolt,
                                contentDescription = "Energia",
                                tint = badgeColor,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "${info.used}/${info.limit}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Barra de progresso horizontal - cresce conforme USO
                        Box(
                            modifier = Modifier
                                .width(100.dp)
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(badgeBg)
                                .then(
                                    if (!isDarkTheme) {
                                        Modifier.border(0.5.dp, Color(0xFFBDBDBD), RoundedCornerShape(4.dp))
                                    } else Modifier
                                )
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(consumptionPercentage)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(badgeColor)
                            )
                        }
                    }
                }

                // BOTÃO DE TEMA
                IconButton(
                    onClick = { scope.launch { themeManager.toggleTheme() } },
                    modifier = Modifier
                        .size(42.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            CircleShape
                        )
                ) {
                    Icon(
                        imageVector = if (isDarkTheme) Icons.Default.WbSunny else Icons.Default.DarkMode,
                        contentDescription = "Alterar Tema",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        // Linha inferior: Saudação e Nome
        Spacer(Modifier.height(16.dp))

        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                "Olá,",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = fullName,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun SecaoStreak(state: HomeUiState, isDarkTheme: Boolean) {
    // Azul para o Streak
    val streakColor = if (isDarkTheme) Color(0xFF00BCD4) else Color(0xFF0097A7)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = null,
                        tint = streakColor,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(
                            "Sequência",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (!state.isLoadingStreak) {
                            Text(
                                "${state.streakCount} Dias",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                if (!state.isLoadingStreak && state.streakCount > 0) {
                    Surface(
                        color = streakColor.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            "Ativo",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            color = streakColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            if (state.isLoadingStreak) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.primary)
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    state.streakDays.forEach { day ->
                        DiaStreakItemModerno(day = day, activeColor = streakColor)
                    }
                }
            }
        }
    }
}

@Composable
fun DiaStreakItemModerno(day: StreakDay, activeColor: Color) {
    val isCompleted = day.status == StreakStatus.STUDIED
    val isMissed = day.status == StreakStatus.MISSED

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            day.dayLetter,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(
                    when {
                        isCompleted -> activeColor
                        isMissed -> MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isCompleted) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(18.dp)
                )
            } else if (isMissed) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun CartaoSessaoEstudo(modifier: Modifier = Modifier, navController: NavController) {
    val primaryColor = MaterialTheme.colorScheme.primary

    Card(
        modifier = modifier
            .height(160.dp)
            .clickable { navController.navigate(BIBLIOTECA_SCREEN_ROUTE) },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = primaryColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Elemento decorativo
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 30.dp, y = (-30).dp)
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.15f))
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(Color.Black.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column {
                    Text(
                        "Sessão Rápida",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Text(
                        "Iniciar agora",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

@Composable
fun CartaoProgresso(modifier: Modifier = Modifier, state: HomeUiState) {
    Card(
        modifier = modifier.height(160.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (state.isLoadingProgress) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        progress = 1f,
                        modifier = Modifier.size(75.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        strokeWidth = 8.dp,
                        strokeCap = StrokeCap.Round
                    )
                    val animatedProgress by animateFloatAsState(
                        targetValue = state.studyTimerProgress,
                        animationSpec = tween(1000),
                        label = "progress"
                    )
                    CircularProgressIndicator(
                        progress = animatedProgress,
                        modifier = Modifier.size(75.dp),
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 8.dp,
                        strokeCap = StrokeCap.Round
                    )

                    Text(
                        "${(state.studyTimerProgress * 100).toInt()}%",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(Modifier.height(12.dp))
                Text(
                    "Meta Semanal",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun SecaoContinuarEstudando(state: DeckListState, navController: NavController) {
    if (state is DeckListState.Success && state.decks.isNotEmpty()) {
        val recentDecks = state.decks.take(5)

        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Recentes",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(start = 4.dp)
                )
                TextButton(onClick = { navController.navigate(BIBLIOTECA_SCREEN_ROUTE) }) {
                    Text(
                        "Ver tudo",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp)
            ) {
                items(recentDecks.size) { index ->
                    CartaoDeckRecenteModerno(
                        deck = recentDecks[index],
                        onClick = {
                            navController.navigate("$ESCOLHA_MODO_ESTUDO_ROUTE/${recentDecks[index].id}")
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun CartaoDeckRecenteModerno(deck: DeckResponse, onClick: () -> Unit) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val isDarkTheme = isSystemInDarkTheme()
    val quizColor = if (isDarkTheme) Color(0xFF00BCD4) else Color(0xFF0097A7)

    Card(
        onClick = onClick,
        modifier = Modifier
            .width(260.dp)
            .height(180.dp)
            .shadow(4.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Topo: Ícone e Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Ícone do Deck
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(primaryColor.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.MenuBook,
                        contentDescription = null,
                        tint = primaryColor,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Badges (Amarelo + Azul)
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Badge Flashcards (Amarelo)
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = primaryColor.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.3f))
                    ) {
                        Text(
                            "Flashcards",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (!isDarkTheme) Color(0xFFF57F17) else primaryColor
                        )
                    }

                    // Badge Quiz (Azul)
                    if (deck.hasQuiz) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = quizColor.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, quizColor.copy(alpha = 0.3f))
                        ) {
                            Text(
                                "Quiz",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = quizColor
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Título
            Text(
                text = deck.filePath,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 20.sp
            )

            Spacer(Modifier.height(12.dp))

            // Botão SÓLIDO (Preenchido) para contraste
            Button(
                onClick = onClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp),
                contentPadding = PaddingValues(0.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = primaryColor,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Text(
                    "Acessar Deck",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.width(6.dp))
                Icon(
                    Icons.Default.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}
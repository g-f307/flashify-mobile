package com.example.flashify.view.ui.screen.principal

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.flashify.model.data.DeckResponse
import com.example.flashify.model.data.NavItem
import com.example.flashify.model.util.*
import com.example.flashify.view.ui.components.GradientBackgroundScreen
import com.example.flashify.view.ui.components.NavegacaoBotaoAbaixo
import com.example.flashify.view.ui.theme.TextSecondary
import com.example.flashify.view.ui.theme.YellowAccent
import com.example.flashify.viewmodel.*

@Composable
fun TelaPrincipal(
    navController: NavController,
    deckViewModel: DeckViewModel = viewModel(),
    homeViewModel: HomeViewModel = viewModel()
) {
    // Estados dos ViewModels
    val deckState by deckViewModel.deckListState.collectAsStateWithLifecycle()
    val homeState by homeViewModel.uiState.collectAsState()

    // Observa o ciclo de vida da tela para recarregar os dados
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            // Se o evento for ON_RESUME, significa que a tela
            // voltou a ficar visível (ex: ao voltar de outra tela)
            if (event == Lifecycle.Event.ON_RESUME) {
                homeViewModel.refresh()
                deckViewModel.fetchDecks()
            }
        }

        // Adiciona o observador
        lifecycleOwner.lifecycle.addObserver(observer)

        // Remove o observador ao sair da tela
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Lógica da barra de navegação
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
            BottomAppBar(
                containerColor = Color.Transparent,
                tonalElevation = 0.dp,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                NavegacaoBotaoAbaixo(
                    modifier = Modifier.clip(RoundedCornerShape(50)),
                    navItems = navItems,
                    selectedItem = selectedItem,
                    onItemSelected = { clickedIndex ->
                        selectedItem = clickedIndex
                        when (navItems[clickedIndex].label) {
                            "Início" -> { /* Já está aqui */ }
                            "Criar" -> navController.navigate(CREATE_FLASHCARD_ROUTE) {
                                popUpTo(MAIN_SCREEN_ROUTE)
                            }
                            "Biblioteca" -> navController.navigate(BIBLIOTECA_SCREEN_ROUTE) {
                                popUpTo(MAIN_SCREEN_ROUTE)
                            }
                            "Progresso" -> navController.navigate(PROGRESSO_SCREEN_ROUTE) {
                                popUpTo(MAIN_SCREEN_ROUTE)
                            }
                            "Config" -> navController.navigate(CONFIGURATION_SCREEN_ROUTE) {
                                popUpTo(MAIN_SCREEN_ROUTE)
                            }
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        GradientBackgroundScreen {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(Modifier.height(16.dp))
                CabecalhoUsuario(homeViewModel)
                Spacer(Modifier.height(24.dp))
                SecaoStreak(state = homeState)
                Spacer(Modifier.height(24.dp))
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
                Spacer(Modifier.height(24.dp))
                SecaoContinuarEstudando(
                    state = deckState,
                    navController = navController
                )
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun CabecalhoUsuario(homeViewModel: HomeViewModel, settingsViewModel: SettingsViewModel = viewModel()) {
    val userState by settingsViewModel.userState.collectAsStateWithLifecycle()
    var isDarkTheme by remember { mutableStateOf(true) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(YellowAccent.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                when (val state = userState) {
                    is UserState.Success -> {
                        Text(
                            state.user.username.firstOrNull()?.uppercaseChar()?.toString() ?: "U",
                            fontWeight = FontWeight.Bold,
                            color = YellowAccent,
                            fontSize = 18.sp
                        )
                    }
                    else -> {
                        Text(
                            "U",
                            fontWeight = FontWeight.Bold,
                            color = YellowAccent,
                            fontSize = 18.sp
                        )
                    }
                }
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    "Olá,",
                    fontSize = 14.sp,
                    color = TextSecondary
                )
                when (val state = userState) {
                    is UserState.Loading -> {
                        Text(
                            "Carregando...",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    is UserState.Success -> {
                        Text(
                            state.user.username,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    is UserState.Error -> {
                        Text(
                            "Usuário",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }
        }

        // Botão de tema (funcional)
        IconButton(
            onClick = {
                isDarkTheme = !isDarkTheme
                // TODO: Implementar mudança de tema real
                // Por enquanto, apenas visual
            }
        ) {
            Icon(
                imageVector = if (isDarkTheme) Icons.Default.WbSunny else Icons.Default.DarkMode,
                contentDescription = "Mudar Tema",
                tint = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

@Composable
fun SecaoStreak(state: HomeUiState) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Seu Streak",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            if (!state.isLoadingStreak) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = null,
                        tint = YellowAccent,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "${state.streakCount} dias",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = YellowAccent
                    )
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                if (state.isLoadingStreak) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = YellowAccent
                    )
                } else if (state.errorMessage != null) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "Erro ao carregar dados",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            state.errorMessage ?: "Erro desconhecido",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        state.streakDays.forEach { day ->
                            DiaStreakItem(day = day)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DiaStreakItem(day: StreakDay) {
    val (icon, color) = when (day.status) {
        StreakStatus.STUDIED -> "★" to YellowAccent
        StreakStatus.MISSED -> "✕" to Color.Red.copy(alpha = 0.7f)
        StreakStatus.PENDING -> "○" to TextSecondary
    }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            day.dayLetter,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            icon,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
fun CartaoSessaoEstudo(modifier: Modifier = Modifier, navController: NavController) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = null,
                        tint = YellowAccent,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        "Sessão de 15 min",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    "Estude com foco e concentração",
                    fontSize = 13.sp,
                    color = TextSecondary,
                    lineHeight = 18.sp
                )
            }
            Button(
                onClick = { navController.navigate(BIBLIOTECA_SCREEN_ROUTE) },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = YellowAccent),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "Iniciar",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
fun CartaoProgresso(modifier: Modifier = Modifier, state: HomeUiState) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "Progresso Semanal",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.align(Alignment.Start)
            )

            if (state.isLoadingProgress) {
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = YellowAccent
                    )
                }
            } else {
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        progress = state.studyTimerProgress,
                        modifier = Modifier.size(90.dp),
                        color = YellowAccent,
                        strokeWidth = 8.dp,
                        trackColor = MaterialTheme.colorScheme.background
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "${(state.studyTimerProgress * 100).toInt()}%",
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            "${state.cardsStudiedWeek} cards",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SecaoContinuarEstudando(state: DeckListState, navController: NavController) {
    if (state is DeckListState.Success && state.decks.isNotEmpty()) {
        // Pegar os 5 decks mais recentes
        val recentDecks = state.decks.take(5)

        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                "Continue de onde parou",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(16.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(recentDecks.size) { index ->
                    CartaoDeckRecente(
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
fun CartaoDeckRecente(deck: DeckResponse, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .width(280.dp)
            .height(140.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Cabeçalho com ícone e título
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.Default.MenuBook,
                        contentDescription = null,
                        tint = YellowAccent,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        deck.filePath.take(18) + if (deck.filePath.length > 18) "..." else "",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Badges de Flashcards e Quiz
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Badge Flashcards
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(
                            color = YellowAccent.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        Icons.Default.Style,
                        contentDescription = null,
                        tint = YellowAccent,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "${deck.totalFlashcards}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = YellowAccent
                    )
                }

                // Badge Quiz (se houver)
                if (deck.hasQuiz) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(
                                color = Color(0xFF00BCD4).copy(alpha = 0.2f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            Icons.Default.Quiz,
                            contentDescription = null,
                            tint = Color(0xFF00BCD4),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "Quiz",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF00BCD4)
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Botão de estudar
            Button(
                onClick = onClick,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = YellowAccent),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "Estudar",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    }
}
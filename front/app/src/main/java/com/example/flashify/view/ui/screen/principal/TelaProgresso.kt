package com.example.flashify.view.ui.screen.principal

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.flashify.model.data.NavItem
import com.example.flashify.model.util.*
import com.example.flashify.view.ui.components.GradientBackgroundScreen
import com.example.flashify.view.ui.components.NavegacaoBotaoAbaixo
import com.example.flashify.view.ui.theme.TextSecondary
import com.example.flashify.view.ui.theme.YellowAccent
import com.example.flashify.viewmodel.HomeViewModel
import kotlin.math.roundToInt

@Composable
fun TelaProgresso(
    navController: NavController,
    homeViewModel: HomeViewModel = hiltViewModel() // ✅ ATUALIZADO
) {
    val homeState by homeViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        homeViewModel.refresh()
    }

    var selectedItem by remember { mutableStateOf(3) }
    val navItems = listOf(
        NavItem("Início", Icons.Default.Home),
        NavItem("Biblioteca", Icons.Default.FormatListBulleted),
        NavItem("Criar", Icons.Default.Add),
        NavItem("Progresso", Icons.Default.TrendingUp),
        NavItem("Config", Icons.Default.Settings)
    )

    // Calcular precisão dos Flashcards (valor entre 0 e 1)
    val realAccuracy = remember(homeState.generalAccuracy) {
        when {
            // Se vier como 92.3, converter para 0.923
            homeState.generalAccuracy > 1.0 -> homeState.generalAccuracy / 100.0
            // Se já vier como 0.923, manter
            else -> homeState.generalAccuracy
        }.coerceIn(0.0, 1.0)
    }

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
                            "Início" -> navController.navigate(MAIN_SCREEN_ROUTE) {
                                popUpTo(PROGRESSO_SCREEN_ROUTE) { inclusive = true }
                            }
                            "Criar" -> navController.navigate(CREATE_FLASHCARD_ROUTE) {
                                popUpTo(PROGRESSO_SCREEN_ROUTE)
                            }
                            "Biblioteca" -> navController.navigate(BIBLIOTECA_SCREEN_ROUTE) {
                                popUpTo(PROGRESSO_SCREEN_ROUTE)
                            }
                            "Progresso" -> { /* Já está aqui */ }
                            "Config" -> navController.navigate(CONFIGURATION_SCREEN_ROUTE) {
                                popUpTo(PROGRESSO_SCREEN_ROUTE)
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

                // Header com ícone
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        YellowAccent.copy(alpha = 0.3f),
                                        YellowAccent.copy(alpha = 0.15f)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.TrendingUp,
                            contentDescription = null,
                            tint = YellowAccent,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(
                            "Seu Progresso",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            "Acompanhe sua evolução",
                            fontSize = 14.sp,
                            color = TextSecondary
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))

                if (homeState.isLoadingProgress) {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = YellowAccent)
                    }
                } else if (homeState.errorMessage != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Error,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                homeState.errorMessage ?: "Erro desconhecido",
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                } else {
                    // Estatísticas principais em grade
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatCard(
                            title = "Streak",
                            value = "${homeState.streakCount}",
                            subtitle = "dias",
                            icon = Icons.Default.LocalFireDepartment,
                            color = Color(0xFFFF6B35),
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = "Estudados",
                            value = "${homeState.cardsStudiedWeek}",
                            subtitle = "cards",
                            icon = Icons.Default.Style,
                            color = YellowAccent,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    // Precisão dividida por tipo - DADOS REAIS
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        AccuracyCard(
                            title = "Flashcards",
                            percentage = (realAccuracy * 100).roundToInt(),
                            icon = Icons.Default.Style,
                            color = YellowAccent,
                            modifier = Modifier.weight(1f)
                        )

                        AccuracyCard(
                            title = "Quiz",
                            percentage = homeState.quizAverageScore.roundToInt(),
                            icon = Icons.Default.Quiz,
                            color = Color(0xFF00BCD4),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(Modifier.height(24.dp))

                    // Visão geral com gráfico circular - DADOS REAIS
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Insights,
                                    contentDescription = null,
                                    tint = YellowAccent,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text(
                                        "Visão Geral",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        "Seu desempenho total",
                                        fontSize = 13.sp,
                                        color = TextSecondary
                                    )
                                }
                            }

                            Spacer(Modifier.height(24.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Gráfico circular - DADOS REAIS
                                Box(contentAlignment = Alignment.Center) {
                                    CircularProgressBar(
                                        percentage = (realAccuracy * 100).toFloat(),
                                        radius = 70.dp,
                                        color = YellowAccent,
                                        strokeWidth = 10.dp
                                    )
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            "${(realAccuracy * 100).roundToInt()}%",
                                            fontSize = 28.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            "precisão",
                                            fontSize = 12.sp,
                                            color = TextSecondary
                                        )
                                    }
                                }

                                // Estatísticas complementares - DADOS REAIS
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    val totalCards = homeState.cardsStudiedWeek
                                    val acertos = (totalCards * realAccuracy).roundToInt()
                                    val erros = totalCards - acertos

                                    MiniStatItem(
                                        icon = Icons.Default.CheckCircle,
                                        label = "Acertos",
                                        value = "$acertos",
                                        color = Color(0xFF4CAF50)
                                    )
                                    MiniStatItem(
                                        icon = Icons.Default.Cancel,
                                        label = "Erros",
                                        value = "$erros",
                                        color = Color(0xFFE57373)
                                    )
                                    MiniStatItem(
                                        icon = Icons.Default.Star,
                                        label = "Total",
                                        value = "$totalCards",
                                        color = YellowAccent
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    // Atividade semanal - DADOS REAIS
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(modifier = Modifier.padding(24.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.CalendarMonth,
                                    contentDescription = null,
                                    tint = Color(0xFF00BCD4),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text(
                                        "Atividade Semanal",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        "Últimos 7 dias",
                                        fontSize = 13.sp,
                                        color = TextSecondary
                                    )
                                }
                            }

                            Spacer(Modifier.height(20.dp))

                            WeeklyActivityChart(
                                weeklyActivity = homeState.weeklyActivity
                            )
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    // Conquistas - DADOS REAIS
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(modifier = Modifier.padding(24.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.EmojiEvents,
                                    contentDescription = null,
                                    tint = Color(0xFFFFD700),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text(
                                        "Conquistas",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        "Continue progredindo!",
                                        fontSize = 13.sp,
                                        color = TextSecondary
                                    )
                                }
                            }

                            Spacer(Modifier.height(20.dp))

                            AchievementsList(
                                streakCount = homeState.streakCount,
                                cardsStudied = homeState.cardsStudiedWeek,
                                accuracy = realAccuracy
                            )
                        }
                    }

                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(135.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(22.dp)
                )
            }

            Column {
                Text(
                    value,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = color,
                    maxLines = 1
                )
                Text(
                    subtitle,
                    fontSize = 11.sp,
                    color = TextSecondary,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun AccuracyCard(
    title: String,
    percentage: Int,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(135.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column {
                Text(
                    "$percentage%",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = color,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Barra de progresso
                LinearProgressIndicator(
                    progress = { percentage / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = color,
                    trackColor = Color.Gray.copy(alpha = 0.2f)
                )
            }
        }
    }
}

@Composable
fun MiniStatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    color: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(18.dp)
        )
        Column {
            Text(
                label,
                fontSize = 11.sp,
                color = TextSecondary
            )
            Text(
                value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun CircularProgressBar(
    percentage: Float,
    radius: androidx.compose.ui.unit.Dp,
    color: Color,
    strokeWidth: androidx.compose.ui.unit.Dp
) {
    val animatedPercentage by animateFloatAsState(
        targetValue = percentage.coerceIn(0f, 100f),
        animationSpec = tween(durationMillis = 1000),
        label = "progress"
    )

    Canvas(
        modifier = Modifier.size(radius * 2)
    ) {
        val size = this.size.minDimension
        val sweepAngle = (animatedPercentage / 100f) * 360f

        // Background circle
        drawArc(
            color = Color.Gray.copy(alpha = 0.2f),
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round),
            size = Size(size, size),
            topLeft = Offset((this.size.width - size) / 2, (this.size.height - size) / 2)
        )

        // Progress arc
        drawArc(
            color = color,
            startAngle = -90f,
            sweepAngle = sweepAngle,
            useCenter = false,
            style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round),
            size = Size(size, size),
            topLeft = Offset((this.size.width - size) / 2, (this.size.height - size) / 2)
        )
    }
}

@Composable
fun WeeklyActivityChart(weeklyActivity: List<Int>) {
    // A lista do backend começa na Segunda (índice 0)
    val daysOfWeek = listOf("S", "T", "Q", "Q", "S", "S", "D")

    val safeWeeklyActivity = if (weeklyActivity.size >= 7) {
        weeklyActivity.take(7)
    } else {
        weeklyActivity + List(7 - weeklyActivity.size) { 0 }
    }

    val maxValue = safeWeeklyActivity.maxOrNull()?.toFloat() ?: 1f

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            safeWeeklyActivity.forEachIndexed { index, value ->
                val heightFraction = if (maxValue > 0) value / maxValue else 0f
                val isHighest = value == maxValue.toInt() && value > 0

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .width(36.dp)
                            .height(140.dp),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        Box(
                            modifier = Modifier
                                .width(36.dp)
                                .fillMaxHeight(heightFraction.coerceIn(0.15f, 1f))
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (value > 0) {
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                if (isHighest) YellowAccent else Color(0xFF00BCD4),
                                                if (isHighest) YellowAccent.copy(0.7f) else Color(0xFF00BCD4).copy(0.7f)
                                            )
                                        )
                                    } else {
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                Color.Gray.copy(alpha = 0.3f),
                                                Color.Gray.copy(alpha = 0.2f)
                                            )
                                        )
                                    }
                                )
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        daysOfWeek.getOrNull(index) ?: "",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "$value",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (value > 0) {
                            if (isHighest) YellowAccent else Color(0xFF00BCD4)
                        } else TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
fun AchievementsList(
    streakCount: Int,
    cardsStudied: Int,
    accuracy: Double
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Streak achievements
        if (streakCount >= 7) {
            AchievementItem(
                title = "Semana Completa!",
                description = "Estudou por 7 dias consecutivos",
                icon = Icons.Default.LocalFireDepartment,
                color = Color(0xFFFF6B35),
                unlocked = true
            )
        }

        if (streakCount >= 30) {
            AchievementItem(
                title = "Mestre da Consistência",
                description = "30 dias de streak!",
                icon = Icons.Default.EmojiEvents,
                color = Color(0xFFFFD700),
                unlocked = true
            )
        }

        // Cards studied achievements
        if (cardsStudied >= 50) {
            AchievementItem(
                title = "Estudante Dedicado",
                description = "Estudou 50+ cards esta semana",
                icon = Icons.Default.School,
                color = Color(0xFF00BCD4),
                unlocked = true
            )
        }

        // Accuracy achievements
        if (accuracy >= 0.9) {
            AchievementItem(
                title = "Precisão Perfeita",
                description = "90%+ de acertos",
                icon = Icons.Default.Star,
                color = Color(0xFF4CAF50),
                unlocked = true
            )
        }

        // Locked achievements
        if (streakCount < 7) {
            AchievementItem(
                title = "Semana Completa",
                description = "Estude por 7 dias consecutivos",
                icon = Icons.Default.Lock,
                color = Color.Gray,
                unlocked = false
            )
        }

        if (cardsStudied < 50) {
            AchievementItem(
                title = "Estudante Dedicado",
                description = "Estude 50+ cards em uma semana",
                icon = Icons.Default.Lock,
                color = Color.Gray,
                unlocked = false
            )
        }
    }
}

@Composable
fun AchievementItem(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    unlocked: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (unlocked)
                MaterialTheme.colorScheme.surface
            else
                MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(
                        if (unlocked) {
                            Brush.radialGradient(
                                colors = listOf(
                                    color.copy(alpha = 0.3f),
                                    color.copy(alpha = 0.1f)
                                )
                            )
                        } else {
                            Brush.radialGradient(
                                colors = listOf(
                                    Color.Gray.copy(alpha = 0.2f),
                                    Color.Gray.copy(alpha = 0.1f)
                                )
                            )
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (unlocked) color else Color.Gray,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (unlocked)
                        MaterialTheme.colorScheme.onSurface
                    else
                        TextSecondary
                )
                Text(
                    description,
                    fontSize = 13.sp,
                    color = TextSecondary
                )
            }

            if (unlocked) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "Desbloqueado",
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
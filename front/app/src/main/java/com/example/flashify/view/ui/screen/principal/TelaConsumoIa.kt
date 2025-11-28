package com.example.flashify.view.ui.screen.principal

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.flashify.view.ui.components.GradientBackgroundScreen
import com.example.flashify.viewmodel.GenerationLimitViewModel
import java.util.Locale

data class UseCase(
    val id: String,
    val icon: ImageVector,
    val title: String,
    val description: String,
    val example: String,
    val impact: String
)

data class FreeAction(
    val icon: ImageVector,
    val title: String,
    val description: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaConsumoIA(
    navController: NavController,
    viewModel: GenerationLimitViewModel = hiltViewModel()
) {
    val isDarkTheme = isSystemInDarkTheme()
    val limitInfo by viewModel.limitInfo.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    val yellowPrimary = Color(0xFFFFD600)
    val cyanSecondary = if (isDarkTheme) Color(0xFF6BDEF3) else Color(0xFF00BCD4)
    val greenSuccess = if (isDarkTheme) Color(0xFF4CAF50) else Color(0xFF2E7D32)
    val redWarning = if (isDarkTheme) Color(0xFFEF5350) else Color(0xFFD32F2F)
    val orangeWarning = if (isDarkTheme) Color(0xFFFF9800) else Color(0xFFF57C00)

    var selectedUseCase by remember { mutableStateOf<String?>(null) }

    val useCases = remember {
        listOf(
            UseCase(
                "create-deck",
                Icons.Default.NoteAdd,
                "Criar Novo Deck",
                "Ao fazer upload de um arquivo ou inserir texto para criar flashcards ou quiz",
                "Upload de PDF → Gera 10 flashcards",
                "1 geração"
            ),
            UseCase(
                "add-flashcards",
                Icons.Default.AddCard,
                "Adicionar Flashcards",
                "Quando você adiciona mais flashcards a um deck existente",
                "Adicionar +5 flashcards ao deck",
                "1 geração"
            ),
            UseCase(
                "add-questions",
                Icons.Default.QuestionAnswer,
                "Adicionar Perguntas ao Quiz",
                "Ao expandir um quiz com novas perguntas geradas pela IA",
                "Adicionar +3 perguntas ao quiz",
                "1 geração"
            ),
            UseCase(
                "generate-quiz",
                Icons.Default.Quiz,
                "Criar Quiz para Deck",
                "Quando você gera um quiz pela primeira vez em um deck que só tinha flashcards",
                "Criar quiz com 10 perguntas",
                "1 geração"
            ),
            UseCase(
                "generate-flashcards",
                Icons.Default.Style,
                "Criar Flashcards para Deck",
                "Quando você gera flashcards pela primeira vez em um deck que só tinha quiz",
                "Criar 10 flashcards",
                "1 geração"
            )
        )
    }

    val freeActions = remember {
        listOf(
            FreeAction(
                Icons.Default.PlayArrow,
                "Estudar com Flashcards",
                "Revisar e estudar flashcards existentes não consome gerações"
            ),
            FreeAction(
                Icons.Default.CheckCircle,
                "Fazer Quizzes",
                "Responder quizzes já criados é ilimitado"
            ),
            FreeAction(
                Icons.Default.Edit,
                "Editar Conteúdo",
                "Editar flashcards e organizar seus decks não gasta limite"
            )
        )
    }

    LaunchedEffect(Unit) {
        viewModel.fetchGenerationLimit()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Consumo de IA",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        GradientBackgroundScreen(isDarkTheme = isDarkTheme) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(
                    start = 20.dp,
                    end = 20.dp,
                    top = 12.dp,
                    bottom = 24.dp
                ),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Header com ícone
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(yellowPrimary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Memory,
                                contentDescription = null,
                                modifier = Modifier.size(26.dp),
                                tint = yellowPrimary
                            )
                        }
                        Column {
                            Text(
                                "Consumo de IA",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 20.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                "Entenda como funcionam as gerações",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Card de Status Atual
                item {
                    StatusCard(
                        limitInfo = limitInfo,
                        isLoading = isLoading,
                        yellowPrimary = yellowPrimary,
                        redWarning = redWarning,
                        orangeWarning = orangeWarning,
                        cyanSecondary = cyanSecondary
                    )
                }

                // Card "Como Funciona"
                item {
                    HowItWorksCard(
                        useCases = useCases,
                        selectedUseCase = selectedUseCase,
                        onUseCaseSelected = { selectedUseCase = it },
                        yellowPrimary = yellowPrimary,
                        cyanSecondary = cyanSecondary
                    )
                }

                // Card "Ações Gratuitas"
                item {
                    FreeActionsCard(
                        freeActions = freeActions,
                        greenSuccess = greenSuccess
                    )
                }

                // Card de Dicas
                item {
                    TipsCard(cyanSecondary = cyanSecondary)
                }
            }
        }
    }
}

@Composable
fun StatusCard(
    limitInfo: com.example.flashify.model.data.GenerationLimitResponse?,
    isLoading: Boolean,
    yellowPrimary: Color,
    redWarning: Color,
    orangeWarning: Color,
    cyanSecondary: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                "Seu Consumo Atual",
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(32.dp),
                        color = yellowPrimary
                    )
                }
            } else if (limitInfo != null) {
                val percentage = (limitInfo.used.toFloat() / limitInfo.limit) * 100
                val progressColor = when {
                    percentage >= 90 -> redWarning
                    percentage >= 70 -> orangeWarning
                    else -> cyanSecondary
                }

                // Contador de gerações
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Gerações utilizadas hoje",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "${limitInfo.used} de ${limitInfo.limit}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = progressColor
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Barra de progresso
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(percentage / 100f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(4.dp))
                            .background(progressColor)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Info de reset
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                "Próximo reset",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            "em ${String.format(Locale.getDefault(), "%.1f", limitInfo.hoursUntilReset)}h",
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Alerta de limite quase atingido
                if (percentage >= 90) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = redWarning.copy(alpha = 0.1f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, redWarning.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = redWarning
                            )
                            Column {
                                Text(
                                    "Limite quase atingido!",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = redWarning
                                )
                                Text(
                                    "Você tem apenas ${limitInfo.remaining} geração(ões) restante(s). Continue estudando seus decks existentes!",
                                    fontSize = 12.sp,
                                    color = redWarning.copy(alpha = 0.9f),
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }
            } else {
                Text(
                    "Não foi possível carregar as informações",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp)
                )
            }
        }
    }
}

@Composable
fun HowItWorksCard(
    useCases: List<UseCase>,
    selectedUseCase: String?,
    onUseCaseSelected: (String?) -> Unit,
    yellowPrimary: Color,
    cyanSecondary: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                "Como Funciona?",
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "Cada interação com nossa IA generativa consome 1 geração do seu limite diário",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Info sobre limite diário
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = yellowPrimary.copy(alpha = 0.1f),
                border = androidx.compose.foundation.BorderStroke(1.dp, yellowPrimary.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("💡", fontSize = 18.sp)
                    Text(
                        "Limite Diário: Você pode fazer até 10 gerações por dia com nossa IA. Este limite é resetado automaticamente a cada 24 horas.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "O que conta como uma geração?",
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "Clique em cada item para ver mais detalhes:",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            useCases.forEach { useCase ->
                UseCaseItem(
                    useCase = useCase,
                    isSelected = selectedUseCase == useCase.id,
                    onSelected = {
                        onUseCaseSelected(if (selectedUseCase == useCase.id) null else useCase.id)
                    },
                    accentColor = cyanSecondary
                )
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}

@Composable
fun UseCaseItem(
    useCase: UseCase,
    isSelected: Boolean,
    onSelected: () -> Unit,
    accentColor: Color
) {
    val rotationAngle by animateFloatAsState(
        targetValue = if (isSelected) 90f else 0f,
        animationSpec = tween(300),
        label = "rotation"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        border = if (isSelected) {
            androidx.compose.foundation.BorderStroke(1.5.dp, accentColor)
        } else null,
        onClick = onSelected
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = useCase.icon,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = accentColor
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        useCase.title,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        useCase.description,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp
                    )
                }

                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = accentColor.copy(alpha = 0.12f)
                    ) {
                        Text(
                            useCase.impact,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = accentColor
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        modifier = Modifier
                            .size(18.dp)
                            .rotate(rotationAngle),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            AnimatedVisibility(
                visible = isSelected,
                enter = expandVertically(tween(300)) + fadeIn(tween(300)),
                exit = shrinkVertically(tween(300)) + fadeOut(tween(300))
            ) {
                Column(modifier = Modifier.padding(start = 12.dp, end = 12.dp, bottom = 12.dp)) {
                    Divider(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = accentColor.copy(alpha = 0.08f)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                "Exemplo prático:",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                useCase.example,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FreeActionsCard(
    freeActions: List<FreeAction>,
    greenSuccess: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                "O que NÃO consome gerações?",
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "Estas ações são ilimitadas e podem ser feitas quantas vezes quiser:",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            freeActions.forEach { action ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = greenSuccess.copy(alpha = 0.08f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, greenSuccess.copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(greenSuccess.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = action.icon,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = greenSuccess
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                action.title,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                action.description,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}

@Composable
fun TipsCard(cyanSecondary: Color) {
    val tips = listOf(
        "Planeje suas gerações" to "Crie decks completos de uma vez. É melhor fazer 1 deck com 20 flashcards do que 2 decks com 10 cada (economiza 1 geração).",
        "Priorize o estudo" to "Estudar com os flashcards e fazer quizzes é ilimitado. Foque em revisar antes de criar novos conteúdos.",
        "Organize-se" to "Use pastas para organizar seus decks. Editar e reorganizar não consome gerações!"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                "Dicas para Aproveitar Melhor",
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            tips.forEach { (title, description) ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(cyanSecondary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = cyanSecondary
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                title,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                description,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}
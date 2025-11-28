package com.example.flashify.view.ui.screen.principal

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.flashify.view.ui.components.GradientBackgroundScreen

data class ManualStep(
    val stepNumber: Int,
    val title: String,
    val description: String,
    val icon: ImageVector? = null
)

data class ManualSection(
    val title: String,
    val icon: ImageVector,
    val steps: List<ManualStep>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaManualUsuario(navController: NavController) {
    val isDarkTheme = isSystemInDarkTheme()

    // Cores da aplicação - APENAS estas duas
    val yellowPrimary = Color(0xFFFFD600)
    val cyanSecondary = if (isDarkTheme) Color(0xFF6BDEF3) else Color(0xFF00BCD4)

    val manualSections = remember {
        listOf(
            ManualSection(
                title = "Criando seu Primeiro Deck",
                icon = Icons.Default.Add,
                steps = listOf(
                    ManualStep(1, "Clique no botão \"+\"", "Você vai clicar no botão de \"+\" na barra de navegação.", Icons.Default.Add),
                    ManualStep(2, "Nomeie seu deck", "Você vai nomear o seu deck, como quiser!", Icons.Default.Edit),
                    ManualStep(3, "Escolha como fornecer seu material de estudo", "Escolha como fornecer o seu material de estudo.", Icons.Default.Description),
                    ManualStep(4, "Opção de incluir quiz ou não", "Você tem a opção de incluir quiz ou não.", Icons.Default.QuestionAnswer),
                    ManualStep(5, "Escolha a quantidade e nível de dificuldade", "Ao incluir o quiz, você deve escolher a quantidade de perguntas e o nível de dificuldade.", Icons.Default.TrendingUp)
                )
            ),
            ManualSection(
                title = "Estudando com Flashcards",
                icon = Icons.Default.Style,
                steps = listOf(
                    ManualStep(1, "Selecione um deck", "Na tela principal, clique no deck que você deseja estudar.", Icons.Default.TouchApp),
                    ManualStep(2, "Escolha o modo de estudo", "Selecione entre estudar com flashcards ou fazer um quiz.", Icons.Default.SelectAll),
                    ManualStep(3, "Visualize o flashcard", "Leia a pergunta na frente do card e tente lembrar a resposta.", Icons.Default.Visibility),
                    ManualStep(4, "Revele a resposta", "Clique no card para virar e ver a resposta no verso.", Icons.Default.FlipToBack),
                    ManualStep(5, "Avalie seu conhecimento", "Indique se você acertou, errou ou teve dúvidas para acompanhar seu progresso.", Icons.Default.CheckCircle)
                )
            ),
            ManualSection(
                title = "Fazendo Quizzes",
                icon = Icons.Default.Quiz,
                steps = listOf(
                    ManualStep(1, "Acesse o quiz", "Selecione um deck que possua quiz e escolha a opção \"Fazer Quiz\".", Icons.Default.PlayArrow),
                    ManualStep(2, "Leia a pergunta", "Cada questão apresenta uma pergunta e múltiplas alternativas.", Icons.Default.Help),
                    ManualStep(3, "Selecione sua resposta", "Escolha a alternativa que você considera correta.", Icons.Default.RadioButtonChecked),
                    ManualStep(4, "Veja o resultado", "O app mostrará se você acertou ou errou, com explicação.", Icons.Default.Info),
                    ManualStep(5, "Complete o quiz", "Responda todas as perguntas e veja sua pontuação final.", Icons.Default.EmojiEvents)
                )
            ),
            ManualSection(
                title = "Organizando sua Biblioteca",
                icon = Icons.Default.Folder,
                steps = listOf(
                    ManualStep(1, "Acesse a Biblioteca", "Clique no ícone de biblioteca na barra de navegação.", Icons.Default.LibraryBooks),
                    ManualStep(2, "Crie uma pasta", "Toque no botão \"+\" para criar uma nova pasta de organização.", Icons.Default.CreateNewFolder),
                    ManualStep(3, "Nomeie a pasta", "Escolha um nome descritivo para sua pasta (ex: \"Matemática\").", Icons.Default.DriveFileRenameOutline),
                    ManualStep(4, "Mova decks para pastas", "Arraste decks para as pastas ou use o menu de opções.", Icons.Default.DriveFileMove),
                    ManualStep(5, "Gerencie suas pastas", "Renomeie, delete ou reorganize suas pastas conforme necessário.", Icons.Default.Settings)
                )
            ),
            ManualSection(
                title = "Acompanhando seu Progresso",
                icon = Icons.Default.TrendingUp,
                steps = listOf(
                    ManualStep(1, "Acesse a tela de Progresso", "Clique no ícone de gráfico na barra de navegação.", Icons.Default.BarChart),
                    ManualStep(2, "Visualize estatísticas", "Veja quantos cards você estudou, sua precisão e sequência de dias.", Icons.Default.Analytics),
                    ManualStep(3, "Acompanhe sua evolução", "O gráfico semanal mostra sua atividade de estudo ao longo dos dias.", Icons.Default.Timeline),
                    ManualStep(4, "Desbloqueie conquistas", "Complete desafios para ganhar conquistas e manter-se motivado.", Icons.Default.EmojiEvents),
                    ManualStep(5, "Mantenha a consistência", "Estude regularmente para aumentar sua sequência de dias e melhorar seus resultados.", Icons.Default.CalendarToday)
                )
            )
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Manual do Usuário",
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
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Header introdutório
                item {
                    WelcomeHeader(yellowPrimary)
                }

                // Seções do manual
                itemsIndexed(manualSections) { index, section ->
                    val isEven = index % 2 == 0
                    ManualSectionCard(
                        section = section,
                        accentColor = if (isEven) yellowPrimary else cyanSecondary
                    )
                }

                // Card de ajuda
                item {
                    SupportCard(cyanSecondary)
                }
            }
        }
    }
}

@Composable
fun WelcomeHeader(yellowPrimary: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(yellowPrimary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MenuBook,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = Color.Black
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Bem-vindo ao Manual",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 24.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Aprenda a usar todas as funcionalidades do Flashify de forma rápida e eficiente",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

@Composable
fun ManualSectionCard(
    section: ManualSection,
    accentColor: Color
) {
    var expanded by remember { mutableStateOf(false) }
    val rotationAngle by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(300),
        label = "rotation"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header clicável
            Surface(
                onClick = { expanded = !expanded },
                color = Color.Transparent
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Ícone da seção
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(accentColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = section.icon,
                            contentDescription = null,
                            modifier = Modifier.size(26.dp),
                            tint = accentColor
                        )
                    }

                    // Título e contador
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            section.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 22.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = accentColor.copy(alpha = 0.12f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ListAlt,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = accentColor
                                )
                                Text(
                                    "${section.steps.size} passos",
                                    fontSize = 12.sp,
                                    color = accentColor,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    // Botão de expandir
                    Surface(
                        modifier = Modifier.size(36.dp),
                        shape = CircleShape,
                        color = accentColor.copy(alpha = 0.12f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.ExpandMore,
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier
                                    .size(20.dp)
                                    .rotate(rotationAngle)
                            )
                        }
                    }
                }
            }

            // Conteúdo expandível
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(animationSpec = tween(300)) + fadeIn(tween(300)),
                exit = shrinkVertically(animationSpec = tween(300)) + fadeOut(tween(300))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 20.dp, bottom = 20.dp)
                ) {
                    Divider(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                        thickness = 1.dp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    section.steps.forEachIndexed { index, step ->
                        StepItem(
                            step = step,
                            isLast = index == section.steps.lastIndex,
                            accentColor = accentColor
                        )
                        if (index != section.steps.lastIndex) {
                            Spacer(modifier = Modifier.height(18.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StepItem(
    step: ManualStep,
    isLast: Boolean,
    accentColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Timeline vertical
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Círculo do passo
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(accentColor),
                contentAlignment = Alignment.Center
            ) {
                if (step.icon != null) {
                    Icon(
                        imageVector = step.icon,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = Color.Black
                    )
                } else {
                    Text(
                        text = step.stepNumber.toString(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.Black
                    )
                }
            }

            // Linha conectora
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(42.dp)
                        .background(accentColor.copy(alpha = 0.25f))
                )
            }
        }

        // Conteúdo do passo
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(top = 2.dp)
        ) {
            Text(
                text = step.title,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 20.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = step.description,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
fun SupportCard(cyanSecondary: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(cyanSecondary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.HelpOutline,
                    contentDescription = null,
                    modifier = Modifier.size(30.dp),
                    tint = cyanSecondary
                )
            }

            Text(
                "Precisa de mais ajuda?",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                "Visite nossa Central de Ajuda para mais informações e suporte técnico completo",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
        }
    }
}
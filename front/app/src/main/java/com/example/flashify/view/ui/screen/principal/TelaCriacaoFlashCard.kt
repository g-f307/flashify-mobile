package com.example.flashify.view.ui.screen.principal

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.flashify.model.data.NavItem
import com.example.flashify.model.manager.ThemeManager
import com.example.flashify.model.util.*
import com.example.flashify.view.ui.components.GenerationLimitBar
import com.example.flashify.view.ui.components.GradientBackgroundScreen
import com.example.flashify.view.ui.components.NavegacaoBotaoAbaixo
import com.example.flashify.viewmodel.DeckCreationState
import com.example.flashify.viewmodel.DeckViewModel
import com.example.flashify.viewmodel.GenerationLimitState
import kotlin.math.roundToInt

@Composable
fun TelaCriacaoFlashCard(
    navController: NavController,
    viewModel: DeckViewModel = hiltViewModel(),
    folderId: Int? = null
) {
    // --- LÓGICA DO TEMA ---
    val context = LocalContext.current
    val themeManager = remember { ThemeManager(context) }
    val isDarkTheme by themeManager.isDarkTheme.collectAsState(initial = isSystemInDarkTheme())

    // Cores do Tema (Ajustadas para contraste)
    val primaryColor = MaterialTheme.colorScheme.primary
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val outlineColor = MaterialTheme.colorScheme.outline

    var currentStep by remember { mutableStateOf(1) }
    var deckName by remember { mutableStateOf("") }
    var contentText by remember { mutableStateOf("") }
    var flashcardQuantity by remember { mutableStateOf(10f) }
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
    var creationMode by remember { mutableStateOf("text") }
    var includeQuiz by remember { mutableStateOf(false) }

    // Estados para customização de Quiz
    var quizQuantity by remember { mutableStateOf(5f) }
    var difficulty by remember { mutableStateOf("Médio") }

    val creationState by viewModel.deckCreationState.collectAsStateWithLifecycle()
    val generationLimitState by viewModel.generationLimitState.collectAsStateWithLifecycle()

    // Verificar limite ao entrar na tela
    LaunchedEffect(Unit) {
        viewModel.checkGenerationLimit()
    }

    LaunchedEffect(creationState) {
        when (val state = creationState) {
            is DeckCreationState.Success -> {
                navController.navigate(
                    "$CONTENT_LOADER_ROUTE/${state.deck.id}/${state.deck.generatesFlashcards}/${state.deck.generatesQuizzes}"
                ) {
                    popUpTo(MAIN_SCREEN_ROUTE)
                }
                viewModel.resetCreationState()
            }
            is DeckCreationState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                viewModel.resetCreationState()
            }
            else -> {}
        }
    }

    val isNextEnabled = when (currentStep) {
        1 -> deckName.isNotBlank()
        2 -> (creationMode == "text" && contentText.isNotBlank()) ||
                (creationMode == "file" && selectedFileUri != null)
        else -> true
    }

    val isLimitReached = remember(generationLimitState) {
        if (generationLimitState is GenerationLimitState.Success) {
            val info = (generationLimitState as GenerationLimitState.Success).info
            info.used >= info.limit
        } else {
            false
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = {
            val navItems = listOf(
                NavItem("Início", Icons.Default.Home),
                NavItem("Biblioteca", Icons.Default.FormatListBulleted),
                NavItem("Criar", Icons.Default.Add),
                NavItem("Progresso", Icons.Default.TrendingUp),
                NavItem("Config", Icons.Default.Settings)
            )
            val selectedItemIndex = 2

            Column(modifier = Modifier.navigationBarsPadding()) {
                NavegacaoBotaoAbaixo(
                    navItems = navItems,
                    selectedItem = selectedItemIndex,
                    onItemSelected = { clickedIndex ->
                        when (navItems[clickedIndex].label) {
                            "Início" -> navController.navigate(MAIN_SCREEN_ROUTE) {
                                popUpTo(MAIN_SCREEN_ROUTE) { inclusive = true }
                            }
                            "Criar" -> { /* Já está aqui */ }
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
        GradientBackgroundScreen(isDarkTheme = isDarkTheme) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(20.dp))

                // Cabeçalho com ícone (Borda reforçada)
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    primaryColor.copy(alpha = 0.2f),
                                    primaryColor.copy(alpha = 0.1f)
                                )
                            ),
                            shape = CircleShape
                        )
                        // Borda sólida para contraste no modo claro
                        .border(1.5.dp, primaryColor.copy(alpha = 0.6f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = null,
                        tint = primaryColor,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Criar Novo Deck",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Stepper visual
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    (1..4).forEach { step ->
                        StepIndicator(
                            stepNumber = step,
                            currentStep = currentStep,
                            isCompleted = step < currentStep
                        )
                        if (step < 4) {
                            Box(
                                modifier = Modifier
                                    .width(30.dp)
                                    .height(3.dp) // Linha um pouco mais grossa
                                    .background(
                                        if (step < currentStep) primaryColor
                                        else outlineColor.copy(alpha = 0.4f), // Mais visível
                                        RoundedCornerShape(1.dp)
                                    )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = getStepTitle(currentStep),
                    color = onSurfaceVariant,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Conteúdo baseado no passo
                when (currentStep) {
                    1 -> StepDeckName(
                        deckName = deckName,
                        onDeckNameChange = { deckName = it }
                    )
                    2 -> StepContent(
                        creationMode = creationMode,
                        onModeChange = { creationMode = it },
                        contentText = contentText,
                        onContentTextChange = { contentText = it },
                        selectedFileUri = selectedFileUri,
                        onFileSelected = { selectedFileUri = it },
                        context = context
                    )
                    3 -> StepQuantity(
                        flashcardQuantity = flashcardQuantity,
                        onQuantityChange = { flashcardQuantity = it }
                    )
                    4 -> {
                        StepOptions(
                            includeQuiz = includeQuiz,
                            onIncludeQuizChange = { includeQuiz = it },
                            quizQuantity = quizQuantity,
                            onQuizQuantityChange = { quizQuantity = it },
                            difficulty = difficulty,
                            onDifficultyChange = { difficulty = it }
                        )

                        Spacer(modifier = Modifier.height(24.dp))
                        if (generationLimitState is GenerationLimitState.Success) {
                            val info = (generationLimitState as GenerationLimitState.Success).info
                            GenerationLimitBar(
                                used = info.used,
                                limit = info.limit,
                                hoursUntilReset = info.hoursUntilReset
                            )
                        } else if (generationLimitState is GenerationLimitState.Loading) {
                            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = primaryColor)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Botões de Navegação
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (currentStep > 1) {
                        OutlinedButton(
                            onClick = { currentStep-- },
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.5.dp, primaryColor), // Borda mais grossa
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = primaryColor
                            )
                        ) {
                            Icon(
                                Icons.Default.ArrowBack,
                                null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Voltar",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    }

                    Button(
                        onClick = {
                            if (currentStep < 4) {
                                currentStep++
                            } else {
                                if (creationMode == "text") {
                                    viewModel.createDeckFromText(
                                        title = deckName,
                                        text = contentText,
                                        quantity = flashcardQuantity.roundToInt(),
                                        generateQuiz = includeQuiz,
                                        numQuestions = quizQuantity.roundToInt(),
                                        folderId = folderId
                                    )
                                } else {
                                    selectedFileUri?.let { uri ->
                                        viewModel.createDeckFromFile(
                                            title = deckName,
                                            fileUri = uri,
                                            quantity = flashcardQuantity.roundToInt(),
                                            generateQuiz = includeQuiz,
                                            numQuestions = quizQuantity.roundToInt(),
                                            folderId = folderId
                                        )
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 2.dp,
                            pressedElevation = 4.dp
                        ),
                        enabled = isNextEnabled && creationState !is DeckCreationState.Loading && (!isLimitReached || currentStep < 4),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isLimitReached && currentStep == 4) Color.Gray else primaryColor,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        if (creationState is DeckCreationState.Loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.5.dp
                            )
                        } else {
                            Text(
                                if (currentStep < 4) "Próximo" else "Criar Deck",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Spacer(Modifier.width(8.dp))
                            Icon(
                                if (currentStep < 4) Icons.Default.ArrowForward else Icons.Default.Check,
                                null,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun StepIndicator(
    stepNumber: Int,
    currentStep: Int,
    isCompleted: Boolean
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val outlineColor = MaterialTheme.colorScheme.outline

    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(
                when {
                    isCompleted -> primaryColor
                    stepNumber == currentStep -> primaryColor.copy(alpha = 0.15f)
                    else -> Color.Transparent
                }
            )
            .border(
                width = if (stepNumber == currentStep) 2.dp else 1.5.dp, // Bordas mais grossas
                color = if (isCompleted || stepNumber == currentStep) primaryColor else outlineColor.copy(alpha = 0.5f),
                shape = CircleShape
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
        } else {
            Text(
                stepNumber.toString(),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = if (stepNumber == currentStep) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

fun getStepTitle(step: Int): String {
    return when (step) {
        1 -> "Nomeie seu deck"
        2 -> "Adicione o conteúdo"
        3 -> "Defina a quantidade"
        4 -> "Configure as opções"
        else -> ""
    }
}

@Composable
fun StepDeckName(
    deckName: String,
    onDeckNameChange: (String) -> Unit
) {
    val primaryColor = MaterialTheme.colorScheme.primary

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        // ✅ Borda ACENTUADA para definição no modo claro
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                "Nome do Deck",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Escolha um nome descritivo para seus estudos",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(24.dp))
            OutlinedTextField(
                value = deckName,
                onValueChange = onDeckNameChange,
                label = { Text("Ex: Biologia - Células") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = primaryColor,
                    focusedLabelColor = primaryColor,
                    cursorColor = primaryColor,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                )
            )
        }
    }
}

@Composable
fun StepContent(
    creationMode: String,
    onModeChange: (String) -> Unit,
    contentText: String,
    onContentTextChange: (String) -> Unit,
    selectedFileUri: Uri?,
    onFileSelected: (Uri?) -> Unit,
    context: Context
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        onFileSelected(uri)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        // ✅ Borda ACENTUADA
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                "Conteúdo",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Escolha como fornecer o conteúdo para a IA",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ModeButton(
                    label = "Texto",
                    icon = Icons.Default.TextFields,
                    selected = creationMode == "text",
                    onClick = { onModeChange("text") },
                    modifier = Modifier.weight(1f)
                )
                ModeButton(
                    label = "Arquivo",
                    icon = Icons.Default.UploadFile,
                    selected = creationMode == "file",
                    onClick = { onModeChange("file") },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(24.dp))

            if (creationMode == "text") {
                OutlinedTextField(
                    value = contentText,
                    onValueChange = onContentTextChange,
                    label = { Text("Cole ou digite o texto") },
                    placeholder = { Text("Insira o conteúdo aqui...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                    maxLines = 12,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = primaryColor,
                        focusedLabelColor = primaryColor,
                        cursorColor = primaryColor,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    )
                )
            } else {
                if (selectedFileUri != null) {
                    // Card de Arquivo Selecionado
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = primaryColor.copy(alpha = 0.05f),
                        border = BorderStroke(1.5.dp, primaryColor.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Description,
                                contentDescription = null,
                                tint = primaryColor,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Arquivo selecionado",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    selectedFileUri.lastPathSegment ?: "arquivo.pdf",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            IconButton(onClick = { onFileSelected(null) }) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Remover",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                }

                OutlinedButton(
                    onClick = { filePickerLauncher.launch("*/*") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, primaryColor),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = primaryColor
                    )
                ) {
                    Icon(Icons.Default.UploadFile, null)
                    Spacer(Modifier.width(12.dp))
                    Text(if (selectedFileUri == null) "Selecionar Arquivo" else "Trocar Arquivo")
                }
            }
        }
    }
}

@Composable
fun StepQuantity(
    flashcardQuantity: Float,
    onQuantityChange: (Float) -> Unit
) {
    val primaryColor = MaterialTheme.colorScheme.primary

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        // ✅ Borda ACENTUADA
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                "Quantidade",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Quantos flashcards deseja gerar?",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(32.dp))

            // Display Grande do Número
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "${flashcardQuantity.roundToInt()}",
                    fontSize = 64.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = primaryColor
                )
            }

            Spacer(Modifier.height(24.dp))

            Slider(
                value = flashcardQuantity,
                onValueChange = onQuantityChange,
                valueRange = 5f..20f,
                steps = 14,
                modifier = Modifier.fillMaxWidth(),
                colors = SliderDefaults.colors(
                    thumbColor = primaryColor,
                    activeTrackColor = primaryColor,
                    inactiveTrackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("5", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                Text("20", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun StepOptions(
    includeQuiz: Boolean,
    onIncludeQuizChange: (Boolean) -> Unit,
    quizQuantity: Float,
    onQuizQuantityChange: (Float) -> Unit,
    difficulty: String,
    onDifficultyChange: (String) -> Unit
) {
    val primaryColor = MaterialTheme.colorScheme.primary

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        // ✅ Borda ACENTUADA
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                "Opções Adicionais",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(24.dp))

            // Card de Seleção do Quiz
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (includeQuiz) primaryColor.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface,
                border = BorderStroke(
                    width = if (includeQuiz) 1.5.dp else 1.dp,
                    color = if (includeQuiz) primaryColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                ),
                modifier = Modifier.clickable { onIncludeQuizChange(!includeQuiz) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Quiz,
                        contentDescription = null,
                        tint = if (includeQuiz) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Incluir Quiz",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            "Gerar perguntas extras",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = includeQuiz,
                        onCheckedChange = onIncludeQuizChange,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                            checkedTrackColor = primaryColor,
                            uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                            uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                }
            }

            if (includeQuiz) {
                Spacer(Modifier.height(24.dp))

                // Controlo de Quantidade
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Perguntas: ${quizQuantity.roundToInt()}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }
                Slider(
                    value = quizQuantity,
                    onValueChange = onQuizQuantityChange,
                    valueRange = 3f..15f,
                    steps = 11,
                    colors = SliderDefaults.colors(thumbColor = primaryColor, activeTrackColor = primaryColor)
                )

                Spacer(Modifier.height(16.dp))

                // Nível de Dificuldade
                Text("Dificuldade", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DifficultyButton(
                        label = "Fácil",
                        icon = Icons.Default.SentimentSatisfied,
                        selected = difficulty == "Fácil",
                        onClick = { onDifficultyChange("Fácil") },
                        modifier = Modifier.weight(1f)
                    )
                    DifficultyButton(
                        label = "Médio",
                        icon = Icons.Default.RemoveCircleOutline,
                        selected = difficulty == "Médio",
                        onClick = { onDifficultyChange("Médio") },
                        modifier = Modifier.weight(1f)
                    )
                    DifficultyButton(
                        label = "Difícil",
                        icon = Icons.Default.Whatshot,
                        selected = difficulty == "Difícil",
                        onClick = { onDifficultyChange("Difícil") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun ModeButton(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary

    Surface(
        onClick = onClick,
        modifier = modifier.height(100.dp),
        shape = RoundedCornerShape(16.dp),
        color = if (selected) primaryColor.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp, // Borda mais grossa se selecionado
            color = if (selected) primaryColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (selected) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(28.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                label,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun DifficultyButton(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary

    Surface(
        onClick = onClick,
        modifier = modifier.height(70.dp),
        shape = RoundedCornerShape(12.dp),
        color = if (selected) primaryColor.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp, // Borda mais grossa se selecionado
            color = if (selected) primaryColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (selected) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                label,
                fontSize = 12.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
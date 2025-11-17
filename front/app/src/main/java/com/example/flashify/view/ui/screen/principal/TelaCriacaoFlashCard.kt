package com.example.flashify.view.ui.screen.principal

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.flashify.model.data.NavItem
import com.example.flashify.model.util.*
import com.example.flashify.view.ui.components.GradientBackgroundScreen
import com.example.flashify.view.ui.components.NavegacaoBotaoAbaixo
import com.example.flashify.view.ui.theme.TextSecondary
import com.example.flashify.view.ui.theme.YellowAccent
import com.example.flashify.viewmodel.DeckCreationState
import com.example.flashify.viewmodel.DeckViewModel
import kotlin.math.roundToInt

@Composable
fun TelaCriacaoFlashCard(
    navController: NavController,
    viewModel: DeckViewModel = viewModel(),
    folderId: Int? = null
) {
    var currentStep by remember { mutableStateOf(1) }
    var deckName by remember { mutableStateOf("") }
    var contentText by remember { mutableStateOf("") }
    var flashcardQuantity by remember { mutableStateOf(10f) }
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
    var creationMode by remember { mutableStateOf("text") }
    var includeQuiz by remember { mutableStateOf(false) }

    // ✅ NOVOS ESTADOS PARA CUSTOMIZAÇÃO DE QUIZ
    var quizQuantity by remember { mutableStateOf(5f) }
    var difficulty by remember { mutableStateOf("Médio") }

    val creationState by viewModel.deckCreationState.collectAsStateWithLifecycle()
    val context = LocalContext.current

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

            BottomAppBar(
                containerColor = Color.Transparent,
                tonalElevation = 0.dp,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                NavegacaoBotaoAbaixo(
                    modifier = Modifier.clip(RoundedCornerShape(50)),
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
        GradientBackgroundScreen {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(20.dp))

                // Cabeçalho com ícone
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .shadow(8.dp, CircleShape)
                        .background(YellowAccent.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = null,
                        tint = YellowAccent,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Criar Novo Deck",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.3.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Stepper visual
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                                    .width(24.dp)
                                    .height(2.dp)
                                    .background(
                                        if (step < currentStep) YellowAccent
                                        else Color.Gray.copy(alpha = 0.3f),
                                        RoundedCornerShape(1.dp)
                                    )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = getStepTitle(currentStep),
                    color = TextSecondary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.2.sp
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Progress bar
                LinearProgressIndicator(
                    progress = { currentStep / 4f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = YellowAccent,
                    trackColor = Color.Gray.copy(alpha = 0.2f),
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Content based on current step
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
                    4 -> StepOptions(
                        includeQuiz = includeQuiz,
                        onIncludeQuizChange = { includeQuiz = it },
                        quizQuantity = quizQuantity,
                        onQuizQuantityChange = { quizQuantity = it },
                        difficulty = difficulty,
                        onDifficultyChange = { difficulty = it }
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Navigation buttons
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
                            border = BorderStroke(1.5.dp, YellowAccent)
                        ) {
                            Icon(
                                Icons.Default.ArrowBack,
                                null,
                                tint = YellowAccent,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Voltar",
                                color = YellowAccent,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp,
                                letterSpacing = 0.3.sp
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
                            .height(52.dp)
                            .shadow(6.dp, RoundedCornerShape(14.dp)),
                        enabled = isNextEnabled && creationState !is DeckCreationState.Loading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = YellowAccent,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        if (creationState is DeckCreationState.Loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = Color.Black,
                                strokeWidth = 2.5.dp
                            )
                        } else {
                            Text(
                                if (currentStep < 4) "Próximo" else "Criar Deck",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                letterSpacing = 0.3.sp
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
    Box(
        modifier = Modifier
            .size(36.dp)
            .background(
                when {
                    isCompleted -> YellowAccent
                    stepNumber == currentStep -> YellowAccent.copy(alpha = 0.3f)
                    else -> Color.Gray.copy(alpha = 0.2f)
                },
                CircleShape
            )
            .then(
                if (stepNumber == currentStep) {
                    Modifier.shadow(4.dp, CircleShape)
                } else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isCompleted) {
            Icon(
                Icons.Default.Check,
                contentDescription = null,
                tint = Color.Black,
                modifier = Modifier.size(20.dp)
            )
        } else {
            Text(
                stepNumber.toString(),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = if (stepNumber == currentStep) YellowAccent else Color.Gray
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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(YellowAccent.copy(alpha = 0.15f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.DriveFileRenameOutline,
                    contentDescription = null,
                    tint = YellowAccent,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(Modifier.height(20.dp))
            Text(
                "Nome do Deck",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                letterSpacing = 0.3.sp
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Escolha um nome descritivo e memorável para seu deck de estudos",
                fontSize = 14.sp,
                color = TextSecondary,
                lineHeight = 20.sp,
                letterSpacing = 0.2.sp
            )
            Spacer(Modifier.height(24.dp))
            OutlinedTextField(
                value = deckName,
                onValueChange = onDeckNameChange,
                label = { Text("Nome do deck") },
                placeholder = { Text("Ex: Biologia - Células") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = YellowAccent,
                    focusedLabelColor = YellowAccent,
                    cursorColor = YellowAccent
                ),
                leadingIcon = {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = null,
                        tint = if (deckName.isNotEmpty()) YellowAccent else Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                }
            )

            if (deckName.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            YellowAccent.copy(alpha = 0.1f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(12.dp)
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = YellowAccent,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Ótimo nome! Continue para o próximo passo",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
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
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        onFileSelected(uri)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(YellowAccent.copy(alpha = 0.15f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Description,
                    contentDescription = null,
                    tint = YellowAccent,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(Modifier.height(20.dp))
            Text(
                "Conteúdo",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                letterSpacing = 0.3.sp
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Escolha como fornecer o conteúdo para gerar os flashcards",
                fontSize = 14.sp,
                color = TextSecondary,
                lineHeight = 20.sp,
                letterSpacing = 0.2.sp
            )
            Spacer(Modifier.height(24.dp))

            // Mode selection
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
                    placeholder = { Text("Insira o conteúdo para gerar flashcards...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                    maxLines = 12,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = YellowAccent,
                        focusedLabelColor = YellowAccent,
                        cursorColor = YellowAccent
                    )
                )
                if (contentText.isNotEmpty()) {
                    Spacer(Modifier.height(12.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "${contentText.length} caracteres • ${contentText.split("\\s+".toRegex()).size} palavras",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }
            } else {
                if (selectedFileUri != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = YellowAccent.copy(alpha = 0.1f)
                        ),
                        border = BorderStroke(1.dp, YellowAccent.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(YellowAccent.copy(alpha = 0.2f), RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Description,
                                    contentDescription = null,
                                    tint = YellowAccent,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Arquivo selecionado",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    selectedFileUri.lastPathSegment ?: "arquivo.pdf",
                                    fontSize = 13.sp,
                                    color = TextSecondary,
                                    maxLines = 1
                                )
                            }
                            IconButton(
                                onClick = { onFileSelected(null) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Remover",
                                    tint = Color.Gray,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                }

                Button(
                    onClick = { filePickerLauncher.launch("*/*") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(),
                    border = BorderStroke(1.5.dp, YellowAccent)
                ) {
                    Icon(
                        Icons.Default.UploadFile,
                        null,
                        tint = YellowAccent,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        if (selectedFileUri == null) "Selecionar Arquivo" else "Trocar Arquivo",
                        color = YellowAccent,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    )
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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(YellowAccent.copy(alpha = 0.15f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Numbers,
                    contentDescription = null,
                    tint = YellowAccent,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(Modifier.height(20.dp))
            Text(
                "Quantidade",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                letterSpacing = 0.3.sp
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Quantos flashcards deseja gerar a partir do conteúdo?",
                fontSize = 14.sp,
                color = TextSecondary,
                lineHeight = 20.sp,
                letterSpacing = 0.2.sp
            )
            Spacer(Modifier.height(36.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(YellowAccent.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "${flashcardQuantity.roundToInt()}",
                        fontSize = 56.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = YellowAccent,
                        letterSpacing = 1.sp
                    )
                    Text(
                        "flashcards",
                        fontSize = 16.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            Slider(
                value = flashcardQuantity,
                onValueChange = onQuantityChange,
                valueRange = 5f..20f,  // ✅ ALTERADO DE 50f PARA 20f
                steps = 14,             // ✅ ALTERADO DE 44 PARA 14 (5 a 20 = 15 valores, -1 = 14 steps)
                modifier = Modifier.fillMaxWidth(),
                colors = SliderDefaults.colors(
                    thumbColor = YellowAccent,
                    activeTrackColor = YellowAccent,
                    inactiveTrackColor = Color.Gray.copy(alpha = 0.2f)
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "5 mínimo",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    "20 máximo",  // ✅ ALTERADO DE "50 máximo" PARA "20 máximo"
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.background.copy(alpha = 0.5f),
                        RoundedCornerShape(10.dp)
                    )
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Lightbulb,
                    contentDescription = null,
                    tint = YellowAccent,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    "Recomendamos 10-20 flashcards para melhor retenção",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    lineHeight = 16.sp
                )
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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(YellowAccent.copy(alpha = 0.15f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Settings,
                    contentDescription = null,
                    tint = YellowAccent,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(Modifier.height(20.dp))
            Text(
                "Opções Adicionais",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                letterSpacing = 0.3.sp
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Configure recursos extras para enriquecer seu aprendizado",
                fontSize = 14.sp,
                color = TextSecondary,
                lineHeight = 20.sp,
                letterSpacing = 0.2.sp
            )
            Spacer(Modifier.height(24.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onIncludeQuizChange(!includeQuiz) },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (includeQuiz)
                        YellowAccent.copy(alpha = 0.15f)
                    else
                        MaterialTheme.colorScheme.background.copy(alpha = 0.5f)
                ),
                border = BorderStroke(
                    width = 2.dp,
                    color = if (includeQuiz) YellowAccent else Color.Gray.copy(alpha = 0.3f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(
                                if (includeQuiz)
                                    YellowAccent.copy(alpha = 0.25f)
                                else
                                    Color.Gray.copy(alpha = 0.15f),
                                RoundedCornerShape(14.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Quiz,
                            contentDescription = null,
                            tint = if (includeQuiz) YellowAccent else Color.Gray,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(Modifier.width(18.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Incluir Quiz",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            letterSpacing = 0.2.sp
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Gerar perguntas de múltipla escolha",
                            fontSize = 14.sp,
                            color = TextSecondary,
                            lineHeight = 18.sp
                        )
                    }

                    Spacer(Modifier.width(12.dp))

                    Switch(
                        checked = includeQuiz,
                        onCheckedChange = onIncludeQuizChange,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = YellowAccent,
                            uncheckedThumbColor = Color.Gray,
                            uncheckedTrackColor = Color.Gray.copy(alpha = 0.3f)
                        )
                    )
                }
            }

            // ✅ SEÇÃO DE CUSTOMIZAÇÃO DO QUIZ (APARECE QUANDO includeQuiz É TRUE)
            if (includeQuiz) {
                Spacer(Modifier.height(24.dp))

                // Quantidade de Perguntas
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            YellowAccent.copy(alpha = 0.05f),
                            RoundedCornerShape(16.dp)
                        )
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Numbers,
                                contentDescription = null,
                                tint = YellowAccent,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(
                                "Perguntas do Quiz",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            "${quizQuantity.roundToInt()}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = YellowAccent
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    Slider(
                        value = quizQuantity,
                        onValueChange = onQuizQuantityChange,
                        valueRange = 3f..15f,
                        steps = 11,
                        modifier = Modifier.fillMaxWidth(),
                        colors = SliderDefaults.colors(
                            thumbColor = YellowAccent,
                            activeTrackColor = YellowAccent,
                            inactiveTrackColor = Color.Gray.copy(alpha = 0.2f)
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "3 mínimo",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            "15 máximo",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Nível de Dificuldade
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            YellowAccent.copy(alpha = 0.05f),
                            RoundedCornerShape(16.dp)
                        )
                        .padding(20.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.TrendingUp,
                            contentDescription = null,
                            tint = YellowAccent,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            "Nível de Dificuldade",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
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

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            YellowAccent.copy(alpha = 0.1f),
                            RoundedCornerShape(10.dp)
                        )
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = YellowAccent,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        "Quiz configurado! ${quizQuantity.roundToInt()} perguntas em nível $difficulty",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 18.sp
                    )
                }
            } else {
                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Color(0xFF00BCD4).copy(alpha = 0.1f),
                            RoundedCornerShape(10.dp)
                        )
                        .padding(14.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = Color(0xFF00BCD4),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        "O quiz será gerado automaticamente com base no conteúdo dos flashcards, perfeito para testar seu conhecimento!",
                        fontSize = 13.sp,
                        color = TextSecondary,
                        lineHeight = 18.sp,
                        letterSpacing = 0.2.sp
                    )
                }
            }
        }
    }
}

@Composable
fun ModeButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .height(110.dp)
            .then(
                if (selected) {
                    Modifier.shadow(6.dp, RoundedCornerShape(14.dp))
                } else Modifier
            ),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected)
                YellowAccent.copy(alpha = 0.15f)
            else
                MaterialTheme.colorScheme.background.copy(alpha = 0.5f)
        ),
        border = BorderStroke(
            width = 2.dp,
            color = if (selected) YellowAccent else Color.Gray.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        if (selected) YellowAccent.copy(alpha = 0.25f)
                        else Color.Gray.copy(alpha = 0.15f),
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = if (selected) YellowAccent else Color.Gray,
                    modifier = Modifier.size(26.dp)
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(
                label,
                fontSize = 15.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
                color = if (selected)
                    MaterialTheme.colorScheme.onSurface
                else
                    TextSecondary,
                letterSpacing = 0.3.sp
            )
        }
    }
}

// ✅ NOVO COMPOSABLE PARA BOTÕES DE DIFICULDADE
@Composable
fun DifficultyButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(80.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected)
                YellowAccent.copy(alpha = 0.2f)
            else
                MaterialTheme.colorScheme.background.copy(alpha = 0.5f)
        ),
        border = BorderStroke(
            width = 2.dp,
            color = if (selected) YellowAccent else Color.Gray.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (selected) YellowAccent else Color.Gray,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.height(6.dp))
            Text(
                label,
                fontSize = 13.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected)
                    MaterialTheme.colorScheme.onSurface
                else
                    TextSecondary
            )
        }
    }
}
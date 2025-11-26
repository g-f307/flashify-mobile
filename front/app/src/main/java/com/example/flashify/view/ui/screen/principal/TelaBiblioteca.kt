package com.example.flashify.view.ui.screen.principal

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.flashify.model.data.DeckResponse
import com.example.flashify.model.data.FolderWithDocumentsResponse
import com.example.flashify.model.data.NavItem
import com.example.flashify.model.manager.ThemeManager
import com.example.flashify.model.util.*
import com.example.flashify.view.ui.components.GradientBackgroundScreen
import com.example.flashify.view.ui.components.NavegacaoBotaoAbaixo
import com.example.flashify.viewmodel.*
import kotlinx.coroutines.launch

@Composable
fun TelaPrincipalBiblioteca(
    navController: NavController,
    deckViewModel: DeckViewModel = hiltViewModel(),
    folderViewModel: FolderViewModel = hiltViewModel()
) {
    // --- LÓGICA DO TEMA ---
    val context = LocalContext.current
    val themeManager = remember { ThemeManager(context) }
    val isDarkTheme by themeManager.isDarkTheme.collectAsState(initial = isSystemInDarkTheme())
    val primaryColor = MaterialTheme.colorScheme.primary

    val deckActionState by deckViewModel.deckActionState.collectAsStateWithLifecycle()
    val libraryState by folderViewModel.libraryState.collectAsStateWithLifecycle()
    val folderOperationState by folderViewModel.operationState.collectAsStateWithLifecycle()

    // Estados para diálogos
    var showDeleteDeckDialog by remember { mutableStateOf(false) }
    var showEditDeckDialog by remember { mutableStateOf(false) }
    var showCreateFolderDialog by remember { mutableStateOf(false) }
    var showMoveToPastaDialog by remember { mutableStateOf(false) }
    var deckToActOn by remember { mutableStateOf<DeckResponse?>(null) }

    var showEditFolderDialog by remember { mutableStateOf(false) }
    var showDeleteFolderDialog by remember { mutableStateOf(false) }
    var folderToActOn by remember { mutableStateOf<FolderWithDocumentsResponse?>(null) }

    LaunchedEffect(Unit) {
        folderViewModel.loadLibrary()
    }

    // Feedback de ações
    LaunchedEffect(deckActionState) {
        if (deckActionState is DeckActionState.Success) {
            Toast.makeText(context, (deckActionState as DeckActionState.Success).message, Toast.LENGTH_SHORT).show()
            deckViewModel.resetActionState()
            showDeleteDeckDialog = false
            showEditDeckDialog = false
            showMoveToPastaDialog = false
            deckToActOn = null
            folderViewModel.loadLibrary()
        } else if (deckActionState is DeckActionState.Error) {
            Toast.makeText(context, (deckActionState as DeckActionState.Error).message, Toast.LENGTH_SHORT).show()
            deckViewModel.resetActionState()
        }
    }

    LaunchedEffect(folderOperationState) {
        if (folderOperationState is FolderOperationState.Success) {
            Toast.makeText(context, "Operação realizada com sucesso", Toast.LENGTH_SHORT).show()
            folderViewModel.resetOperationState()
            showCreateFolderDialog = false
            showEditFolderDialog = false
            showDeleteFolderDialog = false
            folderToActOn = null
            folderViewModel.loadLibrary()
        } else if (folderOperationState is FolderOperationState.Error) {
            Toast.makeText(context, (folderOperationState as FolderOperationState.Error).message, Toast.LENGTH_SHORT).show()
            folderViewModel.resetOperationState()
        }
    }

    // Diálogos
    if (showDeleteDeckDialog && deckToActOn != null) {
        DeleteDeckDialog(
            deckName = deckToActOn!!.filePath,
            onConfirm = { deckViewModel.deleteDeck(deckToActOn!!.id) },
            onDismiss = { showDeleteDeckDialog = false; deckToActOn = null },
            isLoading = deckActionState is DeckActionState.Loading
        )
    }

    if (showEditDeckDialog && deckToActOn != null) {
        EditDeckDialog(
            currentName = deckToActOn!!.filePath,
            onConfirm = { newName -> deckViewModel.renameDeck(deckToActOn!!.id, newName) },
            onDismiss = { showEditDeckDialog = false; deckToActOn = null },
            isLoading = deckActionState is DeckActionState.Loading
        )
    }

    if (showMoveToPastaDialog && deckToActOn != null && libraryState is LibraryState.Success) {
        MoveToPastaDialog(
            folders = (libraryState as LibraryState.Success).library.folders,
            currentFolderId = null,
            onConfirm = { folderId -> deckViewModel.moveDeckToFolder(deckToActOn!!.id, folderId) },
            onDismiss = { showMoveToPastaDialog = false; deckToActOn = null }
        )
    }

    if (showEditFolderDialog && folderToActOn != null) {
        EditFolderDialog(
            currentName = folderToActOn!!.name,
            onConfirm = { newName -> folderViewModel.updateFolder(folderToActOn!!.id, newName) },
            onDismiss = { showEditFolderDialog = false; folderToActOn = null },
            isLoading = folderOperationState is FolderOperationState.Loading
        )
    }

    if (showDeleteFolderDialog && folderToActOn != null) {
        DeleteFolderDialog(
            folderName = folderToActOn!!.name,
            deckCount = folderToActOn!!.documents.size,
            onConfirm = { deleteDecks -> folderViewModel.deleteFolder(folderToActOn!!.id, deleteDecks) },
            onDismiss = { showDeleteFolderDialog = false; folderToActOn = null },
            isLoading = folderOperationState is FolderOperationState.Loading
        )
    }

    if (showCreateFolderDialog) {
        CreateFolderDialog(
            onConfirm = { folderName -> folderViewModel.createFolder(folderName) },
            onCancel = { showCreateFolderDialog = false }
        )
    }

    var selectedItem by remember { mutableStateOf(1) }
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
                            "Início" -> navController.navigate(MAIN_SCREEN_ROUTE) { popUpTo(BIBLIOTECA_SCREEN_ROUTE) { inclusive = true } }
                            "Biblioteca" -> { /* Já está aqui */ }
                            "Criar" -> navController.navigate(CREATE_FLASHCARD_ROUTE) { popUpTo(BIBLIOTECA_SCREEN_ROUTE) }
                            "Progresso" -> navController.navigate(PROGRESSO_SCREEN_ROUTE) { popUpTo(BIBLIOTECA_SCREEN_ROUTE) }
                            "Config" -> navController.navigate(CONFIGURATION_SCREEN_ROUTE) { popUpTo(BIBLIOTECA_SCREEN_ROUTE) }
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        GradientBackgroundScreen(isDarkTheme = isDarkTheme) {
            when (val state = libraryState) {
                is LibraryState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = primaryColor)
                    }
                }
                is LibraryState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(state.message, color = MaterialTheme.colorScheme.error)
                            Button(
                                onClick = { folderViewModel.loadLibrary() },
                                colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                            ) {
                                Text("Tentar Novamente", color = MaterialTheme.colorScheme.onPrimary)
                            }
                        }
                    }
                }
                is LibraryState.Success -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Cabeçalho
                        item {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    "Biblioteca",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    "${state.library.folders.size} pastas · ${state.library.rootDocuments.size} decks",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Botões de Ação
                        item {
                            ActionButtons(
                                onCreateFolderClick = { showCreateFolderDialog = true },
                                onCreateDeckClick = { navController.navigate(CREATE_FLASHCARD_ROUTE) }
                            )
                        }

                        // Seção Pastas
                        if (state.library.folders.isNotEmpty()) {
                            item {
                                Text(
                                    "Pastas",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                            items(state.library.folders) { folder ->
                                FolderItemWithMenu(
                                    folder = folder,
                                    onClick = { navController.navigate("$DETALHE_PASTA_ROUTE/${folder.id}/${folder.name}") },
                                    onEditClick = { folderToActOn = folder; showEditFolderDialog = true },
                                    onDeleteClick = { folderToActOn = folder; showDeleteFolderDialog = true }
                                )
                            }
                        }

                        // Seção Decks
                        item {
                            Text(
                                "Seus Decks",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }

                        if (state.library.rootDocuments.isEmpty()) {
                            item { EmptyStateDecks { navController.navigate(CREATE_FLASHCARD_ROUTE) } }
                        } else {
                            items(state.library.rootDocuments) { deck ->
                                DeckItemCard(
                                    deck = deck,
                                    onStudyClick = { navController.navigate("$ESCOLHA_MODO_ESTUDO_ROUTE/${deck.id}") },
                                    onEditClick = { deckToActOn = deck; showEditDeckDialog = true },
                                    onDeleteClick = { deckToActOn = deck; showDeleteDeckDialog = true },
                                    onMoveToPastaClick = { deckToActOn = deck; showMoveToPastaDialog = true }
                                )
                            }
                        }

                        item { Spacer(Modifier.height(80.dp)) }
                    }
                }
                else -> {}
            }
        }
    }
}

@Composable
fun FolderItemWithMenu(
    folder: FolderWithDocumentsResponse,
    onClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    val primaryColor = MaterialTheme.colorScheme.primary

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(20.dp)), // Mesmo arredondamento do Deck
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Ícone de Pasta
                Box(
                    modifier = Modifier
                        .size(44.dp) // Tamanho consistente com Deck
                        .background(primaryColor.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Folder,
                        contentDescription = null,
                        tint = primaryColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        folder.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        "${folder.documents.size} decks",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Box {
                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        Icons.Default.MoreVert,
                        "Opções",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    DropdownMenuItem(
                        text = { Text("Renomear") },
                        onClick = { showMenu = false; onEditClick() },
                        leadingIcon = { Icon(Icons.Default.Edit, null) }
                    )
                    DropdownMenuItem(
                        text = { Text("Excluir", color = MaterialTheme.colorScheme.error) },
                        onClick = { showMenu = false; onDeleteClick() },
                        leadingIcon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) }
                    )
                }
            }
        }
    }
}

@Composable
fun DeckItemCard(
    deck: DeckResponse,
    onStudyClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onMoveToPastaClick: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    val primaryColor = MaterialTheme.colorScheme.primary
    val isDarkTheme = isSystemInDarkTheme()
    val quizColor = if (isDarkTheme) Color(0xFF00BCD4) else Color(0xFF0097A7)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        // Borda subtil para definição no modo claro
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Topo: Ícone, Badges e Menu
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

                // Badges e Menu (Agrupados)
                Row(verticalAlignment = Alignment.Top) {
                    // Badges (Empilhados verticalmente)
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        // Badge Flashcards
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
                                // Cor escura no modo claro para leitura
                                color = if (!isDarkTheme) Color(0xFFF57F17) else primaryColor
                            )
                        }

                        // Badge Quiz
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

                    // Menu de Opções (Três pontinhos)
                    Box {
                        IconButton(
                            onClick = { showMenu = true },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                Icons.Default.MoreVert,
                                "Opções",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                            containerColor = MaterialTheme.colorScheme.surface
                        ) {
                            DropdownMenuItem(
                                text = { Text("Editar") },
                                onClick = { showMenu = false; onEditClick() },
                                leadingIcon = { Icon(Icons.Default.Edit, null) }
                            )
                            DropdownMenuItem(
                                text = { Text("Mover") },
                                onClick = { showMenu = false; onMoveToPastaClick() },
                                leadingIcon = { Icon(Icons.Default.Folder, null) }
                            )
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text("Excluir", color = MaterialTheme.colorScheme.error) },
                                onClick = { showMenu = false; onDeleteClick() },
                                leadingIcon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) }
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Título do Deck
            Text(
                text = deck.filePath,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 20.sp
            )

            Spacer(Modifier.height(12.dp))

            // Botão SÓLIDO (Preenchido) para Acessar
            Button(
                onClick = onStudyClick,
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
                    fontSize = 13.sp,
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

@Composable
fun ActionButtons(onCreateFolderClick: () -> Unit, onCreateDeckClick: () -> Unit) {
    val primaryColor = MaterialTheme.colorScheme.primary

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedButton(
            onClick = onCreateFolderClick,
            modifier = Modifier.weight(1f).height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
        ) {
            Icon(Icons.Default.CreateNewFolder, null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text("Nova Pasta", fontWeight = FontWeight.SemiBold)
        }

        Button(
            onClick = onCreateDeckClick,
            modifier = Modifier.weight(1f).height(52.dp).shadow(4.dp, RoundedCornerShape(14.dp)),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
        ) {
            Icon(Icons.Default.Add, null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onPrimary)
            Spacer(Modifier.width(8.dp))
            Text("Novo Deck", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
        }
    }
}

@Composable
fun EmptyStateDecks(onCreateClick: () -> Unit) {
    val primaryColor = MaterialTheme.colorScheme.primary

    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier.size(80.dp).background(primaryColor.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = Icons.Default.LibraryBooks, contentDescription = null, tint = primaryColor, modifier = Modifier.size(40.dp))
            }
            Spacer(Modifier.height(20.dp))
            Text("Nenhum deck criado ainda", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(8.dp))
            Text("Crie seu primeiro deck para começar a estudar", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = onCreateClick,
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor, contentColor = MaterialTheme.colorScheme.onPrimary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.height(48.dp)
            ) {
                Icon(Icons.Default.Add, null)
                Spacer(Modifier.width(8.dp))
                Text("Criar Primeiro Deck", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// Dialogs Components (CreateFolderDialog, EditDeckDialog, etc.)
// Certifique-se de que eles usem MaterialTheme.colorScheme.primary e onSurfaceVariant.
// O código abaixo é um exemplo genérico para eles:

@Composable
fun CreateFolderDialog(onConfirm: (String) -> Unit, onCancel: () -> Unit) {
    var folderName by remember { mutableStateOf("") }
    val primaryColor = MaterialTheme.colorScheme.primary

    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text("Nova Pasta", fontWeight = FontWeight.Bold) },
        text = {
            OutlinedTextField(
                value = folderName,
                onValueChange = { folderName = it },
                label = { Text("Nome da pasta") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = primaryColor,
                    focusedLabelColor = primaryColor,
                    cursorColor = primaryColor
                )
            )
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(folderName) },
                enabled = folderName.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor, contentColor = MaterialTheme.colorScheme.onPrimary)
            ) {
                Text("Criar", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) { Text("Cancelar") }
        },
        containerColor = MaterialTheme.colorScheme.surface
    )
}

@Composable
fun EditDeckDialog(currentName: String, onConfirm: (String) -> Unit, onDismiss: () -> Unit, isLoading: Boolean) {
    var newName by remember { mutableStateOf(currentName) }
    val primaryColor = MaterialTheme.colorScheme.primary

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Nome") },
        text = {
            OutlinedTextField(
                value = newName,
                onValueChange = { newName = it },
                label = { Text("Novo nome") },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primaryColor, focusedLabelColor = primaryColor)
            )
        },
        confirmButton = {
            Button(onClick = { onConfirm(newName) }, enabled = !isLoading && newName.isNotBlank(), colors = ButtonDefaults.buttonColors(containerColor = primaryColor, contentColor = MaterialTheme.colorScheme.onPrimary)) {
                if (isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary) else Text("Salvar")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
        containerColor = MaterialTheme.colorScheme.surface
    )
}

@Composable
fun DeleteDeckDialog(deckName: String, onConfirm: () -> Unit, onDismiss: () -> Unit, isLoading: Boolean) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.error) },
        title = { Text("Excluir Deck") },
        text = { Text("Tem certeza que deseja excluir \"$deckName\"? Esta ação não pode ser desfeita.") },
        confirmButton = {
            Button(onClick = onConfirm, enabled = !isLoading, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
                if (isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White) else Text("Excluir", color = Color.White)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
        containerColor = MaterialTheme.colorScheme.surface
    )
}

@Composable
fun EditFolderDialog(currentName: String, onConfirm: (String) -> Unit, onDismiss: () -> Unit, isLoading: Boolean) {
    var newName by remember { mutableStateOf(currentName) }
    val primaryColor = MaterialTheme.colorScheme.primary

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Renomear Pasta") },
        text = {
            OutlinedTextField(
                value = newName,
                onValueChange = { newName = it },
                label = { Text("Novo nome") },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primaryColor, focusedLabelColor = primaryColor)
            )
        },
        confirmButton = {
            Button(onClick = { onConfirm(newName) }, enabled = !isLoading && newName.isNotBlank(), colors = ButtonDefaults.buttonColors(containerColor = primaryColor, contentColor = MaterialTheme.colorScheme.onPrimary)) {
                if (isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary) else Text("Salvar")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
        containerColor = MaterialTheme.colorScheme.surface
    )
}

@Composable
fun DeleteFolderDialog(folderName: String, deckCount: Int, onConfirm: (Boolean) -> Unit, onDismiss: () -> Unit, isLoading: Boolean) {
    var deleteDecks by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.error) },
        title = { Text("Excluir Pasta") },
        text = {
            Column {
                Text("Tem certeza que deseja excluir \"$folderName\"?")
                if (deckCount > 0) {
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = deleteDecks, onCheckedChange = { deleteDecks = it }, colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.error))
                        Text("Excluir também os $deckCount decks contidos", fontSize = 13.sp)
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(deleteDecks) }, enabled = !isLoading, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
                if (isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White) else Text("Excluir", color = Color.White)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
        containerColor = MaterialTheme.colorScheme.surface
    )
}

@Composable
fun MoveToPastaDialog(folders: List<FolderWithDocumentsResponse>, currentFolderId: Int?, onConfirm: (Int?) -> Unit, onDismiss: () -> Unit) {
    var selectedFolderId by remember { mutableStateOf<Int?>(null) }
    val primaryColor = MaterialTheme.colorScheme.primary

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Mover para Pasta") },
        text = {
            Column {
                Text("Selecione o destino:")
                Spacer(Modifier.height(8.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth().clickable { selectedFolderId = null },
                    color = if (selectedFolderId == null) primaryColor.copy(alpha = 0.1f) else Color.Transparent,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Raiz (Sem pasta)", modifier = Modifier.padding(12.dp), color = MaterialTheme.colorScheme.onSurface)
                }
                folders.filter { it.id != currentFolderId }.forEach { folder ->
                    Surface(
                        modifier = Modifier.fillMaxWidth().clickable { selectedFolderId = folder.id },
                        color = if (selectedFolderId == folder.id) primaryColor.copy(alpha = 0.1f) else Color.Transparent,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(folder.name, modifier = Modifier.padding(12.dp), color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(selectedFolderId) }, colors = ButtonDefaults.buttonColors(containerColor = primaryColor, contentColor = MaterialTheme.colorScheme.onPrimary)) {
                Text("Mover")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
        containerColor = MaterialTheme.colorScheme.surface
    )
}
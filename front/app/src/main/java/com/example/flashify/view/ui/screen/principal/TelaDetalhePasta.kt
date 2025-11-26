package com.example.flashify.view.ui.screen.principal

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FolderOpen
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.flashify.model.data.DeckResponse
import com.example.flashify.model.data.FolderWithDocumentsResponse
import com.example.flashify.model.manager.ThemeManager
import com.example.flashify.model.util.*
import com.example.flashify.view.ui.components.GradientBackgroundScreen
import com.example.flashify.viewmodel.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaDetalhePasta(
    navController: NavController,
    folderId: Int,
    folderName: String,
    folderViewModel: FolderViewModel = hiltViewModel(),
    deckViewModel: DeckViewModel = hiltViewModel()
) {
    // --- LÓGICA DO TEMA ---
    val context = LocalContext.current
    val themeManager = remember { ThemeManager(context) }
    val isDarkTheme by themeManager.isDarkTheme.collectAsState(initial = isSystemInDarkTheme())
    val primaryColor = MaterialTheme.colorScheme.primary

    val libraryState by folderViewModel.libraryState.collectAsStateWithLifecycle()
    val deckActionState by deckViewModel.deckActionState.collectAsStateWithLifecycle()
    val folderOperationState by folderViewModel.operationState.collectAsStateWithLifecycle()

    // Estados para diálogos
    var showDeleteDeckDialog by remember { mutableStateOf(false) }
    var showEditDeckDialog by remember { mutableStateOf(false) }
    var showMoveToPastaDialog by remember { mutableStateOf(false) }
    var deckToActOn by remember { mutableStateOf<DeckResponse?>(null) }
    var showDeleteFolderDialog by remember { mutableStateOf(false) }

    // Buscar a pasta específica
    val currentFolder = remember(libraryState) {
        if (libraryState is LibraryState.Success) {
            val library = (libraryState as LibraryState.Success).library
            library.folders.find { it.id == folderId }
        } else {
            null
        }
    }

    val decksInFolder = remember(libraryState) {
        if (libraryState is LibraryState.Success) {
            val library = (libraryState as LibraryState.Success).library
            val folder = library.folders.find { it.id == folderId }
            folder?.documents ?: emptyList()
        } else {
            emptyList()
        }
    }

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
        }
    }

    LaunchedEffect(folderOperationState) {
        if (folderOperationState is FolderOperationState.Success) {
            Toast.makeText(context, "Pasta excluída com sucesso", Toast.LENGTH_SHORT).show()
            folderViewModel.resetOperationState()
            showDeleteFolderDialog = false
            navController.popBackStack()
        }
    }

    // Diálogos
    if (showDeleteDeckDialog && deckToActOn != null) {
        DeleteDeckDialogInFolder(
            deckName = deckToActOn!!.filePath,
            onConfirm = { deckViewModel.deleteDeck(deckToActOn!!.id) },
            onDismiss = { showDeleteDeckDialog = false; deckToActOn = null },
            isLoading = deckActionState is DeckActionState.Loading
        )
    }

    if (showEditDeckDialog && deckToActOn != null) {
        EditDeckDialogInFolder(
            currentName = deckToActOn!!.filePath,
            onConfirm = { newName -> deckViewModel.renameDeck(deckToActOn!!.id, newName) },
            onDismiss = { showEditDeckDialog = false; deckToActOn = null },
            isLoading = deckActionState is DeckActionState.Loading
        )
    }

    if (showMoveToPastaDialog && deckToActOn != null && libraryState is LibraryState.Success) {
        MoveToPastaDialogInFolder(
            folders = (libraryState as LibraryState.Success).library.folders,
            currentFolderId = folderId,
            onConfirm = { newFolderId -> deckViewModel.moveDeckToFolder(deckToActOn!!.id, newFolderId) },
            onDismiss = { showMoveToPastaDialog = false; deckToActOn = null }
        )
    }

    if (showDeleteFolderDialog && currentFolder != null) {
        DeleteFolderDialogInFolder(
            folderName = currentFolder.name,
            deckCount = currentFolder.documents.size,
            onConfirm = { deleteDecks -> folderViewModel.deleteFolder(folderId, deleteDecks) },
            onDismiss = { showDeleteFolderDialog = false },
            isLoading = folderOperationState is FolderOperationState.Loading
        )
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
                                folderName,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                "${decksInFolder.size} ${if (decksInFolder.size == 1) "deck" else "decks"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                Icons.Default.ArrowBack,
                                "Voltar",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { showDeleteFolderDialog = true }) {
                            Icon(
                                Icons.Default.Delete,
                                "Excluir Pasta",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (libraryState) {
                    is LibraryState.Loading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = primaryColor)
                        }
                    }
                    is LibraryState.Error -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Erro ao carregar pasta", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.height(8.dp))
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
                                .padding(horizontal = 20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(vertical = 16.dp)
                        ) {
                            item {
                                Button(
                                    onClick = { navController.navigate("$CREATE_FLASHCARD_ROUTE?folderId=$folderId") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(52.dp)
                                        .shadow(4.dp, RoundedCornerShape(14.dp)),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = primaryColor,
                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                    ),
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Icon(Icons.Default.Add, null, modifier = Modifier.size(20.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        "Criar Novo Deck",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                }
                            }

                            if (decksInFolder.isEmpty()) {
                                item {
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 24.dp),
                                        shape = RoundedCornerShape(20.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                        // ✅ Borda para modo claro
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(32.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(80.dp)
                                                    .background(primaryColor.copy(alpha = 0.15f), CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Outlined.FolderOpen,
                                                    contentDescription = null,
                                                    tint = primaryColor,
                                                    modifier = Modifier.size(40.dp)
                                                )
                                            }
                                            Spacer(Modifier.height(20.dp))
                                            Text(
                                                "Pasta vazia",
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Spacer(Modifier.height(8.dp))
                                            Text(
                                                "Use o botão acima para criar seu primeiro deck nesta pasta",
                                                fontSize = 14.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                }
                            } else {
                                items(decksInFolder.size) { index ->
                                    // ✅ Usando o novo Card Padronizado
                                    DeckItemCardInFolder(
                                        deck = decksInFolder[index],
                                        onStudyClick = { navController.navigate("$ESCOLHA_MODO_ESTUDO_ROUTE/${decksInFolder[index].id}") },
                                        onEditClick = { deckToActOn = decksInFolder[index]; showEditDeckDialog = true },
                                        onDeleteClick = { deckToActOn = decksInFolder[index]; showDeleteDeckDialog = true },
                                        onMoveToPastaClick = { deckToActOn = decksInFolder[index]; showMoveToPastaDialog = true }
                                    )
                                }
                            }

                            item { Spacer(Modifier.height(20.dp)) }
                        }
                    }
                    is LibraryState.Idle -> {}
                }
            }
        }
    }
}

// ✅ Card de Deck Padronizado (IDÊNTICO ao da TelaBiblioteca/Principal)
@Composable
fun DeckItemCardInFolder(
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
            .shadow(4.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        // ✅ Borda subtil
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
                // Ícone
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
                    // Badges (Flashcards + Quiz)
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(end = 4.dp)
                    ) {
                        // Badge Flashcards
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = primaryColor.copy(alpha = 0.1f),
                            border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.3f))
                        ) {
                            Text(
                                "${deck.totalFlashcards} Flashcards",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (!isDarkTheme) Color(0xFFF57F17) else primaryColor
                            )
                        }

                        // Badge Quiz
                        if (deck.hasQuiz) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = quizColor.copy(alpha = 0.1f),
                                border = BorderStroke(1.dp, quizColor.copy(alpha = 0.3f))
                            ) {
                                Text(
                                    "Quiz Disponível",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = quizColor
                                )
                            }
                        }
                    }

                    // Menu de Opções
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

            // Título
            Text(
                text = deck.filePath,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(12.dp))

            // Botão Sólido (Estudar)
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
                    "Estudar",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.width(6.dp))
                Icon(
                    Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

// Diálogos (EditDeck, DeleteFolder, etc.)
// Mantendo a estrutura mas garantindo cores semânticas.

@Composable
fun EditDeckDialogInFolder(currentName: String, onConfirm: (String) -> Unit, onDismiss: () -> Unit, isLoading: Boolean) {
    var newName by remember { mutableStateOf(currentName) }
    val primaryColor = MaterialTheme.colorScheme.primary

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Nome do Deck", fontSize = 20.sp, fontWeight = FontWeight.Bold) },
        text = {
            OutlinedTextField(
                value = newName,
                onValueChange = { newName = it },
                label = { Text("Novo nome") },
                singleLine = true,
                enabled = !isLoading,
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
                onClick = { onConfirm(newName) },
                enabled = !isLoading && newName.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor, contentColor = MaterialTheme.colorScheme.onPrimary),
                shape = RoundedCornerShape(10.dp)
            ) {
                if (isLoading) CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                else Text("Salvar")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss, enabled = !isLoading, shape = RoundedCornerShape(10.dp)) { Text("Cancelar") } },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
fun DeleteFolderDialogInFolder(folderName: String, deckCount: Int, onConfirm: (Boolean) -> Unit, onDismiss: () -> Unit, isLoading: Boolean) {
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
                        Checkbox(
                            checked = deleteDecks,
                            onCheckedChange = { deleteDecks = it },
                            colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.error)
                        )
                        Text("Excluir também os $deckCount decks contidos", fontSize = 13.sp)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(deleteDecks) },
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                shape = RoundedCornerShape(10.dp)
            ) {
                if (isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White) else Text("Excluir")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
fun DeleteDeckDialogInFolder(deckName: String, onConfirm: () -> Unit, onDismiss: () -> Unit, isLoading: Boolean) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.error) },
        title = { Text("Excluir Deck") },
        text = { Text("Tem certeza que deseja excluir \"$deckName\"? Esta ação não pode ser desfeita.") },
        confirmButton = {
            Button(onClick = onConfirm, enabled = !isLoading, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error), shape = RoundedCornerShape(10.dp)) {
                if (isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White) else Text("Excluir")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
fun MoveToPastaDialogInFolder(folders: List<FolderWithDocumentsResponse>, currentFolderId: Int, onConfirm: (Int?) -> Unit, onDismiss: () -> Unit) {
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
            Button(onClick = { onConfirm(selectedFolderId) }, colors = ButtonDefaults.buttonColors(containerColor = primaryColor, contentColor = MaterialTheme.colorScheme.onPrimary), shape = RoundedCornerShape(10.dp)) {
                Text("Mover")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp)
    )
}
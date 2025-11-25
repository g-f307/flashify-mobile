package com.example.flashify.view.ui.screen.principal

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.flashify.model.data.DeckResponse
import com.example.flashify.model.manager.ThemeManager
import com.example.flashify.model.util.*
import com.example.flashify.view.ui.components.GradientBackgroundScreen
import com.example.flashify.view.ui.theme.TextSecondary
import com.example.flashify.view.ui.theme.YellowAccent
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

    // Cores do Tema
    val primaryColor = MaterialTheme.colorScheme.primary
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

    val libraryState by folderViewModel.libraryState.collectAsStateWithLifecycle()
    val deckActionState by deckViewModel.deckActionState.collectAsStateWithLifecycle()
    val folderOperationState by folderViewModel.operationState.collectAsStateWithLifecycle()

    // Estados para diálogos de deck
    var showDeleteDeckDialog by remember { mutableStateOf(false) }
    var showEditDeckDialog by remember { mutableStateOf(false) }
    var showMoveToPastaDialog by remember { mutableStateOf(false) }
    var deckToActOn by remember { mutableStateOf<DeckResponse?>(null) }

    // Estado para diálogo de excluir pasta
    var showDeleteFolderDialog by remember { mutableStateOf(false) }

    // Buscar a pasta específica do estado da biblioteca
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

    // Diálogos e UI
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
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                "${decksInFolder.size} ${if (decksInFolder.size == 1) "deck" else "decks"}",
                                fontSize = 12.sp,
                                color = onSurfaceVariant
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
                                        .height(56.dp)
                                        .shadow(4.dp, RoundedCornerShape(14.dp)),
                                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Icon(Icons.Default.Add, null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                                    Spacer(Modifier.width(12.dp))
                                    Text(
                                        "Criar Novo Deck",
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        letterSpacing = 0.3.sp
                                    )
                                }
                            }

                            if (decksInFolder.isEmpty()) {
                                item {
                                    Card(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                                        shape = RoundedCornerShape(20.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                                    ) {
                                        Column(modifier = Modifier.fillMaxWidth().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                            Box(modifier = Modifier.size(80.dp).background(primaryColor.copy(alpha = 0.15f), CircleShape), contentAlignment = Alignment.Center) {
                                                Icon(Icons.Default.FolderOpen, null, tint = primaryColor, modifier = Modifier.size(40.dp))
                                            }
                                            Spacer(Modifier.height(20.dp))
                                            Text("Pasta vazia", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                            Spacer(Modifier.height(8.dp))
                                            Text("Use o botão acima para criar seu primeiro deck nesta pasta", fontSize = 14.sp, color = onSurfaceVariant, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                                        }
                                    }
                                }
                            } else {
                                items(decksInFolder.size) { index ->
                                    DeckItemCardInFolder(
                                        deck = decksInFolder[index],
                                        onStudyClick = { navController.navigate("$ESCOLHA_MODO_ESTUDO_ROUTE/${decksInFolder[index].id}") },
                                        onEditClick = { deckToActOn = decksInFolder[index]; showEditDeckDialog = true },
                                        onDeleteClick = { deckToActOn = decksInFolder[index]; showDeleteDeckDialog = true },
                                        onMoveToPastaClick = { deckToActOn = decksInFolder[index]; showMoveToPastaDialog = true }
                                    )
                                }
                            }
                        }
                    }
                    is LibraryState.Idle -> {}
                }
            }
        }
    }
}

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
        modifier = Modifier.fillMaxWidth().shadow(4.dp, RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
    ) {
        Column(Modifier.padding(18.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.SpaceBetween) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(modifier = Modifier.size(48.dp).background(primaryColor.copy(alpha = 0.15f), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.MenuBook, "Deck", tint = primaryColor, modifier = Modifier.size(26.dp))
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(deck.filePath, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = MaterialTheme.colorScheme.onSurface, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            if (deck.totalFlashcards > 0) {
                                Surface(shape = RoundedCornerShape(8.dp), color = primaryColor.copy(alpha = 0.2f)) {
                                    Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Style, null, tint = primaryColor, modifier = Modifier.size(12.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("${deck.totalFlashcards}", fontSize = 12.sp, color = primaryColor, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            if (deck.hasQuiz) {
                                Surface(shape = RoundedCornerShape(8.dp), color = quizColor.copy(alpha = 0.2f)) {
                                    Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Quiz, null, tint = quizColor, modifier = Modifier.size(12.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("Quiz", fontSize = 12.sp, color = quizColor, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                Box {
                    IconButton(onClick = { showMenu = true }, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.MoreVert, "Opções", tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), modifier = Modifier.size(22.dp))
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }, shape = RoundedCornerShape(12.dp), containerColor = MaterialTheme.colorScheme.surface) {
                        DropdownMenuItem(text = { Text("Editar", fontSize = 14.sp) }, onClick = { showMenu = false; onEditClick() }, leadingIcon = { Icon(Icons.Default.Edit, null) })
                        DropdownMenuItem(text = { Text("Mover para Pasta", fontSize = 14.sp) }, onClick = { showMenu = false; onMoveToPastaClick() }, leadingIcon = { Icon(Icons.Default.Folder, null) })
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        DropdownMenuItem(text = { Text("Excluir", color = MaterialTheme.colorScheme.error, fontSize = 14.sp) }, onClick = { showMenu = false; onDeleteClick() }, leadingIcon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) })
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = onStudyClick,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Icon(Icons.Default.PlayArrow, "Estudar", tint = MaterialTheme.colorScheme.onPrimary)
                Spacer(Modifier.width(8.dp))
                Text("Estudar", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }
}

// Diálogos (EditDeck, DeleteFolder, DeleteDeck, MoveToPasta) seguem a mesma estrutura
// Mas agora usam MaterialTheme.colorScheme.primary e onSurfaceVariant
// Vou colocar apenas um exemplo para brevidade, mas todos devem ser atualizados:

@Composable
fun EditDeckDialogInFolder(currentName: String, onConfirm: (String) -> Unit, onDismiss: () -> Unit, isLoading: Boolean) {
    var newName by remember { mutableStateOf(currentName) }
    val primaryColor = MaterialTheme.colorScheme.primary

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Nome do Deck", fontSize = 20.sp, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text("Digite o novo nome:", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("Novo nome") },
                    singleLine = true,
                    enabled = !isLoading,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primaryColor, cursorColor = primaryColor, focusedLabelColor = primaryColor),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(newName) },
                enabled = !isLoading && newName.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                shape = RoundedCornerShape(10.dp)
            ) {
                if (isLoading) CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                else Text("Salvar", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isLoading, shape = RoundedCornerShape(10.dp)) {
                Text("Cancelar", fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }
        },
        shape = RoundedCornerShape(20.dp),
        containerColor = MaterialTheme.colorScheme.surface
    )
}

@Composable
fun DeleteFolderDialogInFolder(folderName: String, deckCount: Int, onConfirm: (Boolean) -> Unit, onDismiss: () -> Unit, isLoading: Boolean) {
    var deleteDecks by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(32.dp)) },
        title = { Text("Excluir Pasta", fontSize = 20.sp, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text("Tem certeza que deseja excluir a pasta:", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.3f))
                ) {
                    Text("\"$folderName\"", modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.error)
                }

                if (deckCount > 0) {
                    Spacer(Modifier.height(16.dp))
                    Text("Esta pasta contém $deckCount deck${if (deckCount != 1) "s" else ""}.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth().clickable { deleteDecks = !deleteDecks }.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = deleteDecks, onCheckedChange = { deleteDecks = it }, colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.error, uncheckedColor = MaterialTheme.colorScheme.outline))
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text("Excluir os decks junto com a pasta", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                            Text(if (deleteDecks) "Os decks serão apagados permanentemente" else "Os decks serão movidos para a biblioteca", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
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
                if (isLoading) CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = Color.White)
                else Text("Excluir", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isLoading, shape = RoundedCornerShape(10.dp)) {
                Text("Cancelar", fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }
        },
        shape = RoundedCornerShape(20.dp),
        containerColor = MaterialTheme.colorScheme.surface
    )
}

@Composable
fun DeleteDeckDialogInFolder(deckName: String, onConfirm: () -> Unit, onDismiss: () -> Unit, isLoading: Boolean) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(32.dp)) },
        title = { Text("Excluir Deck", fontSize = 20.sp, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text("Tem certeza que deseja excluir o deck:", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.3f))
                ) {
                    Text("\"$deckName\"", modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.error)
                }
                Spacer(Modifier.height(12.dp))
                Text("Esta ação não pode ser desfeita.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                shape = RoundedCornerShape(10.dp)
            ) {
                if (isLoading) CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = Color.White)
                else Text("Excluir", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isLoading, shape = RoundedCornerShape(10.dp)) {
                Text("Cancelar", fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }
        },
        shape = RoundedCornerShape(20.dp),
        containerColor = MaterialTheme.colorScheme.surface
    )
}

@Composable
fun MoveToPastaDialogInFolder(folders: List<com.example.flashify.model.data.FolderWithDocumentsResponse>, currentFolderId: Int, onConfirm: (Int?) -> Unit, onDismiss: () -> Unit) {
    var selectedFolderId by remember { mutableStateOf<Int?>(null) }
    val primaryColor = MaterialTheme.colorScheme.primary

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Mover para Pasta", fontSize = 20.sp, fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Selecione a pasta de destino:", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(16.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth().clickable { selectedFolderId = null },
                    shape = RoundedCornerShape(12.dp),
                    color = if (selectedFolderId == null) primaryColor.copy(alpha = 0.15f) else Color.Transparent,
                    border = BorderStroke(1.dp, if (selectedFolderId == null) primaryColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = selectedFolderId == null, onClick = { selectedFolderId = null }, colors = RadioButtonDefaults.colors(selectedColor = primaryColor))
                        Spacer(Modifier.width(8.dp))
                        Icon(Icons.Default.Home, null, tint = if (selectedFolderId == null) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Raiz (sem pasta)", fontWeight = if (selectedFolderId == null) FontWeight.Bold else FontWeight.Normal, color = if (selectedFolderId == null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Spacer(Modifier.height(12.dp))

                folders.filter { it.id != currentFolderId }.forEach { folder ->
                    Spacer(Modifier.height(8.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth().clickable { selectedFolderId = folder.id },
                        shape = RoundedCornerShape(12.dp),
                        color = if (selectedFolderId == folder.id) primaryColor.copy(alpha = 0.15f) else Color.Transparent,
                        border = BorderStroke(1.dp, if (selectedFolderId == folder.id) primaryColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = selectedFolderId == folder.id, onClick = { selectedFolderId = folder.id }, colors = RadioButtonDefaults.colors(selectedColor = primaryColor))
                            Spacer(Modifier.width(8.dp))
                            Icon(Icons.Default.Folder, null, tint = if (selectedFolderId == folder.id) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(folder.name, fontWeight = if (selectedFolderId == folder.id) FontWeight.Bold else FontWeight.Normal, color = if (selectedFolderId == folder.id) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(selectedFolderId) }, colors = ButtonDefaults.buttonColors(containerColor = primaryColor), shape = RoundedCornerShape(10.dp)) {
                Text("Mover", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, shape = RoundedCornerShape(10.dp)) {
                Text("Cancelar", fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }
        },
        shape = RoundedCornerShape(20.dp),
        containerColor = MaterialTheme.colorScheme.surface
    )
}
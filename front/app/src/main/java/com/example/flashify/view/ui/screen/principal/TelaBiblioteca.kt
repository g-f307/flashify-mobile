package com.example.flashify.view.ui.screen.principal

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.flashify.model.data.DeckResponse
import com.example.flashify.model.data.FolderWithDocumentsResponse
import com.example.flashify.model.data.NavItem
import com.example.flashify.model.util.*
import com.example.flashify.view.ui.components.GradientBackgroundScreen
import com.example.flashify.view.ui.components.NavegacaoBotaoAbaixo
import com.example.flashify.view.ui.theme.TextSecondary
import com.example.flashify.view.ui.theme.YellowAccent
import com.example.flashify.viewmodel.*

@Composable
fun TelaPrincipalBiblioteca(
    navController: NavController,
    deckViewModel: DeckViewModel = viewModel(),
    folderViewModel: FolderViewModel = viewModel()
) {
    val deckActionState by deckViewModel.deckActionState.collectAsStateWithLifecycle()
    val libraryState by folderViewModel.libraryState.collectAsStateWithLifecycle()
    val folderOperationState by folderViewModel.operationState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Estados para diálogos
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showCreateFolderDialog by remember { mutableStateOf(false) }
    var showMoveToPastaDialog by remember { mutableStateOf(false) }
    var deckToActOn by remember { mutableStateOf<DeckResponse?>(null) }

    // Carregar biblioteca ao iniciar
    LaunchedEffect(Unit) {
        folderViewModel.loadLibrary()
    }

    // Feedback de ações
    LaunchedEffect(deckActionState) {
        when (val state = deckActionState) {
            is DeckActionState.Success -> {
                Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                deckViewModel.resetActionState()
                showDeleteDialog = false
                showEditDialog = false
                showMoveToPastaDialog = false
                deckToActOn = null
                folderViewModel.loadLibrary()
            }
            is DeckActionState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                deckViewModel.resetActionState()
            }
            else -> {}
        }
    }

    LaunchedEffect(folderOperationState) {
        when (val state = folderOperationState) {
            is FolderOperationState.Success -> {
                Toast.makeText(context, "Operação realizada com sucesso", Toast.LENGTH_SHORT).show()
                folderViewModel.resetOperationState()
                showCreateFolderDialog = false
            }
            is FolderOperationState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                folderViewModel.resetOperationState()
            }
            else -> {}
        }
    }

    // Diálogos
    if (showDeleteDialog && deckToActOn != null) {
        DeleteDeckDialog(
            deckName = deckToActOn!!.filePath,
            onConfirm = { deckViewModel.deleteDeck(deckToActOn!!.id) },
            onDismiss = {
                showDeleteDialog = false
                deckToActOn = null
            },
            isLoading = deckActionState is DeckActionState.Loading
        )
    }

    if (showEditDialog && deckToActOn != null) {
        EditDeckDialog(
            currentName = deckToActOn!!.filePath,
            onConfirm = { newName -> deckViewModel.renameDeck(deckToActOn!!.id, newName) },
            onDismiss = {
                showEditDialog = false
                deckToActOn = null
            },
            isLoading = deckActionState is DeckActionState.Loading
        )
    }

    if (showCreateFolderDialog) {
        CreateFolderDialog(
            onConfirm = { folderName -> folderViewModel.createFolder(folderName) },
            onCancel = { showCreateFolderDialog = false }
        )
    }

    if (showMoveToPastaDialog && deckToActOn != null && libraryState is LibraryState.Success) {
        MoveToPastaDialog(
            folders = (libraryState as LibraryState.Success).library.folders,
            onConfirm = { folderId -> deckViewModel.moveDeckToFolder(deckToActOn!!.id, folderId) },
            onDismiss = {
                showMoveToPastaDialog = false
                deckToActOn = null
            }
        )
    }

    // Navegação
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
                                popUpTo(BIBLIOTECA_SCREEN_ROUTE) { inclusive = true }
                            }
                            "Biblioteca" -> { /* Já está aqui */ }
                            "Criar" -> navController.navigate(CREATE_FLASHCARD_ROUTE) {
                                popUpTo(BIBLIOTECA_SCREEN_ROUTE)
                            }
                            "Progresso" -> navController.navigate(PROGRESSO_SCREEN_ROUTE) {
                                popUpTo(BIBLIOTECA_SCREEN_ROUTE)
                            }
                            "Config" -> navController.navigate(CONFIGURATION_SCREEN_ROUTE) {
                                popUpTo(BIBLIOTECA_SCREEN_ROUTE)
                            }
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        GradientBackgroundScreen {
            when (val state = libraryState) {
                is LibraryState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(
                                color = YellowAccent,
                                strokeWidth = 3.dp,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(Modifier.height(16.dp))
                            Text(
                                "Carregando biblioteca...",
                                color = TextSecondary,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
                is LibraryState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(Modifier.height(16.dp))
                            Text(
                                "Erro ao carregar",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                state.message,
                                color = TextSecondary,
                                textAlign = TextAlign.Center,
                                fontSize = 14.sp
                            )
                            Spacer(Modifier.height(24.dp))
                            Button(
                                onClick = { folderViewModel.loadLibrary() },
                                colors = ButtonDefaults.buttonColors(containerColor = YellowAccent),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Refresh, null, tint = Color.Black)
                                Spacer(Modifier.width(8.dp))
                                Text("Tentar Novamente", color = Color.Black, fontWeight = FontWeight.Bold)
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
                        // Título e subtítulo
                        item {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    "Sua Biblioteca",
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    letterSpacing = 0.3.sp
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "${state.library.folders.size} pastas · ${state.library.rootDocuments.size} decks",
                                    fontSize = 14.sp,
                                    color = TextSecondary,
                                    letterSpacing = 0.2.sp
                                )
                            }
                        }

                        // Botões de ação
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            ActionButtons(
                                onCreateFolderClick = { showCreateFolderDialog = true },
                                onCreateDeckClick = { navController.navigate(CREATE_FLASHCARD_ROUTE) }
                            )
                        }

                        // Seção de Pastas
                        if (state.library.folders.isNotEmpty()) {
                            item {
                                Spacer(modifier = Modifier.height(16.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Pastas",
                                        color = MaterialTheme.colorScheme.onBackground,
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.3.sp
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = YellowAccent.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            "${state.library.folders.size}",
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                            color = YellowAccent,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                            }

                            items(state.library.folders, key = { it.id }) { folder ->
                                FolderItem(
                                    folder = folder,
                                    onClick = {
                                        navController.navigate("$DETALHE_PASTA_ROUTE/${folder.id}/${folder.name}")
                                    }
                                )
                            }
                        }

                        // Título dos Decks
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Seus Decks",
                                    color = MaterialTheme.colorScheme.onBackground,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.3.sp
                                )
                                if (state.library.rootDocuments.isNotEmpty()) {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = YellowAccent.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            "${state.library.rootDocuments.size}",
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                            color = YellowAccent,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                            }
                        }

                        // Lista de Decks
                        if (state.library.rootDocuments.isEmpty()) {
                            item {
                                EmptyStateDecks(onCreateClick = { navController.navigate(CREATE_FLASHCARD_ROUTE) })
                            }
                        } else {
                            items(state.library.rootDocuments, key = { it.id }) { deck ->
                                DeckItemCard(
                                    deck = deck,
                                    onStudyClick = {
                                        navController.navigate("$ESCOLHA_MODO_ESTUDO_ROUTE/${deck.id}")
                                    },
                                    onEditClick = {
                                        deckToActOn = deck
                                        showEditDialog = true
                                    },
                                    onDeleteClick = {
                                        deckToActOn = deck
                                        showDeleteDialog = true
                                    },
                                    onMoveToPastaClick = {
                                        deckToActOn = deck
                                        showMoveToPastaDialog = true
                                    }
                                )
                            }
                        }

                        // Espaçamento final
                        item { Spacer(Modifier.height(16.dp)) }
                    }
                }
                else -> {}
            }
        }
    }
}

@Composable
fun ActionButtons(
    onCreateFolderClick: () -> Unit,
    onCreateDeckClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedButton(
            onClick = onCreateFolderClick,
            modifier = Modifier
                .weight(1f)
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.onSurface
            ),
            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
        ) {
            Icon(
                Icons.Default.CreateNewFolder,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                "Nova Pasta",
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                letterSpacing = 0.3.sp
            )
        }

        Button(
            onClick = onCreateDeckClick,
            modifier = Modifier
                .weight(1f)
                .height(52.dp)
                .shadow(4.dp, RoundedCornerShape(14.dp)),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = YellowAccent)
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = Color.Black
            )
            Spacer(Modifier.width(8.dp))
            Text(
                "Novo Deck",
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                fontSize = 14.sp,
                letterSpacing = 0.3.sp
            )
        }
    }
}

@Composable
fun EmptyStateDecks(onCreateClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
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
                    .background(YellowAccent.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LibraryBooks,
                    contentDescription = null,
                    tint = YellowAccent,
                    modifier = Modifier.size(40.dp)
                )
            }
            Spacer(Modifier.height(20.dp))
            Text(
                "Nenhum deck criado ainda",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Crie seu primeiro deck para começar a estudar",
                fontSize = 14.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = onCreateClick,
                colors = ButtonDefaults.buttonColors(containerColor = YellowAccent),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.height(48.dp)
            ) {
                Icon(Icons.Default.Add, null, tint = Color.Black)
                Spacer(Modifier.width(8.dp))
                Text(
                    "Criar Primeiro Deck",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun FolderItem(folder: FolderWithDocumentsResponse, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
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
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(YellowAccent.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Folder,
                        contentDescription = null,
                        tint = YellowAccent,
                        modifier = Modifier.size(26.dp)
                    )
                }
                Spacer(Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        folder.name,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        letterSpacing = 0.2.sp
                    )
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "${folder.documents.size} deck${if (folder.documents.size != 1) "s" else ""}",
                            color = TextSecondary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(YellowAccent.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Abrir",
                    tint = YellowAccent,
                    modifier = Modifier.size(18.dp)
                )
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

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(YellowAccent.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.MenuBook,
                            contentDescription = "Deck",
                            tint = YellowAccent,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = deck.filePath,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            letterSpacing = 0.2.sp,
                            lineHeight = 22.sp
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (deck.totalFlashcards > 0) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = YellowAccent.copy(alpha = 0.2f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.Style,
                                            contentDescription = null,
                                            tint = YellowAccent,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        Text(
                                            "${deck.totalFlashcards}",
                                            fontSize = 12.sp,
                                            color = YellowAccent,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                            if (deck.hasQuiz) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFF00BCD4).copy(alpha = 0.2f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.Quiz,
                                            contentDescription = null,
                                            tint = Color(0xFF00BCD4),
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        Text(
                                            "Quiz",
                                            fontSize = 12.sp,
                                            color = Color(0xFF00BCD4),
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.MoreVert,
                            "Opções",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    "Editar",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            },
                            onClick = {
                                showMenu = false
                                onEditClick()
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Edit,
                                    null,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Text(
                                    "Mover para Pasta",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            },
                            onClick = {
                                showMenu = false
                                onMoveToPastaClick()
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Folder,
                                    null,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 4.dp),
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                        )
                        DropdownMenuItem(
                            text = {
                                Text(
                                    "Excluir",
                                    color = MaterialTheme.colorScheme.error,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            },
                            onClick = {
                                showMenu = false
                                onDeleteClick()
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Delete,
                                    null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Botão de estudar
            Button(
                onClick = onStudyClick,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = YellowAccent),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 2.dp,
                    pressedElevation = 0.dp
                )
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = "Estudar",
                        tint = Color.Black,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Estudar Agora",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        letterSpacing = 0.3.sp
                    )
                }
            }
        }
    }
}

@Composable
fun CreateFolderDialog(
    onConfirm: (String) -> Unit,
    onCancel: () -> Unit
) {
    var folderName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onCancel,
        title = {
            Text(
                "Nova Pasta",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    "Digite o nome da pasta:",
                    fontSize = 14.sp,
                    color = TextSecondary
                )
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = folderName,
                    onValueChange = { folderName = it },
                    label = { Text("Nome da pasta") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = YellowAccent,
                        cursorColor = YellowAccent,
                        focusedLabelColor = YellowAccent
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(folderName) },
                enabled = folderName.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = YellowAccent),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    "Criar",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onCancel,
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    "Cancelar",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
fun MoveToPastaDialog(
    folders: List<FolderWithDocumentsResponse>,
    onConfirm: (Int?) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedFolderId by remember { mutableStateOf<Int?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Mover para Pasta",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "Selecione a pasta de destino:",
                    fontSize = 14.sp,
                    color = TextSecondary
                )
                Spacer(Modifier.height(16.dp))

                // Opção para raiz (sem pasta)
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedFolderId = null },
                    shape = RoundedCornerShape(12.dp),
                    color = if (selectedFolderId == null)
                        YellowAccent.copy(alpha = 0.15f)
                    else
                        Color.Transparent,
                    border = BorderStroke(
                        1.dp,
                        if (selectedFolderId == null)
                            YellowAccent
                        else
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedFolderId == null,
                            onClick = { selectedFolderId = null },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = YellowAccent
                            )
                        )
                        Spacer(Modifier.width(8.dp))
                        Icon(
                            Icons.Default.Home,
                            null,
                            tint = if (selectedFolderId == null) YellowAccent else TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Raiz (sem pasta)",
                            fontWeight = if (selectedFolderId == null) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedFolderId == null)
                                MaterialTheme.colorScheme.onSurface
                            else
                                TextSecondary
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Lista de pastas
                folders.forEach { folder ->
                    Spacer(Modifier.height(8.dp))
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedFolderId = folder.id },
                        shape = RoundedCornerShape(12.dp),
                        color = if (selectedFolderId == folder.id)
                            YellowAccent.copy(alpha = 0.15f)
                        else
                            Color.Transparent,
                        border = BorderStroke(
                            1.dp,
                            if (selectedFolderId == folder.id)
                                YellowAccent
                            else
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedFolderId == folder.id,
                                onClick = { selectedFolderId = folder.id },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = YellowAccent
                                )
                            )
                            Spacer(Modifier.width(8.dp))
                            Icon(
                                Icons.Default.Folder,
                                null,
                                tint = if (selectedFolderId == folder.id) YellowAccent else TextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                folder.name,
                                fontWeight = if (selectedFolderId == folder.id) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedFolderId == folder.id)
                                    MaterialTheme.colorScheme.onSurface
                                else
                                    TextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(selectedFolderId) },
                colors = ButtonDefaults.buttonColors(containerColor = YellowAccent),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    "Mover",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    "Cancelar",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
fun EditDeckDialog(
    currentName: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
    isLoading: Boolean
) {
    var newName by remember { mutableStateOf(currentName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Editar Nome do Deck",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    "Digite o novo nome:",
                    fontSize = 14.sp,
                    color = TextSecondary
                )
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("Novo nome") },
                    singleLine = true,
                    enabled = !isLoading,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = YellowAccent,
                        cursorColor = YellowAccent,
                        focusedLabelColor = YellowAccent
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(newName) },
                enabled = !isLoading && newName.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = YellowAccent),
                shape = RoundedCornerShape(10.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = Color.Black
                    )
                } else {
                    Text(
                        "Salvar",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isLoading,
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    "Cancelar",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
fun DeleteDeckDialog(
    deckName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    isLoading: Boolean
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Text(
                "Excluir Deck",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    "Tem certeza que deseja excluir o deck:",
                    fontSize = 14.sp,
                    color = TextSecondary
                )
                Spacer(Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.3f))
                ) {
                    Text(
                        "\"$deckName\"",
                        modifier = Modifier.padding(12.dp),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    "Esta ação não pode ser desfeita.",
                    fontSize = 13.sp,
                    color = TextSecondary,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                } else {
                    Text(
                        "Excluir",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isLoading,
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    "Cancelar",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}
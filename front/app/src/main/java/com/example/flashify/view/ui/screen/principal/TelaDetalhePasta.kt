package com.example.flashify.view.ui.screen.principal

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.flashify.model.data.DeckResponse
import com.example.flashify.model.util.*
import com.example.flashify.view.ui.components.GradientBackgroundScreen
import com.example.flashify.view.ui.theme.TextSecondary
import com.example.flashify.view.ui.theme.YellowAccent
import com.example.flashify.viewmodel.LibraryState
import com.example.flashify.viewmodel.FolderViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaDetalhePasta(
    navController: NavController,
    folderId: Int,
    folderName: String,
    folderViewModel: FolderViewModel = viewModel()
) {
    val libraryState by folderViewModel.libraryState.collectAsStateWithLifecycle()

    // Buscar a pasta específica do estado da biblioteca
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

    GradientBackgroundScreen {
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
                                color = TextSecondary
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
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = YellowAccent)
                        }
                    }
                    is LibraryState.Error -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    "Erro ao carregar pasta",
                                    color = MaterialTheme.colorScheme.error,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(Modifier.height(8.dp))
                                Button(onClick = { folderViewModel.loadLibrary() }) {
                                    Text("Tentar Novamente")
                                }
                            }
                        }
                    }
                    is LibraryState.Success -> {
                        if (decksInFolder.isEmpty()) {
                            // Pasta vazia
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(32.dp)
                                ) {
                                    Icon(
                                        Icons.Default.FolderOpen,
                                        contentDescription = null,
                                        tint = TextSecondary,
                                        modifier = Modifier.size(80.dp)
                                    )
                                    Spacer(Modifier.height(16.dp))
                                    Text(
                                        "Pasta vazia",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        "Crie um novo deck para adicionar a esta pasta",
                                        fontSize = 14.sp,
                                        color = TextSecondary,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                    Spacer(Modifier.height(24.dp))
                                    Button(
                                        onClick = {
                                            navController.navigate(CREATE_FLASHCARD_ROUTE)
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = YellowAccent),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Add,
                                            contentDescription = null,
                                            tint = Color.Black
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            "Criar Deck",
                                            color = Color.Black,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        } else {
                            // Lista de decks
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 24.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                contentPadding = PaddingValues(vertical = 16.dp)
                            ) {
                                items(decksInFolder.size) { index ->
                                    DeckItemCardSimple(
                                        deck = decksInFolder[index],
                                        onStudyClick = {
                                            navController.navigate("$ESCOLHA_MODO_ESTUDO_ROUTE/${decksInFolder[index].id}")
                                        }
                                    )
                                }
                            }
                        }
                    }
                    is LibraryState.Idle -> {
                        // Estado inicial - não faz nada, aguarda o LaunchedEffect
                    }
                }
            }
        }
    }
}

@Composable
fun DeckItemCardSimple(
    deck: DeckResponse,
    onStudyClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(
                        Icons.Default.MenuBook,
                        contentDescription = "Deck",
                        tint = YellowAccent,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            text = deck.filePath,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (deck.totalFlashcards > 0) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0xFF00BCD4).copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        "${deck.totalFlashcards} cards",
                                        fontSize = 12.sp,
                                        color = Color(0xFF00BCD4),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            if (deck.hasQuiz) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = YellowAccent.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        "Quiz",
                                        fontSize = 12.sp,
                                        color = YellowAccent,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Botão de estudar
            Button(
                onClick = onStudyClick,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = YellowAccent),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = "Estudar", tint = Color.Black)
                Spacer(Modifier.width(8.dp))
                Text(
                    "Estudar",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}
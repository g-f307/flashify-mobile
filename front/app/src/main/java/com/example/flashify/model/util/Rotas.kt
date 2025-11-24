package com.example.flashify.model.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.flashify.view.ui.screen.login.TelaLogin
import com.example.flashify.view.ui.screen.principal.TelaPrincipal
import com.example.flashify.view.ui.screen.principal.TelaPrincipalBiblioteca
import com.example.flashify.view.ui.screen.principal.TelaPrincipalConfiguracao
import com.example.flashify.view.ui.screen.principal.TelaProgresso
import com.example.flashify.view.ui.screen.principal.TelaCriacaoFlashCard
import com.example.flashify.view.ui.screen.principal.TelaEstudo
import com.example.flashify.view.ui.screen.principal.TelaQuiz
import com.example.flashify.view.ui.screen.principal.TelaEscolhaModoEstudo
import com.example.flashify.view.ui.screen.principal.TelaDetalhePasta
import com.example.flashify.view.ui.screen.principal.TelaContentLoader
import com.example.flashify.view.ui.screen.landing.TelaLanding


// Importando o TokenManager
import com.example.flashify.model.manager.TokenManager
import com.example.flashify.view.ui.screen.login.TelaRegistro

// --- Definindo as "Rotas" (os nomes das nossas telas) ---
const val LANDING_SCREEN_ROUTE = "landing_screen"
const val LOGIN_SCREEN_ROUTE = "login_screen"
const val REGISTER_SCREEN_ROUTE = "register_screen"
const val MAIN_SCREEN_ROUTE = "main_screen"
const val CREATE_FLASHCARD_ROUTE = "create_flashcard_screen"
const val BIBLIOTECA_SCREEN_ROUTE = "biblioteca_screen_route"
const val PROGRESSO_SCREEN_ROUTE = "progresso_screen_route"
const val CONFIGURATION_SCREEN_ROUTE = "configuration_screen_route"
const val ESTUDO_SCREEN_ROUTE = "estudo_screen_route"
const val QUIZ_SCREEN_ROUTE = "quiz_screen_route"
const val ESCOLHA_MODO_ESTUDO_ROUTE = "escolha_modo_estudo_route"
const val DETALHE_PASTA_ROUTE = "detalhe_pasta_route"
const val CONTENT_LOADER_ROUTE = "content_loader_route"


@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current
    // 1. Cria uma instância do TokenManager
    val tokenManager = remember { TokenManager(context) }
    // 2. Verifica se existe um token guardado
    val token = tokenManager.getToken()
    // 3. Define a rota inicial com base na existência do token
    val startDestination =
        if (token != null && token.startsWith("Bearer ")) {
        MAIN_SCREEN_ROUTE
    } else {
        LANDING_SCREEN_ROUTE // ✅ MUDANÇA AQUI (era LOGIN_SCREEN_ROUTE)
    }

    NavHost(
        navController = navController,
        startDestination = startDestination // Usa a rota inicial dinâmica
    ) {
        // --- Definições das Rotas (como antes) ---
        composable(route = LANDING_SCREEN_ROUTE) {
            TelaLanding(navController = navController)
        }
        composable(route = LOGIN_SCREEN_ROUTE) {
            TelaLogin(navController = navController)
        }
        composable(route = REGISTER_SCREEN_ROUTE) {
            TelaRegistro(navController = navController) // Corrigido o nome da tela
        }
        composable(route = MAIN_SCREEN_ROUTE) {
            TelaPrincipal(navController = navController)
        }
        composable(route = BIBLIOTECA_SCREEN_ROUTE){
            TelaPrincipalBiblioteca(navController)
        }
        composable(route = PROGRESSO_SCREEN_ROUTE){
            TelaProgresso(navController)
        }
        composable(route = CONFIGURATION_SCREEN_ROUTE){
            TelaPrincipalConfiguracao(navController = navController) // Passa o navController
        }
        composable(
            route = "$ESTUDO_SCREEN_ROUTE/{deckId}",
            arguments = listOf(navArgument("deckId") { type = NavType.IntType })
        ) { backStackEntry ->
            val deckId = backStackEntry.arguments?.getInt("deckId") ?: -1
            if (deckId != -1) {
                TelaEstudo(navController = navController, deckId = deckId)
            }
        }
        composable(
            route = "$QUIZ_SCREEN_ROUTE/{deckId}",
            arguments = listOf(navArgument("deckId") { type = NavType.IntType })
        ) { backStackEntry ->
            val deckId = backStackEntry.arguments?.getInt("deckId") ?: -1
            if (deckId != -1) {
                TelaQuiz(navController = navController, documentId = deckId)
            }
        }
        composable(
            route = "$ESCOLHA_MODO_ESTUDO_ROUTE/{deckId}",
            arguments = listOf(navArgument("deckId") { type = NavType.IntType })
        ) { backStackEntry ->
            val deckId = backStackEntry.arguments?.getInt("deckId") ?: -1
            if (deckId != -1) {
                TelaEscolhaModoEstudo(navController = navController, deckId = deckId)
            }
        }
        composable(
            route = "$DETALHE_PASTA_ROUTE/{folderId}/{folderName}",
            arguments = listOf(
                navArgument("folderId") { type = NavType.IntType },
                navArgument("folderName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val folderId = backStackEntry.arguments?.getInt("folderId") ?: -1
            val folderName = backStackEntry.arguments?.getString("folderName") ?: "Pasta"
            if (folderId != -1) {
                TelaDetalhePasta(
                    navController = navController,
                    folderId = folderId,
                    folderName = folderName
                )
            }
        }

        composable(
            route = "$CREATE_FLASHCARD_ROUTE?folderId={folderId}",
            arguments = listOf(
                navArgument("folderId") {
                    type = NavType.IntType
                    defaultValue = -1  // -1 significa "sem pasta" (criar na raiz)
                }
            )
        ) { backStackEntry ->
            val folderId = backStackEntry.arguments?.getInt("folderId") ?: -1
            TelaCriacaoFlashCard(
                navController = navController,
                folderId = if (folderId == -1) null else folderId
            )
        }
        
        composable(
            route = "$CONTENT_LOADER_ROUTE/{documentId}/{generatesFlashcards}/{generatesQuizzes}",
            arguments = listOf(
                navArgument("documentId") { type = NavType.IntType },
                navArgument("generatesFlashcards") { type = NavType.BoolType },
                navArgument("generatesQuizzes") { type = NavType.BoolType }
            )
        ) { backStackEntry ->
            val documentId = backStackEntry.arguments?.getInt("documentId") ?: -1
            val generatesFlashcards = backStackEntry.arguments?.getBoolean("generatesFlashcards") ?: true
            val generatesQuizzes = backStackEntry.arguments?.getBoolean("generatesQuizzes") ?: false
            if (documentId != -1) {
                TelaContentLoader(
                    navController = navController,
                    documentId = documentId,
                    generatesFlashcards = generatesFlashcards,
                    generatesQuizzes = generatesQuizzes
                )
            }
        }
    }
}


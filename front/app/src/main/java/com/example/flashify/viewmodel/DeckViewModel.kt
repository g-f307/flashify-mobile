package com.example.flashify.viewmodel

//Importando o database e o DAO
import android.app.Application
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.flashify.model.data.DeckResponse
import com.example.flashify.model.data.DeckStatsResponse
import com.example.flashify.model.data.DeckUpdateRequest
import com.example.flashify.model.data.TextDeckCreateRequest
import com.example.flashify.model.database.AppDatabase
import com.example.flashify.model.database.dataclass.DeckEntity
import com.example.flashify.model.manager.TokenManager
import com.example.flashify.model.network.Api
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody // <-- CORREÇÃO: Importar RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

import java.io.InputStream


// Define os estados da UI para a lista de decks
sealed class DeckListState {
    object Loading : DeckListState()
    data class Success(
        val decks: List<DeckResponse>,
        val recentDeck: DeckResponse?
    ) : DeckListState()
    data class Error(val message: String) : DeckListState()
}

// Define os estados da UI para a criação de um novo deck
sealed class DeckCreationState {
    object Idle : DeckCreationState()
    object Loading : DeckCreationState()
    data class Success(val deck: DeckResponse) : DeckCreationState()
    data class Error(val message: String) : DeckCreationState()
}

// ESTADO PARA MONITORAR O PROCESSAMENTO DO DOCUMENTO
sealed class DocumentProcessingState {
    object Idle : DocumentProcessingState()
    data class Processing(val currentStep: String?) : DocumentProcessingState()
    object Completed : DocumentProcessingState()
    data class Error(val message: String) : DocumentProcessingState()
}

// ESTADO PARA AÇÕES DE DECK (DELETAR, EDITAR)
sealed class DeckActionState {
    object Idle : DeckActionState()
    object Loading : DeckActionState()
    data class Success(val message: String) : DeckActionState()
    data class Error(val message: String) : DeckActionState()
}

// ✅ NOVO ESTADO PARA AS ESTATÍSTICAS DE UM DECK
sealed class DeckStatsState {
    object Idle : DeckStatsState()
    object Loading : DeckStatsState()
    data class Success(val stats: DeckStatsResponse) : DeckStatsState()
    data class Error(val message: String) : DeckStatsState()
}


class DeckViewModel(application: Application) : AndroidViewModel(application) {

    private val tokenManager = TokenManager(application)
    private val apiService = Api.retrofitService
    private val flashcardDao = AppDatabase.getDatabase(application).flashcardDao()
    private val deckDao = AppDatabase.getDatabase(application).deckDao()

    // StateFlow para a lista de decks
    private val _deckListState = MutableStateFlow<DeckListState>(DeckListState.Loading)
    val deckListState = _deckListState.asStateFlow()

    // StateFlow para o processo de criação
    private val _deckCreationState = MutableStateFlow<DeckCreationState>(DeckCreationState.Idle)
    val deckCreationState = _deckCreationState.asStateFlow()

    // STATEFLOW PARA AÇÕES
    private val _deckActionState = MutableStateFlow<DeckActionState>(DeckActionState.Idle)
    val deckActionState = _deckActionState.asStateFlow()

    // STATEFLOW PARA PROCESSAMENTO DE DOCUMENTO
    private val _documentProcessingState = MutableStateFlow<DocumentProcessingState>(DocumentProcessingState.Idle)
    val documentProcessingState = _documentProcessingState.asStateFlow()

    // ✅ NOVO STATEFLOW PARA ESTATÍSTICAS
    private val _deckStatsState = MutableStateFlow<DeckStatsState>(DeckStatsState.Idle)
    val deckStatsState = _deckStatsState.asStateFlow()

    // Job para controlar o polling
    private var pollingJob: kotlinx.coroutines.Job? = null

    init {
        // Ao iniciar o ViewModel, busca a lista de decks
        fetchDecks()
    }

    //Função auxiliar para obter o ID do usuário
    private fun getCurrentUserId(): Int = tokenManager.getUserId()

    fun fetchDecks() {
        viewModelScope.launch {
            _deckListState.value = DeckListState.Loading
            val userId = getCurrentUserId()
            if (userId == TokenManager.INVALID_USER_ID) {
                _deckListState.value = DeckListState.Error("Utilizador inválido.")
                return@launch
            }

            val token = tokenManager.getToken()
            if (token == null) {
                _deckListState.value = DeckListState.Error("Sua sessão expirou.")
                return@launch
            }

            try {
                // BUSCA DIRETAMENTE DA REDE
                val networkDecksResponse = apiService.getDecks(token)

                // Encontra o deck mais recente que foi estudado
                val recentDeck = networkDecksResponse
                    .filter { it.studiedFlashcards > 0 }
                    .maxByOrNull { it.createdAt }

                _deckListState.value = DeckListState.Success(networkDecksResponse, recentDeck)

                // OPCIONAL: Ainda atualiza o cache em background
                val networkDeckEntities = networkDecksResponse.map { it.toDeckEntity(userId) }
                deckDao.insertDecks(networkDeckEntities)

            } catch (e: Exception) {
                // Se a rede falhar, tenta carregar do cache como fallback
                println("Aviso: Falha ao buscar decks da rede: ${e.message}")
                val localDecks = deckDao.getAllDecksForUser(userId).map { it.toDeckResponse() }

                if (localDecks.isNotEmpty()) {
                    val recentDeck = localDecks
                        .filter { it.studiedFlashcards > 0 }
                        .maxByOrNull { it.createdAt }
                    _deckListState.value = DeckListState.Success(localDecks, recentDeck)
                } else {
                    _deckListState.value = DeckListState.Error("Falha ao carregar decks: ${e.message}")
                }
            }
        }
    }

    // ✅ NOVA FUNÇÃO PARA BUSCAR ESTATÍSTICAS DE UM DECK ESPECÍFICO
    fun fetchDeckStats(documentId: Int) {
        viewModelScope.launch {
            _deckStatsState.value = DeckStatsState.Loading
            try {
                val token = tokenManager.getToken()
                if (token == null) {
                    _deckStatsState.value = DeckStatsState.Error("Sessão expirada")
                    return@launch
                }

                // Chama o endpoint de estatísticas, igual a app web faz
                val stats = Api.retrofitService.getDocumentStats(token, documentId)
                _deckStatsState.value = DeckStatsState.Success(stats)

                Log.d("DeckViewModel", "📊 Stats atualizadas para deck $documentId")

            } catch (e: Exception) {
                Log.e("DeckViewModel", "❌ Erro ao buscar stats: ${e.message}")
                _deckStatsState.value = DeckStatsState.Error(e.message ?: "Erro ao buscar estatísticas")
            }
        }
    }

    // ✅ NOVA FUNÇÃO PARA LIMPAR O ESTADO DAS ESTATÍSTICAS
    fun resetStatsState() {
        _deckStatsState.value = DeckStatsState.Idle
    }

    fun createDeckFromText(
        title: String,
        text: String,
        quantity: Int,
        generateQuiz: Boolean = false,
        numQuestions: Int = 5,
        folderId: Int? = null  // ✅ NOVO PARÂMETRO
    ) {
        viewModelScope.launch {
            _deckCreationState.value = DeckCreationState.Loading
            val token = tokenManager.getToken()
            if (token == null) {
                _deckCreationState.value = DeckCreationState.Error("A sua sessão expirou. Por favor, faça login novamente.")
                return@launch
            }

            try {
                val request = TextDeckCreateRequest(
                    text = text,
                    title = title,
                    num_flashcards = quantity,
                    difficulty = "Médio",
                    generate_flashcards = true,
                    generate_quizzes = generateQuiz,
                    content_type = if (generateQuiz) "both" else "flashcards",
                    num_questions = numQuestions,
                    folder_id = folderId  // ✅ ADICIONAR AO REQUEST
                )
                val response = apiService.createDeckFromText(token, request)
                _deckCreationState.value = DeckCreationState.Success(response)
                fetchDecks()
            } catch (e: Exception) {
                _deckCreationState.value = DeckCreationState.Error("Erro ao criar deck: ${e.message}")
            }
        }
    }

    fun createDeckFromFile(
        title: String,
        fileUri: Uri,
        quantity: Int,
        generateQuiz: Boolean = false,
        numQuestions: Int = 5,
        folderId: Int? = null  // ✅ NOVO PARÂMETRO
    ) {
        viewModelScope.launch {
            _deckCreationState.value = DeckCreationState.Loading
            val token = tokenManager.getToken()
            if (token == null) {
                _deckCreationState.value = DeckCreationState.Error("Sessão expirada.")
                return@launch
            }

            try {
                val context = getApplication<Application>().applicationContext
                val fileName = getFileName(context, fileUri)

                val plainTextType = "text/plain".toMediaTypeOrNull()
                val titlePart = title.toRequestBody(plainTextType)
                val quantityPart = quantity.toString().toRequestBody(plainTextType)
                val difficultyPart = "Médio".toRequestBody(plainTextType)
                val generateFlashcardsPart = "true".toRequestBody(plainTextType)
                val generateQuizzesPart = generateQuiz.toString().toRequestBody(plainTextType)
                val contentTypePart = (if (generateQuiz) "both" else "flashcards").toRequestBody(plainTextType)
                val numQuestionsPart = numQuestions.toString().toRequestBody(plainTextType)

                // ✅ ADICIONAR FOLDER_ID COMO PART
                val folderIdPart = folderId?.toString()?.toRequestBody(plainTextType)

                val inputStream: InputStream? = context.contentResolver.openInputStream(fileUri)
                val fileBytes = inputStream?.readBytes()
                inputStream?.close()

                if (fileBytes == null) {
                    _deckCreationState.value = DeckCreationState.Error("Não foi possível ler o arquivo selecionado.")
                    return@launch
                }

                val mimeType = context.contentResolver.getType(fileUri)
                val mediaType = if (mimeType != null) mimeType.toMediaTypeOrNull() else null
                val fileRequestBody = fileBytes.toRequestBody(mediaType)
                val filePart = MultipartBody.Part.createFormData("file", fileName, fileRequestBody)

                val response = apiService.uploadDocument(
                    token = token,
                    file = filePart,
                    title = titlePart,
                    numFlashcards = quantityPart,
                    difficulty = difficultyPart,
                    generatesFlashcards = generateFlashcardsPart,
                    generatesQuizzes = generateQuizzesPart,
                    contentType = contentTypePart,
                    numQuestions = numQuestionsPart,
                    folderId = folderIdPart  // ✅ PASSAR FOLDER_ID
                )

                _deckCreationState.value = DeckCreationState.Success(response)

            } catch (e: Exception) {
                _deckCreationState.value = DeckCreationState.Error("Erro ao enviar arquivo: ${e.message}")
                e.printStackTrace()
            }
        }
    }


    fun deleteDeck(deckId: Int) {
        viewModelScope.launch {
            _deckActionState.value = DeckActionState.Loading
            val token = tokenManager.getToken()
            val userId = getCurrentUserId()

            if (token == null || userId == TokenManager.INVALID_USER_ID) {
                _deckActionState.value = DeckActionState.Error("Sessão inválida.")
                return@launch
            }

            try {
                apiService.deleteDocument(token, deckId)
                deckDao.deleteDeckByIdForUser(deckId, userId)

                val currentState = _deckListState.value
                if (currentState is DeckListState.Success) {
                    val updatedDecks = currentState.decks.filterNot { it.id == deckId }
                    val recentDeck = updatedDecks
                        .filter { it.studiedFlashcards > 0 }
                        .maxByOrNull { it.createdAt }

                    _deckListState.value = DeckListState.Success(updatedDecks, recentDeck)
                }
                _deckActionState.value = DeckActionState.Success("Deck excluído com sucesso!")
            } catch (e: Exception) {
                _deckActionState.value = DeckActionState.Error(e.message ?: "Falha ao excluir deck")
            }
        }
    }


    fun renameDeck(deckId: Int, newTitle: String) {
        viewModelScope.launch {
            _deckActionState.value = DeckActionState.Loading
            val token = tokenManager.getToken()
            val userId = getCurrentUserId()
            if (token == null || userId == TokenManager.INVALID_USER_ID) {
                _deckActionState.value = DeckActionState.Error("Sessão expirada.")
                return@launch
            }

            try {
                val request = DeckUpdateRequest(title = newTitle)
                val updatedDeckResponse = apiService.updateDocument(token, deckId, request)
                val updatedEntity = updatedDeckResponse.toDeckEntity(userId)
                deckDao.insertDecks(listOf(updatedEntity))

                val currentState = _deckListState.value
                if (currentState is DeckListState.Success) {
                    val updatedDecks = currentState.decks.map {
                        if (it.id == deckId) {
                            updatedDeckResponse
                        } else {
                            it
                        }
                    }
                    val recentDeck = updatedDecks
                        .filter { it.studiedFlashcards > 0 }
                        .maxByOrNull { it.createdAt }

                    _deckListState.value = DeckListState.Success(updatedDecks, recentDeck)
                }
                _deckActionState.value = DeckActionState.Success("Deck renomeado com sucesso!")
            } catch (e: Exception) {
                _deckActionState.value = DeckActionState.Error(e.message ?: "Falha ao renomear deck")
            }
        }
    }


    fun resetActionState() {
        _deckActionState.value = DeckActionState.Idle
    }

    fun moveDeckToFolder(deckId: Int, folderId: Int?) {
        viewModelScope.launch {
            _deckActionState.value = DeckActionState.Loading
            try {
                val token = tokenManager.getToken()
                if (token == null) {
                    _deckActionState.value = DeckActionState.Error("Token não encontrado")
                    return@launch
                }

                val request = com.example.flashify.model.data.DocumentUpdateFolder(folderId)
                Api.retrofitService.moveDocumentToFolder(token, deckId, request)

                _deckActionState.value = DeckActionState.Success("Deck movido com sucesso")
                fetchDecks() // Recarregar lista
            } catch (e: Exception) {
                _deckActionState.value = DeckActionState.Error(e.message ?: "Erro ao mover deck")
            }
        }
    }

    fun resetCreationState() {
        _deckCreationState.value = DeckCreationState.Idle
    }

    private fun getFileName(context: android.content.Context, uri: Uri): String {
        var fileName: String? = null
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) {
                    fileName = it.getString(nameIndex)
                }
            }
        }
        return fileName ?: "unknown_file"
    }

    fun DeckEntity.toDeckResponse(): DeckResponse {
        return DeckResponse(id, filePath, status, createdAt, totalFlashcards, studiedFlashcards, null /*currentStep não está na Entity*/)
    }

    fun DeckResponse.toDeckEntity(userId: Int): DeckEntity {
        return DeckEntity(id, filePath, status, createdAt, totalFlashcards, studiedFlashcards, userId) // Inclui userId
    }

    fun startDocumentPolling(documentId: Int) {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            _documentProcessingState.value = DocumentProcessingState.Processing(null)
            while (true) {
                try {
                    val token = tokenManager.getToken()
                    if (token == null) {
                        _documentProcessingState.value = DocumentProcessingState.Error("Sessão expirada")
                        break
                    }
                    val documentDetail = apiService.getDocumentDetailWithQuiz(token, documentId)
                    when (documentDetail.status.uppercase()) {
                        "COMPLETED" -> {
                            _documentProcessingState.value = DocumentProcessingState.Completed
                            break
                        }
                        "FAILED" -> {
                            _documentProcessingState.value = DocumentProcessingState.Error(
                                documentDetail.currentStep ?: "Erro no processamento"
                            )
                            break
                        }
                        "CANCELLED" -> {
                            _documentProcessingState.value = DocumentProcessingState.Error("Processamento cancelado")
                            break
                        }
                        "PROCESSING" -> {
                            _documentProcessingState.value = DocumentProcessingState.Processing(
                                documentDetail.currentStep
                            )
                        }
                    }
                    kotlinx.coroutines.delay(2000)
                } catch (e: Exception) {
                    _documentProcessingState.value = DocumentProcessingState.Error(
                        e.message ?: "Erro ao verificar status"
                    )
                    break
                }
            }
        }
    }

    fun stopDocumentPolling() {
        pollingJob?.cancel()
        pollingJob = null
        _documentProcessingState.value = DocumentProcessingState.Idle
    }
}
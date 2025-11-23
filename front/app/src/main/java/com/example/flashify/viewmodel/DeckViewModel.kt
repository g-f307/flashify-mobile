package com.example.flashify.viewmodel

import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flashify.model.data.DeckResponse
import com.example.flashify.model.data.DeckStatsResponse
import com.example.flashify.model.data.DeckUpdateRequest
import com.example.flashify.model.data.TextDeckCreateRequest
import com.example.flashify.model.database.dao.DeckDao
import com.example.flashify.model.database.dao.FlashcardDao
import com.example.flashify.model.database.dataclass.DeckEntity
import com.example.flashify.model.manager.TokenManager
import com.example.flashify.model.network.ApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.InputStream
import javax.inject.Inject

sealed class DeckListState {
    object Loading : DeckListState()
    data class Success(
        val decks: List<DeckResponse>,
        val recentDeck: DeckResponse?
    ) : DeckListState()
    data class Error(val message: String) : DeckListState()
}

sealed class DeckCreationState {
    object Idle : DeckCreationState()
    object Loading : DeckCreationState()
    data class Success(val deck: DeckResponse) : DeckCreationState()
    data class Error(val message: String) : DeckCreationState()
}

sealed class DocumentProcessingState {
    object Idle : DocumentProcessingState()
    data class Processing(val currentStep: String?) : DocumentProcessingState()
    object Completed : DocumentProcessingState()
    data class Error(val message: String) : DocumentProcessingState()
}

sealed class DeckActionState {
    object Idle : DeckActionState()
    object Loading : DeckActionState()
    data class Success(val message: String) : DeckActionState()
    data class Error(val message: String) : DeckActionState()
}

sealed class DeckStatsState {
    object Idle : DeckStatsState()
    object Loading : DeckStatsState()
    data class Success(val stats: DeckStatsResponse) : DeckStatsState()
    data class Error(val message: String) : DeckStatsState()
}

sealed class GenerationLimitState {
    object Idle : GenerationLimitState()
    object Loading : GenerationLimitState()
    data class Success(val info: com.example.flashify.model.data.GenerationLimitResponse) : GenerationLimitState()
    data class Error(val message: String) : GenerationLimitState()
}

sealed class AddContentState {
    object Idle : AddContentState()
    object Loading : AddContentState()
    data class Success(val message: String) : AddContentState()
    data class Error(val message: String) : AddContentState()
}

@HiltViewModel
class DeckViewModel @Inject constructor(
    private val tokenManager: TokenManager,
    private val apiService: ApiService,
    private val deckDao: DeckDao,
    private val flashcardDao: FlashcardDao,
    private val contentResolver: ContentResolver
) : ViewModel() {

    private val _deckListState = MutableStateFlow<DeckListState>(DeckListState.Loading)
    val deckListState = _deckListState.asStateFlow()

    private val _deckCreationState = MutableStateFlow<DeckCreationState>(DeckCreationState.Idle)
    val deckCreationState = _deckCreationState.asStateFlow()

    private val _deckActionState = MutableStateFlow<DeckActionState>(DeckActionState.Idle)
    val deckActionState = _deckActionState.asStateFlow()

    private val _documentProcessingState = MutableStateFlow<DocumentProcessingState>(DocumentProcessingState.Idle)
    val documentProcessingState = _documentProcessingState.asStateFlow()

    private val _deckStatsState = MutableStateFlow<DeckStatsState>(DeckStatsState.Idle)
    val deckStatsState = _deckStatsState.asStateFlow()

    private val _generationLimitState = MutableStateFlow<GenerationLimitState>(GenerationLimitState.Idle)
    val generationLimitState = _generationLimitState.asStateFlow()

    private val _addContentState = MutableStateFlow<AddContentState>(AddContentState.Idle)
    val addContentState = _addContentState.asStateFlow()

    // ✅ CORRIGIDO: StateFlow para sincronização de dados
    private val _syncCompleted = MutableStateFlow(false)
    val syncCompleted = _syncCompleted.asStateFlow()

    private var pollingJob: kotlinx.coroutines.Job? = null

    init {
        fetchDecks()
    }

    private fun getCurrentUserId(): Int = tokenManager.getUserId()

    fun fetchDecks(showLoading: Boolean = true) {
        viewModelScope.launch {
            if (showLoading) {
                _deckListState.value = DeckListState.Loading
            }

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
                val networkDecksResponse = apiService.getDecks(token)

                val recentDeck = networkDecksResponse
                    .filter { it.studiedFlashcards > 0 }
                    .maxByOrNull { it.createdAt }

                _deckListState.value = DeckListState.Success(networkDecksResponse, recentDeck)

                val networkDeckEntities = networkDecksResponse.map { it.toDeckEntity(userId) }
                deckDao.insertDecks(networkDeckEntities)

            } catch (e: Exception) {
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

    fun fetchDeckStats(documentId: Int, showLoading: Boolean = true) {
        viewModelScope.launch {
            if (showLoading) {
                _deckStatsState.value = DeckStatsState.Loading
            }

            try {
                val token = tokenManager.getToken()
                if (token == null) {
                    _deckStatsState.value = DeckStatsState.Error("Sessão expirada")
                    return@launch
                }

                val stats = apiService.getDocumentStats(token, documentId)
                _deckStatsState.value = DeckStatsState.Success(stats)

                Log.d("DeckViewModel", "📊 Stats atualizadas para deck $documentId")

            } catch (e: Exception) {
                Log.e("DeckViewModel", "❌ Erro ao buscar stats: ${e.message}")
                _deckStatsState.value = DeckStatsState.Error(e.message ?: "Erro ao buscar estatísticas")
            }
        }
    }

    fun checkGenerationLimit() {
        viewModelScope.launch {
            _generationLimitState.value = GenerationLimitState.Loading
            val token = tokenManager.getToken()
            if (token == null) {
                _generationLimitState.value = GenerationLimitState.Error("Sessão inválida")
                return@launch
            }

            try {
                val limitInfo = apiService.getGenerationLimit(token)
                _generationLimitState.value = GenerationLimitState.Success(limitInfo)
            } catch (e: Exception) {
                _generationLimitState.value = GenerationLimitState.Error(e.message ?: "Erro ao verificar limite")
            }
        }
    }

    // ✅ CORRIGIDO: Adicionar Flashcards com sincronização adequada
    fun addFlashcardsToDeck(documentId: Int, quantity: Int, difficulty: String) {
        viewModelScope.launch {
            _addContentState.value = AddContentState.Loading
            _syncCompleted.value = false

            val token = tokenManager.getToken()
            if (token == null) {
                _addContentState.value = AddContentState.Error("Sessão expirada")
                return@launch
            }

            try {
                // 1️⃣ Fazer a requisição ao backend
                val request = com.example.flashify.model.data.AddFlashcardsRequest(numFlashcards = quantity)
                apiService.addMoreFlashcards(token, documentId, request)

                Log.d("DeckViewModel", "✅ Requisição de flashcards enviada ao backend")

                // 2️⃣ Aguardar 2 segundos para o backend processar
                kotlinx.coroutines.delay(2000)

                // 3️⃣ Buscar dados atualizados
                try {
                    fetchDecks(showLoading = false)
                    fetchDeckStats(documentId, showLoading = false)
                    checkGenerationLimit()

                    Log.d("DeckViewModel", "📥 Dados atualizados buscados")

                    // 4️⃣ Aguardar mais 1 segundo para garantir que StateFlows sejam atualizados
                    kotlinx.coroutines.delay(1000)

                    // 5️⃣ Verificar se os dados foram realmente atualizados
                    val currentStats = _deckStatsState.value
                    if (currentStats is DeckStatsState.Success) {
                        Log.d("DeckViewModel", "✅ Stats confirmadas: ${currentStats.stats.flashcards.total} flashcards")
                        _syncCompleted.value = true
                    } else {
                        Log.w("DeckViewModel", "⚠️ Stats ainda não carregadas, forçando sync")
                        _syncCompleted.value = true
                    }

                } catch (e: Exception) {
                    Log.e("DeckViewModel", "❌ Erro na sincronização: ${e.message}")
                    _syncCompleted.value = true // Permite continuar mesmo com erro
                }

                _addContentState.value = AddContentState.Success("Novos flashcards adicionados com sucesso!")

            } catch (e: Exception) {
                _syncCompleted.value = false
                _addContentState.value = AddContentState.Error(handleError(e, "Erro ao adicionar flashcards"))
            }
        }
    }

    // ✅ CORRIGIDO: Adicionar Perguntas com sincronização adequada
    fun addQuestionsToQuiz(documentId: Int, quantity: Int, difficulty: String) {
        viewModelScope.launch {
            _addContentState.value = AddContentState.Loading
            _syncCompleted.value = false

            val token = tokenManager.getToken()
            if (token == null) {
                _addContentState.value = AddContentState.Error("Sessão expirada")
                return@launch
            }

            try {
                // 1️⃣ Fazer a requisição ao backend
                val request = com.example.flashify.model.data.AddQuestionsRequest(numQuestions = quantity)
                apiService.addMoreQuestions(token, documentId, request)

                Log.d("DeckViewModel", "✅ Requisição de perguntas enviada ao backend")

                // 2️⃣ Aguardar 2 segundos para o backend processar
                kotlinx.coroutines.delay(2000)

                // 3️⃣ Buscar dados atualizados
                try {
                    fetchDecks(showLoading = false)
                    fetchDeckStats(documentId, showLoading = false)
                    checkGenerationLimit()

                    Log.d("DeckViewModel", "📥 Dados atualizados buscados")

                    // 4️⃣ Aguardar mais 1 segundo para garantir que StateFlows sejam atualizados
                    kotlinx.coroutines.delay(1000)

                    // 5️⃣ Verificar se os dados foram realmente atualizados
                    val currentStats = _deckStatsState.value
                    if (currentStats is DeckStatsState.Success) {
                        Log.d("DeckViewModel", "✅ Stats confirmadas")
                        _syncCompleted.value = true
                    } else {
                        Log.w("DeckViewModel", "⚠️ Stats ainda não carregadas, forçando sync")
                        _syncCompleted.value = true
                    }

                } catch (e: Exception) {
                    Log.e("DeckViewModel", "❌ Erro na sincronização: ${e.message}")
                    _syncCompleted.value = true // Permite continuar mesmo com erro
                }

                _addContentState.value = AddContentState.Success("Novas perguntas adicionadas com sucesso!")

            } catch (e: Exception) {
                _syncCompleted.value = false
                _addContentState.value = AddContentState.Error(handleError(e, "Erro ao adicionar perguntas"))
            }
        }
    }

    fun resetAddContentState() {
        _addContentState.value = AddContentState.Idle
        _syncCompleted.value = false
    }

    fun resetStatsState() {
        _deckStatsState.value = DeckStatsState.Idle
    }

    // ✅ CORRIGIDO: Verificar limite ANTES de criar deck
    fun createDeckFromText(
        title: String,
        text: String,
        quantity: Int,
        generateQuiz: Boolean = false,
        numQuestions: Int = 5,
        folderId: Int? = null
    ) {
        viewModelScope.launch {
            // ✅ VERIFICAR LIMITE ANTES DE PROCESSAR
            val currentLimit = _generationLimitState.value
            if (currentLimit is GenerationLimitState.Success) {
                if (currentLimit.info.used >= currentLimit.info.limit) {
                    _deckCreationState.value = DeckCreationState.Error(
                        "Limite diário de gerações atingido! Aguarde ${String.format("%.1f", currentLimit.info.hoursUntilReset)}h para resetar."
                    )
                    return@launch
                }
            }

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
                    folder_id = folderId
                )
                val response = apiService.createDeckFromText(token, request)
                _deckCreationState.value = DeckCreationState.Success(response)
                fetchDecks()
                checkGenerationLimit()
            } catch (e: Exception) {
                val errorMessage = handleError(e, "Erro ao criar deck")
                _deckCreationState.value = DeckCreationState.Error(errorMessage)
            }
        }
    }

    // ✅ CORRIGIDO: Verificar limite ANTES de criar deck
    fun createDeckFromFile(
        title: String,
        fileUri: Uri,
        quantity: Int,
        generateQuiz: Boolean = false,
        numQuestions: Int = 5,
        folderId: Int? = null
    ) {
        viewModelScope.launch {
            // ✅ VERIFICAR LIMITE ANTES DE PROCESSAR
            val currentLimit = _generationLimitState.value
            if (currentLimit is GenerationLimitState.Success) {
                if (currentLimit.info.used >= currentLimit.info.limit) {
                    _deckCreationState.value = DeckCreationState.Error(
                        "Limite diário de gerações atingido! Aguarde ${String.format("%.1f", currentLimit.info.hoursUntilReset)}h para resetar."
                    )
                    return@launch
                }
            }

            _deckCreationState.value = DeckCreationState.Loading
            val token = tokenManager.getToken()
            if (token == null) {
                _deckCreationState.value = DeckCreationState.Error("Sessão expirada.")
                return@launch
            }

            try {
                val fileName = getFileName(fileUri)

                val plainTextType = "text/plain".toMediaTypeOrNull()
                val titlePart = title.toRequestBody(plainTextType)
                val quantityPart = quantity.toString().toRequestBody(plainTextType)
                val difficultyPart = "Médio".toRequestBody(plainTextType)
                val generateFlashcardsPart = "true".toRequestBody(plainTextType)
                val generateQuizzesPart = generateQuiz.toString().toRequestBody(plainTextType)
                val contentTypePart = (if (generateQuiz) "both" else "flashcards").toRequestBody(plainTextType)
                val numQuestionsPart = numQuestions.toString().toRequestBody(plainTextType)

                val folderIdPart = folderId?.toString()?.toRequestBody(plainTextType)

                val inputStream: InputStream? = contentResolver.openInputStream(fileUri)
                val fileBytes = inputStream?.readBytes()
                inputStream?.close()

                if (fileBytes == null) {
                    _deckCreationState.value = DeckCreationState.Error("Não foi possível ler o arquivo selecionado.")
                    return@launch
                }

                val mimeType = contentResolver.getType(fileUri)
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
                    folderId = folderIdPart
                )

                _deckCreationState.value = DeckCreationState.Success(response)
                checkGenerationLimit()

            } catch (e: Exception) {
                val errorMessage = handleError(e, "Erro ao enviar arquivo")
                _deckCreationState.value = DeckCreationState.Error(errorMessage)
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
                apiService.moveDocumentToFolder(token, deckId, request)

                _deckActionState.value = DeckActionState.Success("Deck movido com sucesso")
                fetchDecks()
            } catch (e: Exception) {
                _deckActionState.value = DeckActionState.Error(e.message ?: "Erro ao mover deck")
            }
        }
    }

    fun resetCreationState() {
        _deckCreationState.value = DeckCreationState.Idle
    }

    // ✅ CORRIGIDO: Verificar limite ANTES de gerar
    fun generateFlashcardsForDocument(documentId: Int) {
        viewModelScope.launch {
            // ✅ VERIFICAR LIMITE ANTES DE PROCESSAR
            val currentLimit = _generationLimitState.value
            if (currentLimit is GenerationLimitState.Success) {
                if (currentLimit.info.used >= currentLimit.info.limit) {
                    _deckActionState.value = DeckActionState.Error(
                        "Limite diário de gerações atingido! Aguarde ${String.format("%.1f", currentLimit.info.hoursUntilReset)}h para resetar."
                    )
                    return@launch
                }
            }

            _deckActionState.value = DeckActionState.Loading
            try {
                val token = tokenManager.getToken()
                if (token == null) {
                    _deckActionState.value = DeckActionState.Error("Sessão expirada")
                    return@launch
                }

                apiService.generateFlashcardsForDocument(token, documentId)

                _deckActionState.value = DeckActionState.Success("Flashcards gerados com sucesso!")

                fetchDecks()
                fetchDeckStats(documentId)
                checkGenerationLimit()

            } catch (e: Exception) {
                val errorMessage = handleError(e, "Erro ao gerar flashcards")
                _deckActionState.value = DeckActionState.Error(errorMessage)
            }
        }
    }

    // ✅ CORRIGIDO: Verificar limite ANTES de gerar
    fun generateQuizForDocument(documentId: Int) {
        viewModelScope.launch {
            // ✅ VERIFICAR LIMITE ANTES DE PROCESSAR
            val currentLimit = _generationLimitState.value
            if (currentLimit is GenerationLimitState.Success) {
                if (currentLimit.info.used >= currentLimit.info.limit) {
                    _deckActionState.value = DeckActionState.Error(
                        "Limite diário de gerações atingido! Aguarde ${String.format("%.1f", currentLimit.info.hoursUntilReset)}h para resetar."
                    )
                    return@launch
                }
            }

            _deckActionState.value = DeckActionState.Loading
            try {
                val token = tokenManager.getToken()
                if (token == null) {
                    _deckActionState.value = DeckActionState.Error("Sessão expirada")
                    return@launch
                }

                apiService.generateQuizForDocument(token, documentId)

                _deckActionState.value = DeckActionState.Success("Quiz gerado com sucesso!")

                fetchDecks()
                fetchDeckStats(documentId)
                checkGenerationLimit()

            } catch (e: Exception) {
                val errorMessage = handleError(e, "Erro ao gerar quiz")
                _deckActionState.value = DeckActionState.Error(errorMessage)
            }
        }
    }

    private fun getFileName(uri: Uri): String {
        var fileName: String? = null
        val cursor = contentResolver.query(uri, null, null, null, null)
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

    private fun DeckEntity.toDeckResponse(): DeckResponse {
        return DeckResponse(id, filePath, status, createdAt, totalFlashcards, studiedFlashcards, null)
    }

    private fun DeckResponse.toDeckEntity(userId: Int): DeckEntity {
        return DeckEntity(id, filePath, status, createdAt, totalFlashcards, studiedFlashcards, userId)
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

    fun reloadDeck(documentId: Int) {
        viewModelScope.launch {
            try {
                Log.d("DeckViewModel", "📄 Recarregando deck $documentId...")

                fetchDecks(showLoading = false)
                fetchDeckStats(documentId, showLoading = false)

                Log.d("DeckViewModel", "✅ Deck $documentId recarregado com sucesso")
            } catch (e: Exception) {
                Log.e("DeckViewModel", "❌ Erro ao recarregar deck: ${e.message}")
            }
        }
    }

    fun reloadQuiz(documentId: Int) {
        viewModelScope.launch {
            try {
                Log.d("DeckViewModel", "📄 Recarregando quiz $documentId...")

                fetchDecks(showLoading = false)
                fetchDeckStats(documentId, showLoading = false)

                Log.d("DeckViewModel", "✅ Quiz $documentId recarregado com sucesso")
            } catch (e: Exception) {
                Log.e("DeckViewModel", "❌ Erro ao recarregar quiz: ${e.message}")
            }
        }
    }

    fun stopDocumentPolling() {
        pollingJob?.cancel()
        pollingJob = null
        _documentProcessingState.value = DocumentProcessingState.Idle
    }

    private fun handleError(e: Exception, defaultMessage: String): String {
        return if (e is retrofit2.HttpException && e.code() == 429) {
            "Limite diário de gerações atingido! Tente novamente mais tarde."
        } else {
            "${defaultMessage}: ${e.message ?: "Erro desconhecido"}"
        }
    }
}
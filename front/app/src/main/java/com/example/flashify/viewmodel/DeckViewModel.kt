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
import com.example.flashify.model.database.dao.AnswerDao
import com.example.flashify.model.database.dao.DeckDao
import com.example.flashify.model.database.dao.FlashcardDao
import com.example.flashify.model.database.dao.QuestionDao
import com.example.flashify.model.database.dao.QuizDao
import com.example.flashify.model.database.dataclass.AnswerEntity
import com.example.flashify.model.database.dataclass.DeckEntity
import com.example.flashify.model.database.dataclass.FlashcardEntity
import com.example.flashify.model.database.dataclass.QuestionEntity
import com.example.flashify.model.database.dataclass.QuizEntity
import com.example.flashify.model.manager.LocalUserManager
import com.example.flashify.model.manager.SyncManager
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
    private val contentResolver: ContentResolver,
    private val syncManager: SyncManager,
    private val localUserManager: LocalUserManager,
    // ✅ ADICIONAR ESTES DAOs:
    private val quizDao: QuizDao,
    private val questionDao: QuestionDao,
    private val answerDao: AnswerDao
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

    private val _syncCompleted = MutableStateFlow(false)
    val syncCompleted = _syncCompleted.asStateFlow()

    private var pollingJob: kotlinx.coroutines.Job? = null

    init {
        fetchDecks()
    }

    private fun getCurrentUserId(): Int = tokenManager.getUserId()

    /**
     * ✅ CORRIGIDO: Busca decks com suporte offline completo
     */
    /**
     * ✅ CORRIGIDO: Agora salva flashcards e quizzes no cache
     */
    // Em DeckViewModel.kt
// Substitua o método fetchDecks() existente por esta versão corrigida:

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

            // ✅ 1️⃣ Carregar do cache PRIMEIRO
            try {
                val localDecks = deckDao.getAllDecksForUser(userId).map { it.toDeckResponse() }

                Log.d("DeckViewModel", "🔍 === CACHE DE DECKS ===")
                Log.d("DeckViewModel", "🔍 User ID: $userId")
                Log.d("DeckViewModel", "🔍 Total decks: ${localDecks.size}")

                if (localDecks.isNotEmpty()) {
                    val recentDeck = localDecks
                        .filter { it.studiedFlashcards > 0 }
                        .maxByOrNull { it.createdAt }

                    _deckListState.value = DeckListState.Success(localDecks, recentDeck)
                    Log.d("DeckViewModel", "📦 ${localDecks.size} decks carregados do cache")
                }
            } catch (e: Exception) {
                Log.e("DeckViewModel", "❌ Erro ao ler cache: ${e.message}", e)
            }

            // ✅ 2️⃣ Se estiver ONLINE, sincronizar
            if (!syncManager.isOnline()) {
                Log.d("DeckViewModel", "📵 Modo offline - usando cache")
                if ((_deckListState.value as? DeckListState.Success)?.decks.isNullOrEmpty()) {
                    _deckListState.value = DeckListState.Error(
                        "Nenhum deck disponível offline. Conecte-se à internet primeiro."
                    )
                }
                return@launch
            }

            // ✅ 3️⃣ Sincronizar com verificação contínua de conectividade
            try {
                val networkDecksResponse = apiService.getDecks(token)
                val recentDeck = networkDecksResponse
                    .filter { it.studiedFlashcards > 0 }
                    .maxByOrNull { it.createdAt }

                _deckListState.value = DeckListState.Success(networkDecksResponse, recentDeck)

                // Atualizar cache de DECKS
                val networkDeckEntities = networkDecksResponse.map { it.toDeckEntity(userId) }
                deckDao.insertDecks(networkDeckEntities)
                Log.d("DeckViewModel", "🔄 ${networkDecksResponse.size} decks sincronizados")

                // ✅ 4️⃣ SALVAR FLASHCARDS com verificação de conectividade
                networkDecksResponse.forEach { deck ->
                    // ✅ VERIFICAR CONECTIVIDADE ANTES DE CADA CHAMADA
                    if (!syncManager.isOnline()) {
                        Log.w("DeckViewModel", "⚠️ Rede perdida durante sincronização - abortando")
                        return@launch
                    }

                    try {
                        val flashcardsResponse = apiService.getFlashcardsForDocument(token, deck.id)
                        val flashcardEntities = flashcardsResponse.map { flashcard ->
                            FlashcardEntity(
                                id = flashcard.id,
                                front = flashcard.front,
                                back = flashcard.back,
                                type = flashcard.type,
                                deckId = flashcard.documentId,
                                userId = userId
                            )
                        }

                        flashcardDao.deleteFlashcardsForDeckForUser(deck.id, userId)
                        flashcardDao.insertFlashcards(flashcardEntities)
                        Log.d("DeckViewModel", "✅ ${flashcardEntities.size} flashcards salvos para deck ${deck.id}")
                    } catch (e: Exception) {
                        // ✅ Não falhar toda a sincronização por erro em um deck
                        Log.e("DeckViewModel", "❌ Erro ao salvar flashcards do deck ${deck.id}: ${e.message}")
                        // Continua para o próximo deck
                    }
                }

                // ✅ 5️⃣ SALVAR QUIZZES com verificação de conectividade
                networkDecksResponse.filter { it.hasQuiz }.forEach { deck ->
                    // ✅ VERIFICAR CONECTIVIDADE ANTES DE CADA CHAMADA
                    if (!syncManager.isOnline()) {
                        Log.w("DeckViewModel", "⚠️ Rede perdida durante sincronização de quizzes - abortando")
                        return@launch
                    }

                    try {
                        val documentDetail = apiService.getDocumentDetailWithQuiz(token, deck.id)

                        if (documentDetail.quiz != null) {
                            val quiz = documentDetail.quiz

                            // Salvar quiz
                            val quizEntity = QuizEntity(
                                id = quiz.id,
                                title = quiz.title,
                                documentId = quiz.documentId,
                                userId = userId,
                                isSynced = true
                            )
                            quizDao.insertQuiz(quizEntity)

                            // Salvar perguntas
                            val questionEntities = quiz.questions.mapIndexed { index, q ->
                                QuestionEntity(
                                    id = q.id,
                                    text = q.text,
                                    quizId = q.quizId,
                                    userId = userId,
                                    orderIndex = index,
                                    isSynced = true
                                )
                            }
                            questionDao.insertQuestions(questionEntities)

                            // Salvar respostas
                            quiz.questions.forEach { question ->
                                val answerEntities = question.answers.mapIndexed { index, a ->
                                    AnswerEntity(
                                        id = a.id,
                                        text = a.text,
                                        isCorrect = a.isCorrect,
                                        explanation = a.explanation,
                                        questionId = a.questionId,
                                        userId = userId,
                                        orderIndex = index,
                                        isSynced = true
                                    )
                                }
                                answerDao.insertAnswers(answerEntities)
                            }

                            Log.d("DeckViewModel", "✅ Quiz ${quiz.id} salvo para deck ${deck.id}")
                        }
                    } catch (e: Exception) {
                        // ✅ Não falhar toda a sincronização por erro em um quiz
                        Log.e("DeckViewModel", "❌ Erro ao salvar quiz do deck ${deck.id}: ${e.message}")
                        // Continua para o próximo deck
                    }
                }

                Log.d("DeckViewModel", "✅ Sincronização completa")

            } catch (e: Exception) {
                Log.e("DeckViewModel", "⚠️ Erro na rede: ${e.message}")
                // ✅ Não sobrescrever estado de sucesso se já temos dados do cache
                if (_deckListState.value !is DeckListState.Success) {
                    _deckListState.value = DeckListState.Error("Falha ao conectar. Mostrando dados locais.")
                }
            }
        }
    }

    // DeckViewModel.kt
    fun fetchDeckStats(documentId: Int, showLoading: Boolean = true) {
        viewModelScope.launch {
            if (showLoading) {
                _deckStatsState.value = DeckStatsState.Loading
            }

            // 1. Se estiver ONLINE, busca da API (comportamento normal)
            if (syncManager.isOnline()) {
                try {
                    val token = tokenManager.getToken() ?: return@launch
                    val stats = apiService.getDocumentStats(token, documentId)
                    _deckStatsState.value = DeckStatsState.Success(stats)
                    Log.d("DeckViewModel", "📊 Stats atualizadas da REDE para deck $documentId")
                    return@launch
                } catch (e: Exception) {
                    Log.e("DeckViewModel", "⚠️ Erro na rede, tentando local: ${e.message}")
                }
            }

            // 2. Se estiver OFFLINE (ou rede falhou), calcula stats locais
            Log.d("DeckViewModel", "📵 Offline/Fallback - Calculando stats locais")
            val userId = getCurrentUserId()

            // Buscar deck local para totais
            val localDeck = deckDao.getDeckByIdForUser(documentId, userId)

            if (localDeck != null) {
                // Calcular progresso Flashcards
                val progress = if (localDeck.totalFlashcards > 0)
                    (localDeck.studiedFlashcards.toFloat() / localDeck.totalFlashcards) * 100f
                else 0f

                // Buscar tentativas de Quiz locais (Isso requer uma query no DAO, vou simular ou você pode adicionar)
                // Se você tiver acesso ao QuizDao aqui, pode fazer:
                // val quiz = quizDao.getQuizByDocumentId(documentId, userId)
                // val attempts = if (quiz != null) quizAttemptDao.getAttemptsByQuizId(quiz.id, userId) else emptyList()

                // Por enquanto, retornamos o básico do deck para desbloquear a UI
                _deckStatsState.value = DeckStatsState.Success(
                    DeckStatsResponse(
                        flashcards = com.example.flashify.model.data.FlashcardStatsResponse(
                            known = localDeck.studiedFlashcards,
                            learning = localDeck.totalFlashcards - localDeck.studiedFlashcards,
                            total = localDeck.totalFlashcards,
                            progressPercentage = progress
                        ),
                        quiz = null // O Quiz será ativado pelo 'hasQuiz' do Deck, não por stats nulas
                    )
                )
            } else {
                _deckStatsState.value = DeckStatsState.Error("Deck não encontrado localmente")
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
                val request = com.example.flashify.model.data.AddFlashcardsRequest(numFlashcards = quantity)
                apiService.addMoreFlashcards(token, documentId, request)

                Log.d("DeckViewModel", "✅ Requisição de flashcards enviada ao backend")

                kotlinx.coroutines.delay(2000)

                try {
                    fetchDecks(showLoading = false)
                    fetchDeckStats(documentId, showLoading = false)
                    checkGenerationLimit()

                    Log.d("DeckViewModel", "📥 Dados atualizados buscados")

                    kotlinx.coroutines.delay(1000)

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
                    _syncCompleted.value = true
                }

                _addContentState.value = AddContentState.Success("Novos flashcards adicionados com sucesso!")

            } catch (e: Exception) {
                _syncCompleted.value = false
                _addContentState.value = AddContentState.Error(handleError(e, "Erro ao adicionar flashcards"))
            }
        }
    }

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
                val request = com.example.flashify.model.data.AddQuestionsRequest(numQuestions = quantity)
                apiService.addMoreQuestions(token, documentId, request)

                Log.d("DeckViewModel", "✅ Requisição de perguntas enviada ao backend")

                kotlinx.coroutines.delay(2000)

                try {
                    fetchDecks(showLoading = false)
                    fetchDeckStats(documentId, showLoading = false)
                    checkGenerationLimit()

                    Log.d("DeckViewModel", "📥 Dados atualizados buscados")

                    kotlinx.coroutines.delay(1000)

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
                    _syncCompleted.value = true
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

    fun createDeckFromText(
        title: String,
        text: String,
        quantity: Int,
        generateQuiz: Boolean = false,
        numQuestions: Int = 5,
        folderId: Int? = null
    ) {
        viewModelScope.launch {
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

    fun createDeckFromFile(
        title: String,
        fileUri: Uri,
        quantity: Int,
        generateQuiz: Boolean = false,
        numQuestions: Int = 5,
        folderId: Int? = null
    ) {
        viewModelScope.launch {
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

    fun generateFlashcardsForDocument(documentId: Int) {
        viewModelScope.launch {
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

    fun generateQuizForDocument(documentId: Int) {
        viewModelScope.launch {
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
        return DeckResponse(
            id = id,
            filePath = filePath,
            status = status,
            createdAt = createdAt,
            totalFlashcards = totalFlashcards,
            studiedFlashcards = studiedFlashcards,
            currentStep = null,
            hasQuiz = hasQuiz, // ✅ LÊ DO BANCO
            folderId = null
        )
    }

    // ✅ CORRIGIDO: Mapeamento agora salva hasQuiz
    private fun DeckResponse.toDeckEntity(userId: Int): DeckEntity {
        return DeckEntity(
            id = id,
            filePath = filePath,
            status = status,
            createdAt = createdAt,
            totalFlashcards = totalFlashcards,
            studiedFlashcards = studiedFlashcards,
            userId = userId,
            hasQuiz = hasQuiz // ✅ SALVA NO BANCO
        )
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

    fun forceResyncQuizzes() {
        viewModelScope.launch {
            val userId = getCurrentUserId()
            if (userId == TokenManager.INVALID_USER_ID) return@launch

            try {
                Log.d("DeckViewModel", "🔄 Forçando ressincronização de quizzes...")

                // Limpar todos os quizzes do cache
                quizDao.deleteAllQuizzesForUser(userId)
                questionDao.deleteQuestionsByQuizId(-1, userId) // Limpa todas
                answerDao.deleteAnswersByQuestionId(-1, userId) // Limpa todas

                Log.d("DeckViewModel", "✅ Cache de quizzes limpo")

                // Buscar novamente
                fetchDecks(showLoading = true)

            } catch (e: Exception) {
                Log.e("DeckViewModel", "❌ Erro ao forçar resync: ${e.message}")
            }
        }
    }
}
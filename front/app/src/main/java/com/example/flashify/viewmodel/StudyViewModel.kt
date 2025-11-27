package com.example.flashify.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flashify.model.data.FlashcardResponse
import com.example.flashify.model.data.StudyLogRequest
import com.example.flashify.model.database.dao.DeckDao
import com.example.flashify.model.database.dao.FlashcardDao
import com.example.flashify.model.database.dao.StudyLogDao
import com.example.flashify.model.database.dataclass.FlashcardEntity
import com.example.flashify.model.database.dataclass.StudyLogEntity
import com.example.flashify.model.manager.SyncManager
import com.example.flashify.model.manager.TokenManager
import com.example.flashify.model.network.ApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class StudyState {
    object Loading : StudyState()
    data class Success(val flashcards: List<FlashcardResponse>) : StudyState()
    data class Error(val message: String) : StudyState()
}

sealed class FlashcardEditState {
    object Idle : FlashcardEditState()
    object Loading : FlashcardEditState()
    object Success : FlashcardEditState()
    data class Error(val message: String) : FlashcardEditState()
}

@HiltViewModel
class StudyViewModel @Inject constructor(
    private val tokenManager: TokenManager,
    private val apiService: ApiService,
    private val deckDao: DeckDao,
    private val flashcardDao: FlashcardDao,
    private val studyLogDao: StudyLogDao, // ✅ NOVO
    private val syncManager: SyncManager // ✅ NOVO
) : ViewModel() {

    private val _studyState = MutableStateFlow<StudyState>(StudyState.Loading)
    val studyState = _studyState.asStateFlow()

    private val _editState = MutableStateFlow<FlashcardEditState>(FlashcardEditState.Idle)
    val editState = _editState.asStateFlow()

    private var currentDeckId: Int? = null

    private fun getCurrentUserId(): Int = tokenManager.getUserId()

    // StudyViewModel.kt
    fun fetchFlashcards(deckId: Int) {
        currentDeckId = deckId
        viewModelScope.launch {
            _studyState.value = StudyState.Loading
            val userId = getCurrentUserId()

            Log.d("StudyViewModel", "🔍 === INÍCIO BUSCA FLASHCARDS ===")
            Log.d("StudyViewModel", "🔍 Deck ID: $deckId")
            Log.d("StudyViewModel", "🔍 User ID: $userId")
            Log.d("StudyViewModel", "🔍 Online: ${syncManager.isOnline()}")

            if (userId == TokenManager.INVALID_USER_ID) {
                _studyState.value = StudyState.Error("Utilizador inválido.")
                return@launch
            }

            try {
                // ✅ 1️⃣ SEMPRE tentar carregar do cache PRIMEIRO
                Log.d("StudyViewModel", "🔍 Tentando ler do CACHE...")

                val localFlashcards = flashcardDao.getFlashcardsForDeckForUser(deckId, userId)

                Log.d("StudyViewModel", "🔍 Resultado da query no CACHE: ${localFlashcards.size} flashcards")

                // 🔍 DETALHE DE CADA FLASHCARD (primeiros 3)
                localFlashcards.take(3).forEachIndexed { index, flashcard ->
                    Log.d("StudyViewModel", "🔍 Flashcard[$index]: id=${flashcard.id}, deckId=${flashcard.deckId}, userId=${flashcard.userId}")
                }

                val flashcardResponses = localFlashcards.map { it.toFlashcardResponse() }

                if (flashcardResponses.isNotEmpty()) {
                    // ✅ MOSTRAR CACHE IMEDIATAMENTE
                    _studyState.value = StudyState.Success(flashcardResponses)
                    Log.d("StudyViewModel", "✅ Flashcards carregados do CACHE (offline-ready)")

                    // ✅ Se estiver ONLINE, atualizar em background (não bloqueia a UI)
                    if (syncManager.isOnline()) {
                        Log.d("StudyViewModel", "🔄 Online detectado - atualizando cache em background")
                        fetchFlashcardsFromNetwork(deckId, userId, silent = true)
                    } else {
                        Log.d("StudyViewModel", "📵 Offline - usando apenas cache")
                    }
                    return@launch
                } else {
                    Log.w("StudyViewModel", "⚠️ Cache vazio - ${localFlashcards.size} flashcards encontrados")
                }

                // ✅ 2️⃣ Cache vazio - VERIFICAR se está offline
                if (!syncManager.isOnline()) {
                    Log.w("StudyViewModel", "📵 Offline e SEM CACHE - não é possível buscar")
                    _studyState.value = StudyState.Error(
                        "Estes flashcards não estão disponíveis offline. Conecte-se à internet para baixá-los."
                    )
                    return@launch
                }

                // ✅ 3️⃣ Online e cache vazio - buscar da rede
                Log.d("StudyViewModel", "🌐 Online e cache vazio - buscando da rede")
                fetchFlashcardsFromNetwork(deckId, userId, silent = false)

            } catch (e: Exception) {
                Log.e("StudyViewModel", "❌ Erro crítico: ${e.message}", e)
                _studyState.value = StudyState.Error("Erro ao carregar flashcards: ${e.message}")
            }
        }
    }


    /**
     * Busca flashcards da rede e atualiza o cache
     */
    private suspend fun fetchFlashcardsFromNetwork(deckId: Int, userId: Int, silent: Boolean) {
        val token = tokenManager.getToken()
        if (token == null) {
            if (!silent) {
                _studyState.value = StudyState.Error("Sessão inválida. Faça login novamente.")
            }
            return
        }

        try {
            val networkFlashcardsResponse = apiService.getFlashcardsForDocument(token, deckId)
            val networkFlashcardEntities = networkFlashcardsResponse.map {
                it.toFlashcardEntity(userId)
            }

            // Atualizar cache
            flashcardDao.deleteFlashcardsForDeckForUser(deckId, userId)
            flashcardDao.insertFlashcards(networkFlashcardEntities)

            Log.d("StudyViewModel", "🔄 ${networkFlashcardsResponse.size} flashcards sincronizados")

            // Atualizar UI
            val updatedLocalFlashcards = flashcardDao.getFlashcardsForDeckForUser(deckId, userId)
                .map { it.toFlashcardResponse() }

            if (updatedLocalFlashcards.isNotEmpty()) {
                _studyState.value = StudyState.Success(updatedLocalFlashcards)
            } else {
                if (!silent) {
                    _studyState.value = StudyState.Error("Este deck não possui flashcards.")
                }
            }

        } catch (e: Exception) {
            Log.e("StudyViewModel", "❌ Erro ao buscar flashcards: ${e.message}")
            if (!silent) {
                _studyState.value = StudyState.Error("Falha ao carregar flashcards: ${e.message}")
            }
        }
    }

    /**
     * ✅ NOVO: Registra estudo com suporte offline
     */
    fun logStudyResult(flashcardId: Int, accuracy: Float) {
        viewModelScope.launch {
            val userId = getCurrentUserId()
            val deckId = currentDeckId

            if (userId == TokenManager.INVALID_USER_ID || deckId == null) {
                Log.e("StudyViewModel", "❌ Erro: userId ou deckId inválido")
                return@launch
            }

            // 1️⃣ Salvar log localmente SEMPRE (offline-first)
            try {
                val logEntity = StudyLogEntity(
                    flashcardId = flashcardId,
                    userId = userId,
                    accuracy = accuracy,
                    studyDate = System.currentTimeMillis(),
                    isSynced = false // Marca como não sincronizado
                )

                studyLogDao.insertLog(logEntity)
                Log.d("StudyViewModel", "💾 Log salvo localmente (flashcard: $flashcardId)")

            } catch (e: Exception) {
                Log.e("StudyViewModel", "❌ Erro ao salvar log localmente: ${e.message}")
            }

            // 2️⃣ Se estiver online, tentar sincronizar imediatamente
            if (syncManager.isOnline()) {
                val token = tokenManager.getToken()
                if (token != null) {
                    try {
                        apiService.logStudy(token, flashcardId, StudyLogRequest(accuracy = accuracy))

                        // Marcar log como sincronizado
                        val unsyncedLogs = studyLogDao.getUnsyncedLogs(userId)
                        val thisLog = unsyncedLogs.lastOrNull { it.flashcardId == flashcardId }
                        thisLog?.let {
                            studyLogDao.markLogAsSynced(it.localId)
                            Log.d("StudyViewModel", "✅ Log sincronizado imediatamente")
                        }

                    } catch (e: Exception) {
                        Log.w("StudyViewModel", "⚠️ Erro ao sincronizar log: ${e.message} (será sincronizado depois)")
                    }
                }
            } else {
                Log.d("StudyViewModel", "📵 Offline - log será sincronizado quando estiver online")
            }
        }
    }

    /**
     * ✅ ATUALIZADO: Edita flashcard com suporte offline
     */
    fun updateFlashcard(flashcardId: Int, newFront: String?, newBack: String?) {
        viewModelScope.launch {
            _editState.value = FlashcardEditState.Loading
            val userId = getCurrentUserId()

            if (userId == TokenManager.INVALID_USER_ID) {
                _editState.value = FlashcardEditState.Error("Utilizador inválido.")
                return@launch
            }

            // 1️⃣ Atualizar localmente SEMPRE
            try {
                val currentFlashcard = currentDeckId?.let {
                    flashcardDao.getFlashcardsForDeckForUser(it, userId)
                        .find { it.id == flashcardId }
                }

                if (currentFlashcard != null) {
                    val updatedFlashcard = currentFlashcard.copy(
                        front = newFront ?: currentFlashcard.front,
                        back = newBack ?: currentFlashcard.back
                    )

                    flashcardDao.updateFlashcard(updatedFlashcard)
                    Log.d("StudyViewModel", "💾 Flashcard atualizado localmente")

                    // Atualizar UI
                    currentDeckId?.let { deckId ->
                        val localFlashcards = flashcardDao.getFlashcardsForDeckForUser(deckId, userId)
                            .map { it.toFlashcardResponse() }
                        _studyState.value = StudyState.Success(localFlashcards)
                    }
                }

            } catch (e: Exception) {
                Log.e("StudyViewModel", "❌ Erro ao atualizar localmente: ${e.message}")
                _editState.value = FlashcardEditState.Error("Erro ao salvar alteração")
                return@launch
            }

            // 2️⃣ Se estiver online, sincronizar com servidor
            if (syncManager.isOnline()) {
                val token = tokenManager.getToken()
                if (token != null) {
                    try {
                        val request = com.example.flashify.model.data.FlashcardUpdateRequest(
                            front = newFront,
                            back = newBack
                        )

                        apiService.updateFlashcard(token, flashcardId, request)
                        Log.d("StudyViewModel", "✅ Flashcard sincronizado com servidor")

                    } catch (e: Exception) {
                        Log.w("StudyViewModel", "⚠️ Erro ao sincronizar: ${e.message}")
                        // Não falhar - a alteração local já foi feita
                    }
                }
            }

            _editState.value = FlashcardEditState.Success
        }
    }

    fun resetEditState() {
        _editState.value = FlashcardEditState.Idle
    }

    private fun FlashcardEntity.toFlashcardResponse(): FlashcardResponse {
        return FlashcardResponse(id, front, back, type, deckId)
    }

    private fun FlashcardResponse.toFlashcardEntity(userId: Int): FlashcardEntity {
        return FlashcardEntity(id, front, back, type, documentId, userId)
    }
}
package com.example.flashify.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flashify.model.data.FlashcardResponse
import com.example.flashify.model.data.StudyLogRequest
import com.example.flashify.model.database.dao.DeckDao
import com.example.flashify.model.database.dao.FlashcardDao
import com.example.flashify.model.database.dataclass.FlashcardEntity
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
    private val flashcardDao: FlashcardDao
) : ViewModel() {

    private val _studyState = MutableStateFlow<StudyState>(StudyState.Loading)
    val studyState = _studyState.asStateFlow()

    private val _editState = MutableStateFlow<FlashcardEditState>(FlashcardEditState.Idle)
    val editState = _editState.asStateFlow()

    private var currentDeckId: Int? = null

    private fun getCurrentUserId(): Int = tokenManager.getUserId()

    fun fetchFlashcards(deckId: Int) {
        currentDeckId = deckId
        viewModelScope.launch {
            _studyState.value = StudyState.Loading
            val userId = getCurrentUserId()
            if (userId == TokenManager.INVALID_USER_ID) {
                _studyState.value = StudyState.Error("Utilizador inválido.")
                return@launch
            }

            try {
                val localFlashcards = flashcardDao.getFlashcardsForDeckForUser(deckId, userId)
                    .map { it.toFlashcardResponse() }

                if (localFlashcards.isNotEmpty()) {
                    _studyState.value = StudyState.Success(localFlashcards)
                    println("Flashcards carregados do cache local (offline).")
                    return@launch
                }
            } catch (e: Exception) {
                println("Erro ao ler cache de flashcards: ${e.message}")
            }

            println("Cache vazio para o deck $deckId. Buscando da rede...")
            val token = tokenManager.getToken()
            if (token == null) {
                _studyState.value = StudyState.Error("Sessão inválida. Faça login novamente.")
                return@launch
            }

            try {
                val networkFlashcardsResponse = apiService.getFlashcardsForDocument(token, deckId)
                val networkFlashcardEntities = networkFlashcardsResponse.map { it.toFlashcardEntity(userId) }

                flashcardDao.deleteFlashcardsForDeckForUser(deckId, userId)
                flashcardDao.insertFlashcards(networkFlashcardEntities)

                val updatedLocalFlashcards = flashcardDao.getFlashcardsForDeckForUser(deckId, userId)
                    .map { it.toFlashcardResponse() }

                if (updatedLocalFlashcards.isNotEmpty()) {
                    _studyState.value = StudyState.Success(updatedLocalFlashcards)
                } else {
                    _studyState.value = StudyState.Error("Este deck (ainda) não possui flashcards.")
                }

            } catch (e: Exception) {
                println("Error fetching network flashcards: ${e.message}")
                _studyState.value = StudyState.Error("Falha ao carregar flashcards: ${e.message}")
            }
        }
    }

    fun logStudyResult(flashcardId: Int, accuracy: Float) {
        viewModelScope.launch {
            val userId = getCurrentUserId()
            val deckId = currentDeckId
            if (userId == TokenManager.INVALID_USER_ID || deckId == null) {
                println("Error logging study: Invalid user or deckId is null")
                return@launch
            }
            val token = tokenManager.getToken() ?: return@launch

            try {
                apiService.logStudy(token, flashcardId, StudyLogRequest(accuracy = accuracy))
                println("Log de estudo enviado com sucesso para flashcard $flashcardId")

                val deck = deckDao.getDeckByIdForUser(deckId, userId)
                if (deck != null) {
                    if (accuracy >= 0.5f) {
                        val currentCount = flashcardDao.getFlashcardsForDeckForUser(deckId, userId)
                            .count { fc -> true }
                        println("Local deck progress update logic needs refinement.")
                    }
                }

            } catch (e: Exception) {
                println("Erro ao enviar log de estudo para API: ${e.message}")
            }
        }
    }

    fun updateFlashcard(flashcardId: Int, newFront: String?, newBack: String?) {
        viewModelScope.launch {
            _editState.value = FlashcardEditState.Loading
            val userId = getCurrentUserId()

            if (userId == TokenManager.INVALID_USER_ID) {
                _editState.value = FlashcardEditState.Error("Utilizador inválido.")
                return@launch
            }

            val token = tokenManager.getToken()
            if (token == null) {
                _editState.value = FlashcardEditState.Error("Sessão inválida.")
                return@launch
            }

            try {
                val request = com.example.flashify.model.data.FlashcardUpdateRequest(
                    front = newFront,
                    back = newBack
                )

                val updatedFlashcard = apiService.updateFlashcard(token, flashcardId, request)

                val updatedEntity = updatedFlashcard.toFlashcardEntity(userId)
                flashcardDao.updateFlashcard(updatedEntity)

                currentDeckId?.let { deckId ->
                    val localFlashcards = flashcardDao.getFlashcardsForDeckForUser(deckId, userId)
                        .map { it.toFlashcardResponse() }
                    _studyState.value = StudyState.Success(localFlashcards)
                }

                _editState.value = FlashcardEditState.Success

            } catch (e: Exception) {
                _editState.value = FlashcardEditState.Error(e.message ?: "Erro ao atualizar flashcard")
            }
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
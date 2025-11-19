package com.example.flashify.viewmodel // Or your correct package

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.flashify.model.data.FlashcardResponse
import com.example.flashify.model.data.StudyLogRequest
import com.example.flashify.model.database.AppDatabase // Correct import for AppDatabase
import com.example.flashify.model.database.dataclass.DeckEntity // Correct import for DeckEntity
import com.example.flashify.model.database.dataclass.FlashcardEntity // Correct import for FlashcardEntity
import com.example.flashify.model.manager.TokenManager
import com.example.flashify.model.network.Api
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Sealed class for UI states remains the same
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

class StudyViewModel(application: Application) : AndroidViewModel(application) {
    private val tokenManager = TokenManager(application)
    private val apiService = Api.retrofitService
    private val deckDao = AppDatabase.getDatabase(application).deckDao()
    private val flashcardDao = AppDatabase.getDatabase(application).flashcardDao()

    private val _studyState = MutableStateFlow<StudyState>(StudyState.Loading)
    val studyState = _studyState.asStateFlow()

    private val _editState = MutableStateFlow<FlashcardEditState>(FlashcardEditState.Idle)
    val editState = _editState.asStateFlow()

    // --- CORREÇÃO 1: Store the current deckId ---
    private var currentDeckId: Int? = null

    private fun getCurrentUserId(): Int = tokenManager.getUserId()

    fun fetchFlashcards(deckId: Int) {
        // --- CORREÇÃO 2: Save the deckId when fetching ---
        currentDeckId = deckId
        viewModelScope.launch {
            _studyState.value = StudyState.Loading
            val userId = getCurrentUserId()
            if (userId == TokenManager.INVALID_USER_ID) {
                _studyState.value = StudyState.Error("Utilizador inválido.")
                return@launch
            }

            // --- INÍCIO DA LÓGICA "CACHE-FIRST" ---

            // 1. TENTA BUSCAR DO CACHE LOCAL PRIMEIRO
            try {
                val localFlashcards = flashcardDao.getFlashcardsForDeckForUser(deckId, userId)
                    .map { it.toFlashcardResponse() }

                if (localFlashcards.isNotEmpty()) {
                    // 2. ENCONTROU NO CACHE! EXIBE OS DADOS E PARA A EXECUÇÃO.
                    _studyState.value = StudyState.Success(localFlashcards)
                    println("Flashcards carregados do cache local (offline).")
                    return@launch // <-- ESSA É A MUDANÇA MAIS IMPORTANTE
                }
            } catch (e: Exception) {
                println("Erro ao ler cache de flashcards: ${e.message}")
                // Se o cache falhar, não tem problema, vamos tentar a rede.
            }

            // 3. CACHE ESTÁ VAZIO. AGORA, VAMOS TENTAR A REDE.
            println("Cache vazio para o deck $deckId. Buscando da rede...")
            val token = tokenManager.getToken()
            if (token == null) {
                _studyState.value = StudyState.Error("Sessão inválida. Faça login novamente.")
                return@launch
            }

            try {
                // 4. BUSCA DA API
                val networkFlashcardsResponse = apiService.getFlashcardsForDocument(token, deckId)
                val networkFlashcardEntities = networkFlashcardsResponse.map { it.toFlashcardEntity(userId) }

                // 5. SALVA OS DADOS BAIXADOS NO CACHE
                flashcardDao.deleteFlashcardsForDeckForUser(deckId, userId) // Limpa dados antigos se houver
                flashcardDao.insertFlashcards(networkFlashcardEntities)

                // 6. LÊ DE VOLTA DO CACHE PARA EXIBIR (garante consistência)
                val updatedLocalFlashcards = flashcardDao.getFlashcardsForDeckForUser(deckId, userId)
                    .map { it.toFlashcardResponse() }

                if (updatedLocalFlashcards.isNotEmpty()) {
                    _studyState.value = StudyState.Success(updatedLocalFlashcards)
                } else {
                    // A rede funcionou, mas não retornou flashcards
                    _studyState.value = StudyState.Error("Este deck (ainda) não possui flashcards.")
                }

            } catch (e: Exception) {
                // 7. A REDE FALHOU (é aqui que o seu erro acontece)
                println("Error fetching network flashcards: ${e.message}")
                _studyState.value = StudyState.Error("Falha ao carregar flashcards: ${e.message}")
            }
        }
    }

    fun logStudyResult(flashcardId: Int, accuracy: Float) {
        viewModelScope.launch {
            val userId = getCurrentUserId()
            // --- CORREÇÃO: Usa 'currentDeckId' e verifica se não é nulo ---
            val deckId = currentDeckId // Pega o ID guardado
            if (userId == TokenManager.INVALID_USER_ID || deckId == null) { // Verifica se deckId é nulo
                println("Error logging study: Invalid user or deckId is null")
                return@launch
            }
            // -----------------------------------------------------------------
            val token = tokenManager.getToken() ?: return@launch

            // Send log to API
            try {
                apiService.logStudy(token, flashcardId, StudyLogRequest(accuracy = accuracy))
                println("Log de estudo enviado com sucesso para flashcard $flashcardId")

                // Update local deck progress (Example logic - adjust as needed)
                // --- CORREÇÃO: Usa 'deckId' (que agora contém o valor de 'currentDeckId') ---
                val deck = deckDao.getDeckByIdForUser(deckId, userId)
                if (deck != null) {
                    if (accuracy >= 0.5f) {
                        // ... (A lógica de atualização do progresso local, como antes) ...
                        // Pode usar 'deckId' aqui também se precisar, por exemplo:
                        val currentCount = flashcardDao.getFlashcardsForDeckForUser(deckId, userId)
                            .count { fc -> /* Logic to check if fc was studied */ true }
                        println("Local deck progress update logic needs refinement.")
                    }
                }

            } catch (e: Exception) {
                println("Erro ao enviar log de estudo para API: ${e.message}")
            }
        }
    }

    // --- Helper Conversion Functions (consider moving to a separate Util file) ---
    private fun FlashcardEntity.toFlashcardResponse(): FlashcardResponse {
        return FlashcardResponse(id, front, back, type, deckId)
    }

    private fun FlashcardResponse.toFlashcardEntity(userId: Int): FlashcardEntity {
        // documentId from response maps to deckId in the entity
        return FlashcardEntity(id, front, back, type, documentId, userId)
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

                // Atualiza na API
                val updatedFlashcard = apiService.updateFlashcard(token, flashcardId, request)

                // Atualiza no cache local
                val updatedEntity = updatedFlashcard.toFlashcardEntity(userId)
                flashcardDao.updateFlashcard(updatedEntity)

                // Recarrega a lista de flashcards
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

}
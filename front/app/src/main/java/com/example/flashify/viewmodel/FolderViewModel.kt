package com.example.flashify.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.flashify.model.data.FolderRequest
import com.example.flashify.model.data.FolderResponse
import com.example.flashify.model.data.FolderWithDocumentsResponse
import com.example.flashify.model.data.LibraryResponse
import com.example.flashify.model.manager.TokenManager
import com.example.flashify.model.network.Api
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Estados para a biblioteca com pastas
 */
sealed class LibraryState {
    object Idle : LibraryState()
    object Loading : LibraryState()
    data class Success(val library: LibraryResponse) : LibraryState()
    data class Error(val message: String) : LibraryState()
}

/**
 * Estados para operações de pasta
 */
sealed class FolderOperationState {
    object Idle : FolderOperationState()
    object Loading : FolderOperationState()
    object Success : FolderOperationState()
    data class Error(val message: String) : FolderOperationState()
}

/**
 * ViewModel para gerenciar pastas e biblioteca
 */
class FolderViewModel(application: Application) : AndroidViewModel(application) {

    private val tokenManager = TokenManager(application)

    // Estado da biblioteca
    private val _libraryState = MutableStateFlow<LibraryState>(LibraryState.Idle)
    val libraryState: StateFlow<LibraryState> = _libraryState

    // Estado de operações (criar, editar, deletar)
    private val _operationState = MutableStateFlow<FolderOperationState>(FolderOperationState.Idle)
    val operationState: StateFlow<FolderOperationState> = _operationState

    /**
     * Carrega a biblioteca completa (pastas + decks)
     */
    fun loadLibrary() {
        viewModelScope.launch {
            _libraryState.value = LibraryState.Loading
            try {
                val token = tokenManager.getToken()
                if (token == null) {
                    _libraryState.value = LibraryState.Error("Token de autenticação não encontrado")
                    return@launch
                }

                val library = Api.retrofitService.getLibrary(token)
                _libraryState.value = LibraryState.Success(library)
            } catch (e: Exception) {
                _libraryState.value = LibraryState.Error(e.message ?: "Erro ao carregar biblioteca")
            }
        }
    }

    /**
     * Cria uma nova pasta
     */
    fun createFolder(name: String) {
        viewModelScope.launch {
            _operationState.value = FolderOperationState.Loading
            try {
                val token = tokenManager.getToken()
                if (token == null) {
                    _operationState.value = FolderOperationState.Error("Token de autenticação não encontrado")
                    return@launch
                }

                val request = FolderRequest(name)
                Api.retrofitService.createFolder(token, request)
                
                _operationState.value = FolderOperationState.Success
                
                // Recarregar biblioteca
                loadLibrary()
            } catch (e: Exception) {
                _operationState.value = FolderOperationState.Error(e.message ?: "Erro ao criar pasta")
            }
        }
    }

    /**
     * Atualiza o nome de uma pasta
     */
    fun updateFolder(folderId: Int, newName: String) {
        viewModelScope.launch {
            _operationState.value = FolderOperationState.Loading
            try {
                val token = tokenManager.getToken()
                if (token == null) {
                    _operationState.value = FolderOperationState.Error("Token de autenticação não encontrado")
                    return@launch
                }

                val request = FolderRequest(newName)
                Api.retrofitService.updateFolder(token, folderId, request)
                
                _operationState.value = FolderOperationState.Success
                
                // Recarregar biblioteca
                loadLibrary()
            } catch (e: Exception) {
                _operationState.value = FolderOperationState.Error(e.message ?: "Erro ao atualizar pasta")
            }
        }
    }

    /**
     * Deleta uma pasta
     */
    fun deleteFolder(folderId: Int, deleteDecks: Boolean = false) {
        viewModelScope.launch {
            _operationState.value = FolderOperationState.Loading
            try {
                val token = tokenManager.getToken()
                if (token == null) {
                    _operationState.value = FolderOperationState.Error("Token de autenticação não encontrado")
                    return@launch
                }

                Api.retrofitService.deleteFolder(token, folderId, deleteDecks)
                
                _operationState.value = FolderOperationState.Success
                
                // Recarregar biblioteca
                loadLibrary()
            } catch (e: Exception) {
                _operationState.value = FolderOperationState.Error(e.message ?: "Erro ao deletar pasta")
            }
        }
    }

    /**
     * Reseta o estado de operação
     */
    fun resetOperationState() {
        _operationState.value = FolderOperationState.Idle
    }
}

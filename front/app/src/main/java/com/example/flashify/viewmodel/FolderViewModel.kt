package com.example.flashify.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flashify.model.data.FolderRequest
import com.example.flashify.model.data.LibraryResponse
import com.example.flashify.model.manager.TokenManager
import com.example.flashify.model.network.ApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class LibraryState {
    object Idle : LibraryState()
    object Loading : LibraryState()
    data class Success(val library: LibraryResponse) : LibraryState()
    data class Error(val message: String) : LibraryState()
}

sealed class FolderOperationState {
    object Idle : FolderOperationState()
    object Loading : FolderOperationState()
    object Success : FolderOperationState()
    data class Error(val message: String) : FolderOperationState()
}

@HiltViewModel
class FolderViewModel @Inject constructor(
    private val tokenManager: TokenManager,
    private val apiService: ApiService
) : ViewModel() {

    private val _libraryState = MutableStateFlow<LibraryState>(LibraryState.Idle)
    val libraryState: StateFlow<LibraryState> = _libraryState

    private val _operationState = MutableStateFlow<FolderOperationState>(FolderOperationState.Idle)
    val operationState: StateFlow<FolderOperationState> = _operationState

    fun loadLibrary() {
        viewModelScope.launch {
            _libraryState.value = LibraryState.Loading
            try {
                val token = tokenManager.getToken()
                if (token == null) {
                    _libraryState.value = LibraryState.Error("Token de autenticação não encontrado")
                    return@launch
                }

                val library = apiService.getLibrary(token)
                _libraryState.value = LibraryState.Success(library)
            } catch (e: Exception) {
                _libraryState.value = LibraryState.Error(e.message ?: "Erro ao carregar biblioteca")
            }
        }
    }

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
                apiService.createFolder(token, request)

                _operationState.value = FolderOperationState.Success

                loadLibrary()
            } catch (e: Exception) {
                _operationState.value = FolderOperationState.Error(e.message ?: "Erro ao criar pasta")
            }
        }
    }

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
                apiService.updateFolder(token, folderId, request)

                _operationState.value = FolderOperationState.Success

                loadLibrary()
            } catch (e: Exception) {
                _operationState.value = FolderOperationState.Error(e.message ?: "Erro ao atualizar pasta")
            }
        }
    }

    fun deleteFolder(folderId: Int, deleteDecks: Boolean = false) {
        viewModelScope.launch {
            _operationState.value = FolderOperationState.Loading
            try {
                val token = tokenManager.getToken()
                if (token == null) {
                    _operationState.value = FolderOperationState.Error("Token de autenticação não encontrado")
                    return@launch
                }

                apiService.deleteFolder(token, folderId, deleteDecks)

                _operationState.value = FolderOperationState.Success

                loadLibrary()
            } catch (e: Exception) {
                _operationState.value = FolderOperationState.Error(e.message ?: "Erro ao deletar pasta")
            }
        }
    }

    fun resetOperationState() {
        _operationState.value = FolderOperationState.Idle
    }
}
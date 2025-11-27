package com.example.flashify.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flashify.model.data.UserReadResponse
import com.example.flashify.model.manager.LocalUserManager
import com.example.flashify.model.manager.TokenManager
import com.example.flashify.model.network.ApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class UserState {
    object Loading : UserState()
    data class Success(val user: UserReadResponse) : UserState()
    data class Error(val message: String) : UserState()
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val tokenManager: TokenManager,
    private val apiService: ApiService,
    private val localUserManager: LocalUserManager  // ✅ NOVO
) : ViewModel() {

    private val _userState = MutableStateFlow<UserState>(UserState.Loading)
    val userState = _userState.asStateFlow()

    init {
        fetchCurrentUser()
    }

    fun fetchCurrentUser() {
        viewModelScope.launch {
            _userState.value = UserState.Loading

            // 1️⃣ Primeiro, tentar carregar do cache local
            val localUser = localUserManager.getLocalUser()
            if (localUser != null) {
                _userState.value = UserState.Success(
                    UserReadResponse(
                        id = localUser.id,
                        username = localUser.username,
                        email = localUser.email,
                        isActive = localUser.isActive,
                        profilePictureUrl = localUser.profilePictureUrl,
                        provider = localUser.provider
                    )
                )
                Log.d("SettingsViewModel", "📦 Usuário carregado do cache")

                // Se estiver online, atualizar em background
                updateUserFromNetwork()
                return@launch
            }

            // 2️⃣ Se não houver cache, buscar da rede
            updateUserFromNetwork()
        }
    }

    private suspend fun updateUserFromNetwork() {
        val token = tokenManager.getToken()

        if (token == null) {
            _userState.value = UserState.Error("Sessão inválida. Por favor, faça login novamente.")
            return
        }

        try {
            val userResponse = apiService.getCurrentUser(token)

            // Salvar no cache
            localUserManager.saveUser(userResponse)

            _userState.value = UserState.Success(userResponse)
            Log.d("SettingsViewModel", "🔄 Usuário sincronizado da rede")
        } catch (e: Exception) {
            Log.e("SettingsViewModel", "⚠️ Erro ao buscar usuário: ${e.message}")

            // Se a rede falhar mas temos cache, manter o cache
            if (_userState.value !is UserState.Success) {
                _userState.value = UserState.Error(e.message ?: "Falha ao carregar dados do usuário.")
            }
        }
    }
}
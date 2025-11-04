package com.example.flashify.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.flashify.model.data.UserReadResponse
import com.example.flashify.model.manager.TokenManager
import com.example.flashify.model.network.Api
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Define os estados da UI para os dados do usuário
sealed class UserState {
    object Loading : UserState()
    data class Success(val user: UserReadResponse) : UserState()
    data class Error(val message: String) : UserState()
}

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val tokenManager = TokenManager(application)
    private val apiService = Api.retrofitService

    // StateFlow para expor os dados do usuário para a UI
    private val _userState = MutableStateFlow<UserState>(UserState.Loading)
    val userState = _userState.asStateFlow()

    init {
        // Assim que o ViewModel é criado, busca as informações do usuário
        fetchCurrentUser()
    }

    fun fetchCurrentUser() {
        viewModelScope.launch {
            _userState.value = UserState.Loading
            val token = tokenManager.getToken()

            if (token == null) {
                _userState.value = UserState.Error("Sessão inválida. Por favor, faça login novamente.")
                return@launch
            }

            try {
                // Chama o endpoint "users/me" da API
                val userResponse = apiService.getCurrentUser(token)
                _userState.value = UserState.Success(userResponse)
            } catch (e: Exception) {
                _userState.value = UserState.Error(e.message ?: "Falha ao carregar dados do usuário.")
                e.printStackTrace()
            }
        }
    }
}

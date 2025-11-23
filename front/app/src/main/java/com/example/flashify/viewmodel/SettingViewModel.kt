package com.example.flashify.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flashify.model.data.UserReadResponse
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
    private val apiService: ApiService
) : ViewModel() {

    private val _userState = MutableStateFlow<UserState>(UserState.Loading)
    val userState = _userState.asStateFlow()

    init {
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
                val userResponse = apiService.getCurrentUser(token)
                _userState.value = UserState.Success(userResponse)
            } catch (e: Exception) {
                _userState.value = UserState.Error(e.message ?: "Falha ao carregar dados do usuário.")
                e.printStackTrace()
            }
        }
    }
}
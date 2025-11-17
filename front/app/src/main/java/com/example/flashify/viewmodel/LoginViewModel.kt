// ==================== LoginViewModel.kt ====================
package com.example.flashify.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.flashify.model.manager.TokenManager
import com.example.flashify.model.network.Api
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

sealed class LoginUIState {
    object Idle : LoginUIState()
    object Loading : LoginUIState()
    data class Success(val token: String) : LoginUIState()
    data class Error(val message: String, val errorType: ErrorType) : LoginUIState()
}

enum class ErrorType {
    INVALID_CREDENTIALS,
    USER_NOT_FOUND,
    NETWORK_ERROR,
    SERVER_ERROR,
    UNKNOWN
}

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val tokenManager = TokenManager(application)

    private val _loginState = MutableStateFlow<LoginUIState>(LoginUIState.Idle)
    val loginState: StateFlow<LoginUIState> = _loginState

    fun loginUser(username: String, password: String) {
        viewModelScope.launch {
            _loginState.value = LoginUIState.Loading
            try {
                // 1. Faz o login para obter o token
                val tokenResponse = Api.retrofitService.login(
                    username = username,
                    password = password
                )
                val token = "Bearer ${tokenResponse.accessToken}"

                // 2. Com o token, busca os dados do utilizador (incluindo ID)
                val userResponse = Api.retrofitService.getCurrentUser(token)
                val userId = userResponse.id

                // 3. Guarda o token E o userId
                tokenManager.saveAuthData(tokenResponse.accessToken, userId)

                _loginState.value = LoginUIState.Success(tokenResponse.accessToken)

            } catch (e: Exception) {
                tokenManager.clearAuthData()

                // Tratamento de erros detalhado
                val (message, errorType) = when (e) {
                    is HttpException -> {
                        when (e.code()) {
                            401 -> Pair(
                                "Email/usuário ou senha incorretos",
                                ErrorType.INVALID_CREDENTIALS
                            )
                            404 -> Pair(
                                "Usuário não encontrado. Verifique seus dados.",
                                ErrorType.USER_NOT_FOUND
                            )
                            500, 502, 503 -> Pair(
                                "Erro no servidor. Tente novamente mais tarde.",
                                ErrorType.SERVER_ERROR
                            )
                            else -> Pair(
                                "Erro ao fazer login: ${e.message()}",
                                ErrorType.UNKNOWN
                            )
                        }
                    }
                    is UnknownHostException, is ConnectException -> Pair(
                        "Sem conexão com a internet. Verifique sua rede.",
                        ErrorType.NETWORK_ERROR
                    )
                    is SocketTimeoutException -> Pair(
                        "Tempo de conexão esgotado. Tente novamente.",
                        ErrorType.NETWORK_ERROR
                    )
                    else -> Pair(
                        e.message ?: "Erro desconhecido ao fazer login",
                        ErrorType.UNKNOWN
                    )
                }

                _loginState.value = LoginUIState.Error(message, errorType)
            }
        }
    }

    fun resetState() {
        _loginState.value = LoginUIState.Idle
    }

    fun logout() {
        tokenManager.clearAuthData()
    }
}
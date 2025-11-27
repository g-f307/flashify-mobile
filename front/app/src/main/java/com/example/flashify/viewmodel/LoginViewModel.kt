package com.example.flashify.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flashify.model.manager.LocalUserManager
import com.example.flashify.model.manager.TokenManager
import com.example.flashify.model.network.ApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject

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

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val tokenManager: TokenManager,
    private val apiService: ApiService,
    private val localUserManager: LocalUserManager  // ✅ NOVO
) : ViewModel() {

    private val _loginState = MutableStateFlow<LoginUIState>(LoginUIState.Idle)
    val loginState: StateFlow<LoginUIState> = _loginState

    fun loginUser(username: String, password: String) {
        viewModelScope.launch {
            _loginState.value = LoginUIState.Loading
            try {
                val tokenResponse = apiService.login(
                    username = username,
                    password = password
                )
                val token = "Bearer ${tokenResponse.accessToken}"

                val userResponse = apiService.getCurrentUser(token)
                val userId = userResponse.id

                // ✅ NOVO: Salvar usuário localmente
                localUserManager.saveUser(userResponse)
                Log.d("LoginViewModel", "✅ Usuário salvo no cache local")

                tokenManager.saveAuthData(tokenResponse.accessToken, userId)

                _loginState.value = LoginUIState.Success(tokenResponse.accessToken)

            } catch (e: Exception) {
                tokenManager.clearAuthData()

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
        viewModelScope.launch {
            tokenManager.clearAuthData()
            // ✅ NOVO: Limpar usuário do cache ao fazer logout
            localUserManager.clearLocalUser()
            Log.d("LoginViewModel", "✅ Logout - Cache de usuário limpo")
        }
    }
}

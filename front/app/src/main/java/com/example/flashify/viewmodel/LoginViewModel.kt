package com.example.flashify.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flashify.model.data.GoogleIdTokenRequest
import com.example.flashify.model.manager.GoogleAuthManager
import com.example.flashify.model.manager.GoogleSignInResult
import com.example.flashify.model.manager.LocalUserManager
import com.example.flashify.model.manager.ProfileImageManager
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
    private val localUserManager: LocalUserManager,
    private val profileImageManager: ProfileImageManager,
    private val googleAuthManager: GoogleAuthManager
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

                // Salvar usuário localmente
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

    fun loginWithGoogle(context: Context, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _loginState.value = LoginUIState.Loading

            Log.d("LoginViewModel", "🔵 Iniciando login com Google...")

            when (val result = googleAuthManager.signIn()) {
                is GoogleSignInResult.Success -> {
                    Log.d("LoginViewModel", "✅ Google Sign-In bem-sucedido")
                    Log.d("LoginViewModel", "📸 URL da foto recebida: ${result.profilePictureUrl}")
                    Log.d("LoginViewModel", "👤 Nome: ${result.displayName}")
                    Log.d("LoginViewModel", "📧 Email: ${result.email}")

                    try {
                        // Faz login na API
                        Log.d("LoginViewModel", "🔵 Autenticando com API...")
                        val response = apiService.loginWithGoogleMobile(
                            GoogleIdTokenRequest(idToken = result.idToken)
                        )

                        val token = "Bearer ${response.accessToken}"
                        Log.d("LoginViewModel", "✅ Token obtido da API")

                        // Busca dados do usuário
                        val userResponse = apiService.getCurrentUser(token)
                        val userId = userResponse.id

                        // Salva usuário localmente
                        localUserManager.saveUser(userResponse)
                        Log.d("LoginViewModel", "✅ Usuário salvo localmente")

                        // Salva token
                        tokenManager.saveAuthData(response.accessToken, userId)
                        Log.d("LoginViewModel", "✅ Token salvo")

                        // ✅ SALVA A FOTO DE PERFIL
                        if (result.profilePictureUrl != null) {
                            profileImageManager.saveProfileImageUrl(result.profilePictureUrl)
                            Log.d("LoginViewModel", "✅ Foto de perfil SALVA: ${result.profilePictureUrl}")

                            // Verifica se foi salva
                            profileImageManager.profileImageUrl.collect { savedUrl ->
                                Log.d("LoginViewModel", "🔍 Verificação - URL salva: $savedUrl")
                                return@collect
                            }
                        } else {
                            Log.w("LoginViewModel", "⚠️ Nenhuma URL de foto fornecida pelo Google")
                        }

                        _loginState.value = LoginUIState.Success(response.accessToken)
                        onSuccess()

                    } catch (e: Exception) {
                        Log.e("LoginViewModel", "❌ Erro ao autenticar com Google", e)
                        tokenManager.clearAuthData()
                        _loginState.value = LoginUIState.Error(
                            "Erro ao autenticar: ${e.message}",
                            ErrorType.SERVER_ERROR
                        )
                        onError("Erro ao autenticar: ${e.message}")
                    }
                }
                is GoogleSignInResult.Error -> {
                    Log.e("LoginViewModel", "❌ Erro no login Google: ${result.message}")
                    _loginState.value = LoginUIState.Error(
                        result.message,
                        ErrorType.UNKNOWN
                    )
                    onError(result.message)
                }
            }
        }
    }

    fun resetState() {
        _loginState.value = LoginUIState.Idle
    }

    fun logout() {
        viewModelScope.launch {
            tokenManager.clearAuthData()
            localUserManager.clearLocalUser()
            profileImageManager.clearProfileImage()
            Log.d("LoginViewModel", "✅ Logout - Cache de usuário e foto limpos")
        }
    }
}
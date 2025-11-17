package com.example.flashify.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.flashify.model.data.GoogleIdTokenRequest
import com.example.flashify.model.manager.GoogleAuthManager
import com.example.flashify.model.manager.GoogleSignInResult
import com.example.flashify.model.manager.TokenManager
import com.example.flashify.model.network.Api
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Estados da UI para Login Social
 */
sealed class SocialLoginUIState {
    object Idle : SocialLoginUIState()
    object Loading : SocialLoginUIState()
    data class Success(val token: String) : SocialLoginUIState()
    data class Error(val message: String) : SocialLoginUIState()
}

/**
 * ViewModel para gerenciar o login social (Google)
 */
class SocialLoginViewModel(application: Application) : AndroidViewModel(application) {

    private val tokenManager = TokenManager(application)
    private val googleAuthManager = GoogleAuthManager(application)

    private val _socialLoginState = MutableStateFlow<SocialLoginUIState>(SocialLoginUIState.Idle)
    val socialLoginState: StateFlow<SocialLoginUIState> = _socialLoginState

    /**
     * Inicia o fluxo de login com Google
     */
    fun signInWithGoogle() {
        viewModelScope.launch {
            _socialLoginState.value = SocialLoginUIState.Loading

            // 1. Obter o ID Token do Google
            when (val result = googleAuthManager.signIn()) {
                is GoogleSignInResult.Success -> {
                    // 2. Enviar o ID Token para o backend
                    authenticateWithBackend(result.idToken)
                }
                is GoogleSignInResult.Error -> {
                    _socialLoginState.value = SocialLoginUIState.Error(result.message)
                }
            }
        }
    }

    /**
     * Envia o ID Token do Google para o backend para autenticação
     */
    private suspend fun authenticateWithBackend(idToken: String) {
        try {
            // Usar o endpoint específico para mobile que aceita ID Token
            val request = GoogleIdTokenRequest(idToken = idToken)

            val tokenResponse = Api.retrofitService.loginWithGoogleMobile(request)

            // 3. Buscar dados do usuário com o token recebido
            val token = "Bearer ${tokenResponse.accessToken}"
            val userResponse = Api.retrofitService.getCurrentUser(token)

            // 4. Salvar token e userId
            tokenManager.saveAuthData(tokenResponse.accessToken, userResponse.id)

            _socialLoginState.value = SocialLoginUIState.Success(tokenResponse.accessToken)

        } catch (e: Exception) {
            tokenManager.clearAuthData()
            _socialLoginState.value = SocialLoginUIState.Error(
                e.message ?: "Erro ao autenticar com o servidor"
            )
        }
    }

    /**
     * Reseta o estado para Idle
     */
    fun resetState() {
        _socialLoginState.value = SocialLoginUIState.Idle
    }
}
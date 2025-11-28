package com.example.flashify.viewmodel

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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class SocialLoginUIState {
    object Idle : SocialLoginUIState()
    object Loading : SocialLoginUIState()
    data class Success(val token: String) : SocialLoginUIState()
    data class Error(val message: String) : SocialLoginUIState()
}

@HiltViewModel
class SocialLoginViewModel @Inject constructor(
    private val tokenManager: TokenManager,
    private val googleAuthManager: GoogleAuthManager,
    private val apiService: ApiService,
    private val localUserManager: LocalUserManager,
    private val profileImageManager: ProfileImageManager
) : ViewModel() {

    private val _socialLoginState = MutableStateFlow<SocialLoginUIState>(SocialLoginUIState.Idle)
    val socialLoginState: StateFlow<SocialLoginUIState> = _socialLoginState

    fun signInWithGoogle() {
        viewModelScope.launch {
            try {
                _socialLoginState.value = SocialLoginUIState.Loading
                Log.d("SocialLoginViewModel", "🔵 Estado alterado para Loading")

                Log.d("SocialLoginViewModel", "🔵 Iniciando login com Google...")

                when (val result = googleAuthManager.signIn()) {
                    is GoogleSignInResult.Success -> {
                        Log.d("SocialLoginViewModel", "✅ Google Sign-In bem-sucedido")
                        Log.d("SocialLoginViewModel", "📸 URL da foto recebida: ${result.profilePictureUrl}")
                        Log.d("SocialLoginViewModel", "👤 Nome: ${result.displayName}")
                        Log.d("SocialLoginViewModel", "📧 Email: ${result.email}")

                        authenticateWithBackend(result)
                    }
                    is GoogleSignInResult.Error -> {
                        Log.e("SocialLoginViewModel", "❌ Erro no Google Sign-In: ${result.message}")
                        _socialLoginState.value = SocialLoginUIState.Error(result.message)
                        Log.d("SocialLoginViewModel", "🔴 Estado alterado para Error")
                    }
                }
            } catch (e: Exception) {
                Log.e("SocialLoginViewModel", "❌ Exceção não tratada em signInWithGoogle", e)
                _socialLoginState.value = SocialLoginUIState.Error(
                    e.message ?: "Erro inesperado ao fazer login"
                )
            }
        }
    }

    private suspend fun authenticateWithBackend(googleResult: GoogleSignInResult.Success) {
        try {
            Log.d("SocialLoginViewModel", "🔵 Autenticando com API...")

            val request = GoogleIdTokenRequest(idToken = googleResult.idToken)
            val tokenResponse = apiService.loginWithGoogleMobile(request)

            val token = "Bearer ${tokenResponse.accessToken}"
            Log.d("SocialLoginViewModel", "✅ Token obtido da API")

            val userResponse = apiService.getCurrentUser(token)
            Log.d("SocialLoginViewModel", "✅ Dados do usuário obtidos - ID: ${userResponse.id}")

            // Salva usuário localmente
            localUserManager.saveUser(userResponse)
            Log.d("SocialLoginViewModel", "✅ Usuário salvo localmente")

            // Salva token
            tokenManager.saveAuthData(tokenResponse.accessToken, userResponse.id)
            Log.d("SocialLoginViewModel", "✅ Token salvo")

            // ✅ SALVA A FOTO DE PERFIL (CORRIGIDO - sem collect infinito)
            if (!googleResult.profilePictureUrl.isNullOrEmpty()) {
                profileImageManager.saveProfileImageUrl(googleResult.profilePictureUrl)
                Log.d("SocialLoginViewModel", "✅ Foto de perfil SALVA: ${googleResult.profilePictureUrl}")

                // ✅ CORRIGIDO: Usa first() em vez de collect
                val savedUrl = profileImageManager.profileImageUrl.first()
                Log.d("SocialLoginViewModel", "🔍 Verificação - URL salva no DataStore: $savedUrl")
            } else {
                Log.w("SocialLoginViewModel", "⚠️ Nenhuma URL de foto fornecida pelo Google")
            }

            // ✅ IMPORTANTE: Muda o estado para Success
            _socialLoginState.value = SocialLoginUIState.Success(tokenResponse.accessToken)
            Log.d("SocialLoginViewModel", "✅ Estado alterado para Success")

        } catch (e: Exception) {
            Log.e("SocialLoginViewModel", "❌ Erro ao autenticar com backend", e)
            Log.e("SocialLoginViewModel", "❌ Stack trace:", e)
            tokenManager.clearAuthData()
            _socialLoginState.value = SocialLoginUIState.Error(
                e.message ?: "Erro ao autenticar com o servidor"
            )
            Log.d("SocialLoginViewModel", "🔴 Estado alterado para Error")
        }
    }

    fun resetState() {
        _socialLoginState.value = SocialLoginUIState.Idle
        Log.d("SocialLoginViewModel", "🔄 Estado resetado para Idle")
    }
}
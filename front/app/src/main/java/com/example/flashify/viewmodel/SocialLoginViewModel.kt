package com.example.flashify.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flashify.model.data.GoogleIdTokenRequest
import com.example.flashify.model.manager.GoogleAuthManager
import com.example.flashify.model.manager.GoogleSignInResult
import com.example.flashify.model.manager.TokenManager
import com.example.flashify.model.network.ApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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
    private val apiService: ApiService
) : ViewModel() {

    private val _socialLoginState = MutableStateFlow<SocialLoginUIState>(SocialLoginUIState.Idle)
    val socialLoginState: StateFlow<SocialLoginUIState> = _socialLoginState

    fun signInWithGoogle() {
        viewModelScope.launch {
            _socialLoginState.value = SocialLoginUIState.Loading

            when (val result = googleAuthManager.signIn()) {
                is GoogleSignInResult.Success -> {
                    authenticateWithBackend(result.idToken)
                }
                is GoogleSignInResult.Error -> {
                    _socialLoginState.value = SocialLoginUIState.Error(result.message)
                }
            }
        }
    }

    private suspend fun authenticateWithBackend(idToken: String) {
        try {
            val request = GoogleIdTokenRequest(idToken = idToken)

            val tokenResponse = apiService.loginWithGoogleMobile(request)

            val token = "Bearer ${tokenResponse.accessToken}"
            val userResponse = apiService.getCurrentUser(token)

            tokenManager.saveAuthData(tokenResponse.accessToken, userResponse.id)

            _socialLoginState.value = SocialLoginUIState.Success(tokenResponse.accessToken)

        } catch (e: Exception) {
            tokenManager.clearAuthData()
            _socialLoginState.value = SocialLoginUIState.Error(
                e.message ?: "Erro ao autenticar com o servidor"
            )
        }
    }

    fun resetState() {
        _socialLoginState.value = SocialLoginUIState.Idle
    }
}
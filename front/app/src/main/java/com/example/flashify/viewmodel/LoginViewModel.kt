package com.example.flashify.viewmodel


/*

Lógica do registro

 */

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flashify.model.manager.TokenManager
import com.example.flashify.model.network.Api
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch



// Os estados da UI são semelhantes aos do registo
sealed class LoginUIState {
    object Idle : LoginUIState()
    object Loading : LoginUIState()
    data class Success(val token: String) : LoginUIState()
    data class Error(val message: String) : LoginUIState()
}

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    //Instanciando o TokenManager
    private val tokenManager = TokenManager(application)

    private val _loginState = MutableStateFlow<LoginUIState>(LoginUIState.Idle)
    val loginState: StateFlow<LoginUIState> = _loginState

    fun loginUser(username: String, password: String) {
        viewModelScope.launch {
            _loginState.value = LoginUIState.Loading
            try {
                // 1. Faz o login para obter o token
                val tokenResponse = Api.retrofitService.login(username = username, password = password)
                val token = "Bearer ${tokenResponse.accessToken}" // Formata o token

                // 2. Com o token, busca os dados do utilizador (incluindo ID)
                val userResponse = Api.retrofitService.getCurrentUser(token)
                val userId = userResponse.id

                // 3. Guarda o token E o userId
                tokenManager.saveAuthData(tokenResponse.accessToken, userId)

                _loginState.value = LoginUIState.Success(tokenResponse.accessToken)

            } catch (e: Exception) {
                tokenManager.clearAuthData() // Limpa dados inválidos em caso de erro
                _loginState.value = LoginUIState.Error(e.message ?: "Email ou palavra-passe incorretos")
            }
        }
    }
    // Função para resetar o estado
    fun resetState() {
        _loginState.value = LoginUIState.Idle
    }

    //Função para fazer logout

    fun logout(){
        tokenManager.clearAuthData()
    }
}

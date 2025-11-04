package com.example.flashify.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flashify.model.data.UserCreateRequest
import com.example.flashify.model.network.Api
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// Define os possíveis estados da nossa UI
sealed class RegisterUIState {
    object Idle : RegisterUIState() // Estado inicial
    object Loading : RegisterUIState() // A carregar
    data class Success(val message: String) : RegisterUIState() // Sucesso
    data class Error(val message: String) : RegisterUIState() // Erro
}

class RegisterViewModel : ViewModel() {

    // Estado privado que só o ViewModel pode modificar
    private val _registerState = MutableStateFlow<RegisterUIState>(RegisterUIState.Idle)
    // Estado público que a UI pode observar para se atualizar
    val registerState: StateFlow<RegisterUIState> = _registerState

    fun registerUser(username: String, email: String, password: String) {
        // Inicia uma coroutine segura no escopo do ViewModel
        viewModelScope.launch {
            // 1. Mude o estado para Loading (a UI vai mostrar um spinner)
            _registerState.value = RegisterUIState.Loading

            try {
                // 2. Crie o objeto de pedido com os dados da UI
                val userRequest = UserCreateRequest(
                    username = username,
                    email = email,
                    password = password
                )

                // 3. Chame a ApiService (a ponte para o nosso back-end)
                val response = Api.retrofitService.registerUser(userRequest)

                // 4. Se correu bem, mude o estado para Success
                _registerState.value = RegisterUIState.Success("Utilizador ${response.username} criado com sucesso!")

            } catch (e: Exception) {
                // 5. Se deu erro, mude o estado para Error
                _registerState.value = RegisterUIState.Error(e.message ?: "Ocorreu um erro desconhecido")
            }
        }
    }
}

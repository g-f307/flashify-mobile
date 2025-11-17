package com.example.flashify.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flashify.model.data.UserCreateRequest
import com.example.flashify.model.network.Api
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.HttpException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

sealed class RegisterUIState {
    object Idle : RegisterUIState()
    object Loading : RegisterUIState()
    data class Success(val message: String) : RegisterUIState()
    data class Error(
        val message: String,
        val field: String? = null // Campo específico com erro
    ) : RegisterUIState()
}

class RegisterViewModel : ViewModel() {

    private val _registerState = MutableStateFlow<RegisterUIState>(RegisterUIState.Idle)
    val registerState: StateFlow<RegisterUIState> = _registerState

    fun registerUser(username: String, email: String, password: String) {
        viewModelScope.launch {
            _registerState.value = RegisterUIState.Loading

            try {
                val userRequest = UserCreateRequest(
                    username = username,
                    email = email,
                    password = password
                )

                val response = Api.retrofitService.registerUser(userRequest)

                _registerState.value = RegisterUIState.Success(
                    "Conta criada com sucesso! Faça login para continuar."
                )

            } catch (e: Exception) {
                val (message, field) = when (e) {
                    is HttpException -> {
                        when (e.code()) {
                            400 -> {
                                // Tenta extrair detalhes do erro do corpo da resposta
                                try {
                                    val errorBody = e.response()?.errorBody()?.string()
                                    if (errorBody != null) {
                                        val json = JSONObject(errorBody)
                                        val detail = json.optString("detail", "")

                                        when {
                                            detail.contains("username", ignoreCase = true) -> {
                                                Pair(
                                                    "Este nome de usuário já está em uso",
                                                    "username"
                                                )
                                            }
                                            detail.contains("email", ignoreCase = true) -> {
                                                Pair(
                                                    "Este email já está cadastrado",
                                                    "email"
                                                )
                                            }
                                            detail.contains("password", ignoreCase = true) -> {
                                                Pair(
                                                    "A senha não atende aos requisitos mínimos",
                                                    "password"
                                                )
                                            }
                                            else -> {
                                                Pair(detail, null)
                                            }
                                        }
                                    } else {
                                        Pair("Dados inválidos. Verifique as informações.", null)
                                    }
                                } catch (jsonException: Exception) {
                                    Pair("Erro ao processar resposta do servidor", null)
                                }
                            }
                            409 -> {
                                // Conflito - geralmente usuário ou email já existe
                                val errorBody = e.response()?.errorBody()?.string()
                                if (errorBody?.contains("username", ignoreCase = true) == true) {
                                    Pair(
                                        "Este nome de usuário já está em uso",
                                        "username"
                                    )
                                } else {
                                    Pair(
                                        "Este email já está cadastrado",
                                        "email"
                                    )
                                }
                            }
                            422 -> {
                                Pair(
                                    "Dados inválidos. Verifique o formato dos campos.",
                                    null
                                )
                            }
                            500, 502, 503 -> {
                                Pair(
                                    "Erro no servidor. Tente novamente mais tarde.",
                                    null
                                )
                            }
                            else -> {
                                Pair("Erro ao criar conta: ${e.message()}", null)
                            }
                        }
                    }
                    is UnknownHostException, is ConnectException -> {
                        Pair(
                            "Sem conexão com a internet. Verifique sua rede.",
                            null
                        )
                    }
                    is SocketTimeoutException -> {
                        Pair(
                            "Tempo de conexão esgotado. Tente novamente.",
                            null
                        )
                    }
                    else -> {
                        Pair(
                            e.message ?: "Erro desconhecido ao criar conta",
                            null
                        )
                    }
                }

                _registerState.value = RegisterUIState.Error(message, field)
            }
        }
    }

    fun resetState() {
        _registerState.value = RegisterUIState.Idle
    }
}
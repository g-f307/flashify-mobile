package com.example.flashify.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flashify.model.data.GenerationLimitResponse
import com.example.flashify.model.manager.TokenManager
import com.example.flashify.model.network.Api
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GenerationLimitViewModel @Inject constructor(
    private val tokenManager: TokenManager
) : ViewModel() {

    companion object {
        private const val TAG = "GenerationLimitVM"
    }

    private val _limitInfo = MutableStateFlow<GenerationLimitResponse?>(null)
    val limitInfo: StateFlow<GenerationLimitResponse?> = _limitInfo.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        fetchGenerationLimit()
    }

    fun fetchGenerationLimit() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                val token = tokenManager.getToken()
                if (token == null) {
                    Log.e(TAG, "Token não encontrado")
                    _error.value = "Erro de autenticação"
                    return@launch
                }

                Log.d(TAG, "Buscando limite de gerações...")
                val response = Api.retrofitService.getGenerationLimit(token)

                Log.d(TAG, "Limite de gerações obtido: ${response.used}/${response.limit}")
                _limitInfo.value = response

            } catch (e: Exception) {
                Log.e(TAG, "Erro ao buscar limite de gerações", e)
                _error.value = "Erro ao carregar informações: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}
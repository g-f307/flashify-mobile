package com.example.flashify.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.flashify.model.data.ProgressStatsResponse
import com.example.flashify.model.manager.TokenManager
import com.example.flashify.model.network.Api
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.TimeZone
import java.util.concurrent.TimeUnit

// 1. Definir os estados da UI
sealed class ProgressUIState {
    object Loading : ProgressUIState()
    data class Success(val stats: ProgressStatsResponse) : ProgressUIState()
    data class Error(val message: String) : ProgressUIState()
}

class ProgressViewModel(application: Application) : AndroidViewModel(application) {

    private val tokenManager = TokenManager(application)
    private val apiService = Api.retrofitService

    // 2. Criar o StateFlow
    private val _progressState = MutableStateFlow<ProgressUIState>(ProgressUIState.Loading)
    val progressState = _progressState.asStateFlow()

    init {
        // 3. Buscar os dados assim que o ViewModel for criado
        fetchProgressStats()
    }

    fun fetchProgressStats() {
        viewModelScope.launch {
            _progressState.value = ProgressUIState.Loading
            val token = tokenManager.getToken()
            if (token == null) {
                _progressState.value = ProgressUIState.Error("Sessão expirada.")
                return@launch
            }

            try {
                // 4. Calcular o offset do fuso horário local em minutos
                // O backend precisa disso para calcular o "hoje" e "ontem" corretamente
                val timeZone = TimeZone.getDefault()
                val offsetInMillis = timeZone.getOffset(Calendar.getInstance().timeInMillis).toLong()
                val offsetInMinutes = TimeUnit.MILLISECONDS.toMinutes(offsetInMillis).toInt()

                // 5. Chamar a API com o token e o offset
                val statsResponse = apiService.getProgressStats(token, offsetInMinutes)

                // 6. Emitir o estado de Sucesso
                _progressState.value = ProgressUIState.Success(statsResponse)

            } catch (e: Exception) {
                // 7. Emitir o estado de Erro
                _progressState.value = ProgressUIState.Error(e.message ?: "Falha ao carregar progresso")
                e.printStackTrace() // Ajuda a depurar
            }
        }
    }
}
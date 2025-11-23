package com.example.flashify.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flashify.model.data.ProgressStatsResponse
import com.example.flashify.model.manager.TokenManager
import com.example.flashify.model.network.ApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.TimeZone
import java.util.concurrent.TimeUnit
import javax.inject.Inject

sealed class ProgressUIState {
    object Loading : ProgressUIState()
    data class Success(val stats: ProgressStatsResponse) : ProgressUIState()
    data class Error(val message: String) : ProgressUIState()
}

@HiltViewModel
class ProgressViewModel @Inject constructor(
    private val tokenManager: TokenManager,
    private val apiService: ApiService
) : ViewModel() {

    private val _progressState = MutableStateFlow<ProgressUIState>(ProgressUIState.Loading)
    val progressState = _progressState.asStateFlow()

    init {
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
                val timeZone = TimeZone.getDefault()
                val offsetInMillis = timeZone.getOffset(Calendar.getInstance().timeInMillis).toLong()
                val offsetInMinutes = TimeUnit.MILLISECONDS.toMinutes(offsetInMillis).toInt()

                val statsResponse = apiService.getProgressStats(token, offsetInMinutes)

                _progressState.value = ProgressUIState.Success(statsResponse)

            } catch (e: Exception) {
                _progressState.value = ProgressUIState.Error(e.message ?: "Falha ao carregar progresso")
                e.printStackTrace()
            }
        }
    }
}
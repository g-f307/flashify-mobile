package com.example.flashify.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flashify.model.data.ProgressStatsResponse
import com.example.flashify.model.manager.ConnectivityState
import com.example.flashify.model.manager.SyncManager
import com.example.flashify.model.manager.TokenManager
import com.example.flashify.model.network.ApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import java.util.TimeZone
import javax.inject.Inject

enum class StreakStatus {
    STUDIED, MISSED, PENDING
}

data class StreakDay(
    val dayLetter: String,
    val date: LocalDate,
    val status: StreakStatus
)

data class HomeUiState(
    val isLoadingStreak: Boolean = true,
    val isLoadingProgress: Boolean = true,
    val streakDays: List<StreakDay> = emptyList(),
    val streakCount: Int = 0,
    val cardsStudiedWeek: Int = 0,
    val generalAccuracy: Double = 0.0,
    val studyTimerProgress: Float = 0f,
    val weeklyActivity: List<Int> = emptyList(),
    val errorMessage: String? = null,
    val quizAverageScore: Double = 0.0,
    val quizzesCompletedWeek: Int = 0
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val tokenManager: TokenManager,
    private val apiService: ApiService,
    private val syncManager: SyncManager
) : ViewModel() {

    // --- 1. VARIÁVEIS DE ESTADO E CONSTANTES (Declaradas PRIMEIRO) ---

    private val SESSION_DURATION_SECONDS = 15 * 60

    // Inicializamos esta variável primeiro para evitar NullPointerException no init
    private val _timeStudiedToday = MutableStateFlow(0)

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    val connectivityState = syncManager.connectivityState
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ConnectivityState()
        )

    // ✅ Método público para forçar sincronização
    fun forceSyncNow() {
        Log.d("HomeViewModel", "🔄 forceSyncNow chamado")
        syncManager.forceSyncNow()
    }

    // --- 2. BLOCO DE INICIALIZAÇÃO (Executa DEPOIS das variáveis existirem) ---

    init {
        fetchProgressData()
        observeStudyTimer()
    }

    // --- 3. FUNÇÕES PÚBLICAS E LÓGICA ---

    fun addStudyTime(seconds: Int) {
        _timeStudiedToday.value += seconds
    }

    fun refresh() {
        fetchProgressData()
    }

    private fun fetchProgressData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingStreak = true, isLoadingProgress = true) }

            try {
                val token = tokenManager.getToken()
                if (token == null) {
                    _uiState.update {
                        it.copy(
                            isLoadingStreak = false,
                            isLoadingProgress = false,
                            errorMessage = "Token não encontrado"
                        )
                    }
                    return@launch
                }

                val timezoneOffset = TimeZone.getDefault().rawOffset / (1000 * 60)
                val stats = apiService.getProgressStats(token, timezoneOffset)

                val streakDays = buildStreakDays(stats)

                val estimatedTimeStudied = stats.cardsStudiedWeek * 30
                val timerProgress = (estimatedTimeStudied.toFloat() / SESSION_DURATION_SECONDS).coerceAtMost(1.0f)

                _uiState.update {
                    it.copy(
                        isLoadingStreak = false,
                        isLoadingProgress = false,
                        streakDays = streakDays,
                        streakCount = stats.streakDays,
                        cardsStudiedWeek = stats.cardsStudiedWeek,
                        generalAccuracy = stats.flashcardAccuracy,
                        studyTimerProgress = timerProgress,
                        weeklyActivity = stats.flashcardWeeklyActivity,
                        errorMessage = null,
                        quizAverageScore = stats.quizAverageScore,
                        quizzesCompletedWeek = stats.quizzesCompletedWeek
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoadingStreak = false,
                        isLoadingProgress = false,
                        errorMessage = e.message ?: "Erro ao carregar dados"
                    )
                }
            }
        }
    }

    private fun buildStreakDays(stats: ProgressStatsResponse): List<StreakDay> {
        val today = LocalDate.now()

        val monday = if (today.dayOfWeek == DayOfWeek.SUNDAY) {
            today.minusDays(6)
        } else {
            today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        }

        Log.d("HomeViewModel", "=== STREAK CALCULATION ===")
        Log.d("HomeViewModel", "Hoje: $today (${today.dayOfWeek})")
        Log.d("HomeViewModel", "Segunda da semana: $monday")
        Log.d("HomeViewModel", "Backend data: ${stats.flashcardWeeklyActivity}")

        val daysOfWeekLetters = listOf("S", "T", "Q", "Q", "S", "S", "D")

        val todayIndex = (0..6).indexOfFirst { i ->
            val date = monday.plusDays(i.toLong())
            date.isEqual(today)
        }

        // Lógica para lidar com possíveis inconsistências do backend (buggy backend check)
        val backendIsBuggy = stats.cardsStudiedWeek > 0 &&
                todayIndex >= 0 &&
                (stats.flashcardWeeklyActivity.getOrNull(todayIndex) ?: 0) == 0

        val streakDays = (0..6).map { i ->
            val date = monday.plusDays(i.toLong())
            val activityCount = stats.flashcardWeeklyActivity.getOrNull(i) ?: 0

            val status = when {
                date.isAfter(today) -> StreakStatus.PENDING

                date.isEqual(today) -> {
                    if (backendIsBuggy) {
                        StreakStatus.STUDIED
                    } else if (activityCount > 0) {
                        StreakStatus.STUDIED
                    } else {
                        StreakStatus.PENDING
                    }
                }

                else -> {
                    // Dias passados
                    if (backendIsBuggy && activityCount > 0) {
                        val totalDaysWithActivity = stats.flashcardWeeklyActivity.count { it > 0 }
                        if (totalDaysWithActivity == 1) {
                            StreakStatus.MISSED
                        } else {
                            StreakStatus.STUDIED
                        }
                    } else if (activityCount > 0) {
                        StreakStatus.STUDIED
                    } else {
                        StreakStatus.MISSED
                    }
                }
            }

            StreakDay(
                dayLetter = daysOfWeekLetters[i],
                date = date,
                status = status
            )
        }

        return streakDays
    }

    private fun observeStudyTimer() {
        viewModelScope.launch {
            // Coleta o fluxo de tempo estudado
            _timeStudiedToday.collect { timeInSeconds ->
                val progress = (timeInSeconds.toFloat() / SESSION_DURATION_SECONDS).coerceAtMost(1.0f)
                _uiState.update {
                    it.copy(studyTimerProgress = progress)
                }
            }
        }
    }
}
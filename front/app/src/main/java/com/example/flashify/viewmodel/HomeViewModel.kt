package com.example.flashify.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.flashify.model.data.ProgressStatsResponse
import com.example.flashify.model.manager.TokenManager
import com.example.flashify.model.network.Api
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import java.util.TimeZone

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

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val tokenManager = TokenManager(application)

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    private val SESSION_DURATION_SECONDS = 15 * 60

    private val _timeStudiedToday = MutableStateFlow(0)

    init {
        fetchProgressData()
        observeStudyTimer()
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
                val stats = Api.retrofitService.getProgressStats(token, timezoneOffset)

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

    /**
     * ✅ SOLUÇÃO DEFINITIVA: Marca apenas o dia com maior atividade recente
     * ou o dia de hoje se tiver atividade no índice correto
     */
    private fun buildStreakDays(stats: ProgressStatsResponse): List<StreakDay> {
        val today = LocalDate.now()

        // Calcular a segunda-feira correta
        val monday = if (today.dayOfWeek == DayOfWeek.SUNDAY) {
            today.minusDays(6)
        } else {
            today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        }

        Log.d("HomeViewModel", "=== STREAK CALCULATION ===")
        Log.d("HomeViewModel", "Hoje: $today (${today.dayOfWeek})")
        Log.d("HomeViewModel", "Segunda da semana: $monday")
        Log.d("HomeViewModel", "Backend data: ${stats.flashcardWeeklyActivity}")
        Log.d("HomeViewModel", "Cards studied: ${stats.cardsStudiedWeek}")

        val daysOfWeekLetters = listOf("S", "T", "Q", "Q", "S", "S", "D")

        // ✅ ENCONTRAR O ÍNDICE CORRETO DO DIA ATUAL
        val todayIndex = (0..6).indexOfFirst { i ->
            val date = monday.plusDays(i.toLong())
            date.isEqual(today)
        }

        Log.d("HomeViewModel", "Índice do dia atual: $todayIndex")

        // ✅ NOVA LÓGICA: Verificar se o backend está retornando dados bugados
        // Se cardsStudiedWeek > 0 mas o índice de hoje está zerado, então o backend está bugado
        val backendIsBuggy = stats.cardsStudiedWeek > 0 &&
                todayIndex >= 0 &&
                (stats.flashcardWeeklyActivity.getOrNull(todayIndex) ?: 0) == 0

        Log.d("HomeViewModel", "Backend bugado? $backendIsBuggy")

        val streakDays = (0..6).map { i ->
            val date = monday.plusDays(i.toLong())
            val activityCount = stats.flashcardWeeklyActivity.getOrNull(i) ?: 0

            val status = when {
                // Dia no futuro
                date.isAfter(today) -> {
                    Log.d("HomeViewModel", "[$i] $date - FUTURO -> PENDING")
                    StreakStatus.PENDING
                }

                // É o dia de hoje
                date.isEqual(today) -> {
                    if (backendIsBuggy) {
                        // Backend está bugado, então assume que estudou hoje
                        Log.d("HomeViewModel", "[$i] $date - HOJE (backend bugado, forçando STUDIED)")
                        StreakStatus.STUDIED
                    } else if (activityCount > 0) {
                        // Backend está OK e tem atividade
                        Log.d("HomeViewModel", "[$i] $date - HOJE com activity=$activityCount -> STUDIED")
                        StreakStatus.STUDIED
                    } else {
                        // Backend está OK mas sem atividade
                        Log.d("HomeViewModel", "[$i] $date - HOJE sem atividade -> PENDING")
                        StreakStatus.PENDING
                    }
                }

                // Dia no passado
                else -> {
                    // ✅ CORREÇÃO CRÍTICA: Se o backend está bugado E este índice tem atividade,
                    // então essa atividade é FALSA (é do dia de hoje marcado errado)
                    if (backendIsBuggy && activityCount > 0) {
                        // Verifica se este é o único dia com atividade (indica que é o bug)
                        val totalDaysWithActivity = stats.flashcardWeeklyActivity.count { it > 0 }
                        if (totalDaysWithActivity == 1) {
                            // É o único dia marcado, então é o bug do backend
                            Log.d("HomeViewModel", "[$i] $date - PASSADO mas é o bug do backend -> MISSED")
                            StreakStatus.MISSED
                        } else {
                            // Tem outros dias marcados, então este é legítimo
                            Log.d("HomeViewModel", "[$i] $date - PASSADO com activity=$activityCount -> STUDIED")
                            StreakStatus.STUDIED
                        }
                    } else if (activityCount > 0) {
                        Log.d("HomeViewModel", "[$i] $date - PASSADO com activity=$activityCount -> STUDIED")
                        StreakStatus.STUDIED
                    } else {
                        Log.d("HomeViewModel", "[$i] $date - PASSADO sem atividade -> MISSED")
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

        Log.d("HomeViewModel", "Resultado final: ${streakDays.mapIndexed { idx, day -> "$idx:${day.dayLetter}=${day.status}" }}")
        Log.d("HomeViewModel", "=== FIM DO CÁLCULO ===")

        return streakDays
    }

    private fun observeStudyTimer() {
        viewModelScope.launch {
            _timeStudiedToday.collect { timeInSeconds ->
                val progress = (timeInSeconds.toFloat() / SESSION_DURATION_SECONDS).coerceAtMost(1.0f)
                _uiState.update {
                    it.copy(studyTimerProgress = progress)
                }
            }
        }
    }

    fun addStudyTime(seconds: Int) {
        _timeStudiedToday.value += seconds
    }

    fun refresh() {
        fetchProgressData()
    }
}
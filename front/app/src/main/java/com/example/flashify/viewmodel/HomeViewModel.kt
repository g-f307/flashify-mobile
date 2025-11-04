package com.example.flashify.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.flashify.model.data.DeckResponse
import com.example.flashify.model.data.ProgressStatsResponse
import com.example.flashify.model.manager.TokenManager
import com.example.flashify.model.network.Api
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import java.util.TimeZone

// --- 1. DEFINIÇÕES DE ESTADO ---

// Status para cada dia do Streak
enum class StreakStatus {
    STUDIED, MISSED, PENDING
}

// Representa um dia na barra de streak
data class StreakDay(
    val dayLetter: String,
    val date: LocalDate,
    val status: StreakStatus
)

// O estado completo da UI da Home
data class HomeUiState(
    val isLoadingStreak: Boolean = true,
    val isLoadingProgress: Boolean = true,
    val streakDays: List<StreakDay> = emptyList(),
    val streakCount: Int = 0,
    val cardsStudiedWeek: Int = 0,
    val generalAccuracy: Double = 0.0,
    val studyTimerProgress: Float = 0f, // 0.0f a 1.0f
    val weeklyActivity: List<Int> = emptyList(),
    val errorMessage: String? = null,

    // ▼▼▼ CAMPOS ADICIONADOS ▼▼▼
    val quizAverageScore: Double = 0.0,
    val quizzesCompletedWeek: Int = 0
    // ▲▲▲ FIM DA ADIÇÃO ▲▲▲
)

// --- 2. O VIEWMODEL ---

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val tokenManager = TokenManager(application)

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    // Duração da sessão em segundos (15 minutos)
    private val SESSION_DURATION_SECONDS = 15 * 60

    // Tempo estudado em segundos
    private val _timeStudiedToday = MutableStateFlow(0)

    init {
        fetchProgressData()
        observeStudyTimer()
    }

    /**
     * Busca os dados de progresso do backend
     */
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

                // Calcular timezone offset em minutos
                val timezoneOffset = TimeZone.getDefault().rawOffset / (1000 * 60)

                // Buscar estatísticas do backend
                val stats = Api.retrofitService.getProgressStats(token, timezoneOffset)

                // Processar dados de streak
                val streakDays = buildStreakDays(stats)

                // Calcular progresso da sessão de estudo
                // Assumindo que cada card estudado leva ~30 segundos
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

                        // ▼▼▼ CAMPOS ADICIONADOS ▼▼▼
                        quizAverageScore = stats.quizAverageScore,
                        quizzesCompletedWeek = stats.quizzesCompletedWeek
                        // ▲▲▲ FIM DA ADIÇÃO ▲▲▲
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
     * Constrói a lista de dias do streak baseado nos dados do backend
     */
    private fun buildStreakDays(stats: ProgressStatsResponse): List<StreakDay> {
        val today = LocalDate.now()

        // java.time.DayOfWeek: MONDAY(1) ... SUNDAY(7)
        // Queremos encontrar a Segunda-feira desta semana.
        val daysToSubtract = today.dayOfWeek.value - 1 // Se hoje for Seg(1) -> subtrai 0. Se for Dom(7) -> subtrai 6.
        val monday = today.minusDays(daysToSubtract.toLong())

        // Letras dos dias da semana, começando por Segunda
        val daysOfWeekLetters = listOf("S", "T", "Q", "Q", "S", "S", "D") // Mon, Tue, Wed, Thu, Fri, Sat, Sun

        // Criar lista de 7 dias (Segunda a Domingo)
        return (0..6).map { i ->
            // i = 0 é Segunda, i = 6 é Domingo
            val date = monday.plusDays(i.toLong())

            val activityCount = stats.flashcardWeeklyActivity.getOrNull(i) ?: 0

            val status = when {
                // Se o dia é no futuro
                date.isAfter(today) -> StreakStatus.PENDING

                // Se o dia é hoje
                date.isEqual(today) -> {
                    // Verificar se estudou hoje (atividade > 0)
                    if (activityCount > 0) {
                        StreakStatus.STUDIED
                    } else {
                        StreakStatus.PENDING
                    }
                }

                // Se o dia é no passado
                else -> {
                    // Dias passados
                    if (activityCount > 0) {
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
    }

    /**
     * Observa o tempo estudado e atualiza o progresso
     */
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

    /**
     * Adiciona tempo de estudo
     * Deve ser chamado da TelaEstudo
     */
    fun addStudyTime(seconds: Int) {
        _timeStudiedToday.value += seconds
    }

    /**
     * Recarrega os dados
     */
    fun refresh() {
        fetchProgressData()
    }
}
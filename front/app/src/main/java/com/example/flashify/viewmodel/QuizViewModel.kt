package com.example.flashify.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.flashify.model.data.CheckAnswerRequest
import com.example.flashify.model.data.CheckAnswerResponse
import com.example.flashify.model.data.QuizResponse
import com.example.flashify.model.data.SubmitQuizRequest
import com.example.flashify.model.manager.TokenManager
import com.example.flashify.model.network.Api
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import android.util.Log

sealed class QuizState {
    object Idle : QuizState()
    object Loading : QuizState()
    data class Success(val quiz: QuizResponse) : QuizState()
    data class Error(val message: String) : QuizState()
}

sealed class AnswerCheckState {
    object Idle : AnswerCheckState()
    object Loading : AnswerCheckState()
    data class Success(val result: CheckAnswerResponse) : AnswerCheckState()
    data class Error(val message: String) : AnswerCheckState()
}

sealed class QuizSubmitState {
    object Idle : QuizSubmitState()
    object Loading : QuizSubmitState()
    object Success : QuizSubmitState()
    data class Error(val message: String) : QuizSubmitState()
}

class QuizViewModel(application: Application) : AndroidViewModel(application) {

    private val tokenManager = TokenManager(application)

    private val _quizState = MutableStateFlow<QuizState>(QuizState.Idle)
    val quizState: StateFlow<QuizState> = _quizState

    private val _answerCheckState = MutableStateFlow<AnswerCheckState>(AnswerCheckState.Idle)
    val answerCheckState: StateFlow<AnswerCheckState> = _answerCheckState

    private val _quizSubmitState = MutableStateFlow<QuizSubmitState>(QuizSubmitState.Idle)
    val quizSubmitState: StateFlow<QuizSubmitState> = _quizSubmitState

    fun loadQuiz(documentId: Int) {
        viewModelScope.launch {
            _quizState.value = QuizState.Loading
            try {
                val token = tokenManager.getToken()
                if (token == null) {
                    _quizState.value = QuizState.Error("Token de autenticação não encontrado")
                    return@launch
                }

                val documentDetail = Api.retrofitService.getDocumentDetailWithQuiz(token, documentId)

                if (documentDetail.quiz != null) {
                    _quizState.value = QuizState.Success(documentDetail.quiz)
                } else {
                    _quizState.value = QuizState.Error("Este deck não possui um quiz")
                }
            } catch (e: Exception) {
                _quizState.value = QuizState.Error(e.message ?: "Erro ao carregar quiz")
            }
        }
    }

    fun checkAnswer(questionId: Int, answerId: Int) {
        viewModelScope.launch {
            _answerCheckState.value = AnswerCheckState.Loading
            try {
                val token = tokenManager.getToken()
                if (token == null) {
                    _answerCheckState.value = AnswerCheckState.Error("Token de autenticação não encontrado")
                    return@launch
                }

                val request = CheckAnswerRequest(questionId, answerId)
                val result = Api.retrofitService.checkQuizAnswer(token, request)

                _answerCheckState.value = AnswerCheckState.Success(result)
            } catch (e: Exception) {
                _answerCheckState.value = AnswerCheckState.Error(e.message ?: "Erro ao verificar resposta")
            }
        }
    }

    fun submitQuiz(quizId: Int, score: Float, correctAnswers: Int, totalQuestions: Int) {
        viewModelScope.launch {
            _quizSubmitState.value = QuizSubmitState.Loading
            try {
                val token = tokenManager.getToken()
                if (token == null) {
                    _quizSubmitState.value = QuizSubmitState.Error("Token de autenticação não encontrado")
                    return@launch
                }

                val request = SubmitQuizRequest(score, correctAnswers, totalQuestions)
                Api.retrofitService.submitQuizAttempt(token, quizId, request)

                _quizSubmitState.value = QuizSubmitState.Success
            } catch (e: Exception) {
                _quizSubmitState.value = QuizSubmitState.Error(e.message ?: "Erro ao submeter quiz")
            }
        }
    }

    fun resetAnswerCheckState() {
        _answerCheckState.value = AnswerCheckState.Idle
    }

    fun resetSubmitState() {
        _quizSubmitState.value = QuizSubmitState.Idle
    }

    // ✅ NOVA FUNÇÃO ADICIONADA
    fun refreshDeckStats(documentId: Int, onStatsUpdated: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                val token = tokenManager.getToken() ?: return@launch

                val stats = Api.retrofitService.getDocumentStats(token, documentId)

                Log.d("QuizViewModel", """
                    📊 Stats atualizadas para deck $documentId:
                    - Quiz tentativas: ${stats.quiz?.totalAttempts}
                    - Quiz média: ${stats.quiz?.averageScore}%
                    - Quiz última: ${stats.quiz?.lastScore}%
                """.trimIndent())

                onStatsUpdated()

            } catch (e: Exception) {
                Log.e("QuizViewModel", "❌ Erro ao atualizar stats: ${e.message}")
            }
        }
    }
}

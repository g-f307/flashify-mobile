package com.example.flashify.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flashify.model.data.CheckAnswerRequest
import com.example.flashify.model.data.CheckAnswerResponse
import com.example.flashify.model.data.QuizResponse
import com.example.flashify.model.data.SubmitQuizRequest
import com.example.flashify.model.database.dao.*
import com.example.flashify.model.database.dataclass.*
import com.example.flashify.model.manager.SyncManager
import com.example.flashify.model.manager.TokenManager
import com.example.flashify.model.network.ApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

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

@HiltViewModel
class QuizViewModel @Inject constructor(
    private val tokenManager: TokenManager,
    private val apiService: ApiService,
    private val syncManager: SyncManager, // ✅ NOVO
    private val quizDao: QuizDao, // ✅ NOVO
    private val questionDao: QuestionDao, // ✅ NOVO
    private val answerDao: AnswerDao, // ✅ NOVO
    private val quizAttemptDao: QuizAttemptDao // ✅ NOVO
) : ViewModel() {

    private val _quizState = MutableStateFlow<QuizState>(QuizState.Idle)
    val quizState: StateFlow<QuizState> = _quizState

    private val _answerCheckState = MutableStateFlow<AnswerCheckState>(AnswerCheckState.Idle)
    val answerCheckState: StateFlow<AnswerCheckState> = _answerCheckState

    private val _quizSubmitState = MutableStateFlow<QuizSubmitState>(QuizSubmitState.Idle)
    val quizSubmitState: StateFlow<QuizSubmitState> = _quizSubmitState

    private fun getCurrentUserId(): Int = tokenManager.getUserId()

    /**
     * ✅ NOVO: Carregar quiz com suporte offline
     */
    // QuizViewModel.kt
    fun loadQuiz(documentId: Int) {
        viewModelScope.launch {
            _quizState.value = QuizState.Loading
            val userId = getCurrentUserId()

            Log.d("QuizViewModel", "🔍 === INÍCIO BUSCA QUIZ ===")
            Log.d("QuizViewModel", "🔍 Document ID: $documentId")
            Log.d("QuizViewModel", "🔍 User ID: $userId")
            Log.d("QuizViewModel", "🔍 Online: ${syncManager.isOnline()}")

            if (userId == TokenManager.INVALID_USER_ID) {
                _quizState.value = QuizState.Error("Utilizador inválido")
                return@launch
            }

            // ✅ 1️⃣ SEMPRE tentar carregar do cache PRIMEIRO
            try {
                Log.d("QuizViewModel", "🔍 Tentando ler quiz do CACHE...")

                val localQuiz = quizDao.getQuizByDocumentId(documentId, userId)

                Log.d("QuizViewModel", "🔍 Quiz encontrado no cache: ${localQuiz != null}")

                if (localQuiz != null) {
                    Log.d("QuizViewModel", "🔍 Quiz ID: ${localQuiz.id}, Title: ${localQuiz.title}")

                    val questions = questionDao.getQuestionsByQuizId(localQuiz.id, userId)
                    Log.d("QuizViewModel", "🔍 Perguntas encontradas: ${questions.size}")

                    // 🔍 DETALHE DAS PERGUNTAS
                    questions.take(2).forEachIndexed { index, question ->
                        val answers = answerDao.getAnswersByQuestionId(question.id, userId)
                        Log.d("QuizViewModel", "🔍 Pergunta[$index]: id=${question.id}, quizId=${question.quizId}, respostas=${answers.size}")
                    }

                    val quizResponse = localQuiz.toQuizResponse(questions, answerDao, userId)

                    // ✅ MOSTRAR CACHE IMEDIATAMENTE
                    _quizState.value = QuizState.Success(quizResponse)
                    Log.d("QuizViewModel", "✅ Quiz carregado do cache com ${questions.size} perguntas")

                    // ✅ Se estiver ONLINE, atualizar em background
                    if (syncManager.isOnline()) {
                        Log.d("QuizViewModel", "🔄 Online - atualizando cache em background")
                        loadQuizFromNetwork(documentId, userId, silent = true)
                    } else {
                        Log.d("QuizViewModel", "📵 Offline - usando apenas cache")
                    }
                    return@launch
                } else {
                    Log.d("QuizViewModel", "⚠️ Quiz não encontrado no cache")
                }
            } catch (e: Exception) {
                Log.e("QuizViewModel", "❌ Erro ao ler cache: ${e.message}", e)
            }

            // ✅ 2️⃣ Cache vazio - VERIFICAR se está offline
            if (!syncManager.isOnline()) {
                Log.w("QuizViewModel", "📵 Offline e SEM CACHE")
                _quizState.value = QuizState.Error(
                    "Este quiz não está disponível offline. Conecte-se à internet para baixá-lo."
                )
                return@launch
            }

            // ✅ 3️⃣ Online e cache vazio - buscar da rede
            Log.d("QuizViewModel", "🌐 Online e cache vazio - buscando da rede")
            loadQuizFromNetwork(documentId, userId, silent = false)
        }
    }

    /**
     * ✅ NOVO: Buscar quiz da rede e salvar no cache
     */
    private suspend fun loadQuizFromNetwork(documentId: Int, userId: Int, silent: Boolean) {
        val token = tokenManager.getToken()
        if (token == null) {
            if (!silent) {
                _quizState.value = QuizState.Error("Token de autenticação não encontrado")
            }
            return
        }

        try {
            val documentDetail = apiService.getDocumentDetailWithQuiz(token, documentId)

            if (documentDetail.quiz != null) {
                // Salvar no cache
                val quizEntity = QuizEntity(
                    id = documentDetail.quiz.id,
                    title = documentDetail.quiz.title,
                    documentId = documentDetail.quiz.documentId,
                    userId = userId,
                    isSynced = true
                )
                quizDao.insertQuiz(quizEntity)

                // Salvar perguntas
                val questionEntities = documentDetail.quiz.questions.mapIndexed { index, q ->
                    QuestionEntity(
                        id = q.id,
                        text = q.text,
                        quizId = q.quizId,
                        userId = userId,
                        orderIndex = index,
                        isSynced = true
                    )
                }
                questionDao.insertQuestions(questionEntities)

                // Salvar respostas
                documentDetail.quiz.questions.forEach { question ->
                    val answerEntities = question.answers.mapIndexed { index, a ->
                        AnswerEntity(
                            id = a.id,
                            text = a.text,
                            isCorrect = a.isCorrect,
                            explanation = a.explanation,
                            questionId = a.questionId,
                            userId = userId,
                            orderIndex = index,
                            isSynced = true
                        )
                    }
                    answerDao.insertAnswers(answerEntities)
                }

                Log.d("QuizViewModel", "🔄 Quiz sincronizado e salvo no cache")

                _quizState.value = QuizState.Success(documentDetail.quiz)
            } else {
                if (!silent) {
                    _quizState.value = QuizState.Error("Este deck não possui um quiz")
                }
            }
        } catch (e: Exception) {
            Log.e("QuizViewModel", "❌ Erro ao carregar quiz: ${e.message}")
            if (!silent) {
                _quizState.value = QuizState.Error(e.message ?: "Erro ao carregar quiz")
            }
        }
    }

    fun checkAnswer(questionId: Int, answerId: Int) {
        viewModelScope.launch {
            _answerCheckState.value = AnswerCheckState.Loading

            // ✅ Se estiver OFFLINE, verificar localmente
            if (!syncManager.isOnline()) {
                checkAnswerLocally(questionId, answerId)
                return@launch
            }

            try {
                val token = tokenManager.getToken()
                if (token == null) {
                    _answerCheckState.value = AnswerCheckState.Error("Token de autenticação não encontrado")
                    return@launch
                }

                val request = CheckAnswerRequest(questionId, answerId)
                val result = apiService.checkQuizAnswer(token, request)

                _answerCheckState.value = AnswerCheckState.Success(result)
            } catch (e: Exception) {
                // ✅ Se falhar na rede, tentar localmente
                Log.w("QuizViewModel", "⚠️ Erro na rede, verificando localmente")
                checkAnswerLocally(questionId, answerId)
            }
        }
    }

    /**
     * ✅ NOVO: Verificar resposta usando dados locais
     */
    private suspend fun checkAnswerLocally(questionId: Int, answerId: Int) {
        try {
            val userId = getCurrentUserId()
            val answers = answerDao.getAnswersByQuestionId(questionId, userId)

            val selectedAnswer = answers.find { it.id == answerId }
            val correctAnswer = answers.find { it.isCorrect }

            if (selectedAnswer != null && correctAnswer != null) {
                val result = CheckAnswerResponse(
                    isCorrect = selectedAnswer.isCorrect,
                    correctAnswerId = correctAnswer.id,
                    explanation = selectedAnswer.explanation ?: correctAnswer.explanation ?: "Sem explicação disponível"
                )

                _answerCheckState.value = AnswerCheckState.Success(result)
                Log.d("QuizViewModel", "✅ Resposta verificada localmente (offline)")
            } else {
                _answerCheckState.value = AnswerCheckState.Error("Não foi possível verificar a resposta")
            }
        } catch (e: Exception) {
            Log.e("QuizViewModel", "❌ Erro ao verificar resposta localmente: ${e.message}")
            _answerCheckState.value = AnswerCheckState.Error("Erro ao verificar resposta")
        }
    }

    /**
     * ✅ NOVO: Submeter quiz com suporte offline
     */
    fun submitQuiz(quizId: Int, score: Float, correctAnswers: Int, totalQuestions: Int) {
        viewModelScope.launch {
            _quizSubmitState.value = QuizSubmitState.Loading
            val userId = getCurrentUserId()

            if (userId == TokenManager.INVALID_USER_ID) {
                _quizSubmitState.value = QuizSubmitState.Error("Utilizador inválido")
                return@launch
            }

            // 1️⃣ Salvar tentativa localmente SEMPRE
            try {
                val attemptEntity = QuizAttemptEntity(
                    quizId = quizId,
                    userId = userId,
                    score = score,
                    correctAnswers = correctAnswers,
                    totalQuestions = totalQuestions,
                    attemptDate = System.currentTimeMillis(),
                    isSynced = false
                )

                quizAttemptDao.insertAttempt(attemptEntity)
                Log.d("QuizViewModel", "💾 Tentativa salva localmente")

            } catch (e: Exception) {
                Log.e("QuizViewModel", "❌ Erro ao salvar tentativa localmente: ${e.message}")
            }

            // 2️⃣ Se estiver online, sincronizar imediatamente
            if (syncManager.isOnline()) {
                val token = tokenManager.getToken()
                if (token != null) {
                    try {
                        val request = SubmitQuizRequest(score, correctAnswers, totalQuestions)
                        apiService.submitQuizAttempt(token, quizId, request)

                        // Marcar como sincronizado
                        val unsyncedAttempts = quizAttemptDao.getUnsyncedAttempts(userId)
                        val thisAttempt = unsyncedAttempts.lastOrNull { it.quizId == quizId }
                        thisAttempt?.let {
                            quizAttemptDao.markAttemptAsSynced(it.localId)
                            Log.d("QuizViewModel", "✅ Tentativa sincronizada")
                        }

                    } catch (e: Exception) {
                        Log.w("QuizViewModel", "⚠️ Erro ao sincronizar: ${e.message} (será sincronizado depois)")
                    }
                }
            } else {
                Log.d("QuizViewModel", "📵 Offline - tentativa será sincronizada quando estiver online")
            }

            _quizSubmitState.value = QuizSubmitState.Success
        }
    }

    fun resetAnswerCheckState() {
        _answerCheckState.value = AnswerCheckState.Idle
    }

    fun resetSubmitState() {
        _quizSubmitState.value = QuizSubmitState.Idle
    }

    fun refreshDeckStats(documentId: Int, onStatsUpdated: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                val token = tokenManager.getToken() ?: return@launch

                val stats = apiService.getDocumentStats(token, documentId)

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

    /**
     * ✅ NOVO: Converter entidades locais para resposta da API
     */
    private suspend fun QuizEntity.toQuizResponse(
        questions: List<QuestionEntity>,
        answerDao: AnswerDao,
        userId: Int
    ): QuizResponse {
        val questionResponses = questions.map { question ->
            val answers = answerDao.getAnswersByQuestionId(question.id, userId)

            com.example.flashify.model.data.QuestionResponse(
                id = question.id,
                text = question.text,
                quizId = question.quizId,
                answers = answers.map { answer ->
                    com.example.flashify.model.data.AnswerResponse(
                        id = answer.id,
                        text = answer.text,
                        isCorrect = answer.isCorrect,
                        explanation = answer.explanation,
                        questionId = answer.questionId
                    )
                }
            )
        }

        return QuizResponse(
            id = this.id,
            title = this.title,
            documentId = this.documentId,
            questions = questionResponses
        )
    }
}
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
    // QuizViewModel.kt - Método loadQuiz() CORRIGIDO

    // QuizViewModel.kt - SUBSTITUIR O MÉTODO loadQuiz() COMPLETO

    fun loadQuiz(documentId: Int) {
        viewModelScope.launch {
            _quizState.value = QuizState.Loading
            val userId = getCurrentUserId()

            Log.d("QuizViewModel", "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            Log.d("QuizViewModel", "🔍 INÍCIO BUSCA QUIZ")
            Log.d("QuizViewModel", "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            Log.d("QuizViewModel", "📋 Document ID: $documentId")
            Log.d("QuizViewModel", "👤 User ID: $userId")
            Log.d("QuizViewModel", "🌐 Online: ${syncManager.isOnline()}")

            if (userId == TokenManager.INVALID_USER_ID) {
                _quizState.value = QuizState.Error("Utilizador inválido")
                return@launch
            }

            // ✅ DIAGNÓSTICO COMPLETO DO BANCO
            try {
                Log.d("QuizViewModel", "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                Log.d("QuizViewModel", "🔧 DIAGNÓSTICO DO BANCO DE DADOS")
                Log.d("QuizViewModel", "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                // 1. Verificar TODOS os quizzes no banco
                val allQuizzes = quizDao.getQuizzesForDebug(userId)
                Log.d("QuizViewModel", "📊 Total de quizzes no banco: ${allQuizzes.size}")

                allQuizzes.forEachIndexed { index, quiz ->
                    Log.d("QuizViewModel", "   Quiz #${index + 1}:")
                    Log.d("QuizViewModel", "      ID: ${quiz.id}")
                    Log.d("QuizViewModel", "      Title: ${quiz.title}")
                    Log.d("QuizViewModel", "      DocumentId: ${quiz.documentId}")
                    Log.d("QuizViewModel", "      UserId: ${quiz.userId}")
                    Log.d("QuizViewModel", "      IsSynced: ${quiz.isSynced}")

                    // Verificar perguntas
                    val questions = questionDao.getQuestionsByQuizId(quiz.id, userId)
                    Log.d("QuizViewModel", "      Perguntas: ${questions.size}")

                    questions.take(2).forEachIndexed { qIndex, question ->
                        val answers = answerDao.getAnswersByQuestionId(question.id, userId)
                        Log.d("QuizViewModel", "         Q${qIndex + 1}: ${question.text.take(40)}... (${answers.size} respostas)")
                    }

                    Log.d("QuizViewModel", "   ---")
                }

                // 2. Procurar o quiz específico
                Log.d("QuizViewModel", "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                Log.d("QuizViewModel", "🔍 BUSCANDO QUIZ PARA DOCUMENTO $documentId")
                Log.d("QuizViewModel", "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                val targetQuiz = allQuizzes.find { it.documentId == documentId }

                if (targetQuiz != null) {
                    Log.d("QuizViewModel", "✅ Quiz encontrado no diagnóstico!")
                    Log.d("QuizViewModel", "   ID do Quiz: ${targetQuiz.id}")
                    Log.d("QuizViewModel", "   Agora tentando query normal...")

                    // Tentar query normal
                    val normalQuery = quizDao.getQuizByDocumentId(documentId, userId)

                    if (normalQuery != null) {
                        Log.d("QuizViewModel", "✅ Query normal funcionou!")
                    } else {
                        Log.e("QuizViewModel", "❌ ERRO CRÍTICO: Query normal falhou!")
                        Log.e("QuizViewModel", "   Quiz existe mas getQuizByDocumentId() retornou NULL")
                        Log.e("QuizViewModel", "   Tentando query direta pelo ID do quiz...")

                        // Usar query direta
                        val directQuery = quizDao.getQuizById(targetQuiz.id, userId)

                        if (directQuery != null) {
                            Log.d("QuizViewModel", "✅ Query direta funcionou!")

                            // Processar quiz encontrado
                            val questions = questionDao.getQuestionsByQuizId(directQuery.id, userId)

                            if (questions.isNotEmpty()) {
                                val quizResponse = directQuery.toQuizResponse(questions, answerDao, userId)
                                _quizState.value = QuizState.Success(quizResponse)
                                Log.d("QuizViewModel", "✅ Quiz carregado via query direta com ${questions.size} perguntas")
                                return@launch
                            } else {
                                Log.e("QuizViewModel", "❌ Quiz sem perguntas!")
                            }
                        } else {
                            Log.e("QuizViewModel", "❌ Query direta também falhou!")
                        }
                    }
                } else {
                    Log.e("QuizViewModel", "❌ Quiz não existe no banco para documentId=$documentId")
                    Log.e("QuizViewModel", "   Quizzes disponíveis:")
                    allQuizzes.forEach { q ->
                        Log.e("QuizViewModel", "      - Doc ${q.documentId}: Quiz ${q.id}")
                    }
                }

            } catch (e: Exception) {
                Log.e("QuizViewModel", "❌ ERRO NO DIAGNÓSTICO: ${e.message}", e)
            }

            // ✅ TENTATIVA 1: Query normal
            Log.d("QuizViewModel", "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            Log.d("QuizViewModel", "📥 TENTATIVA 1: Query Normal")
            Log.d("QuizViewModel", "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

            try {
                val localQuiz = quizDao.getQuizByDocumentId(documentId, userId)

                if (localQuiz != null) {
                    Log.d("QuizViewModel", "✅ Quiz encontrado!")

                    val questions = questionDao.getQuestionsByQuizId(localQuiz.id, userId)
                    Log.d("QuizViewModel", "📝 Perguntas: ${questions.size}")

                    if (questions.isEmpty()) {
                        Log.e("QuizViewModel", "❌ Quiz sem perguntas - forçando reload da rede")

                        if (syncManager.isOnline()) {
                            loadQuizFromNetwork(documentId, userId, silent = false)
                            return@launch
                        } else {
                            _quizState.value = QuizState.Error(
                                "Quiz incompleto. Conecte-se à internet para recarregar."
                            )
                            return@launch
                        }
                    }

                    // Verificar respostas
                    var missingAnswers = false
                    questions.forEach { question ->
                        val answers = answerDao.getAnswersByQuestionId(question.id, userId)
                        if (answers.isEmpty()) {
                            Log.e("QuizViewModel", "❌ Pergunta ${question.id} sem respostas!")
                            missingAnswers = true
                        }
                    }

                    if (missingAnswers) {
                        Log.e("QuizViewModel", "❌ Quiz com respostas faltando - recarregando")

                        if (syncManager.isOnline()) {
                            loadQuizFromNetwork(documentId, userId, silent = false)
                            return@launch
                        }
                    }

                    // Converter e retornar
                    val quizResponse = localQuiz.toQuizResponse(questions, answerDao, userId)
                    _quizState.value = QuizState.Success(quizResponse)
                    Log.d("QuizViewModel", "✅ Quiz carregado com sucesso!")

                    // Atualizar em background se online
                    if (syncManager.isOnline()) {
                        Log.d("QuizViewModel", "🔄 Atualizando em background...")
                        loadQuizFromNetwork(documentId, userId, silent = true)
                    }

                    return@launch
                } else {
                    Log.w("QuizViewModel", "⚠️ Query normal retornou NULL")
                }
            } catch (e: Exception) {
                Log.e("QuizViewModel", "❌ Erro na query normal: ${e.message}", e)
            }

            // ✅ TENTATIVA 2: Buscar da rede
            Log.d("QuizViewModel", "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            Log.d("QuizViewModel", "🌐 TENTATIVA 2: Buscar da Rede")
            Log.d("QuizViewModel", "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

            if (!syncManager.isOnline()) {
                Log.e("QuizViewModel", "❌ Offline e quiz não encontrado no cache")
                _quizState.value = QuizState.Error(
                    "Este quiz não está disponível offline. Conecte-se à internet."
                )
                return@launch
            }

            Log.d("QuizViewModel", "📡 Buscando da rede...")
            loadQuizFromNetwork(documentId, userId, silent = false)
        }
    }

    // ✅ Método para buscar da rede
    private suspend fun loadQuizFromNetwork(documentId: Int, userId: Int, silent: Boolean) {
        val token = tokenManager.getToken()
        if (token == null) {
            if (!silent) {
                _quizState.value = QuizState.Error("Token não encontrado")
            }
            return
        }

        try {
            Log.d("QuizViewModel", "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            Log.d("QuizViewModel", "🌐 BUSCANDO QUIZ DA REDE")
            Log.d("QuizViewModel", "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

            val documentDetail = apiService.getDocumentDetailWithQuiz(token, documentId)

            if (documentDetail.quiz != null) {
                val quiz = documentDetail.quiz

                Log.d("QuizViewModel", "✅ Quiz recebido da API:")
                Log.d("QuizViewModel", "   ID: ${quiz.id}")
                Log.d("QuizViewModel", "   Title: ${quiz.title}")
                Log.d("QuizViewModel", "   Questions: ${quiz.questions.size}")

                // LIMPAR DADOS ANTIGOS PRIMEIRO
                Log.d("QuizViewModel", "🧹 Limpando dados antigos...")
                try {
                    // Deletar quiz antigo se existir
                    quizDao.deleteQuizByDocumentId(documentId, userId)
                    Log.d("QuizViewModel", "✅ Dados antigos removidos")
                } catch (e: Exception) {
                    Log.w("QuizViewModel", "⚠️ Erro ao limpar: ${e.message}")
                }

                // Salvar novo quiz
                val quizEntity = QuizEntity(
                    id = quiz.id,
                    title = quiz.title,
                    documentId = quiz.documentId,
                    userId = userId,
                    isSynced = true
                )

                Log.d("QuizViewModel", "💾 Salvando quiz...")
                quizDao.insertQuiz(quizEntity)

                // Salvar perguntas
                val questionEntities = quiz.questions.mapIndexed { index, q ->
                    QuestionEntity(
                        id = q.id,
                        text = q.text,
                        quizId = q.quizId,
                        userId = userId,
                        orderIndex = index,
                        isSynced = true
                    )
                }

                Log.d("QuizViewModel", "💾 Salvando ${questionEntities.size} perguntas...")
                questionDao.insertQuestions(questionEntities)

                // Salvar respostas
                var totalAnswers = 0
                quiz.questions.forEach { question ->
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
                    totalAnswers += answerEntities.size
                    answerDao.insertAnswers(answerEntities)
                }

                Log.d("QuizViewModel", "💾 Salvando $totalAnswers respostas...")
                Log.d("QuizViewModel", "✅ Quiz sincronizado completamente!")

                _quizState.value = QuizState.Success(quiz)
            } else {
                Log.w("QuizViewModel", "⚠️ Documento não possui quiz")
                if (!silent) {
                    _quizState.value = QuizState.Error("Este deck não possui um quiz")
                }
            }
        } catch (e: Exception) {
            Log.e("QuizViewModel", "❌ Erro ao carregar da rede: ${e.message}", e)
            if (!silent) {
                _quizState.value = QuizState.Error(e.message ?: "Erro ao carregar quiz")
            }
        }
    }

    // ✅ Método auxiliar para converter
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
}
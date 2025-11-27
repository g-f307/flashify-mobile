package com.example.flashify.model.database.dao

import androidx.room.*
import com.example.flashify.model.data.QuizWithQuestionCount
import com.example.flashify.model.database.dataclass.*
import kotlinx.coroutines.flow.Flow

@Dao
interface QuizDao {
    // ===== CRUD BÁSICO =====

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuiz(quiz: QuizEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuizzes(quizzes: List<QuizEntity>)

    @Query("SELECT * FROM quizzes WHERE documentId = :documentId AND userId = :userId LIMIT 1")
    suspend fun getQuizByDocumentId(documentId: Int, userId: Int): QuizEntity?

    @Query("SELECT * FROM quizzes WHERE id = :quizId AND userId = :userId")
    suspend fun getQuizById(quizId: Int, userId: Int): QuizEntity?

    @Query("DELETE FROM quizzes WHERE documentId = :documentId AND userId = :userId")
    suspend fun deleteQuizByDocumentId(documentId: Int, userId: Int)

    @Query("DELETE FROM quizzes WHERE userId = :userId")
    suspend fun deleteAllQuizzesForUser(userId: Int)

    // ===== SINCRONIZAÇÃO =====

    @Query("SELECT * FROM quizzes WHERE userId = :userId AND isSynced = 0")
    suspend fun getUnsyncedQuizzes(userId: Int): List<QuizEntity>

    @Query("UPDATE quizzes SET isSynced = 1 WHERE id = :quizId")
    suspend fun markQuizAsSynced(quizId: Int)

    // ===== DEBUG =====

    /**
     * ✅ MÉTODO DE DEBUG: Lista todos os quizzes de um usuário
     * Útil para verificar se os dados estão realmente no banco
     */
    @Query("SELECT * FROM quizzes WHERE userId = :userId")
    suspend fun getQuizzesForDebug(userId: Int): List<QuizEntity>

    /**
     * ✅ MÉTODO DE DIAGNÓSTICO
     * Use este método para verificar se os índices estão funcionando
     */
    @Query("""
        SELECT q.*, COUNT(qq.id) as question_count
        FROM quizzes q
        LEFT JOIN quiz_questions qq ON q.id = qq.quizId AND qq.userId = :userId
        WHERE q.userId = :userId
        GROUP BY q.id
    """)
    suspend fun getDiagnosticQuizzes(userId: Int): List<QuizWithQuestionCount>

    /**
     * ✅ QUERY ALTERNATIVA SEM USAR O ÍNDICE COMPOSTO
     * Use se a query normal falhar
     */
    @Query("""
        SELECT * FROM quizzes 
        WHERE documentId = :documentId 
        AND userId = :userId 
        LIMIT 1
    """)
    suspend fun getQuizByDocumentIdAlternative(documentId: Int, userId: Int): QuizEntity?
}

@Dao
interface QuestionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestions(questions: List<QuestionEntity>)

    @Query("SELECT * FROM quiz_questions WHERE quizId = :quizId AND userId = :userId ORDER BY orderIndex ASC")
    suspend fun getQuestionsByQuizId(quizId: Int, userId: Int): List<QuestionEntity>

    @Query("DELETE FROM quiz_questions WHERE quizId = :quizId AND userId = :userId")
    suspend fun deleteQuestionsByQuizId(quizId: Int, userId: Int)

    @Query("SELECT * FROM quiz_questions WHERE userId = :userId AND isSynced = 0")
    suspend fun getUnsyncedQuestions(userId: Int): List<QuestionEntity>
}

@Dao
interface AnswerDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnswers(answers: List<AnswerEntity>)

    @Query("SELECT * FROM quiz_answers WHERE questionId = :questionId AND userId = :userId ORDER BY orderIndex ASC")
    suspend fun getAnswersByQuestionId(questionId: Int, userId: Int): List<AnswerEntity>

    @Query("DELETE FROM quiz_answers WHERE questionId = :questionId AND userId = :userId")
    suspend fun deleteAnswersByQuestionId(questionId: Int, userId: Int)

    @Query("SELECT * FROM quiz_answers WHERE userId = :userId AND isSynced = 0")
    suspend fun getUnsyncedAnswers(userId: Int): List<AnswerEntity>
}

@Dao
interface QuizAttemptDao {
    @Insert
    suspend fun insertAttempt(attempt: QuizAttemptEntity): Long

    @Query("SELECT * FROM quiz_attempts WHERE userId = :userId AND isSynced = 0")
    suspend fun getUnsyncedAttempts(userId: Int): List<QuizAttemptEntity>

    @Query("UPDATE quiz_attempts SET isSynced = 1 WHERE localId = :localId")
    suspend fun markAttemptAsSynced(localId: Int)

    @Query("DELETE FROM quiz_attempts WHERE localId = :localId")
    suspend fun deleteAttempt(localId: Int)

    @Query("SELECT * FROM quiz_attempts WHERE quizId = :quizId AND userId = :userId ORDER BY attemptDate DESC")
    suspend fun getAttemptsByQuizId(quizId: Int, userId: Int): List<QuizAttemptEntity>
}

@Dao
interface StudyLogDao {
    @Insert
    suspend fun insertLog(log: StudyLogEntity): Long

    @Query("SELECT * FROM study_logs WHERE userId = :userId AND isSynced = 0")
    suspend fun getUnsyncedLogs(userId: Int): List<StudyLogEntity>

    @Query("UPDATE study_logs SET isSynced = 1 WHERE localId = :localId")
    suspend fun markLogAsSynced(localId: Int)

    @Query("DELETE FROM study_logs WHERE localId = :localId")
    suspend fun deleteLog(localId: Int)

    @Query("SELECT COUNT(*) FROM study_logs WHERE userId = :userId AND isSynced = 0")
    fun getUnsyncedLogsCount(userId: Int): Flow<Int>
}
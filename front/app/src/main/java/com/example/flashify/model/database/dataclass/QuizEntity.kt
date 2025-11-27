package com.example.flashify.model.database.dataclass

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entidade Room para armazenar Quizzes localmente
 */
@Entity(
    tableName = "quizzes",
    foreignKeys = [
        ForeignKey(
            entity = DeckEntity::class,
            parentColumns = ["id"],
            childColumns = ["documentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["documentId"]),
        Index(value = ["userId"]),
        Index(value = ["documentId", "userId"])
    ]
)
data class QuizEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val documentId: Int,
    val userId: Int,
    val createdAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = true // Rastreia se foi sincronizado com o servidor
)

/**
 * Entidade Room para armazenar Questões do Quiz
 */
@Entity(
    tableName = "quiz_questions",
    foreignKeys = [
        ForeignKey(
            entity = QuizEntity::class,
            parentColumns = ["id"],
            childColumns = ["quizId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["quizId"]),
        Index(value = ["userId"])
    ]
)
data class QuestionEntity(
    @PrimaryKey val id: Int,
    val text: String,
    val quizId: Int,
    val userId: Int,
    val orderIndex: Int = 0, // Para manter ordem das questões
    val isSynced: Boolean = true
)

/**
 * Entidade Room para armazenar Respostas das Questões
 */
@Entity(
    tableName = "quiz_answers",
    foreignKeys = [
        ForeignKey(
            entity = QuestionEntity::class,
            parentColumns = ["id"],
            childColumns = ["questionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["questionId"]),
        Index(value = ["userId"])
    ]
)
data class AnswerEntity(
    @PrimaryKey val id: Int,
    val text: String,
    val isCorrect: Boolean,
    val explanation: String?,
    val questionId: Int,
    val userId: Int,
    val orderIndex: Int = 0,
    val isSynced: Boolean = true
)

/**
 * Entidade Room para rastrear tentativas de Quiz offline
 */
@Entity(
    tableName = "quiz_attempts",
    foreignKeys = [
        ForeignKey(
            entity = QuizEntity::class,
            parentColumns = ["id"],
            childColumns = ["quizId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["quizId"]),
        Index(value = ["userId"]),
        Index(value = ["isSynced"])
    ]
)
data class QuizAttemptEntity(
    @PrimaryKey(autoGenerate = true) val localId: Int = 0,
    val quizId: Int,
    val userId: Int,
    val score: Float,
    val correctAnswers: Int,
    val totalQuestions: Int,
    val attemptDate: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false // Será sincronizado quando estiver online
)

/**
 * Entidade Room para rastrear logs de estudo offline
 */
@Entity(
    tableName = "study_logs",
    foreignKeys = [
        ForeignKey(
            entity = FlashcardEntity::class,
            parentColumns = ["id"],
            childColumns = ["flashcardId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["flashcardId"]),
        Index(value = ["userId"]),
        Index(value = ["isSynced"])
    ]
)
data class StudyLogEntity(
    @PrimaryKey(autoGenerate = true) val localId: Int = 0,
    val flashcardId: Int,
    val userId: Int,
    val accuracy: Float,
    val studyDate: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)
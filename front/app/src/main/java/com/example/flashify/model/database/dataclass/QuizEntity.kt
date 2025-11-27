package com.example.flashify.model.database.dataclass

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * ✅ CORRIGIDO: Adicionado índice composto (documentId, userId)
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
        Index(value = ["documentId", "userId"], unique = false) // ✅ NÃO DEVE SER UNIQUE
    ]
)
data class QuizEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val documentId: Int,
    val userId: Int,
    val createdAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = true
)

/**
 * ✅ CORRIGIDO: Adicionado índice composto (quizId, userId)
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
        Index(value = ["userId"]),
        Index(value = ["quizId", "userId"]) // ✅ ADICIONADO
    ]
)
data class QuestionEntity(
    @PrimaryKey val id: Int,
    val text: String,
    val quizId: Int,
    val userId: Int,
    val orderIndex: Int = 0,
    val isSynced: Boolean = true
)

/**
 * ✅ CORRIGIDO: Adicionado índice composto (questionId, userId)
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
        Index(value = ["userId"]),
        Index(value = ["questionId", "userId"]) // ✅ ADICIONADO
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
    val isSynced: Boolean = false
)

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
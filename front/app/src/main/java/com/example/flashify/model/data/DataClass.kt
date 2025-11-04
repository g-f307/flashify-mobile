package com.example.flashify.model.data

import androidx.compose.ui.graphics.vector.ImageVector
import com.google.gson.annotations.SerializedName



/**
 * =====================================================================================
 * FASE 1: TRADUÇÃO DOS SCHEMAS (O "DICIONÁRIO" DE DADOS)
 *
 * Cada `data class` abaixo é a "tradução" em Kotlin de uma classe do seu ficheiro `schemas.py`.
 * Aqui é onde app Android qual o formato dos dados que ele vai enviar e receber.
 * =====================================================================================
 */

// Aqui é a para camada de rede , pois o objetivo é representar exatamente a estrutura dos dados de JSON

data class Decks(
    val title: String,
    val cardCount: Int,
    val progress: Int
)

data class Conquista(
    val icon: ImageVector,
    val title: String,
    val decription: String,
    val currentProgress: Int? = null, // Seria o progresso atual da conquista
    val targetProgress: Int? = null // Objetivos

)

data class Etapas(
    val number: Int, val title: String
)

data class NavItem(val label: String, val icon: ImageVector)


// DataClass para ApiService.kt

// ▼▼▼ ADICIONE ESTA NOVA DATA CLASS ▼▼▼
data class DeckUpdateRequest(
    val title: String
)


data class FlashcardUpdateRequest(
    val front: String?,
    val back: String?
)

data class TextDeckCreateRequest(
    val text: String,
    val title: String,
    val num_flashcards: Int,
    val difficulty: String,
    val generate_flashcards: Boolean = true,   // ✅ ADICIONADO
    val generate_quizzes: Boolean = false,     // ✅ ADICIONADO
    val content_type: String = "flashcards",   // ✅ ADICIONADO
    val num_questions: Int = 5                 // ✅ ADICIONADO (padrão 5 questões)
)

data class UserCreateRequest(
    val username: String,
    val email: String,
    val password: String
)

data class UserReadResponse(
    val id: Int,
    val username: String,
    val email: String,
    @SerializedName("is_active") val isActive: Boolean,
    @SerializedName("profile_picture_url") val profilePictureUrl: String?,
    val provider: String
)

data class UserPasswordUpdateRequest(
    @SerializedName("current_password") val currentPassword: String,
    @SerializedName("new_password") val newPassword: String
)


// Responsavel pelo TOKEN
data class TokenResponse(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("token_type") val tokenType: String
)

// --- Pastas e Documentos (Decks) ---

data class FolderRequest(
    val name: String
)

data class DocumentUpdateFolder(
    @SerializedName("folder_id") val folderId: Int?
)

data class FolderResponse(
    val id: Int,
    val name: String
)

data class DeckResponse(
    val id: Int,
    @SerializedName("file_path") val filePath: String,
    val status: String,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("total_flashcards") val totalFlashcards: Int,
    @SerializedName("studied_flashcards") val studiedFlashcards: Int,
    @SerializedName("current_step") val currentStep: String?,
    @SerializedName("has_quiz") val hasQuiz: Boolean = false,
    @SerializedName("folder_id") val folderId: Int? = null,
    @SerializedName("generates_flashcards") val generatesFlashcards: Boolean = true,
    @SerializedName("generates_quizzes") val generatesQuizzes: Boolean = false,

    // ✅ NOVOS CAMPOS PARA ESTATÍSTICAS DE QUIZ
    @SerializedName("quiz_attempts") val quizAttempts: Int? = null,
    @SerializedName("quiz_average_score") val quizAverageScore: Float? = null,
    @SerializedName("quiz_last_score") val quizLastScore: Float? = null
) {
    // **PROPRIEDADE ADICIONADA**
    // Calcula o progresso de 0.0f a 1.0f
    // Isso é necessário para a UI da TelaPrincipal
    val progress: Float
        get() = if (totalFlashcards > 0) {
            (studiedFlashcards.toFloat() / totalFlashcards).coerceIn(0f, 1f)
        } else {
            0f
        }
}


// para o Flashcards

data class FlashcardResponse(
    val id: Int,
    val front: String,
    val back: String,
    val type: String,
    @SerializedName("document_id") val documentId: Int
) {
    val question: String get() = front
    val answer: String get() = back
}



data class StudyLogRequest(
    val accuracy: Float // 0.0, 0.5, or 1.0
)

// Progresso

// --- ADICIONE ESTA NOVA DATA CLASS ---
// Representa a conquista vinda da API
data class ApiAchievement(
    val key: String,
    val title: String,
    val description: String,
    @SerializedName("icon_name") val iconName: String,
    @SerializedName("is_unlocked") val isUnlocked: Boolean,
    @SerializedName("current_progress") val currentProgress: Int,
    @SerializedName("target_progress") val targetProgress: Int
)

// --- PROGRESSSTATSRESPONSE ATUALIZADO PARA CORRESPONDER AO BACKEND ---
data class ProgressStatsResponse(
    @SerializedName("cards_studied_week") val cardsStudiedWeek: Int,
    @SerializedName("streak_days") val streakDays: Int,
    @SerializedName("flashcard_accuracy") val flashcardAccuracy: Double,
    @SerializedName("flashcard_weekly_activity") val flashcardWeeklyActivity: List<Int>,
    @SerializedName("quizzes_completed_week") val quizzesCompletedWeek: Int,
    @SerializedName("quiz_average_score") val quizAverageScore: Double
) {
    // Propriedades computadas para compatibilidade
    val generalAccuracy: Double get() = flashcardAccuracy
    val weeklyActivity: List<Int> get() = flashcardWeeklyActivity
}

// --- DTOs para Quizzes ---

data class AnswerResponse(
    val id: Int,
    val text: String,
    @SerializedName("is_correct") val isCorrect: Boolean,
    val explanation: String?,
    @SerializedName("question_id") val questionId: Int
)

data class QuestionResponse(
    val id: Int,
    val text: String,
    @SerializedName("quiz_id") val quizId: Int,
    val answers: List<AnswerResponse>
)

data class QuizResponse(
    val id: Int,
    val title: String,
    @SerializedName("document_id") val documentId: Int,
    val questions: List<QuestionResponse>
)

data class CheckAnswerRequest(
    @SerializedName("question_id") val questionId: Int,
    @SerializedName("answer_id") val answerId: Int
)

data class CheckAnswerResponse(
    @SerializedName("is_correct") val isCorrect: Boolean,
    @SerializedName("correct_answer_id") val correctAnswerId: Int,
    val explanation: String
)

data class SubmitQuizRequest(
    val score: Float,
    @SerializedName("correct_answers") val correctAnswers: Int,
    @SerializedName("total_questions") val totalQuestions: Int
)

// --- DTOs para Biblioteca com Pastas ---

data class FolderWithDocumentsResponse(
    val id: Int,
    val name: String,
    val documents: List<DeckResponse>
)

data class LibraryResponse(
    val folders: List<FolderWithDocumentsResponse>,
    @SerializedName("root_documents") val rootDocuments: List<DeckResponse>
)

// --- DTO Atualizado para DocumentDetail (com quiz) ---

data class DocumentDetailResponse(
    val id: Int,
    val status: String,
    @SerializedName("file_path") val filePath: String,
    @SerializedName("extracted_text") val extractedText: String?,
    val quiz: QuizResponse?,
    @SerializedName("generates_flashcards") val generatesFlashcards: Boolean,
    @SerializedName("generates_quizzes") val generatesQuizzes: Boolean,
    @SerializedName("total_flashcards") val totalFlashcards: Int,
    @SerializedName("has_quiz") val hasQuiz: Boolean,
    @SerializedName("current_step") val currentStep: String?
)

data class FlashcardStatsResponse(
    val known: Int,
    val learning: Int,
    val total: Int,
    @SerializedName("progress_percentage") val progressPercentage: Float
)

data class QuizStatsResponse(
    @SerializedName("last_score") val lastScore: Float?,
    @SerializedName("average_score") val averageScore: Float?,
    @SerializedName("total_attempts") val totalAttempts: Int
)

data class DeckStatsResponse(
    val flashcards: FlashcardStatsResponse,
    val quiz: QuizStatsResponse?
)
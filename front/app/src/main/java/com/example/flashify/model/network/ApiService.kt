package com.example.flashify.model.network
import com.example.flashify.model.data.DeckResponse
import com.example.flashify.model.data.DeckUpdateRequest
import com.example.flashify.model.data.FlashcardResponse
import com.example.flashify.model.data.FlashcardUpdateRequest
import com.example.flashify.model.data.GoogleIdTokenRequest
import com.example.flashify.model.data.ProgressStatsResponse
import com.example.flashify.model.data.StudyLogRequest
import com.example.flashify.model.data.TextDeckCreateRequest
import com.example.flashify.model.data.TokenResponse
import com.example.flashify.model.data.UserCreateRequest
import com.example.flashify.model.data.UserPasswordUpdateRequest
import com.example.flashify.model.data.UserReadResponse
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

private const val BASE_URL = "http://10.42.0.1:9000/"

private val okHttpClient = OkHttpClient.Builder()
    .connectTimeout(120, TimeUnit.SECONDS) // 2 minutos para conectar
    .readTimeout(120, TimeUnit.SECONDS)    // 2 minutos para esperar resposta (IA)
    .writeTimeout(120, TimeUnit.SECONDS)   // 2 minutos para enviar (Upload)
    .build()

private val retrofit = Retrofit.Builder()
    .baseUrl(BASE_URL)
    .client(okHttpClient) // ✅ Vincula o cliente configurado
    .addConverterFactory(GsonConverterFactory.create())
    .build()

interface ApiService {

    @POST("users")
    suspend fun registerUser(@Body user: UserCreateRequest): UserReadResponse

    @FormUrlEncoded
    @POST("token")
    suspend fun login(
        @Field("username") username: String,
        @Field("password") password: String
    ): TokenResponse

    @POST("google/mobile")
    suspend fun loginWithGoogleMobile(
        @Body request: GoogleIdTokenRequest
    ): TokenResponse

    @GET("users/me")
    suspend fun getCurrentUser(@Header("Authorization") token: String): UserReadResponse

    @POST("users/me/change-password")
    suspend fun changePassword(@Header("Authorization") token: String, @Body passwordUpdate: UserPasswordUpdateRequest): Response<Unit>

    @GET("documents/")
    suspend fun getDecks(@Header("Authorization") token: String): List<DeckResponse>

    @Multipart
    @POST("documents/upload")
    suspend fun uploadDocument(
        @Header("Authorization") token: String,
        @Part file: MultipartBody.Part,
        @Part("title") title: RequestBody,
        @Part("num_flashcards") numFlashcards: RequestBody,
        @Part("difficulty") difficulty: RequestBody,
        @Part("generates_flashcards") generatesFlashcards: RequestBody,
        @Part("generates_quizzes") generatesQuizzes: RequestBody,
        @Part("content_type") contentType: RequestBody,
        @Part("num_questions") numQuestions: RequestBody? = null,
        @Part("folder_id") folderId: RequestBody? = null  // ✅ NOVO PARÂMETRO
    ): DeckResponse

    @GET("documents/{id}")
    suspend fun getDocumentDetails(
        @Header("Authorization") token: String,
        @Path("id") documentId: Int
    ): DeckResponse

    @PUT("documents/{id}")
    suspend fun updateDocument(
        @Header("Authorization") token: String,
        @Path("id") documentId: Int,
        @Body request: DeckUpdateRequest
    ): DeckResponse

    @GET("documents/{id}/flashcards")
    suspend fun getFlashcardsForDocument(
        @Header("Authorization") token: String,
        @Path("id") documentId: Int
    ): List<FlashcardResponse>

    @DELETE("documents/{id}")
    suspend fun deleteDocument(
        @Header("Authorization") token: String,
        @Path("id") documentId: Int
    ): Response<Unit>

    @POST("flashcards/{id}/log_study")
    suspend fun logStudy(
        @Header("Authorization") token: String,
        @Path("id") flashcardId: Int,
        @Body studyLog: StudyLogRequest
    ): Response<Unit>

    @PUT("flashcards/{flashcard_id}")
    suspend fun updateFlashcard(
        @Header("Authorization") token: String,
        @Path("flashcard_id") flashcardId: Int,
        @Body request: FlashcardUpdateRequest
    ): FlashcardResponse

    @GET("progress/stats")
    suspend fun getProgressStats(
        @Header("Authorization") token: String,
        @Query("utc_offset_minutes") timezoneOffset: Int
    ): ProgressStatsResponse

    @GET("progress/review-flashcards")
    suspend fun getReviewFlashcards(@Header("Authorization") token: String): List<FlashcardResponse>

    @POST("documents/text")
    suspend fun createDeckFromText(
        @Header("Authorization") token: String,
        @Body request: TextDeckCreateRequest
    ): DeckResponse

    @POST("quizzes/check-answer")
    suspend fun checkQuizAnswer(
        @Header("Authorization") token: String,
        @Body request: com.example.flashify.model.data.CheckAnswerRequest
    ): com.example.flashify.model.data.CheckAnswerResponse

    @POST("quizzes/{quiz_id}/submit")
    suspend fun submitQuizAttempt(
        @Header("Authorization") token: String,
        @Path("quiz_id") quizId: Int,
        @Body request: com.example.flashify.model.data.SubmitQuizRequest
    ): Response<Unit>

    @GET("folders/library")
    suspend fun getLibrary(
        @Header("Authorization") token: String
    ): com.example.flashify.model.data.LibraryResponse

    @POST("folders/")
    suspend fun createFolder(
        @Header("Authorization") token: String,
        @Body request: com.example.flashify.model.data.FolderRequest
    ): com.example.flashify.model.data.FolderResponse

    @PUT("folders/{folder_id}")
    suspend fun updateFolder(
        @Header("Authorization") token: String,
        @Path("folder_id") folderId: Int,
        @Body request: com.example.flashify.model.data.FolderRequest
    ): com.example.flashify.model.data.FolderResponse

    @DELETE("folders/{folder_id}")
    suspend fun deleteFolder(
        @Header("Authorization") token: String,
        @Path("folder_id") folderId: Int,
        @Query("delete_decks") deleteDecks: Boolean = false
    ): Response<Unit>

    @PATCH("documents/{document_id}/move")
    suspend fun moveDocumentToFolder(
        @Header("Authorization") token: String,
        @Path("document_id") documentId: Int,
        @Body request: com.example.flashify.model.data.DocumentUpdateFolder
    ): DeckResponse

    @GET("documents/{id}")
    suspend fun getDocumentDetailWithQuiz(
        @Header("Authorization") token: String,
        @Path("id") documentId: Int
    ): com.example.flashify.model.data.DocumentDetailResponse

    // ✅ NOVO ENDPOINT ADICIONADO
    @GET("stats/document/{document_id}")
    suspend fun getDocumentStats(
        @Header("Authorization") token: String,
        @Path("document_id") documentId: Int
    ): com.example.flashify.model.data.DeckStatsResponse

    @POST("documents/{id}/generate-flashcards")
    suspend fun generateFlashcardsForDocument(
        @Header("Authorization") token: String,
        @Path("id") documentId: Int
    ): Response<Unit>

    @POST("documents/{id}/generate-quiz")
    suspend fun generateQuizForDocument(
        @Header("Authorization") token: String,
        @Path("id") documentId: Int
    ): Response<Unit>

    @GET("documents/generation-limit")
    suspend fun getGenerationLimit(
        @Header("Authorization") token: String
    ): com.example.flashify.model.data.GenerationLimitResponse

    @POST("documents/{document_id}/add-flashcards")
    suspend fun addMoreFlashcards(
        @Header("Authorization") token: String,
        @Path("document_id") documentId: Int,
        @Body request: com.example.flashify.model.data.AddFlashcardsRequest
    ): List<FlashcardResponse>

    @POST("documents/{document_id}/add-questions")
    suspend fun addMoreQuestions(
        @Header("Authorization") token: String,
        @Path("document_id") documentId: Int,
        @Body request: com.example.flashify.model.data.AddQuestionsRequest
    ): com.example.flashify.model.data.QuizResponse
}

object Api {
    val retrofitService: ApiService by lazy { retrofit.create(ApiService::class.java) }
}
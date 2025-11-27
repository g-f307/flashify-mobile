package com.example.flashify.di

import android.content.Context
import com.example.flashify.model.database.dao.*
import com.example.flashify.model.manager.GoogleAuthManager
import com.example.flashify.model.manager.SyncManager
import com.example.flashify.model.manager.ThemeManager
import com.example.flashify.model.manager.TokenManager
import com.example.flashify.model.network.ApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideTokenManager(@ApplicationContext context: Context): TokenManager {
        return TokenManager(context)
    }

    @Provides
    @Singleton
    fun provideThemeManager(@ApplicationContext context: Context): ThemeManager {
        return ThemeManager(context)
    }

    @Provides
    fun provideGoogleAuthManager(@ApplicationContext context: Context): GoogleAuthManager {
        return GoogleAuthManager(context)
    }

    // ===== NOVO: SYNCMANAGER =====

    @Provides
    @Singleton
    fun provideSyncManager(
        @ApplicationContext context: Context,
        tokenManager: TokenManager,
        apiService: ApiService,
        deckDao: DeckDao,
        flashcardDao: FlashcardDao,
        studyLogDao: StudyLogDao,
        quizDao: QuizDao,
        quizAttemptDao: QuizAttemptDao
    ): SyncManager {
        return SyncManager(
            context = context,
            tokenManager = tokenManager,
            apiService = apiService,
            deckDao = deckDao,
            flashcardDao = flashcardDao,
            studyLogDao = studyLogDao,
            quizDao = quizDao,
            quizAttemptDao = quizAttemptDao
        )
    }
}
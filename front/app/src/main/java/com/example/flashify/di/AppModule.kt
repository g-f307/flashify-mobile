package com.example.flashify.di

import android.content.Context
import android.content.ContentResolver
import com.example.flashify.model.database.dao.*
import com.example.flashify.model.manager.*
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

    // ===== TOKEN MANAGER =====
    @Provides
    @Singleton
    fun provideTokenManager(@ApplicationContext context: Context): TokenManager {
        return TokenManager(context)
    }

    // ===== THEME MANAGER =====
    @Provides
    @Singleton
    fun provideThemeManager(@ApplicationContext context: Context): ThemeManager {
        return ThemeManager(context)
    }

    // ===== OFFLINE PREFERENCE MANAGER =====
    @Provides
    @Singleton
    fun provideOfflinePreferenceManager(@ApplicationContext context: Context): OfflinePreferenceManager {
        return OfflinePreferenceManager(context)
    }

    // ===== LOCAL USER MANAGER =====
    @Provides
    @Singleton
    fun provideLocalUserManager(userDao: UserDao): LocalUserManager {
        return LocalUserManager(userDao)
    }

    // ===== GOOGLE AUTH MANAGER =====
    @Provides
    fun provideGoogleAuthManager(@ApplicationContext context: Context): GoogleAuthManager {
        return GoogleAuthManager(context)
    }

    // ===== CONTENT RESOLVER ✅ ÚNICO LUGAR ONDE É FORNECIDO =====
    @Provides
    @Singleton
    fun provideContentResolver(@ApplicationContext context: Context): ContentResolver {
        return context.contentResolver
    }

    // ===== SYNC MANAGER =====
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
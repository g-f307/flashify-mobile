package com.example.flashify.di

import android.content.Context
import androidx.room.Room
import com.example.flashify.model.database.AppDatabase
import com.example.flashify.model.database.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "flashify_database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    // ===== USER DAO ✅ NOVO =====
    @Provides
    @Singleton
    fun provideUserDao(database: AppDatabase): UserDao {
        return database.userDao()
    }

    // ===== DECK DAO =====
    @Provides
    @Singleton
    fun provideDeckDao(database: AppDatabase): DeckDao {
        return database.deckDao()
    }

    // ===== FLASHCARD DAO =====
    @Provides
    @Singleton
    fun provideFlashcardDao(database: AppDatabase): FlashcardDao {
        return database.flashcardDao()
    }

    // ===== QUIZ DAO =====
    @Provides
    @Singleton
    fun provideQuizDao(database: AppDatabase): QuizDao {
        return database.quizDao()
    }

    // ===== QUESTION DAO =====
    @Provides
    @Singleton
    fun provideQuestionDao(database: AppDatabase): QuestionDao {
        return database.questionDao()
    }

    // ===== ANSWER DAO =====
    @Provides
    @Singleton
    fun provideAnswerDao(database: AppDatabase): AnswerDao {
        return database.answerDao()
    }

    // ===== QUIZ ATTEMPT DAO =====
    @Provides
    @Singleton
    fun provideQuizAttemptDao(database: AppDatabase): QuizAttemptDao {
        return database.quizAttemptDao()
    }

    // ===== STUDY LOG DAO =====
    @Provides
    @Singleton
    fun provideStudyLogDao(database: AppDatabase): StudyLogDao {
        return database.studyLogDao()
    }
}
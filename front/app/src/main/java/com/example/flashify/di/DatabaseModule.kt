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

    // ===== DAOs EXISTENTES =====

    @Provides
    @Singleton
    fun provideDeckDao(database: AppDatabase): DeckDao {
        return database.deckDao()
    }

    @Provides
    @Singleton
    fun provideFlashcardDao(database: AppDatabase): FlashcardDao {
        return database.flashcardDao()
    }

    // ===== NOVOS DAOs PARA QUIZ =====

    @Provides
    @Singleton
    fun provideQuizDao(database: AppDatabase): QuizDao {
        return database.quizDao()
    }

    @Provides
    @Singleton
    fun provideQuestionDao(database: AppDatabase): QuestionDao {
        return database.questionDao()
    }

    @Provides
    @Singleton
    fun provideAnswerDao(database: AppDatabase): AnswerDao {
        return database.answerDao()
    }

    @Provides
    @Singleton
    fun provideQuizAttemptDao(database: AppDatabase): QuizAttemptDao {
        return database.quizAttemptDao()
    }

    @Provides
    @Singleton
    fun provideStudyLogDao(database: AppDatabase): StudyLogDao {
        return database.studyLogDao()
    }
}
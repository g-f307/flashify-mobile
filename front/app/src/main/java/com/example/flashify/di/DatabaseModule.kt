package com.example.flashify.di

import android.content.Context
import androidx.room.Room
import com.example.flashify.model.database.AppDatabase
import com.example.flashify.model.database.dao.DeckDao
import com.example.flashify.model.database.dao.FlashcardDao
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
}
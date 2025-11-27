package com.example.flashify.model.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.flashify.model.database.dao.*
import com.example.flashify.model.database.dataclass.*

/**
 * Database principal da aplicação com suporte completo a Offline-First
 *
 * Versão 4: Adicionada tabela de usuários para cache de dados do perfil
 */
@Database(
    entities = [
        UserEntity::class,              // ✅ NOVO na v4
        DeckEntity::class,
        FlashcardEntity::class,
        QuizEntity::class,
        QuestionEntity::class,
        AnswerEntity::class,
        QuizAttemptEntity::class,
        StudyLogEntity::class
    ],
    version = 4,                        // ⬆️ ATUALIZADO
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    // DAOs existentes
    abstract fun userDao(): UserDao                     // ✅ NOVO
    abstract fun deckDao(): DeckDao
    abstract fun flashcardDao(): FlashcardDao
    abstract fun quizDao(): QuizDao
    abstract fun questionDao(): QuestionDao
    abstract fun answerDao(): AnswerDao
    abstract fun quizAttemptDao(): QuizAttemptDao
    abstract fun studyLogDao(): StudyLogDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "flashify_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        fun clearInstance() {
            INSTANCE?.close()
            INSTANCE = null
        }
    }
}
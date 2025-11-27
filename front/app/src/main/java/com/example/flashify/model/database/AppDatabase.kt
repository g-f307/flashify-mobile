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
 * Versão 3: Adicionadas tabelas de Quiz, tentativas e logs de sincronização
 */
@Database(
    entities = [
        DeckEntity::class,
        FlashcardEntity::class,
        QuizEntity::class,
        QuestionEntity::class,
        AnswerEntity::class,
        QuizAttemptEntity::class,
        StudyLogEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    // DAOs existentes
    abstract fun deckDao(): DeckDao
    abstract fun flashcardDao(): FlashcardDao

    // Novos DAOs para Quiz
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
                    // Estratégia de migração destrutiva (para desenvolvimento)
                    // Em produção, use Migrations adequadas
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        /**
         * Limpa a instância do database (útil para testes)
         */
        fun clearInstance() {
            INSTANCE?.close()
            INSTANCE = null
        }
    }
}
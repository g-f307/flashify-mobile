package com.example.flashify.model.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.flashify.model.database.dao.DeckDao
import com.example.flashify.model.database.dao.FlashcardDao
import com.example.flashify.model.database.dataclass.DeckEntity
import com.example.flashify.model.database.dataclass.FlashcardEntity

// 1. Incremente a versão (ex: de 1 para 2)
@Database(entities = [DeckEntity::class, FlashcardEntity::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun deckDao(): DeckDao
    abstract fun flashcardDao(): FlashcardDao

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
                    // 2. Adicione esta linha para lidar com a mudança (apaga dados antigos!)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
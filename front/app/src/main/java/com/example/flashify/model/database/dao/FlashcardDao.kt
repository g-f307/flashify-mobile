package com.example.flashify.model.database.dao

// Em FlashcardDao.kt

import androidx.room.*
import com.example.flashify.model.database.dataclass.FlashcardEntity

@Dao
interface FlashcardDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFlashcards(flashcards: List<FlashcardEntity>)

    // Modificado para aceitar userId (e usa o índice composto)
    @Query("SELECT * FROM flashcards WHERE deckId = :deckId AND userId = :userId")
    suspend fun getFlashcardsForDeckForUser(deckId: Int, userId: Int): List<FlashcardEntity>

    // Modificado para aceitar userId
    @Query("DELETE FROM flashcards WHERE deckId = :deckId AND userId = :userId")
    suspend fun deleteFlashcardsForDeckForUser(deckId: Int, userId: Int)

    // (Opcional) Função para apagar todos os flashcards de um utilizador
    @Query("DELETE FROM flashcards WHERE userId = :userId")
    suspend fun deleteAllFlashcardsForUser(userId: Int)

    @Update
    suspend fun updateFlashcard(flashcard: FlashcardEntity)
}
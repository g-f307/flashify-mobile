package com.example.flashify.model.database.dao

import androidx.room.*
import com.example.flashify.model.database.dataclass.DeckEntity

@Dao
interface DeckDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDecks(decks: List<DeckEntity>)

    @Update
    suspend fun updateDeck(deck: DeckEntity)

    // Modificado para aceitar userId
    @Query("SELECT * FROM decks WHERE userId = :userId ORDER BY createdAt DESC")
    suspend fun getAllDecksForUser(userId: Int): List<DeckEntity>

    // Modificado para aceitar userId
    @Query("SELECT * FROM decks WHERE id = :deckId AND userId = :userId")
    suspend fun getDeckByIdForUser(deckId: Int, userId: Int): DeckEntity?

    // Modificado para aceitar userId
    @Query("DELETE FROM decks WHERE userId = :userId")
    suspend fun deleteAllDecksForUser(userId: Int)

    // (Opcional) Função para apagar um deck específico do utilizador
    @Query("DELETE FROM decks WHERE id = :deckId AND userId = :userId")
    suspend fun deleteDeckByIdForUser(deckId: Int, userId: Int)
}
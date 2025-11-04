package com.example.flashify.model.database.dataclass

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "flashcards",
    foreignKeys = [ForeignKey(
        entity = DeckEntity::class,
        parentColumns = ["id"],
        childColumns = ["deckId"],
        onDelete = ForeignKey.CASCADE
    )],
    // Adiciona userId aqui também para poder buscar flashcards diretamente pelo utilizador
    // E um índice composto para buscar flashcards de um deck específico de um utilizador
    indices = [Index(value = ["deckId"]), Index(value = ["userId"]), Index(value = ["deckId", "userId"])]
)
data class FlashcardEntity(
    @PrimaryKey val id: Int,
    val front: String,
    val back: String,
    val type: String,
    val deckId: Int,
    val userId: Int // Novo campo para associar ao utilizador
)

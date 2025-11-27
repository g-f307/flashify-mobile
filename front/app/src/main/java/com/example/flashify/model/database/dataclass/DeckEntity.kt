package com.example.flashify.model.database.dataclass

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "decks",
    indices = [Index(value=["userId"])]
)
data class DeckEntity(
    @PrimaryKey val id: Int,
    val filePath: String,
    val status: String,
    val createdAt: String,
    val totalFlashcards: Int,
    var studiedFlashcards: Int,
    val userId: Int,
    val hasQuiz: Boolean = false // ✅ ADICIONADO: Agora o banco sabe se tem quiz!
)
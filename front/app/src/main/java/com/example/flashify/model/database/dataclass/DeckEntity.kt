package com.example.flashify.model.database.dataclass

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

// Entidade para representar um Deck na base de dados local
@Entity(
    tableName = "decks",
    //Adicionando o índice na coluna de userID para buscar rápido
    indices = [Index(value=["userId"])]
)
data class DeckEntity(
    @PrimaryKey val id: Int, // Usa o mesmo ID da API como chave primária
    val filePath: String,    // Equivalente ao 'title' ou caminho do arquivo
    val status: String,
    val createdAt: String,   // Pode ser guardado como String ou convertido para Long/Date
    val totalFlashcards: Int,
    var studiedFlashcards: Int, // 'var' para poder atualizar o progresso localmente
    val userId: Int // Novo campo para associar ao utilizador
)
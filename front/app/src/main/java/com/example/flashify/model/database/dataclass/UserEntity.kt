package com.example.flashify.model.database.dataclass

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: Int,
    val username: String,
    val email: String,
    val isActive: Boolean,
    val profilePictureUrl: String?,
    val provider: String,
    val lastUpdated: Long = System.currentTimeMillis()
)
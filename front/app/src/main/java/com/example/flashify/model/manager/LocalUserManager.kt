package com.example.flashify.model.manager

import android.util.Log
import com.example.flashify.model.data.UserReadResponse
import com.example.flashify.model.database.dao.UserDao
import com.example.flashify.model.database.dataclass.UserEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalUserManager @Inject constructor(
    private val userDao: UserDao
) {
    companion object {
        private const val TAG = "LocalUserManager"
    }

    /**
     * Salva o usuário logado localmente
     */
    suspend fun saveUser(user: UserReadResponse) {
        try {
            val userEntity = UserEntity(
                id = user.id,
                username = user.username,
                email = user.email,
                isActive = user.isActive,
                profilePictureUrl = user.profilePictureUrl,
                provider = user.provider,
                lastUpdated = System.currentTimeMillis()
            )
            userDao.insertUser(userEntity)
            Log.d(TAG, "✅ Usuário salvo localmente: ${user.username}")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Erro ao salvar usuário: ${e.message}")
        }
    }

    /**
     * Obtém o usuário logado localmente
     */
    suspend fun getLocalUser(): UserEntity? {
        return try {
            val user = userDao.getCurrentUser()
            if (user != null) {
                Log.d(TAG, "📦 Usuário carregado do cache: ${user.username}")
            }
            user
        } catch (e: Exception) {
            Log.e(TAG, "❌ Erro ao obter usuário: ${e.message}")
            null
        }
    }

    /**
     * Verifica se há um usuário logado localmente
     */
    suspend fun isUserLoggedIn(): Boolean {
        return getLocalUser() != null
    }

    /**
     * Limpa dados do usuário (logout)
     */
    suspend fun clearLocalUser() {
        try {
            userDao.clearAllUsers()
            Log.d(TAG, "✅ Dados do usuário limpos (logout)")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Erro ao limpar usuário: ${e.message}")
        }
    }
}
package com.example.flashify.model.manager

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.profileDataStore by preferencesDataStore(name = "profile_settings")

/**
 * Gerenciador de foto de perfil do usuário
 */
class ProfileImageManager(private val context: Context) {

    private val PROFILE_IMAGE_KEY = stringPreferencesKey("profile_image_url")

    /**
     * Salva a URL da foto de perfil
     */
    suspend fun saveProfileImageUrl(url: String?) {
        context.profileDataStore.edit { preferences ->
            if (url != null) {
                preferences[PROFILE_IMAGE_KEY] = url
            } else {
                preferences.remove(PROFILE_IMAGE_KEY)
            }
        }
    }

    /**
     * Obtém a URL da foto de perfil como Flow
     */
    val profileImageUrl: Flow<String?> = context.profileDataStore.data
        .map { preferences ->
            preferences[PROFILE_IMAGE_KEY]
        }

    /**
     * Obtém a URL da foto de perfil de forma síncrona
     */
    suspend fun getProfileImageUrl(): String? {
        var url: String? = null
        context.profileDataStore.data.collect { preferences ->
            url = preferences[PROFILE_IMAGE_KEY]
        }
        return url
    }

    /**
     * Remove a foto de perfil salva
     */
    suspend fun clearProfileImage() {
        context.profileDataStore.edit { preferences ->
            preferences.remove(PROFILE_IMAGE_KEY)
        }
    }
}
package com.example.flashify.model.manager

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private val Context.offlinePrefsDataStore by preferencesDataStore(name = "offline_prefs")

class OfflinePreferenceManager @Inject constructor(private val context: Context) {

    private val LAST_FULL_SYNC_KEY = longPreferencesKey("last_full_sync")
    private val HAS_CACHED_DATA_KEY = booleanPreferencesKey("has_cached_data")

    val lastFullSyncTime: Flow<Long> = context.offlinePrefsDataStore.data
        .map { preferences ->
            preferences[LAST_FULL_SYNC_KEY] ?: 0L
        }

    val hasCachedData: Flow<Boolean> = context.offlinePrefsDataStore.data
        .map { preferences ->
            preferences[HAS_CACHED_DATA_KEY] ?: false
        }

    suspend fun setLastFullSync(timestamp: Long) {
        context.offlinePrefsDataStore.edit { preferences ->
            preferences[LAST_FULL_SYNC_KEY] = timestamp
        }
    }

    suspend fun setCachedDataFlag(hasData: Boolean) {
        context.offlinePrefsDataStore.edit { preferences ->
            preferences[HAS_CACHED_DATA_KEY] = hasData
        }
    }
}
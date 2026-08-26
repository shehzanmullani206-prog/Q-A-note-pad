package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.UUID

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferencesRepository(private val context: Context) {

    private object Keys {
        val USER_ID = stringPreferencesKey("user_id")
        val USER_NAME = stringPreferencesKey("user_name")
        val LAST_NOTE_ID = stringPreferencesKey("last_note_id")
        val LAST_SHARE_CODE = stringPreferencesKey("last_share_code")
    }

    val userNameFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[Keys.USER_NAME] ?: ""
    }

    val userIdFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[Keys.USER_ID] ?: ""
    }

    val lastNoteIdFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[Keys.LAST_NOTE_ID] ?: ""
    }

    suspend fun getOrCreateUserId(): String {
        val currentPrefs = context.dataStore.data.first()
        val existingId = currentPrefs[Keys.USER_ID]
        if (!existingId.isNullOrEmpty()) {
            return existingId
        }
        val newId = "usr_" + UUID.randomUUID().toString().replace("-", "").take(10)
        context.dataStore.edit { preferences ->
            preferences[Keys.USER_ID] = newId
        }
        return newId
    }

    suspend fun saveUserName(name: String) {
        context.dataStore.edit { preferences ->
            preferences[Keys.USER_NAME] = name.trim()
        }
    }

    suspend fun saveLastNote(noteId: String, shareCode: String) {
        context.dataStore.edit { preferences ->
            preferences[Keys.LAST_NOTE_ID] = noteId
            preferences[Keys.LAST_SHARE_CODE] = shareCode
        }
    }

    suspend fun clearLastNote() {
        context.dataStore.edit { preferences ->
            preferences.remove(Keys.LAST_NOTE_ID)
            preferences.remove(Keys.LAST_SHARE_CODE)
        }
    }
}

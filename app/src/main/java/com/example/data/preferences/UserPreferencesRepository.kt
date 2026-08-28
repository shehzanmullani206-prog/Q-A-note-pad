package com.example.data.preferences

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException
import java.util.UUID

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferencesRepository(private val context: Context) {

    private object Keys {
        val USER_ID = stringPreferencesKey("user_id")
        val USER_NAME = stringPreferencesKey("user_name")
        val LAST_NOTE_ID = stringPreferencesKey("last_note_id")
        val LAST_SHARE_CODE = stringPreferencesKey("last_share_code")
        val LAST_ACTIVE_QUESTION_ID = stringPreferencesKey("last_active_question_id")
    }

    private val safeData: Flow<Preferences> = context.dataStore.data.catch { exception ->
        if (exception is IOException) {
            Log.w("UserPrefsRepo", "DataStore IOException: ${exception.message}", exception)
            emit(emptyPreferences())
        } else {
            Log.e("UserPrefsRepo", "DataStore exception: ${exception.message}", exception)
            emit(emptyPreferences())
        }
    }

    val userNameFlow: Flow<String> = safeData.map { preferences ->
        preferences[Keys.USER_NAME] ?: ""
    }

    val userIdFlow: Flow<String> = safeData.map { preferences ->
        preferences[Keys.USER_ID] ?: ""
    }

    val lastNoteIdFlow: Flow<String> = safeData.map { preferences ->
        preferences[Keys.LAST_NOTE_ID] ?: ""
    }

    val lastShareCodeFlow: Flow<String> = safeData.map { preferences ->
        preferences[Keys.LAST_SHARE_CODE] ?: ""
    }

    val lastActiveQuestionIdFlow: Flow<String> = safeData.map { preferences ->
        preferences[Keys.LAST_ACTIVE_QUESTION_ID] ?: ""
    }

    suspend fun getLastNoteInfo(): Pair<String, String> {
        return try {
            val current = safeData.first()
            Pair(current[Keys.LAST_NOTE_ID] ?: "", current[Keys.LAST_SHARE_CODE] ?: "")
        } catch (_: Exception) {
            Pair("", "")
        }
    }

    suspend fun getLastActiveQuestionId(): String {
        return try {
            safeData.first()[Keys.LAST_ACTIVE_QUESTION_ID] ?: ""
        } catch (_: Exception) {
            ""
        }
    }

    suspend fun saveLastActiveQuestion(questionId: String) {
        try {
            context.dataStore.edit { preferences ->
                if (questionId.isNotBlank()) {
                    preferences[Keys.LAST_ACTIVE_QUESTION_ID] = questionId
                } else {
                    preferences.remove(Keys.LAST_ACTIVE_QUESTION_ID)
                }
            }
        } catch (e: Throwable) {
            Log.w("UserPrefsRepo", "Failed saving active question: ${e.message}")
        }
    }

    suspend fun getOrCreateUserId(): String {
        return try {
            val currentPrefs = safeData.first()
            val existingId = currentPrefs[Keys.USER_ID]
            if (!existingId.isNullOrEmpty()) {
                return existingId
            }
            val newId = "usr_" + UUID.randomUUID().toString().replace("-", "").take(10)
            try {
                context.dataStore.edit { preferences ->
                    preferences[Keys.USER_ID] = newId
                }
            } catch (e: Throwable) {
                Log.w("UserPrefsRepo", "Failed saving userId to DataStore: ${e.message}")
            }
            newId
        } catch (e: Throwable) {
            "usr_" + UUID.randomUUID().toString().replace("-", "").take(10)
        }
    }

    suspend fun saveUserName(name: String) {
        try {
            context.dataStore.edit { preferences ->
                preferences[Keys.USER_NAME] = name.trim()
            }
        } catch (e: Throwable) {
            Log.w("UserPrefsRepo", "Failed saving userName: ${e.message}")
        }
    }

    suspend fun saveLastNote(noteId: String, shareCode: String) {
        try {
            context.dataStore.edit { preferences ->
                preferences[Keys.LAST_NOTE_ID] = noteId
                preferences[Keys.LAST_SHARE_CODE] = shareCode
            }
        } catch (e: Throwable) {
            Log.w("UserPrefsRepo", "Failed saving last note: ${e.message}")
        }
    }

    suspend fun clearLastNote() {
        try {
            context.dataStore.edit { preferences ->
                preferences.remove(Keys.LAST_NOTE_ID)
                preferences.remove(Keys.LAST_SHARE_CODE)
            }
        } catch (e: Throwable) {
            Log.w("UserPrefsRepo", "Failed clearing last note: ${e.message}")
        }
    }
}

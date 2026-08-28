package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.SharedNote
import com.example.data.model.UserInfo
import org.json.JSONObject

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey
    val noteId: String,
    val shareCode: String,
    val title: String,
    val createdAt: Long,
    val updatedAt: Long,
    val lastAccessedAt: Long,
    val usersJson: String
) {
    fun toSharedNote(): SharedNote {
        val usersMap = mutableMapOf<String, UserInfo>()
        try {
            if (usersJson.isNotBlank()) {
                val json = JSONObject(usersJson)
                val keys = json.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    val userObj = json.getJSONObject(key)
                    usersMap[key] = UserInfo(
                        userId = userObj.optString("userId", key),
                        name = userObj.optString("name", ""),
                        joinedAt = userObj.optLong("joinedAt", createdAt),
                        lastSeen = userObj.optLong("lastSeen", updatedAt),
                        isOnline = userObj.optBoolean("isOnline", true)
                    )
                }
            }
        } catch (_: Exception) {}

        return SharedNote(
            noteId = noteId,
            shareCode = shareCode,
            title = title,
            createdAt = createdAt,
            updatedAt = updatedAt,
            users = usersMap
        )
    }

    companion object {
        fun fromSharedNote(note: SharedNote, lastAccessedAt: Long = System.currentTimeMillis()): NoteEntity {
            val json = JSONObject()
            note.users.forEach { (key, userInfo) ->
                val userObj = JSONObject()
                userObj.put("userId", userInfo.userId)
                userObj.put("name", userInfo.name)
                userObj.put("joinedAt", userInfo.joinedAt)
                userObj.put("lastSeen", userInfo.lastSeen)
                userObj.put("isOnline", userInfo.isOnline)
                json.put(key, userObj)
            }

            return NoteEntity(
                noteId = note.noteId,
                shareCode = note.shareCode,
                title = note.title,
                createdAt = note.createdAt,
                updatedAt = note.updatedAt,
                lastAccessedAt = lastAccessedAt,
                usersJson = json.toString()
            )
        }
    }
}

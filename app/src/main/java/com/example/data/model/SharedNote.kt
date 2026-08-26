package com.example.data.model

import com.google.firebase.firestore.DocumentSnapshot

data class SharedNote(
    val noteId: String = "",
    val shareCode: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val users: Map<String, UserInfo> = emptyMap(),
    val title: String = "Collaborative Q&A"
) {
    val activeUserCount: Int
        get() = users.size

    companion object {
        fun fromDocument(doc: DocumentSnapshot): SharedNote {
            val noteId = doc.id
            val shareCode = doc.getString("shareCode") ?: ""
            val createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
            val updatedAt = doc.getLong("updatedAt") ?: System.currentTimeMillis()
            val title = doc.getString("title") ?: "Collaborative Q&A"

            val usersMapRaw = doc.get("users") as? Map<String, Map<String, Any?>>
            val users = mutableMapOf<String, UserInfo>()
            usersMapRaw?.forEach { (key, valueMap) ->
                users[key] = UserInfo.fromMap(valueMap)
            }

            return SharedNote(
                noteId = noteId,
                shareCode = shareCode,
                createdAt = createdAt,
                updatedAt = updatedAt,
                users = users,
                title = title
            )
        }
    }
}

package com.example.data.model

import com.google.firebase.Timestamp

data class UserInfo(
    val userId: String = "",
    val name: String = "",
    val joinedAt: Long = System.currentTimeMillis(),
    val lastSeen: Long = System.currentTimeMillis(),
    val isOnline: Boolean = true
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "userId" to userId,
            "name" to name,
            "joinedAt" to joinedAt,
            "lastSeen" to lastSeen,
            "isOnline" to isOnline
        )
    }

    companion object {
        fun fromMap(map: Map<String, Any?>?): UserInfo {
            if (map == null) return UserInfo()
            return UserInfo(
                userId = (map["userId"] as? String) ?: "",
                name = (map["name"] as? String) ?: "",
                joinedAt = (map["joinedAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                lastSeen = (map["lastSeen"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                isOnline = (map["isOnline"] as? Boolean) ?: true
            )
        }
    }
}

package com.example.data.model

import com.google.firebase.firestore.DocumentSnapshot

data class QuestionItem(
    val questionId: String = "",
    val questionText: String = "",
    val answerContent: String = "",
    val formatting: TextFormatting = TextFormatting(),
    val createdBy: String = "",
    val createdByName: String = "",
    val updatedBy: String = "",
    val updatedByName: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val hasAnswer: Boolean
        get() = answerContent.trim().isNotEmpty()

    companion object {
        fun fromDocument(doc: DocumentSnapshot): QuestionItem {
            val formattingMap = doc.get("formatting") as? Map<String, Any?>
            return QuestionItem(
                questionId = doc.id,
                questionText = doc.getString("questionText") ?: "",
                answerContent = doc.getString("answerContent") ?: "",
                formatting = TextFormatting.fromMap(formattingMap),
                createdBy = doc.getString("createdBy") ?: "",
                createdByName = doc.getString("createdByName") ?: "",
                updatedBy = doc.getString("updatedBy") ?: "",
                updatedByName = doc.getString("updatedByName") ?: "",
                createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis(),
                updatedAt = doc.getLong("updatedAt") ?: System.currentTimeMillis()
            )
        }
    }
}

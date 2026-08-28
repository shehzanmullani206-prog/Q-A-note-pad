package com.example.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.data.model.FormatSpan
import com.example.data.model.QuestionItem
import com.example.data.model.TextFormatting
import org.json.JSONArray
import org.json.JSONObject

@Entity(
    tableName = "questions",
    indices = [Index(value = ["noteId"]), Index(value = ["createdAt"])]
)
data class QuestionEntity(
    @PrimaryKey
    val questionId: String,
    val noteId: String,
    val questionText: String,
    val answerContent: String,
    val formattingJson: String,
    val createdBy: String,
    val createdByName: String,
    val updatedBy: String,
    val updatedByName: String,
    val createdAt: Long,
    val updatedAt: Long
) {
    fun toQuestionItem(): QuestionItem {
        val formatting = deserializeFormatting(formattingJson)
        return QuestionItem(
            questionId = questionId,
            questionText = questionText,
            answerContent = answerContent,
            formatting = formatting,
            createdBy = createdBy,
            createdByName = createdByName,
            updatedBy = updatedBy,
            updatedByName = updatedByName,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    companion object {
        fun fromQuestionItem(noteId: String, item: QuestionItem): QuestionEntity {
            return QuestionEntity(
                questionId = item.questionId,
                noteId = noteId,
                questionText = item.questionText,
                answerContent = item.answerContent,
                formattingJson = serializeFormatting(item.formatting),
                createdBy = item.createdBy,
                createdByName = item.createdByName,
                updatedBy = item.updatedBy,
                updatedByName = item.updatedByName,
                createdAt = item.createdAt,
                updatedAt = item.updatedAt
            )
        }

        private fun serializeFormatting(fmt: TextFormatting): String {
            return try {
                val json = JSONObject()
                json.put("colorHex", fmt.colorHex)
                json.put("fontSizeSp", fmt.fontSizeSp)
                json.put("isColorActive", fmt.isColorActive)
                json.put("isSizeActive", fmt.isSizeActive)
                json.put("isBoldActive", fmt.isBoldActive)
                json.put("isItalicActive", fmt.isItalicActive)
                json.put("isUnderlineActive", fmt.isUnderlineActive)

                val spansArray = JSONArray()
                fmt.spans.forEach { span ->
                    val spanObj = JSONObject()
                    spanObj.put("start", span.start)
                    spanObj.put("end", span.end)
                    if (span.colorHex != null) spanObj.put("colorHex", span.colorHex)
                    if (span.fontSizeSp != null) spanObj.put("fontSizeSp", span.fontSizeSp)
                    if (span.isBold != null) spanObj.put("isBold", span.isBold)
                    if (span.isItalic != null) spanObj.put("isItalic", span.isItalic)
                    if (span.isUnderline != null) spanObj.put("isUnderline", span.isUnderline)
                    spansArray.put(spanObj)
                }
                json.put("spans", spansArray)
                json.toString()
            } catch (_: Exception) {
                ""
            }
        }

        private fun deserializeFormatting(jsonString: String): TextFormatting {
            if (jsonString.isBlank()) return TextFormatting()
            return try {
                val json = JSONObject(jsonString)
                val color = json.optString("colorHex", "#58A6FF")
                val size = json.optInt("fontSizeSp", 20)
                val isColor = json.optBoolean("isColorActive", false)
                val isSize = json.optBoolean("isSizeActive", false)
                val isBold = json.optBoolean("isBoldActive", false)
                val isItalic = json.optBoolean("isItalicActive", false)
                val isUnderline = json.optBoolean("isUnderlineActive", false)

                val spans = mutableListOf<FormatSpan>()
                val spansArray = json.optJSONArray("spans")
                if (spansArray != null) {
                    for (i in 0 until spansArray.length()) {
                        val sObj = spansArray.getJSONObject(i)
                        val start = sObj.optInt("start", 0)
                        val end = sObj.optInt("end", 0)
                        if (start < end) {
                            val cHex = if (sObj.has("colorHex")) sObj.getString("colorHex") else null
                            val fSize = if (sObj.has("fontSizeSp")) sObj.getInt("fontSizeSp") else null
                            val b = if (sObj.has("isBold")) sObj.getBoolean("isBold") else null
                            val itl = if (sObj.has("isItalic")) sObj.getBoolean("isItalic") else null
                            val u = if (sObj.has("isUnderline")) sObj.getBoolean("isUnderline") else null
                            spans.add(
                                FormatSpan(
                                    start = start,
                                    end = end,
                                    colorHex = cHex,
                                    fontSizeSp = fSize,
                                    isBold = b,
                                    isItalic = itl,
                                    isUnderline = u
                                )
                            )
                        }
                    }
                }

                TextFormatting(
                    colorHex = color,
                    fontSizeSp = size,
                    isColorActive = isColor,
                    isSizeActive = isSize,
                    isBoldActive = isBold,
                    isItalicActive = isItalic,
                    isUnderlineActive = isUnderline,
                    spans = spans
                )
            } catch (_: Exception) {
                TextFormatting()
            }
        }
    }
}

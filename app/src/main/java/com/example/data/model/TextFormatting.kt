package com.example.data.model

data class FormatSpan(
    val start: Int = 0,
    val end: Int = 0,
    val colorHex: String? = null,
    val fontSizeSp: Int? = null,
    val isBold: Boolean? = null,
    val isItalic: Boolean? = null,
    val isUnderline: Boolean? = null
) {
    fun toMap(): Map<String, Any?> {
        val map = mutableMapOf<String, Any?>("start" to start, "end" to end)
        if (colorHex != null) map["colorHex"] = colorHex
        if (fontSizeSp != null) map["fontSizeSp"] = fontSizeSp
        if (isBold != null) map["isBold"] = isBold
        if (isItalic != null) map["isItalic"] = isItalic
        if (isUnderline != null) map["isUnderline"] = isUnderline
        return map
    }

    companion object {
        fun fromMap(map: Map<String, Any?>?): FormatSpan? {
            if (map == null) return null
            val start = (map["start"] as? Number)?.toInt() ?: 0
            val end = (map["end"] as? Number)?.toInt() ?: 0
            val colorHex = map["colorHex"] as? String
            val fontSizeSp = (map["fontSizeSp"] as? Number)?.toInt()
            val isBold = map["isBold"] as? Boolean
            val isItalic = map["isItalic"] as? Boolean
            val isUnderline = map["isUnderline"] as? Boolean
            if (start >= end) return null
            return FormatSpan(
                start = start,
                end = end,
                colorHex = colorHex,
                fontSizeSp = fontSizeSp,
                isBold = isBold,
                isItalic = isItalic,
                isUnderline = isUnderline
            )
        }
    }
}

data class TextFormatting(
    val colorHex: String = "#58A6FF",
    val fontSizeSp: Int = 20,
    val isColorActive: Boolean = false,
    val isSizeActive: Boolean = false,
    val isBoldActive: Boolean = false,
    val isItalicActive: Boolean = false,
    val isUnderlineActive: Boolean = false,
    val spans: List<FormatSpan> = emptyList()
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "colorHex" to colorHex,
            "fontSizeSp" to fontSizeSp,
            "isColorActive" to isColorActive,
            "isSizeActive" to isSizeActive,
            "isBoldActive" to isBoldActive,
            "isItalicActive" to isItalicActive,
            "isUnderlineActive" to isUnderlineActive,
            "spans" to spans.map { it.toMap() }
        )
    }

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun fromMap(map: Map<String, Any?>?): TextFormatting {
            if (map == null) return TextFormatting()
            val color = (map["colorHex"] as? String) ?: "#58A6FF"
            val size = (map["fontSizeSp"] as? Number)?.toInt() ?: 20
            val isColorActive = (map["isColorActive"] as? Boolean) ?: false
            val isSizeActive = (map["isSizeActive"] as? Boolean) ?: false
            val isBoldActive = (map["isBoldActive"] as? Boolean) ?: false
            val isItalicActive = (map["isItalicActive"] as? Boolean) ?: false
            val isUnderlineActive = (map["isUnderlineActive"] as? Boolean) ?: false
            val spansRaw = map["spans"] as? List<Map<String, Any?>>
            val spans = spansRaw?.mapNotNull { FormatSpan.fromMap(it) } ?: emptyList()

            return TextFormatting(
                colorHex = color,
                fontSizeSp = size,
                isColorActive = isColorActive,
                isSizeActive = isSizeActive,
                isBoldActive = isBoldActive,
                isItalicActive = isItalicActive,
                isUnderlineActive = isUnderlineActive,
                spans = spans
            )
        }
    }
}

package com.example.data.model

data class TextFormatting(
    val colorHex: String = "#F0F6FC",
    val fontSizeSp: Int = 16
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "colorHex" to colorHex,
            "fontSizeSp" to fontSizeSp
        )
    }

    companion object {
        fun fromMap(map: Map<String, Any?>?): TextFormatting {
            if (map == null) return TextFormatting()
            val color = (map["colorHex"] as? String) ?: "#F0F6FC"
            val size = (map["fontSizeSp"] as? Number)?.toInt() ?: 16
            return TextFormatting(colorHex = color, fontSizeSp = size)
        }
    }
}

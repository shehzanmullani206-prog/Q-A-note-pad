package com.example.ui.util

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.sp
import com.example.data.model.FormatSpan

object SpanUtils {

    const val DEFAULT_COLOR_HEX = "#F0F6FC"
    const val DEFAULT_FONT_SIZE_SP = 16

    fun buildAnnotatedString(
        text: String,
        spans: List<FormatSpan>,
        defaultColorHex: String = DEFAULT_COLOR_HEX,
        defaultFontSizeSp: Int = DEFAULT_FONT_SIZE_SP
    ): AnnotatedString {
        if (text.isEmpty()) return AnnotatedString("")
        val builder = AnnotatedString.Builder(text)
        val defaultColor = try {
            Color(android.graphics.Color.parseColor(defaultColorHex))
        } catch (_: Exception) {
            Color(0xFFF0F6FC)
        }

        // Base styling for full text
        builder.addStyle(
            SpanStyle(
                color = defaultColor,
                fontSize = defaultFontSizeSp.sp,
                fontWeight = FontWeight.Normal,
                fontStyle = FontStyle.Normal
            ),
            0,
            text.length
        )

        // Overlay specific styled spans
        spans.forEach { span ->
            val s = span.start.coerceIn(0, text.length)
            val e = span.end.coerceIn(0, text.length)
            if (s < e) {
                var style = SpanStyle()
                if (!span.colorHex.isNullOrBlank()) {
                    try {
                        style = style.copy(color = Color(android.graphics.Color.parseColor(span.colorHex)))
                    } catch (_: Exception) {}
                }
                if (span.fontSizeSp != null && span.fontSizeSp > 0) {
                    style = style.copy(fontSize = span.fontSizeSp.sp)
                }
                if (span.isBold == true) {
                    style = style.copy(fontWeight = FontWeight.Bold)
                }
                if (span.isItalic == true) {
                    style = style.copy(fontStyle = FontStyle.Italic)
                }
                if (span.isUnderline == true) {
                    style = style.copy(textDecoration = TextDecoration.Underline)
                }
                builder.addStyle(style, s, e)
            }
        }
        return builder.toAnnotatedString()
    }

    fun updateSpansOnTextChange(
        oldText: String,
        newText: String,
        oldSpans: List<FormatSpan>,
        isColorActive: Boolean,
        activeColorHex: String,
        isSizeActive: Boolean,
        activeSizeSp: Int,
        isBoldActive: Boolean = false,
        isItalicActive: Boolean = false,
        isUnderlineActive: Boolean = false
    ): List<FormatSpan> {
        if (oldText == newText) return oldSpans
        if (newText.isEmpty()) return emptyList()

        // 1. Calculate common prefix
        var prefixLen = 0
        while (prefixLen < oldText.length && prefixLen < newText.length && oldText[prefixLen] == newText[prefixLen]) {
            prefixLen++
        }

        // 2. Calculate common suffix
        var suffixLen = 0
        while (suffixLen < (oldText.length - prefixLen) && suffixLen < (newText.length - prefixLen) &&
            oldText[oldText.length - 1 - suffixLen] == newText[newText.length - 1 - suffixLen]
        ) {
            suffixLen++
        }

        val deletedStart = prefixLen
        val deletedEnd = oldText.length - suffixLen
        val deletedLength = deletedEnd - deletedStart

        val insertedStart = prefixLen
        val insertedEnd = newText.length - suffixLen
        val insertedLength = insertedEnd - insertedStart

        val adjustedSpans = mutableListOf<FormatSpan>()

        for (span in oldSpans) {
            var s = span.start
            var e = span.end

            if (deletedLength > 0) {
                if (s >= deletedEnd) {
                    s -= deletedLength
                } else if (s > deletedStart) {
                    s = deletedStart
                }

                if (e >= deletedEnd) {
                    e -= deletedLength
                } else if (e > deletedStart) {
                    e = deletedStart
                }
            }

            if (insertedLength > 0) {
                if (s >= insertedStart) {
                    s += insertedLength
                }
                if (e > insertedStart) {
                    e += insertedLength
                }
            }

            if (s < e && e <= newText.length) {
                adjustedSpans.add(span.copy(start = s, end = e))
            }
        }

        // Apply active formatting ONLY to newly inserted characters if any tool button is ON
        val hasAnyActiveFormatting = isColorActive || isSizeActive || isBoldActive || isItalicActive || isUnderlineActive
        if (insertedLength > 0 && hasAnyActiveFormatting) {
            val colorToApply = if (isColorActive) activeColorHex else null
            val sizeToApply = if (isSizeActive) activeSizeSp else null
            val boldToApply = if (isBoldActive) true else null
            val italicToApply = if (isItalicActive) true else null
            val underlineToApply = if (isUnderlineActive) true else null

            adjustedSpans.add(
                FormatSpan(
                    start = insertedStart,
                    end = insertedEnd,
                    colorHex = colorToApply,
                    fontSizeSp = sizeToApply,
                    isBold = boldToApply,
                    isItalic = italicToApply,
                    isUnderline = underlineToApply
                )
            )
        }

        return mergeAndNormalizeSpans(adjustedSpans, newText.length)
    }

    fun applyFormattingToRange(
        spans: List<FormatSpan>,
        rangeStart: Int,
        rangeEnd: Int,
        colorHex: String?,
        fontSizeSp: Int?,
        isBold: Boolean? = null,
        isItalic: Boolean? = null,
        isUnderline: Boolean? = null,
        textLength: Int
    ): List<FormatSpan> {
        val s = rangeStart.coerceIn(0, textLength)
        val e = rangeEnd.coerceIn(0, textLength)
        if (s >= e) return spans

        val newSpans = spans.toMutableList()
        newSpans.add(
            FormatSpan(
                start = s,
                end = e,
                colorHex = colorHex,
                fontSizeSp = fontSizeSp,
                isBold = isBold,
                isItalic = isItalic,
                isUnderline = isUnderline
            )
        )
        return mergeAndNormalizeSpans(newSpans, textLength)
    }

    private fun mergeAndNormalizeSpans(spans: List<FormatSpan>, maxLen: Int): List<FormatSpan> {
        return spans.filter { it.start < it.end && it.end <= maxLen && it.start >= 0 }
    }
}

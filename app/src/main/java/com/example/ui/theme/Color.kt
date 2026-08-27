package com.example.ui.theme

import androidx.compose.ui.graphics.Color

val DarkBg = Color(0xFF0D1117)
val DarkSurface = Color(0xFF161B22)
val DarkSurfaceVariant = Color(0xFF21262D)
val DarkBorder = Color(0xFF30363D)
val DarkTextPrimary = Color(0xFFF0F6FC)
val DarkTextSecondary = Color(0xFF8B949E)
val DarkTextMuted = Color(0xFF6E7681)

val AccentCyan = Color(0xFF58A6FF)
val AccentGreen = Color(0xFF3FB950)
val AccentAmber = Color(0xFFD29922)
val AccentCoral = Color(0xFFF85149)
val AccentPurple = Color(0xFFBC8CFF)

// Selectable Text Colors for Tool 1 with names
data class NamedColor(
    val color: Color,
    val hex: String,
    val name: String
)

val FormatColors = listOf(
    NamedColor(Color(0xFFF0F6FC), "#F0F6FC", "White"),
    NamedColor(Color(0xFF58A6FF), "#58A6FF", "Cyan Blue"),
    NamedColor(Color(0xFF3FB950), "#3FB950", "Emerald"),
    NamedColor(Color(0xFFE3B341), "#E3B341", "Gold"),
    NamedColor(Color(0xFFF85149), "#F85149", "Coral"),
    NamedColor(Color(0xFFBC8CFF), "#BC8CFF", "Purple"),
    NamedColor(Color(0xFF79C0FF), "#79C0FF", "Sky"),
    NamedColor(Color(0xFFFFA657), "#FFA657", "Orange"),
    NamedColor(Color(0xFFFF7B72), "#FF7B72", "Pink"),
    NamedColor(Color(0xFF7EE787), "#7EE787", "Mint"),
    NamedColor(Color(0xFFD2A8FF), "#D2A8FF", "Lavender"),
    NamedColor(Color(0xFFF2CC60), "#F2CC60", "Yellow")
)


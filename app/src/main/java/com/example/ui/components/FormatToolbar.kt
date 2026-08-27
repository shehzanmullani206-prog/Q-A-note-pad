package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatColorText
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.FormatUnderlined
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TextFormatting
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.DarkTextMuted
import com.example.ui.theme.DarkTextPrimary
import com.example.ui.theme.DarkTextSecondary
import com.example.ui.theme.FormatColors

val AvailableFontSizes = listOf(
    12 to "Tiny",
    14 to "Small",
    18 to "Medium",
    22 to "Large",
    26 to "Extra Large",
    32 to "Heading"
)

private enum class ActiveFlyout {
    NONE,
    COLOR,
    SIZE
}

@Composable
fun FormatToolbar(
    currentFormatting: TextFormatting,
    onToggleColor: (Boolean) -> Unit,
    onColorSelected: (String) -> Unit,
    onToggleSize: (Boolean) -> Unit,
    onSizeSelected: (Int) -> Unit,
    onToggleBold: (Boolean) -> Unit,
    onToggleItalic: (Boolean) -> Unit,
    onToggleUnderline: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var activeFlyout by remember { mutableStateOf(ActiveFlyout.NONE) }

    val currentColor = remember(currentFormatting.colorHex) {
        try {
            Color(android.graphics.Color.parseColor(currentFormatting.colorHex))
        } catch (_: Exception) {
            Color(0xFF58A6FF)
        }
    }

    val selectedColorName = remember(currentFormatting.colorHex) {
        FormatColors.find { it.hex.equals(currentFormatting.colorHex, ignoreCase = true) }?.name ?: "Selected"
    }

    Row(modifier = modifier.fillMaxHeight()) {
        // --- PRIMARY VERTICAL TOOLBAR ---
        Surface(
            modifier = Modifier
                .width(72.dp)
                .fillMaxHeight()
                .testTag("vertical_format_toolbar"),
            color = DarkSurface,
            shape = RoundedCornerShape(topEnd = 18.dp, bottomEnd = 18.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 12.dp, horizontal = 5.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Header Label
                Text(
                    text = "TOOLS",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    color = DarkTextMuted,
                    letterSpacing = 1.sp
                )

                HorizontalDivider(
                    color = DarkBorder.copy(alpha = 0.7f),
                    modifier = Modifier.padding(horizontal = 4.dp)
                )

                // ==========================================
                // TOOL 1: COLOUR TOOL
                // ==========================================
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (currentFormatting.isColorActive) currentColor.copy(alpha = 0.15f)
                            else if (activeFlyout == ActiveFlyout.COLOR) DarkSurfaceVariant
                            else Color.Transparent
                        )
                        .border(
                            width = if (currentFormatting.isColorActive) 2.dp else 1.dp,
                            color = if (currentFormatting.isColorActive) currentColor else if (activeFlyout == ActiveFlyout.COLOR) DarkTextSecondary else Color.Transparent,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable {
                            activeFlyout = if (activeFlyout == ActiveFlyout.COLOR) ActiveFlyout.NONE else ActiveFlyout.COLOR
                        }
                        .padding(vertical = 6.dp, horizontal = 2.dp)
                        .testTag("tool_text_color")
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(if (currentFormatting.isColorActive) currentColor else DarkSurfaceVariant)
                            .border(
                                2.dp,
                                if (currentFormatting.isColorActive) Color.White else DarkBorder,
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.FormatColorText,
                            contentDescription = "Text Colour Tool",
                            tint = if (currentFormatting.isColorActive) {
                                if (currentFormatting.colorHex.equals("#F0F6FC", true) || currentFormatting.colorHex.equals("#F2CC60", true)) Color.Black else Color.White
                            } else DarkTextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "Colour",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (currentFormatting.isColorActive) currentColor else DarkTextPrimary
                    )

                    Surface(
                        color = if (currentFormatting.isColorActive) AccentGreen.copy(alpha = 0.25f) else DarkSurfaceVariant,
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Text(
                            text = if (currentFormatting.isColorActive) "ON" else "OFF",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            color = if (currentFormatting.isColorActive) AccentGreen else DarkTextMuted,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }

                // ==========================================
                // TOOL 2: TEXT SIZE TOOL
                // ==========================================
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (currentFormatting.isSizeActive) AccentCyan.copy(alpha = 0.15f)
                            else if (activeFlyout == ActiveFlyout.SIZE) DarkSurfaceVariant
                            else Color.Transparent
                        )
                        .border(
                            width = if (currentFormatting.isSizeActive) 2.dp else 1.dp,
                            color = if (currentFormatting.isSizeActive) AccentCyan else if (activeFlyout == ActiveFlyout.SIZE) DarkTextSecondary else Color.Transparent,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable {
                            activeFlyout = if (activeFlyout == ActiveFlyout.SIZE) ActiveFlyout.NONE else ActiveFlyout.SIZE
                        }
                        .padding(vertical = 6.dp, horizontal = 2.dp)
                        .testTag("tool_text_size")
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (currentFormatting.isSizeActive) AccentCyan else DarkSurfaceVariant)
                            .border(
                                1.5.dp,
                                if (currentFormatting.isSizeActive) Color.White else DarkBorder,
                                RoundedCornerShape(8.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${currentFormatting.fontSizeSp}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = if (currentFormatting.isSizeActive) Color.Black else DarkTextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "Size",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (currentFormatting.isSizeActive) AccentCyan else DarkTextPrimary
                    )

                    Surface(
                        color = if (currentFormatting.isSizeActive) AccentCyan.copy(alpha = 0.25f) else DarkSurfaceVariant,
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Text(
                            text = if (currentFormatting.isSizeActive) "ON" else "OFF",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            color = if (currentFormatting.isSizeActive) AccentCyan else DarkTextMuted,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }

                HorizontalDivider(
                    color = DarkBorder.copy(alpha = 0.6f),
                    modifier = Modifier.padding(horizontal = 4.dp)
                )

                // ==========================================
                // TOOL 3: BOLD TOOL
                // ==========================================
                ToggleFormatButton(
                    icon = Icons.Default.FormatBold,
                    label = "Bold",
                    isActive = currentFormatting.isBoldActive,
                    activeColor = AccentCyan,
                    testTag = "tool_bold",
                    onClick = { onToggleBold(!currentFormatting.isBoldActive) }
                )

                // ==========================================
                // TOOL 4: ITALIC TOOL
                // ==========================================
                ToggleFormatButton(
                    icon = Icons.Default.FormatItalic,
                    label = "Italic",
                    isActive = currentFormatting.isItalicActive,
                    activeColor = AccentPurple,
                    testTag = "tool_italic",
                    onClick = { onToggleItalic(!currentFormatting.isItalicActive) }
                )

                // ==========================================
                // TOOL 5: UNDERLINE TOOL
                // ==========================================
                ToggleFormatButton(
                    icon = Icons.Default.FormatUnderlined,
                    label = "Underline",
                    isActive = currentFormatting.isUnderlineActive,
                    activeColor = AccentGreen,
                    testTag = "tool_underline",
                    onClick = { onToggleUnderline(!currentFormatting.isUnderlineActive) }
                )

                Spacer(modifier = Modifier.weight(1f))
            }
        }

        // --- EXPANDABLE SIDE FLYOUT PANELS (FOR COLOUR & SIZE) ---
        AnimatedVisibility(
            visible = activeFlyout != ActiveFlyout.NONE,
            enter = expandHorizontally() + fadeIn(),
            exit = shrinkHorizontally() + fadeOut()
        ) {
            Surface(
                modifier = Modifier
                    .width(240.dp)
                    .fillMaxHeight()
                    .padding(start = 8.dp, top = 8.dp, bottom = 8.dp, end = 4.dp)
                    .testTag("format_flyout_panel"),
                color = DarkSurface,
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                tonalElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(14.dp)
                ) {
                    // Header with close button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (activeFlyout == ActiveFlyout.COLOR) "Colour Tool" else "Text Size Tool",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkTextPrimary
                        )
                        IconButton(
                            onClick = { activeFlyout = ActiveFlyout.NONE },
                            modifier = Modifier.size(26.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close panel",
                                tint = DarkTextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    HorizontalDivider(
                        color = DarkBorder,
                        modifier = Modifier.padding(vertical = 10.dp)
                    )

                    // 1. COLOUR PALETTE FLYOUT
                    if (activeFlyout == ActiveFlyout.COLOR) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Power Toggle Switch Card
                            Surface(
                                color = if (currentFormatting.isColorActive) DarkSurfaceVariant else DarkSurfaceVariant.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (currentFormatting.isColorActive) AccentGreen else DarkBorder
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            text = "Colour Formatting",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = DarkTextPrimary
                                        )
                                        Text(
                                            text = if (currentFormatting.isColorActive) "Active for new words" else "Turned OFF (Default)",
                                            fontSize = 10.sp,
                                            color = if (currentFormatting.isColorActive) AccentGreen else DarkTextMuted
                                        )
                                    }

                                    Switch(
                                        checked = currentFormatting.isColorActive,
                                        onCheckedChange = onToggleColor,
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = Color.White,
                                            checkedTrackColor = AccentGreen,
                                            uncheckedThumbColor = DarkTextMuted,
                                            uncheckedTrackColor = DarkSurface
                                        )
                                    )
                                }
                            }

                            Text(
                                text = "SELECT COLOUR FOR NEXT WORDS:",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkTextMuted,
                                letterSpacing = 0.5.sp
                            )

                            // 3-Column Color Palette Grid
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                FormatColors.chunked(3).forEach { rowColors ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        rowColors.forEach { namedColor ->
                                            val isSelected = currentFormatting.colorHex.equals(namedColor.hex, ignoreCase = true)
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .clickable {
                                                        onColorSelected(namedColor.hex)
                                                        if (!currentFormatting.isColorActive) {
                                                            onToggleColor(true)
                                                        }
                                                    }
                                                    .padding(4.dp)
                                                    .testTag("color_item_${namedColor.hex}")
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(40.dp)
                                                        .clip(CircleShape)
                                                        .background(namedColor.color)
                                                        .border(
                                                            width = if (isSelected) 3.dp else 1.dp,
                                                            color = if (isSelected) Color.White else DarkBorder,
                                                            shape = CircleShape
                                                        ),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    if (isSelected) {
                                                        Icon(
                                                            imageVector = Icons.Default.Check,
                                                            contentDescription = "Selected",
                                                            tint = if (namedColor.color == Color(0xFFF0F6FC) || namedColor.color == Color(0xFFF2CC60)) Color.Black else Color.White,
                                                            modifier = Modifier.size(18.dp)
                                                        )
                                                    }
                                                }
                                                Spacer(modifier = Modifier.height(3.dp))
                                                Text(
                                                    text = namedColor.name,
                                                    fontSize = 9.sp,
                                                    color = if (isSelected) AccentCyan else DarkTextSecondary,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 2. TEXT SIZE PRESETS FLYOUT
                    if (activeFlyout == ActiveFlyout.SIZE) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Power Toggle Switch Card
                            Surface(
                                color = if (currentFormatting.isSizeActive) DarkSurfaceVariant else DarkSurfaceVariant.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (currentFormatting.isSizeActive) AccentCyan else DarkBorder
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            text = "Size Formatting",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = DarkTextPrimary
                                        )
                                        Text(
                                            text = if (currentFormatting.isSizeActive) "Active for new words" else "Turned OFF (Default 16sp)",
                                            fontSize = 10.sp,
                                            color = if (currentFormatting.isSizeActive) AccentCyan else DarkTextMuted
                                        )
                                    }

                                    Switch(
                                        checked = currentFormatting.isSizeActive,
                                        onCheckedChange = onToggleSize,
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = Color.White,
                                            checkedTrackColor = AccentCyan,
                                            uncheckedThumbColor = DarkTextMuted,
                                            uncheckedTrackColor = DarkSurface
                                        )
                                    )
                                }
                            }

                            Text(
                                text = "SELECT SIZE FOR NEXT WORDS:",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkTextMuted,
                                letterSpacing = 0.5.sp
                            )

                            AvailableFontSizes.forEach { (size, label) ->
                                val isSelected = currentFormatting.fontSizeSp == size
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable {
                                            onSizeSelected(size)
                                            if (!currentFormatting.isSizeActive) {
                                                onToggleSize(true)
                                            }
                                        }
                                        .testTag("size_item_$size"),
                                    color = if (isSelected) DarkSurfaceVariant else Color.Transparent,
                                    border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, AccentCyan) else null,
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "$label (${size}sp)",
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) AccentCyan else DarkTextPrimary
                                        )

                                        Text(
                                            text = "Aa",
                                            fontSize = (size * 0.65).coerceIn(12.0, 22.0).sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) AccentCyan else currentColor
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ToggleFormatButton(
    icon: ImageVector,
    label: String,
    isActive: Boolean,
    activeColor: Color,
    testTag: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isActive) activeColor.copy(alpha = 0.15f) else Color.Transparent)
            .border(
                width = if (isActive) 2.dp else 1.dp,
                color = if (isActive) activeColor else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 6.dp, horizontal = 2.dp)
            .testTag(testTag)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(if (isActive) activeColor else DarkSurfaceVariant)
                .border(
                    1.5.dp,
                    if (isActive) Color.White else DarkBorder,
                    RoundedCornerShape(8.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = "$label tool",
                tint = if (isActive) Color.Black else DarkTextPrimary,
                modifier = Modifier.size(16.dp)
            )
        }

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = if (isActive) activeColor else DarkTextPrimary
        )

        Surface(
            color = if (isActive) activeColor.copy(alpha = 0.25f) else DarkSurfaceVariant,
            shape = RoundedCornerShape(4.dp),
            modifier = Modifier.padding(top = 2.dp)
        ) {
            Text(
                text = if (isActive) "ON" else "OFF",
                fontSize = 8.sp,
                fontWeight = FontWeight.Black,
                color = if (isActive) activeColor else DarkTextMuted,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
            )
        }
    }
}

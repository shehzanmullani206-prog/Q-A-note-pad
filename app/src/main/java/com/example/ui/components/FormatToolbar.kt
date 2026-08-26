package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FormatColorText
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.Popup
import com.example.data.model.TextFormatting
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.DarkTextPrimary
import com.example.ui.theme.DarkTextSecondary
import com.example.ui.theme.FormatColors

private val AvailableFontSizes = listOf(
    14 to "Small",
    16 to "Default",
    20 to "Medium",
    24 to "Large",
    28 to "XL"
)

@Composable
fun FormatToolbar(
    currentFormatting: TextFormatting,
    onColorSelected: (String) -> Unit,
    onSizeSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var showColorPicker by remember { mutableStateOf(false) }
    var showSizePicker by remember { mutableStateOf(false) }

    val currentColor = remember(currentFormatting.colorHex) {
        try {
            Color(android.graphics.Color.parseColor(currentFormatting.colorHex))
        } catch (_: Exception) {
            DarkTextPrimary
        }
    }

    Surface(
        modifier = modifier
            .width(58.dp)
            .fillMaxHeight()
            .testTag("vertical_format_toolbar"),
        color = DarkSurface,
        shape = RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(vertical = 16.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            // Label
            Text(
                text = "TOOLS",
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = DarkTextSecondary,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Tool 1: Text Colour
            Box(contentAlignment = Alignment.Center) {
                IconButton(
                    onClick = {
                        showColorPicker = !showColorPicker
                        showSizePicker = false
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (showColorPicker) DarkSurfaceVariant else Color.Transparent)
                        .testTag("tool_text_color")
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.FormatColorText,
                            contentDescription = "Text Colour Tool",
                            tint = currentColor,
                            modifier = Modifier.size(22.dp)
                        )
                        Box(
                            modifier = Modifier
                                .padding(top = 2.dp)
                                .size(14.dp, 3.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(currentColor)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tool 2: Text Size
            Box(contentAlignment = Alignment.Center) {
                IconButton(
                    onClick = {
                        showSizePicker = !showSizePicker
                        showColorPicker = false
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (showSizePicker) DarkSurfaceVariant else Color.Transparent)
                        .testTag("tool_text_size")
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.FormatSize,
                            contentDescription = "Text Size Tool",
                            tint = if (showSizePicker) AccentCyan else DarkTextPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = "${currentFormatting.fontSizeSp}",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkTextSecondary
                        )
                    }
                }
            }
        }
    }

    // Color Selector Dialog
    if (showColorPicker) {
        Dialog(onDismissRequest = { showColorPicker = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = DarkSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                modifier = Modifier
                    .padding(16.dp)
                    .testTag("color_picker_dialog")
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Text Colour",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = DarkTextPrimary
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // 2 rows of color circles
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        FormatColors.chunked(4).forEach { rowColors ->
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                rowColors.forEach { (color, hex) ->
                                    val isSelected = currentFormatting.colorHex.equals(hex, ignoreCase = true)
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(color)
                                            .border(
                                                width = if (isSelected) 3.dp else 1.dp,
                                                color = if (isSelected) AccentCyan else DarkBorder,
                                                shape = CircleShape
                                            )
                                            .clickable {
                                                onColorSelected(hex)
                                                showColorPicker = false
                                            }
                                            .testTag("color_item_$hex"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isSelected) {
                                            Box(
                                                modifier = Modifier
                                                    .size(10.dp)
                                                    .clip(CircleShape)
                                                    .background(Color.Black.copy(alpha = 0.6f))
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

    // Size Selector Dialog
    if (showSizePicker) {
        Dialog(onDismissRequest = { showSizePicker = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = DarkSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                modifier = Modifier
                    .padding(16.dp)
                    .width(260.dp)
                    .testTag("size_picker_dialog")
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Text Size",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = DarkTextPrimary
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        AvailableFontSizes.forEach { (size, label) ->
                            val isSelected = currentFormatting.fontSizeSp == size
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        onSizeSelected(size)
                                        showSizePicker = false
                                    }
                                    .testTag("size_item_$size"),
                                color = if (isSelected) DarkSurfaceVariant else Color.Transparent,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "$label ($size sp)",
                                        fontSize = 14.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) AccentCyan else DarkTextPrimary
                                    )
                                    Text(
                                        text = "Aa",
                                        fontSize = (size * 0.85).sp,
                                        fontWeight = FontWeight.Medium,
                                        color = if (isSelected) AccentCyan else DarkTextSecondary
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

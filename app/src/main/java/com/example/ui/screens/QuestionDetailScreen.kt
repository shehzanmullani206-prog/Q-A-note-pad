package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FormatSpan
import com.example.data.model.QuestionItem
import com.example.data.model.TextFormatting
import com.example.ui.components.ConnectionStatusBanner
import com.example.ui.components.FormatToolbar
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.DarkBg
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.DarkTextMuted
import com.example.ui.theme.DarkTextPrimary
import com.example.ui.theme.DarkTextSecondary
import com.example.ui.util.SpanUtils

class SpanVisualTransformation(
    private val spans: List<FormatSpan>,
    private val defaultColorHex: String = "#F0F6FC",
    private val defaultFontSizeSp: Int = 16
) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val annotated = SpanUtils.buildAnnotatedString(
            text = text.text,
            spans = spans,
            defaultColorHex = defaultColorHex,
            defaultFontSizeSp = defaultFontSizeSp
        )
        return TransformedText(annotated, OffsetMapping.Identity)
    }
}

@Composable
fun QuestionDetailScreen(
    question: QuestionItem?,
    localQuestionText: String,
    localAnswerContent: String,
    localFormatting: TextFormatting,
    isOnline: Boolean,
    isSyncing: Boolean,
    onBack: () -> Unit,
    onQuestionTextChange: (String) -> Unit,
    onAnswerAndSpansChange: (String, List<FormatSpan>) -> Unit,
    onToggleColor: (Boolean) -> Unit,
    onColorSelected: (String) -> Unit,
    onToggleSize: (Boolean) -> Unit,
    onSizeSelected: (Int) -> Unit,
    onToggleBold: (Boolean) -> Unit,
    onToggleItalic: (Boolean) -> Unit,
    onToggleUnderline: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    val activeCursorColor = remember(localFormatting.isColorActive, localFormatting.colorHex) {
        if (localFormatting.isColorActive) {
            try {
                Color(android.graphics.Color.parseColor(localFormatting.colorHex))
            } catch (_: Exception) {
                AccentCyan
            }
        } else {
            AccentCyan
        }
    }

    val visualTransformation = remember(localFormatting.spans) {
        SpanVisualTransformation(localFormatting.spans)
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBg)
            .statusBarsPadding()
            .navigationBarsPadding(),
        containerColor = DarkBg,
        topBar = {
            Column {
                ConnectionStatusBanner(isOnline = isOnline)

                // Top App Bar
                Surface(
                    color = DarkSurface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("question_detail_top_bar")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            IconButton(
                                onClick = onBack,
                                modifier = Modifier
                                    .size(36.dp)
                                    .testTag("question_detail_back_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back to Questions",
                                    tint = DarkTextPrimary
                                )
                            }

                            Column {
                                Text(
                                    text = "Question Detail",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DarkTextPrimary
                                )
                                Text(
                                    text = "Live collaborative editor",
                                    fontSize = 11.sp,
                                    color = DarkTextSecondary
                                )
                            }
                        }

                        // Live Sync status pill
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = DarkSurfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Icon(
                                    imageVector = if (isSyncing) Icons.Default.Sync else Icons.Default.CloudDone,
                                    contentDescription = null,
                                    tint = if (isSyncing) AccentCyan else AccentGreen,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = if (isSyncing) "Syncing..." else "Real-time",
                                    fontSize = 11.sp,
                                    color = if (isSyncing) AccentCyan else AccentGreen,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
                HorizontalDivider(color = DarkBorder)
            }
        }
    ) { innerPadding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
        ) {
            // LEFT: Vertical Tools Bar (Colour, Size, Bold, Italic, Underline)
            FormatToolbar(
                currentFormatting = localFormatting,
                onToggleColor = onToggleColor,
                onColorSelected = onColorSelected,
                onToggleSize = onToggleSize,
                onSizeSelected = onSizeSelected,
                onToggleBold = onToggleBold,
                onToggleItalic = onToggleItalic,
                onToggleUnderline = onToggleUnderline,
                modifier = Modifier.fillMaxHeight()
            )

            // CENTER / RIGHT: Editable Question and Large Rich Answer Editor
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Section 1: Question
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("question_text_container"),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "QUESTION",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = AccentCyan,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "(editable by both users)",
                                fontSize = 10.sp,
                                color = DarkTextMuted
                            )
                        }

                        BasicTextField(
                            value = localQuestionText,
                            onValueChange = onQuestionTextChange,
                            textStyle = TextStyle(
                                color = DarkTextPrimary,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.SemiBold,
                                lineHeight = 24.sp
                            ),
                            cursorBrush = SolidColor(AccentCyan),
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Sentences
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("question_text_input"),
                            decorationBox = { innerTextField ->
                                Box {
                                    if (localQuestionText.isEmpty()) {
                                        Text(
                                            text = "Enter question...",
                                            color = DarkTextMuted,
                                            fontSize = 17.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                    innerTextField()
                                }
                            }
                        )
                    }
                }

                // Section 2: Large Rich Answer / Notes Area
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .height(380.dp)
                        .testTag("answer_content_container"),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "ANSWER / NOTES",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkTextSecondary,
                                letterSpacing = 0.5.sp
                            )

                            // Status info showing active tool states
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                if (localFormatting.isColorActive) {
                                    Surface(
                                        color = activeCursorColor.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(4.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, activeCursorColor)
                                    ) {
                                        Text(
                                            text = "Color",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = activeCursorColor,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                                if (localFormatting.isSizeActive) {
                                    Surface(
                                        color = AccentCyan.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(4.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, AccentCyan)
                                    ) {
                                        Text(
                                            text = "${localFormatting.fontSizeSp}sp",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = AccentCyan,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                                if (localFormatting.isBoldActive) {
                                    Surface(
                                        color = AccentCyan.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(4.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, AccentCyan)
                                    ) {
                                        Text(
                                            text = "B",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black,
                                            color = AccentCyan,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                                if (localFormatting.isItalicActive) {
                                    Surface(
                                        color = AccentPurple.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(4.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, AccentPurple)
                                    ) {
                                        Text(
                                            text = "I",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black,
                                            color = AccentPurple,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                                if (localFormatting.isUnderlineActive) {
                                    Surface(
                                        color = AccentGreen.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(4.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, AccentGreen)
                                    ) {
                                        Text(
                                            text = "U",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black,
                                            color = AccentGreen,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                                if (!localFormatting.isColorActive && !localFormatting.isSizeActive &&
                                    !localFormatting.isBoldActive && !localFormatting.isItalicActive && !localFormatting.isUnderlineActive) {
                                    Text(
                                        text = "Tools OFF (Normal)",
                                        fontSize = 9.sp,
                                        color = DarkTextMuted
                                    )
                                }
                            }
                        }

                        HorizontalDivider(color = DarkBorder.copy(alpha = 0.5f))

                        BasicTextField(
                            value = localAnswerContent,
                            onValueChange = { newText ->
                                val updatedSpans = SpanUtils.updateSpansOnTextChange(
                                    oldText = localAnswerContent,
                                    newText = newText,
                                    oldSpans = localFormatting.spans,
                                    isColorActive = localFormatting.isColorActive,
                                    activeColorHex = localFormatting.colorHex,
                                    isSizeActive = localFormatting.isSizeActive,
                                    activeSizeSp = localFormatting.fontSizeSp,
                                    isBoldActive = localFormatting.isBoldActive,
                                    isItalicActive = localFormatting.isItalicActive,
                                    isUnderlineActive = localFormatting.isUnderlineActive
                                )
                                onAnswerAndSpansChange(newText, updatedSpans)
                            },
                            textStyle = TextStyle(
                                color = DarkTextPrimary,
                                fontSize = 16.sp,
                                lineHeight = 24.sp,
                                fontWeight = FontWeight.Normal
                            ),
                            visualTransformation = visualTransformation,
                            cursorBrush = SolidColor(activeCursorColor),
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Sentences
                            ),
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("answer_content_input"),
                            decorationBox = { innerTextField ->
                                Box(modifier = Modifier.fillMaxSize()) {
                                    if (localAnswerContent.isEmpty()) {
                                        Text(
                                            text = "Type answer or notes here...\n\n• Use the left sidebar to toggle Colour, Size, Bold (B), Italic (I), and Underline (U).\n• Formatting applies to newly typed words when ON.\n• When toggled OFF, typing returns to standard style while keeping existing formatted text intact.",
                                            color = DarkTextMuted,
                                            fontSize = 15.sp,
                                            lineHeight = 22.sp
                                        )
                                    }
                                    innerTextField()
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

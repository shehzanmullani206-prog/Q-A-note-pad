package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.QuestionItem
import com.example.data.model.TextFormatting
import com.example.ui.components.ConnectionStatusBanner
import com.example.ui.components.FormatToolbar
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.DarkBg
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.DarkTextMuted
import com.example.ui.theme.DarkTextPrimary
import com.example.ui.theme.DarkTextSecondary

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
    onAnswerContentChange: (String) -> Unit,
    onColorSelected: (String) -> Unit,
    onSizeSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    val parsedTextColor = remember(localFormatting.colorHex) {
        try {
            Color(android.graphics.Color.parseColor(localFormatting.colorHex))
        } catch (_: Exception) {
            DarkTextPrimary
        }
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
            // LEFT: Vertical Tools Bar (Tool 1: Text Colour, Tool 2: Text Size)
            FormatToolbar(
                currentFormatting = localFormatting,
                onColorSelected = onColorSelected,
                onSizeSelected = onSizeSelected,
                modifier = Modifier.fillMaxHeight()
            )

            // CENTER / RIGHT: Editable Question and Large Answer Editor
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

                // Section 2: Large Answer / Notes Area
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

                            Text(
                                text = "${localFormatting.fontSizeSp}sp • ${localFormatting.colorHex}",
                                fontSize = 10.sp,
                                color = DarkTextMuted
                            )
                        }

                        HorizontalDivider(color = DarkBorder.copy(alpha = 0.5f))

                        BasicTextField(
                            value = localAnswerContent,
                            onValueChange = onAnswerContentChange,
                            textStyle = TextStyle(
                                color = parsedTextColor,
                                fontSize = localFormatting.fontSizeSp.sp,
                                lineHeight = (localFormatting.fontSizeSp * 1.5).sp
                            ),
                            cursorBrush = SolidColor(AccentCyan),
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
                                            text = "Type answer or notes here...\n\nChanges synchronize live to the connected user. Use the left toolbar to change color or size.",
                                            color = DarkTextMuted,
                                            fontSize = localFormatting.fontSizeSp.sp,
                                            lineHeight = (localFormatting.fontSizeSp * 1.5).sp
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

package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.QuestionItem
import com.example.data.model.SharedNote
import com.example.ui.components.ConnectionStatusBanner
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentCoral
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.DarkBg
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.DarkTextMuted
import com.example.ui.theme.DarkTextPrimary
import com.example.ui.theme.DarkTextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun QuestionListScreen(
    currentNote: SharedNote?,
    questions: List<QuestionItem>,
    currentUserId: String,
    currentUserName: String,
    isOnline: Boolean,
    showAddDialog: Boolean,
    questionToDelete: QuestionItem?,
    questionToEditTitle: QuestionItem?,
    onOpenQuestion: (QuestionItem) -> Unit,
    onShowAddDialog: (Boolean) -> Unit,
    onCreateQuestion: (String) -> Unit,
    onConfirmDelete: (QuestionItem) -> Unit,
    onDismissDeleteDialog: () -> Unit,
    onPerformDelete: () -> Unit,
    onShowEditTitleDialog: (QuestionItem) -> Unit,
    onDismissEditTitleDialog: () -> Unit,
    onSaveEditedTitle: (String) -> Unit,
    onLeaveNote: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var newQuestionInput by remember { mutableStateOf("") }
    var editTitleInput by remember(questionToEditTitle) {
        mutableStateOf(questionToEditTitle?.questionText ?: "")
    }

    val shareCode = currentNote?.shareCode ?: ""
    val usersCount = currentNote?.users?.size ?: 1
    val otherUser = currentNote?.users?.values?.firstOrNull { it.userId != currentUserId }
    val isOtherUserActive = otherUser?.isOnline == true &&
            (System.currentTimeMillis() - (otherUser.lastSeen)) < 60_000L

    fun copyShareCode() {
        if (shareCode.isNotEmpty()) {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Share Code", shareCode)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(context, "Share Code copied: $shareCode", Toast.LENGTH_SHORT).show()
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

                // Top Bar
                Surface(
                    color = DarkSurface,
                    border = androidx.compose.foundation.BorderStroke(0.dp, Color.Transparent),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("question_list_top_bar")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                IconButton(
                                    onClick = onLeaveNote,
                                    modifier = Modifier
                                        .size(36.dp)
                                        .testTag("leave_note_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Leave Note",
                                        tint = DarkTextPrimary
                                    )
                                }
                                Column {
                                    Text(
                                        text = currentNote?.title ?: "Collaborative Q&A",
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = DarkTextPrimary
                                    )
                                    // Connection status indicator
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(7.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    if (!isOnline) AccentCoral
                                                    else if (usersCount >= 2) AccentGreen
                                                    else AccentAmber
                                                )
                                        )
                                        Text(
                                            text = if (!isOnline) "○ Offline"
                                            else if (usersCount >= 2) "● Connected (${otherUser?.name ?: "Partner"})"
                                            else "● Waiting for 2nd user (1/2)",
                                            fontSize = 11.sp,
                                            color = if (!isOnline) DarkTextSecondary
                                            else if (usersCount >= 2) AccentGreen
                                            else AccentAmber,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }

                            // Share Code Pill with Copy Action
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = DarkSurfaceVariant,
                                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .clickable { copyShareCode() }
                                    .testTag("share_code_pill")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "CODE:",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = DarkTextMuted
                                    )
                                    Text(
                                        text = shareCode,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        color = AccentCyan,
                                        letterSpacing = 1.sp
                                    )
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Copy code",
                                        tint = AccentCyan,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }
                HorizontalDivider(color = DarkBorder)
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    newQuestionInput = ""
                    onShowAddDialog(true)
                },
                containerColor = AccentCyan,
                contentColor = DarkBg,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("add_question_fab")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add Question")
                    Text(
                        text = "Add Question",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (questions.isEmpty()) {
                // Empty state
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(DarkSurfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.HelpOutline,
                            contentDescription = null,
                            tint = AccentCyan,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No questions yet",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkTextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Tap '+ Add Question' below to create your first question. Both connected users can add and edit answers in real time.",
                        fontSize = 13.sp,
                        color = DarkTextSecondary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("questions_lazy_column"),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 16.dp,
                        bottom = 88.dp // Space for FAB
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(
                        items = questions,
                        key = { _, item -> item.questionId }
                    ) { index, question ->
                        QuestionCardItem(
                            index = index + 1,
                            question = question,
                            onCardClick = { onOpenQuestion(question) },
                            onEditTitle = { onShowEditTitleDialog(question) },
                            onDelete = { onConfirmDelete(question) }
                        )
                    }
                }
            }
        }
    }

    // Dialog: Create Question
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { onShowAddDialog(false) },
            containerColor = DarkSurface,
            title = {
                Text(
                    text = "Add Question",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkTextPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Enter your question for collaborative editing:",
                        fontSize = 13.sp,
                        color = DarkTextSecondary
                    )
                    OutlinedTextField(
                        value = newQuestionInput,
                        onValueChange = { newQuestionInput = it },
                        placeholder = { Text("Enter your question...", color = DarkTextMuted) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = DarkSurfaceVariant,
                            unfocusedContainerColor = DarkSurfaceVariant,
                            focusedBorderColor = AccentCyan,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = DarkTextPrimary,
                            unfocusedTextColor = DarkTextPrimary
                        ),
                        shape = RoundedCornerShape(10.dp),
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = {
                            if (newQuestionInput.trim().isNotEmpty()) {
                                onCreateQuestion(newQuestionInput)
                            }
                        }),
                        maxLines = 4,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("create_question_input")
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newQuestionInput.trim().isNotEmpty()) {
                            onCreateQuestion(newQuestionInput)
                        }
                    },
                    enabled = newQuestionInput.trim().isNotEmpty(),
                    modifier = Modifier.testTag("create_question_submit_button")
                ) {
                    Text(
                        text = "Create",
                        fontWeight = FontWeight.Bold,
                        color = if (newQuestionInput.trim().isNotEmpty()) AccentCyan else DarkTextMuted
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { onShowAddDialog(false) },
                    modifier = Modifier.testTag("create_question_cancel_button")
                ) {
                    Text("Cancel", color = DarkTextSecondary)
                }
            }
        )
    }

    // Dialog: Edit Question Title
    if (questionToEditTitle != null) {
        AlertDialog(
            onDismissRequest = onDismissEditTitleDialog,
            containerColor = DarkSurface,
            title = {
                Text(
                    text = "Edit Question",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkTextPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = editTitleInput,
                        onValueChange = { editTitleInput = it },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = DarkSurfaceVariant,
                            unfocusedContainerColor = DarkSurfaceVariant,
                            focusedBorderColor = AccentCyan,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = DarkTextPrimary,
                            unfocusedTextColor = DarkTextPrimary
                        ),
                        shape = RoundedCornerShape(10.dp),
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = {
                            if (editTitleInput.trim().isNotEmpty()) {
                                onSaveEditedTitle(editTitleInput)
                            }
                        }),
                        maxLines = 4,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("edit_question_title_input")
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (editTitleInput.trim().isNotEmpty()) {
                            onSaveEditedTitle(editTitleInput)
                        }
                    },
                    enabled = editTitleInput.trim().isNotEmpty(),
                    modifier = Modifier.testTag("edit_question_save_button")
                ) {
                    Text("Save", fontWeight = FontWeight.Bold, color = AccentCyan)
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissEditTitleDialog) {
                    Text("Cancel", color = DarkTextSecondary)
                }
            }
        )
    }

    // Dialog: Delete Question Confirmation
    if (questionToDelete != null) {
        AlertDialog(
            onDismissRequest = onDismissDeleteDialog,
            containerColor = DarkSurface,
            title = {
                Text(
                    text = "Delete Question?",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkTextPrimary
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete \"${questionToDelete.questionText}\"? This will permanently remove it for both connected users.",
                    fontSize = 14.sp,
                    color = DarkTextSecondary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = onPerformDelete,
                    modifier = Modifier.testTag("confirm_delete_button")
                ) {
                    Text("Delete", fontWeight = FontWeight.Bold, color = AccentCoral)
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissDeleteDialog) {
                    Text("Cancel", color = DarkTextSecondary)
                }
            }
        )
    }
}

@Composable
private fun QuestionCardItem(
    index: Int,
    question: QuestionItem,
    onCardClick: () -> Unit,
    onEditTitle: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    val formattedTime = remember(question.updatedAt) {
        val sdf = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
        sdf.format(Date(question.updatedAt))
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onCardClick() }
            .testTag("question_card_${question.questionId}"),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row: Question number & Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Question $index",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = AccentCyan,
                    letterSpacing = 0.5.sp
                )

                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier
                            .size(28.dp)
                            .testTag("question_menu_${question.questionId}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Question options",
                            tint = DarkTextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier.background(DarkSurfaceVariant)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit Title", color = DarkTextPrimary) },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = null,
                                    tint = AccentCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            onClick = {
                                showMenu = false
                                onEditTitle()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete", color = AccentCoral) },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = AccentCoral,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            onClick = {
                                showMenu = false
                                onDelete()
                            }
                        )
                    }
                }
            }

            // Question Text
            Text(
                text = question.questionText,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = DarkTextPrimary,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            HorizontalDivider(color = DarkBorder.copy(alpha = 0.5f))

            // Footer Row: Small answer/content indicator & last updated info (NO full answer)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Small answer indicator badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = null,
                        tint = if (question.hasAnswer) AccentGreen else DarkTextMuted,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = if (question.hasAnswer) "Has notes" else "No notes yet",
                        fontSize = 11.sp,
                        color = if (question.hasAnswer) AccentGreen else DarkTextMuted,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Last updated info
                Text(
                    text = "Updated $formattedTime",
                    fontSize = 11.sp,
                    color = DarkTextMuted
                )
            }
        }
    }
}

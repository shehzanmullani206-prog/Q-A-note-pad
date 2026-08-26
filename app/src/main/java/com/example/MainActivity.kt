package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.NameScreen
import com.example.ui.screens.QuestionDetailScreen
import com.example.ui.screens.QuestionListScreen
import com.example.ui.theme.DarkBg
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.Screen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = DarkBg
                ) {
                    QANotesApp()
                }
            }
        }
    }
}

@Composable
fun QANotesApp(
    viewModel: MainViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when (val screen = uiState.currentScreen) {
        is Screen.NameInput -> {
            NameScreen(
                onContinue = { name -> viewModel.saveUserName(name) },
                errorMessage = uiState.errorMessage
            )
        }

        is Screen.Home -> {
            HomeScreen(
                userName = uiState.userName,
                isLoading = uiState.isLoading,
                errorMessage = uiState.errorMessage,
                isOnline = uiState.isOnline,
                onCreateNote = { viewModel.createNote() },
                onJoinNote = { code -> viewModel.joinNote(code) },
                onClearError = { viewModel.clearErrorMessage() }
            )
        }

        is Screen.QuestionList -> {
            BackHandler {
                viewModel.leaveCurrentNote()
            }

            QuestionListScreen(
                currentNote = uiState.currentNote,
                questions = uiState.questions,
                currentUserId = uiState.userId,
                currentUserName = uiState.userName,
                isOnline = uiState.isOnline,
                showAddDialog = uiState.showAddQuestionDialog,
                questionToDelete = uiState.questionToDelete,
                questionToEditTitle = uiState.questionToEditTitle,
                onOpenQuestion = { q -> viewModel.openQuestion(q) },
                onShowAddDialog = { show -> viewModel.showAddQuestionDialog(show) },
                onCreateQuestion = { text -> viewModel.createQuestion(text) },
                onConfirmDelete = { q -> viewModel.confirmDeleteQuestion(q) },
                onDismissDeleteDialog = { viewModel.dismissDeleteDialog() },
                onPerformDelete = { viewModel.deleteConfirmedQuestion() },
                onShowEditTitleDialog = { q -> viewModel.showEditTitleDialog(q) },
                onDismissEditTitleDialog = { viewModel.dismissEditTitleDialog() },
                onSaveEditedTitle = { newTitle -> viewModel.saveEditedTitle(newTitle) },
                onLeaveNote = { viewModel.leaveCurrentNote() }
            )
        }

        is Screen.QuestionDetail -> {
            BackHandler {
                viewModel.closeQuestionDetail()
            }

            QuestionDetailScreen(
                question = uiState.activeQuestion,
                localQuestionText = uiState.localQuestionText,
                localAnswerContent = uiState.localAnswerContent,
                localFormatting = uiState.localFormatting,
                isOnline = uiState.isOnline,
                isSyncing = uiState.isSyncingQuestion,
                onBack = { viewModel.closeQuestionDetail() },
                onQuestionTextChange = { text -> viewModel.onQuestionTextChange(text) },
                onAnswerContentChange = { content -> viewModel.onAnswerContentChange(content) },
                onColorSelected = { colorHex -> viewModel.onTextColorChange(colorHex) },
                onSizeSelected = { sizeSp -> viewModel.onTextSizeChange(sizeSp) }
            )
        }
    }
}

package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.QuestionItem
import com.example.data.model.SharedNote
import com.example.data.model.TextFormatting
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.repository.JoinResult
import com.example.data.repository.NotesRepository
import com.example.data.repository.NotesRepositoryImpl
import com.example.data.util.NetworkMonitor
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed class Screen {
    data object NameInput : Screen()
    data object Home : Screen()
    data object QuestionList : Screen()
    data class QuestionDetail(val questionId: String) : Screen()
}

data class UiState(
    val currentScreen: Screen = Screen.NameInput,
    val userId: String = "",
    val userName: String = "",
    val currentNote: SharedNote? = null,
    val questions: List<QuestionItem> = emptyList(),
    val activeQuestionId: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val infoMessage: String? = null,
    val isOnline: Boolean = true,
    val showAddQuestionDialog: Boolean = false,
    val questionToDelete: QuestionItem? = null,
    val questionToEditTitle: QuestionItem? = null,
    // Live editing fields for current question detail
    val localQuestionText: String = "",
    val localAnswerContent: String = "",
    val localFormatting: TextFormatting = TextFormatting(),
    val isSyncingQuestion: Boolean = false
) {
    val activeQuestion: QuestionItem?
        get() = questions.find { it.questionId == activeQuestionId }

    val otherUser: com.example.data.model.UserInfo?
        get() = currentNote?.users?.values?.firstOrNull { it.userId != userId }

    val isOtherUserOnline: Boolean
        get() = otherUser?.isOnline == true &&
                (System.currentTimeMillis() - (otherUser?.lastSeen ?: 0L)) < 60_000L
}

class MainViewModel @JvmOverloads constructor(
    application: Application,
    private val preferencesRepository: UserPreferencesRepository = UserPreferencesRepository(application),
    private val notesRepository: NotesRepository = NotesRepositoryImpl(),
    private val networkMonitor: NetworkMonitor = NetworkMonitor(application)
) : AndroidViewModel(application) {

    companion object {
        fun Factory(application: Application): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return MainViewModel(application) as T
            }
        }
    }

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private var noteObservationJob: Job? = null
    private var questionsObservationJob: Job? = null
    private var questionDebounceJob: Job? = null
    private var answerDebounceJob: Job? = null
    private var presenceJob: Job? = null

    init {
        // Observe network state safely
        viewModelScope.launch {
            try {
                networkMonitor.isOnline.collect { online ->
                    _uiState.update { it.copy(isOnline = online) }
                    if (online && _uiState.value.currentNote != null) {
                        pingPresence()
                    }
                }
            } catch (e: Throwable) {
                // Log and keep default online state
            }
        }

        // Initialize user preferences
        viewModelScope.launch {
            try {
                val savedUserId = preferencesRepository.getOrCreateUserId()
                val savedName = preferencesRepository.userNameFlow.first()

                _uiState.update {
                    it.copy(
                        userId = savedUserId,
                        userName = savedName,
                        currentScreen = if (savedName.isNotBlank()) Screen.Home else Screen.NameInput
                    )
                }

                // Ensure auth is established safely
                try {
                    notesRepository.ensureAuth()
                } catch (_: Throwable) {}
            } catch (e: Throwable) {
                _uiState.update {
                    it.copy(
                        userId = "usr_fallback",
                        userName = "",
                        currentScreen = Screen.NameInput
                    )
                }
            }
        }

        // Periodic presence heartbeat
        startPresenceHeartbeat()
    }

    private fun startPresenceHeartbeat() {
        presenceJob?.cancel()
        presenceJob = viewModelScope.launch {
            while (true) {
                delay(20_000L)
                pingPresence()
            }
        }
    }

    private suspend fun pingPresence() {
        val note = _uiState.value.currentNote ?: return
        val uid = _uiState.value.userId
        if (note.noteId.isNotBlank() && uid.isNotBlank() && _uiState.value.isOnline) {
            notesRepository.updatePresence(note.noteId, uid, true)
        }
    }

    fun saveUserName(name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Please enter your name") }
            return
        }

        viewModelScope.launch {
            preferencesRepository.saveUserName(trimmed)
            _uiState.update {
                it.copy(
                    userName = trimmed,
                    currentScreen = Screen.Home,
                    errorMessage = null
                )
            }
        }
    }

    fun createNote(title: String = "Collaborative Q&A") {
        val uid = _uiState.value.userId
        val uName = _uiState.value.userName
        if (uid.isEmpty() || uName.isEmpty()) return

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            val result = notesRepository.createNote(uid, uName, title)
            result.onSuccess { note ->
                preferencesRepository.saveLastNote(note.noteId, note.shareCode)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        currentNote = note,
                        currentScreen = Screen.QuestionList
                    )
                }
                observeCurrentNote(note.noteId)
            }.onFailure { err ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = err.localizedMessage ?: "Failed to create note. Please check connection."
                    )
                }
            }
        }
    }

    fun joinNote(shareCode: String) {
        val cleanCode = shareCode.trim().uppercase()
        if (cleanCode.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Please enter a Share Code") }
            return
        }

        val uid = _uiState.value.userId
        val uName = _uiState.value.userName
        if (uid.isEmpty() || uName.isEmpty()) return

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            when (val result = notesRepository.joinNote(cleanCode, uid, uName)) {
                is JoinResult.Success -> {
                    val note = result.note
                    preferencesRepository.saveLastNote(note.noteId, note.shareCode)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            currentNote = note,
                            currentScreen = Screen.QuestionList,
                            errorMessage = null
                        )
                    }
                    observeCurrentNote(note.noteId)
                }
                is JoinResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.message
                        )
                    }
                }
            }
        }
    }

    private fun observeCurrentNote(noteId: String) {
        noteObservationJob?.cancel()
        questionsObservationJob?.cancel()

        // Observe Note Document
        noteObservationJob = viewModelScope.launch {
            notesRepository.observeNote(noteId).collect { note ->
                if (note != null) {
                    _uiState.update { it.copy(currentNote = note) }
                }
            }
        }

        // Observe Questions Collection
        questionsObservationJob = viewModelScope.launch {
            notesRepository.observeQuestions(noteId).collect { questionsList ->
                _uiState.update { currentState ->
                    val activeId = currentState.activeQuestionId
                    val activeItem = questionsList.find { it.questionId == activeId }

                    // If we're currently viewing a question and an update arrived from the other user
                    var updatedQText = currentState.localQuestionText
                    var updatedAnswer = currentState.localAnswerContent
                    var updatedFormat = currentState.localFormatting

                    if (activeItem != null) {
                        // Only override if the other user updated it, or initial load
                        if (activeItem.updatedBy != currentState.userId) {
                            updatedQText = activeItem.questionText
                            updatedAnswer = activeItem.answerContent
                            updatedFormat = activeItem.formatting
                        }
                    }

                    currentState.copy(
                        questions = questionsList,
                        localQuestionText = if (activeId != null && activeItem != null && activeItem.updatedBy != currentState.userId) updatedQText else currentState.localQuestionText,
                        localAnswerContent = if (activeId != null && activeItem != null && activeItem.updatedBy != currentState.userId) updatedAnswer else currentState.localAnswerContent,
                        localFormatting = if (activeId != null && activeItem != null && activeItem.updatedBy != currentState.userId) updatedFormat else currentState.localFormatting
                    )
                }
            }
        }
    }

    fun openQuestion(question: QuestionItem) {
        _uiState.update {
            it.copy(
                activeQuestionId = question.questionId,
                localQuestionText = question.questionText,
                localAnswerContent = question.answerContent,
                localFormatting = question.formatting,
                currentScreen = Screen.QuestionDetail(question.questionId)
            )
        }
    }

    fun closeQuestionDetail() {
        // Flush any pending debounce immediately
        flushPendingQuestionUpdates()
        _uiState.update {
            it.copy(
                activeQuestionId = null,
                currentScreen = Screen.QuestionList
            )
        }
    }

    fun showAddQuestionDialog(show: Boolean) {
        _uiState.update { it.copy(showAddQuestionDialog = show) }
    }

    fun createQuestion(questionText: String) {
        val trimmed = questionText.trim()
        if (trimmed.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Question cannot be empty") }
            return
        }

        val noteId = _uiState.value.currentNote?.noteId ?: return
        val uid = _uiState.value.userId
        val uName = _uiState.value.userName

        viewModelScope.launch {
            _uiState.update { it.copy(showAddQuestionDialog = false, isSyncingQuestion = true) }
            val result = notesRepository.createQuestion(noteId, trimmed, uid, uName)
            result.onFailure { err ->
                _uiState.update {
                    it.copy(errorMessage = "Failed to create question: ${err.localizedMessage}")
                }
            }
            _uiState.update { it.copy(isSyncingQuestion = false) }
        }
    }

    fun onQuestionTextChange(newText: String) {
        _uiState.update { it.copy(localQuestionText = newText) }

        val noteId = _uiState.value.currentNote?.noteId ?: return
        val qId = _uiState.value.activeQuestionId ?: return
        val uid = _uiState.value.userId
        val uName = _uiState.value.userName

        questionDebounceJob?.cancel()
        questionDebounceJob = viewModelScope.launch {
            delay(400L) // 400ms debounce
            notesRepository.updateQuestionText(noteId, qId, newText, uid, uName)
        }
    }

    fun onAnswerContentChange(newContent: String) {
        _uiState.update { it.copy(localAnswerContent = newContent) }

        val noteId = _uiState.value.currentNote?.noteId ?: return
        val qId = _uiState.value.activeQuestionId ?: return
        val uid = _uiState.value.userId
        val uName = _uiState.value.userName

        answerDebounceJob?.cancel()
        answerDebounceJob = viewModelScope.launch {
            delay(400L) // 400ms debounce
            notesRepository.updateAnswerContent(noteId, qId, newContent, uid, uName)
        }
    }

    fun onTextColorChange(colorHex: String) {
        val currentFormat = _uiState.value.localFormatting
        val newFormat = currentFormat.copy(colorHex = colorHex)
        _uiState.update { it.copy(localFormatting = newFormat) }

        val noteId = _uiState.value.currentNote?.noteId ?: return
        val qId = _uiState.value.activeQuestionId ?: return
        val uid = _uiState.value.userId
        val uName = _uiState.value.userName

        viewModelScope.launch {
            notesRepository.updateFormatting(noteId, qId, newFormat, uid, uName)
        }
    }

    fun onTextSizeChange(sizeSp: Int) {
        val currentFormat = _uiState.value.localFormatting
        val newFormat = currentFormat.copy(fontSizeSp = sizeSp)
        _uiState.update { it.copy(localFormatting = newFormat) }

        val noteId = _uiState.value.currentNote?.noteId ?: return
        val qId = _uiState.value.activeQuestionId ?: return
        val uid = _uiState.value.userId
        val uName = _uiState.value.userName

        viewModelScope.launch {
            notesRepository.updateFormatting(noteId, qId, newFormat, uid, uName)
        }
    }

    private fun flushPendingQuestionUpdates() {
        val noteId = _uiState.value.currentNote?.noteId ?: return
        val qId = _uiState.value.activeQuestionId ?: return
        val uid = _uiState.value.userId
        val uName = _uiState.value.userName

        val text = _uiState.value.localQuestionText
        val answer = _uiState.value.localAnswerContent
        val format = _uiState.value.localFormatting

        viewModelScope.launch {
            questionDebounceJob?.cancel()
            answerDebounceJob?.cancel()
            notesRepository.updateQuestionText(noteId, qId, text, uid, uName)
            notesRepository.updateAnswerContent(noteId, qId, answer, uid, uName)
            notesRepository.updateFormatting(noteId, qId, format, uid, uName)
        }
    }

    fun confirmDeleteQuestion(question: QuestionItem) {
        _uiState.update { it.copy(questionToDelete = question) }
    }

    fun dismissDeleteDialog() {
        _uiState.update { it.copy(questionToDelete = null) }
    }

    fun deleteConfirmedQuestion() {
        val question = _uiState.value.questionToDelete ?: return
        val noteId = _uiState.value.currentNote?.noteId ?: return

        _uiState.update { it.copy(questionToDelete = null) }

        viewModelScope.launch {
            val result = notesRepository.deleteQuestion(noteId, question.questionId)
            result.onFailure { err ->
                _uiState.update {
                    it.copy(errorMessage = "Failed to delete question: ${err.localizedMessage}")
                }
            }
        }
    }

    fun showEditTitleDialog(question: QuestionItem) {
        _uiState.update { it.copy(questionToEditTitle = question) }
    }

    fun dismissEditTitleDialog() {
        _uiState.update { it.copy(questionToEditTitle = null) }
    }

    fun saveEditedTitle(newTitle: String) {
        val question = _uiState.value.questionToEditTitle ?: return
        val noteId = _uiState.value.currentNote?.noteId ?: return
        val uid = _uiState.value.userId
        val uName = _uiState.value.userName

        _uiState.update { it.copy(questionToEditTitle = null) }

        viewModelScope.launch {
            notesRepository.updateQuestionText(noteId, question.questionId, newTitle.trim(), uid, uName)
        }
    }

    fun leaveCurrentNote() {
        val noteId = _uiState.value.currentNote?.noteId
        val uid = _uiState.value.userId

        noteObservationJob?.cancel()
        questionsObservationJob?.cancel()

        if (!noteId.isNullOrEmpty() && uid.isNotBlank()) {
            viewModelScope.launch {
                notesRepository.leaveNote(noteId, uid)
                preferencesRepository.clearLastNote()
            }
        }

        _uiState.update {
            it.copy(
                currentNote = null,
                questions = emptyList(),
                activeQuestionId = null,
                currentScreen = Screen.Home
            )
        }
    }

    fun clearErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun setInfoMessage(msg: String?) {
        _uiState.update { it.copy(infoMessage = msg) }
    }

    override fun onCleared() {
        super.onCleared()
        presenceJob?.cancel()
        noteObservationJob?.cancel()
        questionsObservationJob?.cancel()
    }
}

package com.example.data.repository

import com.example.data.model.QuestionItem
import com.example.data.model.SharedNote
import com.example.data.model.TextFormatting
import com.example.data.model.UserInfo
import kotlinx.coroutines.flow.Flow

sealed class JoinResult {
    data class Success(val note: SharedNote) : JoinResult()
    data class Error(val message: String) : JoinResult()
}

interface NotesRepository {
    suspend fun ensureAuth(): String
    suspend fun createNote(userId: String, userName: String, title: String = "Collaborative Q&A"): Result<SharedNote>
    suspend fun joinNote(shareCode: String, userId: String, userName: String): JoinResult
    suspend fun restoreNote(noteId: String, shareCode: String = "", userId: String, userName: String): SharedNote?
    suspend fun getRecentNote(): SharedNote?
    fun observeNote(noteId: String): Flow<SharedNote?>
    fun observeQuestions(noteId: String): Flow<List<QuestionItem>>
    suspend fun createQuestion(
        noteId: String,
        questionText: String,
        userId: String,
        userName: String
    ): Result<String>
    suspend fun updateQuestionText(
        noteId: String,
        questionId: String,
        questionText: String,
        userId: String,
        userName: String
    ): Result<Unit>
    suspend fun updateAnswerContent(
        noteId: String,
        questionId: String,
        answerContent: String,
        userId: String,
        userName: String
    ): Result<Unit>
    suspend fun updateFormatting(
        noteId: String,
        questionId: String,
        formatting: TextFormatting,
        userId: String,
        userName: String
    ): Result<Unit>
    suspend fun deleteQuestion(noteId: String, questionId: String): Result<Unit>
    suspend fun updatePresence(noteId: String, userId: String, isOnline: Boolean): Result<Unit>
    suspend fun leaveNote(noteId: String, userId: String): Result<Unit>
}

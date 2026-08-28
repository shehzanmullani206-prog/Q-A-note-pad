package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface QuestionDao {
    @Query("SELECT * FROM questions WHERE noteId = :noteId ORDER BY createdAt ASC")
    fun getQuestionsFlow(noteId: String): Flow<List<QuestionEntity>>

    @Query("SELECT * FROM questions WHERE noteId = :noteId ORDER BY createdAt ASC")
    suspend fun getQuestionsList(noteId: String): List<QuestionEntity>

    @Query("SELECT * FROM questions WHERE questionId = :questionId LIMIT 1")
    suspend fun getQuestionById(questionId: String): QuestionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestion(question: QuestionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestions(questions: List<QuestionEntity>)

    @Query("UPDATE questions SET questionText = :text, updatedBy = :userId, updatedByName = :userName, updatedAt = :time WHERE questionId = :questionId")
    suspend fun updateQuestionText(questionId: String, text: String, userId: String, userName: String, time: Long = System.currentTimeMillis())

    @Query("UPDATE questions SET answerContent = :content, updatedBy = :userId, updatedByName = :userName, updatedAt = :time WHERE questionId = :questionId")
    suspend fun updateAnswerContent(questionId: String, content: String, userId: String, userName: String, time: Long = System.currentTimeMillis())

    @Query("UPDATE questions SET formattingJson = :formattingJson, updatedBy = :userId, updatedByName = :userName, updatedAt = :time WHERE questionId = :questionId")
    suspend fun updateFormatting(questionId: String, formattingJson: String, userId: String, userName: String, time: Long = System.currentTimeMillis())

    @Query("DELETE FROM questions WHERE questionId = :questionId")
    suspend fun deleteQuestion(questionId: String)

    @Query("DELETE FROM questions WHERE noteId = :noteId")
    suspend fun deleteQuestionsForNote(noteId: String)
}

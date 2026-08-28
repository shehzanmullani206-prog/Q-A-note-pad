package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes WHERE noteId = :noteId LIMIT 1")
    fun getNoteFlow(noteId: String): Flow<NoteEntity?>

    @Query("SELECT * FROM notes WHERE noteId = :noteId LIMIT 1")
    suspend fun getNoteById(noteId: String): NoteEntity?

    @Query("SELECT * FROM notes WHERE UPPER(shareCode) = UPPER(:shareCode) LIMIT 1")
    suspend fun getNoteByShareCode(shareCode: String): NoteEntity?

    @Query("SELECT * FROM notes ORDER BY lastAccessedAt DESC LIMIT 1")
    suspend fun getMostRecentNote(): NoteEntity?

    @Query("SELECT * FROM notes ORDER BY lastAccessedAt DESC")
    fun getAllNotesFlow(): Flow<List<NoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity)

    @Query("UPDATE notes SET lastAccessedAt = :time WHERE noteId = :noteId")
    suspend fun updateLastAccessed(noteId: String, time: Long = System.currentTimeMillis())

    @Query("DELETE FROM notes WHERE noteId = :noteId")
    suspend fun deleteNote(noteId: String)
}

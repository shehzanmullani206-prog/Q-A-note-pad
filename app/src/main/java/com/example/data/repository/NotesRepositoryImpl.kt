package com.example.data.repository

import com.example.data.model.QuestionItem
import com.example.data.model.SharedNote
import com.example.data.model.TextFormatting
import com.example.data.model.UserInfo
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import java.security.SecureRandom
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

// Extension to await Google Play Tasks without extra dependencies
suspend fun <T> Task<T>.awaitTask(): T = suspendCancellableCoroutine { cont ->
    addOnSuccessListener { result ->
        if (cont.isActive) cont.resume(result)
    }
    addOnFailureListener { exception ->
        if (cont.isActive) cont.resumeWithException(exception)
    }
    addOnCanceledListener {
        if (cont.isActive) cont.cancel()
    }
}

class NotesRepositoryImpl(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : NotesRepository {

    init {
        try {
            val settings = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build()
            firestore.firestoreSettings = settings
        } catch (_: Exception) {
            // Settings already set or default
        }
    }

    private val notesCollection = firestore.collection("sharedNotes")

    override suspend fun ensureAuth(): String {
        return try {
            val currentUser = auth.currentUser
            if (currentUser != null) {
                currentUser.uid
            } else {
                val authResult = auth.signInAnonymously().awaitTask()
                authResult.user?.uid ?: "user_anon"
            }
        } catch (e: Exception) {
            // Fallback gracefully if auth not enabled or offline
            auth.currentUser?.uid ?: "user_local"
        }
    }

    private fun generateShareCode(): String {
        val allowedChars = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ"
        val random = SecureRandom()
        return (1..6)
            .map { allowedChars[random.nextInt(allowedChars.length)] }
            .joinToString("")
    }

    override suspend fun createNote(
        userId: String,
        userName: String,
        title: String
    ): Result<SharedNote> {
        return try {
            ensureAuth()
            val noteDoc = notesCollection.document()
            val noteId = noteDoc.id
            val shareCode = generateShareCode()
            val now = System.currentTimeMillis()

            val creator = UserInfo(
                userId = userId,
                name = userName,
                joinedAt = now,
                lastSeen = now,
                isOnline = true
            )

            val noteData = hashMapOf(
                "noteId" to noteId,
                "shareCode" to shareCode,
                "title" to title,
                "createdAt" to now,
                "updatedAt" to now,
                "users" to mapOf(userId to creator.toMap())
            )

            noteDoc.set(noteData).awaitTask()

            val sharedNote = SharedNote(
                noteId = noteId,
                shareCode = shareCode,
                createdAt = now,
                updatedAt = now,
                users = mapOf(userId to creator),
                title = title
            )
            Result.success(sharedNote)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun joinNote(
        shareCode: String,
        userId: String,
        userName: String
    ): JoinResult {
        return try {
            ensureAuth()
            val cleanCode = shareCode.trim().uppercase()
            if (cleanCode.isEmpty()) {
                return JoinResult.Error("Please enter a Share Code")
            }

            val querySnapshot = notesCollection
                .whereEqualTo("shareCode", cleanCode)
                .limit(1)
                .get()
                .awaitTask()

            if (querySnapshot.isEmpty) {
                return JoinResult.Error("No shared note found with code: $cleanCode")
            }

            val doc = querySnapshot.documents[0]
            val note = SharedNote.fromDocument(doc)

            // Check 2 users constraint
            val isAlreadyMember = note.users.containsKey(userId)
            if (!isAlreadyMember && note.users.size >= 2) {
                return JoinResult.Error("This note already has 2 connected users.")
            }

            val now = System.currentTimeMillis()
            val userInfo = UserInfo(
                userId = userId,
                name = userName,
                joinedAt = if (isAlreadyMember) (note.users[userId]?.joinedAt ?: now) else now,
                lastSeen = now,
                isOnline = true
            )

            // Update user in note document
            doc.reference.update(
                mapOf(
                    "users.$userId" to userInfo.toMap(),
                    "updatedAt" to now
                )
            ).awaitTask()

            val updatedUsers = note.users.toMutableMap()
            updatedUsers[userId] = userInfo
            JoinResult.Success(note.copy(users = updatedUsers, updatedAt = now))
        } catch (e: Exception) {
            JoinResult.Error(e.localizedMessage ?: "Failed to join note")
        }
    }

    override fun observeNote(noteId: String): Flow<SharedNote?> = callbackFlow {
        if (noteId.isEmpty()) {
            trySend(null)
            close()
            return@callbackFlow
        }

        val listener: ListenerRegistration = notesCollection.document(noteId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    // Provide current or null on error without breaking flow
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    trySend(SharedNote.fromDocument(snapshot))
                } else {
                    trySend(null)
                }
            }

        awaitClose {
            listener.remove()
        }
    }

    override fun observeQuestions(noteId: String): Flow<List<QuestionItem>> = callbackFlow {
        if (noteId.isEmpty()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener: ListenerRegistration = notesCollection.document(noteId)
            .collection("questions")
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val questions = snapshot.documents.map { doc ->
                        QuestionItem.fromDocument(doc)
                    }
                    trySend(questions)
                }
            }

        awaitClose {
            listener.remove()
        }
    }

    override suspend fun createQuestion(
        noteId: String,
        questionText: String,
        userId: String,
        userName: String
    ): Result<String> {
        return try {
            val questionsCol = notesCollection.document(noteId).collection("questions")
            val newDoc = questionsCol.document()
            val now = System.currentTimeMillis()

            val data = hashMapOf(
                "questionId" to newDoc.id,
                "questionText" to questionText.trim(),
                "answerContent" to "",
                "formatting" to TextFormatting().toMap(),
                "createdBy" to userId,
                "createdByName" to userName,
                "updatedBy" to userId,
                "updatedByName" to userName,
                "createdAt" to now,
                "updatedAt" to now
            )

            newDoc.set(data).awaitTask()

            // Update parent note timestamp
            notesCollection.document(noteId).update("updatedAt", now)

            Result.success(newDoc.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateQuestionText(
        noteId: String,
        questionId: String,
        questionText: String,
        userId: String,
        userName: String
    ): Result<Unit> {
        return try {
            val now = System.currentTimeMillis()
            notesCollection.document(noteId)
                .collection("questions")
                .document(questionId)
                .update(
                    mapOf(
                        "questionText" to questionText,
                        "updatedBy" to userId,
                        "updatedByName" to userName,
                        "updatedAt" to now
                    )
                ).awaitTask()

            notesCollection.document(noteId).update("updatedAt", now)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateAnswerContent(
        noteId: String,
        questionId: String,
        answerContent: String,
        userId: String,
        userName: String
    ): Result<Unit> {
        return try {
            val now = System.currentTimeMillis()
            notesCollection.document(noteId)
                .collection("questions")
                .document(questionId)
                .update(
                    mapOf(
                        "answerContent" to answerContent,
                        "updatedBy" to userId,
                        "updatedByName" to userName,
                        "updatedAt" to now
                    )
                ).awaitTask()

            notesCollection.document(noteId).update("updatedAt", now)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateFormatting(
        noteId: String,
        questionId: String,
        formatting: TextFormatting,
        userId: String,
        userName: String
    ): Result<Unit> {
        return try {
            val now = System.currentTimeMillis()
            notesCollection.document(noteId)
                .collection("questions")
                .document(questionId)
                .update(
                    mapOf(
                        "formatting" to formatting.toMap(),
                        "updatedBy" to userId,
                        "updatedByName" to userName,
                        "updatedAt" to now
                    )
                ).awaitTask()

            notesCollection.document(noteId).update("updatedAt", now)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteQuestion(noteId: String, questionId: String): Result<Unit> {
        return try {
            val now = System.currentTimeMillis()
            notesCollection.document(noteId)
                .collection("questions")
                .document(questionId)
                .delete()
                .awaitTask()

            notesCollection.document(noteId).update("updatedAt", now)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updatePresence(
        noteId: String,
        userId: String,
        isOnline: Boolean
    ): Result<Unit> {
        if (noteId.isEmpty() || userId.isEmpty()) return Result.success(Unit)
        return try {
            val now = System.currentTimeMillis()
            notesCollection.document(noteId).update(
                mapOf(
                    "users.$userId.lastSeen" to now,
                    "users.$userId.isOnline" to isOnline
                )
            ).awaitTask()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun leaveNote(noteId: String, userId: String): Result<Unit> {
        if (noteId.isEmpty() || userId.isEmpty()) return Result.success(Unit)
        return try {
            val now = System.currentTimeMillis()
            notesCollection.document(noteId).update(
                mapOf(
                    "users.$userId.isOnline" to false,
                    "users.$userId.lastSeen" to now
                )
            ).awaitTask()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

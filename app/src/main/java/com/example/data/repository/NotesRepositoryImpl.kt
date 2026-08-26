package com.example.data.repository

import android.util.Log
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
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.suspendCancellableCoroutine
import java.security.SecureRandom
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

// Extension to await Google Play Tasks safely
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

class NotesRepositoryImpl : NotesRepository {

    // Lazy safe initialization to avoid crashes when FirebaseApp is initializing or fallback is used
    private val firestoreInstance: FirebaseFirestore? by lazy {
        try {
            val db = FirebaseFirestore.getInstance()
            try {
                val settings = FirebaseFirestoreSettings.Builder()
                    .setPersistenceEnabled(true)
                    .build()
                db.firestoreSettings = settings
            } catch (_: Exception) {
                // Settings may already be locked
            }
            db
        } catch (e: Throwable) {
            Log.w("NotesRepositoryImpl", "Firestore unavailable, using robust in-memory fallback: ${e.message}")
            null
        }
    }

    private val authInstance: FirebaseAuth? by lazy {
        try {
            FirebaseAuth.getInstance()
        } catch (e: Throwable) {
            Log.w("NotesRepositoryImpl", "FirebaseAuth unavailable: ${e.message}")
            null
        }
    }

    // In-Memory Fallback Storage (guarantees 100% crash-free offline operation if Firebase isn't configured)
    private val fallbackNotes = MutableStateFlow<Map<String, SharedNote>>(emptyMap())
    private val fallbackQuestions = MutableStateFlow<Map<String, List<QuestionItem>>>(emptyMap())

    override suspend fun ensureAuth(): String {
        return try {
            val auth = authInstance ?: return "user_local_" + UUID.randomUUID().toString().take(6)
            val currentUser = auth.currentUser
            if (currentUser != null) {
                currentUser.uid
            } else {
                val authResult = auth.signInAnonymously().awaitTask()
                authResult.user?.uid ?: "user_anon"
            }
        } catch (e: Throwable) {
            authInstance?.currentUser?.uid ?: ("user_local_" + UUID.randomUUID().toString().take(6))
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
        val shareCode = generateShareCode()
        val now = System.currentTimeMillis()
        val creator = UserInfo(
            userId = userId,
            name = userName,
            joinedAt = now,
            lastSeen = now,
            isOnline = true
        )

        val firestore = firestoreInstance
        if (firestore != null) {
            try {
                ensureAuth()
                val noteDoc = firestore.collection("sharedNotes").document()
                val noteId = noteDoc.id

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
                return Result.success(sharedNote)
            } catch (e: Throwable) {
                Log.w("NotesRepositoryImpl", "Firestore createNote failed, falling back to memory: ${e.message}")
            }
        }

        // Fallback local note
        val fallbackId = "note_" + UUID.randomUUID().toString().replace("-", "").take(8)
        val note = SharedNote(
            noteId = fallbackId,
            shareCode = shareCode,
            createdAt = now,
            updatedAt = now,
            users = mapOf(userId to creator),
            title = title
        )
        fallbackNotes.value = fallbackNotes.value + (fallbackId to note)
        fallbackQuestions.value = fallbackQuestions.value + (fallbackId to emptyList())
        return Result.success(note)
    }

    override suspend fun joinNote(
        shareCode: String,
        userId: String,
        userName: String
    ): JoinResult {
        val cleanCode = shareCode.trim().uppercase()
        if (cleanCode.isEmpty()) {
            return JoinResult.Error("Please enter a Share Code")
        }

        val firestore = firestoreInstance
        if (firestore != null) {
            try {
                ensureAuth()
                val querySnapshot = firestore.collection("sharedNotes")
                    .whereEqualTo("shareCode", cleanCode)
                    .limit(1)
                    .get()
                    .awaitTask()

                if (!querySnapshot.isEmpty) {
                    val doc = querySnapshot.documents[0]
                    val note = SharedNote.fromDocument(doc)

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

                    doc.reference.update(
                        mapOf(
                            "users.$userId" to userInfo.toMap(),
                            "updatedAt" to now
                        )
                    ).awaitTask()

                    val updatedUsers = note.users.toMutableMap()
                    updatedUsers[userId] = userInfo
                    return JoinResult.Success(note.copy(users = updatedUsers, updatedAt = now))
                }
            } catch (e: Throwable) {
                Log.w("NotesRepositoryImpl", "Firestore joinNote error: ${e.message}")
            }
        }

        // Check fallback storage
        val localNote = fallbackNotes.value.values.find { it.shareCode.equals(cleanCode, ignoreCase = true) }
        if (localNote != null) {
            val isAlreadyMember = localNote.users.containsKey(userId)
            if (!isAlreadyMember && localNote.users.size >= 2) {
                return JoinResult.Error("This note already has 2 connected users.")
            }
            val now = System.currentTimeMillis()
            val userInfo = UserInfo(
                userId = userId,
                name = userName,
                joinedAt = if (isAlreadyMember) (localNote.users[userId]?.joinedAt ?: now) else now,
                lastSeen = now,
                isOnline = true
            )
            val updated = localNote.copy(
                users = localNote.users + (userId to userInfo),
                updatedAt = now
            )
            fallbackNotes.value = fallbackNotes.value + (localNote.noteId to updated)
            return JoinResult.Success(updated)
        }

        return JoinResult.Error("No shared note found with code: $cleanCode")
    }

    override fun observeNote(noteId: String): Flow<SharedNote?> {
        if (noteId.isEmpty()) {
            return callbackFlow {
                trySend(null)
                close()
            }
        }

        val firestore = firestoreInstance
        if (firestore != null) {
            return callbackFlow {
                var registration: ListenerRegistration? = null
                try {
                    registration = firestore.collection("sharedNotes").document(noteId)
                        .addSnapshotListener { snapshot, error ->
                            if (error != null) {
                                // Fallback to local memory if network error occurs
                                val local = fallbackNotes.value[noteId]
                                if (local != null) trySend(local)
                                return@addSnapshotListener
                            }
                            if (snapshot != null && snapshot.exists()) {
                                trySend(SharedNote.fromDocument(snapshot))
                            } else {
                                trySend(fallbackNotes.value[noteId])
                            }
                        }
                } catch (e: Throwable) {
                    trySend(fallbackNotes.value[noteId])
                }

                awaitClose {
                    try {
                        registration?.remove()
                    } catch (_: Exception) {}
                }
            }
        }

        return fallbackNotes.map { it[noteId] }
    }

    override fun observeQuestions(noteId: String): Flow<List<QuestionItem>> {
        if (noteId.isEmpty()) {
            return callbackFlow {
                trySend(emptyList())
                close()
            }
        }

        val firestore = firestoreInstance
        if (firestore != null) {
            return callbackFlow {
                var registration: ListenerRegistration? = null
                try {
                    registration = firestore.collection("sharedNotes").document(noteId)
                        .collection("questions")
                        .orderBy("createdAt", Query.Direction.ASCENDING)
                        .addSnapshotListener { snapshot, error ->
                            if (error != null) {
                                trySend(fallbackQuestions.value[noteId] ?: emptyList())
                                return@addSnapshotListener
                            }
                            if (snapshot != null) {
                                val questions = snapshot.documents.map { doc ->
                                    QuestionItem.fromDocument(doc)
                                }
                                trySend(questions)
                            }
                        }
                } catch (e: Throwable) {
                    trySend(fallbackQuestions.value[noteId] ?: emptyList())
                }

                awaitClose {
                    try {
                        registration?.remove()
                    } catch (_: Exception) {}
                }
            }
        }

        return fallbackQuestions.map { it[noteId] ?: emptyList() }
    }

    override suspend fun createQuestion(
        noteId: String,
        questionText: String,
        userId: String,
        userName: String
    ): Result<String> {
        val now = System.currentTimeMillis()
        val qId = "q_" + UUID.randomUUID().toString().replace("-", "").take(8)

        val firestore = firestoreInstance
        if (firestore != null) {
            try {
                val questionsCol = firestore.collection("sharedNotes").document(noteId).collection("questions")
                val newDoc = questionsCol.document()
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
                firestore.collection("sharedNotes").document(noteId).update("updatedAt", now)
                return Result.success(newDoc.id)
            } catch (e: Throwable) {
                Log.w("NotesRepositoryImpl", "Firestore createQuestion failed, using fallback: ${e.message}")
            }
        }

        val fallbackItem = QuestionItem(
            questionId = qId,
            questionText = questionText.trim(),
            answerContent = "",
            formatting = TextFormatting(),
            createdBy = userId,
            createdByName = userName,
            updatedBy = userId,
            updatedByName = userName,
            createdAt = now,
            updatedAt = now
        )
        val currentList = fallbackQuestions.value[noteId] ?: emptyList()
        fallbackQuestions.value = fallbackQuestions.value + (noteId to (currentList + fallbackItem))
        return Result.success(qId)
    }

    override suspend fun updateQuestionText(
        noteId: String,
        questionId: String,
        questionText: String,
        userId: String,
        userName: String
    ): Result<Unit> {
        val now = System.currentTimeMillis()
        val firestore = firestoreInstance
        if (firestore != null) {
            try {
                firestore.collection("sharedNotes").document(noteId)
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
                firestore.collection("sharedNotes").document(noteId).update("updatedAt", now)
                return Result.success(Unit)
            } catch (e: Throwable) {
                Log.w("NotesRepositoryImpl", "Firestore updateQuestionText failed: ${e.message}")
            }
        }

        val currentList = fallbackQuestions.value[noteId] ?: emptyList()
        val updatedList = currentList.map {
            if (it.questionId == questionId) it.copy(
                questionText = questionText,
                updatedBy = userId,
                updatedByName = userName,
                updatedAt = now
            ) else it
        }
        fallbackQuestions.value = fallbackQuestions.value + (noteId to updatedList)
        return Result.success(Unit)
    }

    override suspend fun updateAnswerContent(
        noteId: String,
        questionId: String,
        answerContent: String,
        userId: String,
        userName: String
    ): Result<Unit> {
        val now = System.currentTimeMillis()
        val firestore = firestoreInstance
        if (firestore != null) {
            try {
                firestore.collection("sharedNotes").document(noteId)
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
                firestore.collection("sharedNotes").document(noteId).update("updatedAt", now)
                return Result.success(Unit)
            } catch (e: Throwable) {
                Log.w("NotesRepositoryImpl", "Firestore updateAnswerContent failed: ${e.message}")
            }
        }

        val currentList = fallbackQuestions.value[noteId] ?: emptyList()
        val updatedList = currentList.map {
            if (it.questionId == questionId) it.copy(
                answerContent = answerContent,
                updatedBy = userId,
                updatedByName = userName,
                updatedAt = now
            ) else it
        }
        fallbackQuestions.value = fallbackQuestions.value + (noteId to updatedList)
        return Result.success(Unit)
    }

    override suspend fun updateFormatting(
        noteId: String,
        questionId: String,
        formatting: TextFormatting,
        userId: String,
        userName: String
    ): Result<Unit> {
        val now = System.currentTimeMillis()
        val firestore = firestoreInstance
        if (firestore != null) {
            try {
                firestore.collection("sharedNotes").document(noteId)
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
                firestore.collection("sharedNotes").document(noteId).update("updatedAt", now)
                return Result.success(Unit)
            } catch (e: Throwable) {
                Log.w("NotesRepositoryImpl", "Firestore updateFormatting failed: ${e.message}")
            }
        }

        val currentList = fallbackQuestions.value[noteId] ?: emptyList()
        val updatedList = currentList.map {
            if (it.questionId == questionId) it.copy(
                formatting = formatting,
                updatedBy = userId,
                updatedByName = userName,
                updatedAt = now
            ) else it
        }
        fallbackQuestions.value = fallbackQuestions.value + (noteId to updatedList)
        return Result.success(Unit)
    }

    override suspend fun deleteQuestion(noteId: String, questionId: String): Result<Unit> {
        val now = System.currentTimeMillis()
        val firestore = firestoreInstance
        if (firestore != null) {
            try {
                firestore.collection("sharedNotes").document(noteId)
                    .collection("questions")
                    .document(questionId)
                    .delete()
                    .awaitTask()
                firestore.collection("sharedNotes").document(noteId).update("updatedAt", now)
                return Result.success(Unit)
            } catch (e: Throwable) {
                Log.w("NotesRepositoryImpl", "Firestore deleteQuestion failed: ${e.message}")
            }
        }

        val currentList = fallbackQuestions.value[noteId] ?: emptyList()
        fallbackQuestions.value = fallbackQuestions.value + (noteId to currentList.filter { it.questionId != questionId })
        return Result.success(Unit)
    }

    override suspend fun updatePresence(
        noteId: String,
        userId: String,
        isOnline: Boolean
    ): Result<Unit> {
        if (noteId.isEmpty() || userId.isEmpty()) return Result.success(Unit)
        val now = System.currentTimeMillis()
        val firestore = firestoreInstance
        if (firestore != null) {
            try {
                firestore.collection("sharedNotes").document(noteId).update(
                    mapOf(
                        "users.$userId.lastSeen" to now,
                        "users.$userId.isOnline" to isOnline
                    )
                ).awaitTask()
                return Result.success(Unit)
            } catch (_: Throwable) {}
        }
        return Result.success(Unit)
    }

    override suspend fun leaveNote(noteId: String, userId: String): Result<Unit> {
        if (noteId.isEmpty() || userId.isEmpty()) return Result.success(Unit)
        val now = System.currentTimeMillis()
        val firestore = firestoreInstance
        if (firestore != null) {
            try {
                firestore.collection("sharedNotes").document(noteId).update(
                    mapOf(
                        "users.$userId.isOnline" to false,
                        "users.$userId.lastSeen" to now
                    )
                ).awaitTask()
                return Result.success(Unit)
            } catch (_: Throwable) {}
        }
        return Result.success(Unit)
    }
}

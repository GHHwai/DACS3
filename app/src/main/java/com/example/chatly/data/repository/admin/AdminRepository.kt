package com.example.chatly.data.repository.admin

import com.example.chatly.data.model.User
import com.example.chatly.data.model.admin.*
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow

class AdminRepository {
    private val firestore = FirebaseFirestore.getInstance()

    // 1. Quản lý Users
    fun getAllUsers(): Flow<List<User>> = callbackFlow {
        val listener = firestore.collection("users").addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val users = snapshot?.documents?.mapNotNull { it.toObject(User::class.java) } ?: emptyList()
            trySend(users)
        }
        awaitClose { listener.remove() }
    }

    suspend fun updateUserStatus(userId: String, status: String) {
        firestore.collection("users").document(userId).update("status", status).await()
    }

    // 2. Quản lý System Data (Môn học, lịch học, lịch thi)
    fun getSystemData(type: String): Flow<List<SystemData>> = callbackFlow {
        val listener = firestore.collection("system_data").whereEqualTo("type", type)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val data = snapshot?.documents?.mapNotNull { it.toObject(SystemData::class.java)?.copy(id = it.id) } ?: emptyList()
                trySend(data)
            }
        awaitClose { listener.remove() }
    }

    suspend fun addSystemData(data: SystemData) {
        firestore.collection("system_data").add(data).await()
    }

    suspend fun deleteSystemData(id: String) {
        firestore.collection("system_data").document(id).delete().await()
    }

    // 3. Quản lý Documents
    fun getDocuments(): Flow<List<Document>> = callbackFlow {
        val listener = firestore.collection("documents").addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val docs = snapshot?.documents?.mapNotNull { it.toObject(Document::class.java)?.copy(id = it.id) } ?: emptyList()
            trySend(docs)
        }
        awaitClose { listener.remove() }
    }

    suspend fun updateDocumentStatus(id: String, status: String) {
        firestore.collection("documents").document(id).update("status", status).await()
    }

    suspend fun deleteDocument(id: String) {
        firestore.collection("documents").document(id).delete().await()
    }

    // 4. Chatbot Logs
    fun getChatbotLogs(): Flow<List<ChatbotLog>> = callbackFlow {
        val listener = firestore.collection("chatbot_logs").addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val logs = snapshot?.documents?.mapNotNull { it.toObject(ChatbotLog::class.java)?.copy(id = it.id) } ?: emptyList()
            trySend(logs)
        }
        awaitClose { listener.remove() }
    }

    // 5. Reports
    fun getReports(): Flow<List<Report>> = callbackFlow {
        val listener = firestore.collection("reports").addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val reports = snapshot?.documents?.mapNotNull { it.toObject(Report::class.java)?.copy(id = it.id) } ?: emptyList()
            trySend(reports)
        }
        awaitClose { listener.remove() }
    }
    
    suspend fun resolveReport(id: String) {
        firestore.collection("reports").document(id).update("status", "resolved").await()
    }

    // 6. Stats
    fun getSystemStats(): Flow<SystemStats> = flow {
        // Simplified query for demo purposes, in production this should be computed via Cloud Functions
        val users = firestore.collection("users").get().await().size()
        val activeUsers = firestore.collection("users").whereEqualTo("status", "active").get().await().size()
        val documents = firestore.collection("documents").get().await().size()
        val chatbotRequests = firestore.collection("chatbot_logs").get().await().size()
        val reports = firestore.collection("reports").get().await().size()

        emit(SystemStats(users, activeUsers, documents, chatbotRequests, reports))
    }
}

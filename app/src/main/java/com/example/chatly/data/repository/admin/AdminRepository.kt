package com.example.chatly.data.repository.admin

import com.example.chatly.data.model.User
import com.example.chatly.data.model.StudySchedule
import com.example.chatly.data.model.ExamSchedule
import com.example.chatly.data.model.admin.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
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

    // 2. Quản lý System Data (Gộp dữ liệu từ tất cả Users)
    fun getSystemData(type: String): Flow<List<SystemData>> = callbackFlow {
        val query = when (type) {
            "schedule" -> firestore.collectionGroup("study_schedules")
            "exam" -> firestore.collectionGroup("exam_schedules")
            else -> firestore.collection("system_data").whereEqualTo("type", type)
        }

        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            
            val data = snapshot?.documents?.mapNotNull { doc ->
                when (type) {
                    "schedule" -> {
                        val schedule = doc.toObject(StudySchedule::class.java)
                        schedule?.let {
                            SystemData(
                                id = doc.reference.path, // Lưu lại toàn bộ path để xóa cho đúng
                                type = "schedule",
                                name = it.subject,
                                description = "Room: ${it.room} - Teacher: ${it.teacher}",
                                date = it.dayOfWeek
                            )
                        }
                    }
                    "exam" -> {
                        val exam = doc.toObject(ExamSchedule::class.java)
                        exam?.let {
                            SystemData(
                                id = doc.reference.path,
                                type = "exam",
                                name = it.subject,
                                description = "Note: ${it.note}",
                                date = it.examDate
                            )
                        }
                    }
                    else -> doc.toObject(SystemData::class.java)?.copy(id = doc.id)
                }
            } ?: emptyList()
            
            trySend(data)
        }
        awaitClose { listener.remove() }
    }

    suspend fun addSystemData(data: SystemData) {
        // Mặc định thêm vào system_data chung
        firestore.collection("system_data").add(data).await()
    }

    suspend fun deleteSystemData(pathOrId: String) {
        if (pathOrId.contains("/")) {
            // Nếu là path (từ user collection), xóa theo path
            firestore.document(pathOrId).delete().await()
        } else {
            // Nếu là ID đơn thuần (từ system_data)
            firestore.collection("system_data").document(pathOrId).delete().await()
        }
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

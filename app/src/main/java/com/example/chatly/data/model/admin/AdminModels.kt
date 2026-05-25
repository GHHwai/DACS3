package com.example.chatly.data.model.admin

data class SystemData(
    val id: String = "",
    val type: String = "", // "subject", "schedule", "exam"
    val name: String = "",
    val description: String = "",
    val date: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

data class Document(
    val id: String = "",
    val title: String = "",
    val url: String = "",
    val uploaderId: String = "",
    val status: String = "pending", // "pending", "approved", "rejected"
    val createdAt: Long = System.currentTimeMillis()
)

data class ChatbotLog(
    val id: String = "",
    val userId: String = "",
    val query: String = "",
    val response: String = "",
    val status: String = "success", // "success", "error"
    val errorMessage: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

data class Report(
    val id: String = "",
    val reporterId: String = "",
    val reportedUserId: String = "",
    val reason: String = "",
    val status: String = "pending", // "pending", "resolved"
    val timestamp: Long = System.currentTimeMillis()
)

data class SystemStats(
    val totalUsers: Int = 0,
    val activeUsers: Int = 0,
    val totalDocuments: Int = 0,
    val totalChatbotRequests: Int = 0,
    val totalReports: Int = 0
)

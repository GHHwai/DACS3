package com.example.chatly.data.repository

import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.chatly.data.model.AiChatMessage
import com.example.chatly.data.model.admin.ChatbotLog

class FirebaseAiChatRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    // Lưu lịch sử chat (cho session hiện tại)
    private val chatHistory = mutableListOf<AiChatMessage>()

    // Prompt hệ thống
    private val systemPrompt = """
        Bạn là một trợ lý học tập thông minh và thân thiện trong ứng dụng Chatly.
        Nhiệm vụ:
        1. Trả lời bằng tiếng Việt một cách tự nhiên.
        2. Hỗ trợ học tập: bài tập, lý thuyết, code.
        3. Trình bày công thức/code bằng Markdown.
        4. Khích lệ người dùng học tập.
        5. Không trả lời nội dung độc hại hoặc vi phạm pháp luật.
    """.trimIndent()

    // Model Firebase AI
    private val model by lazy {
        Firebase.ai(backend = GenerativeBackend.googleAI())
            .generativeModel("gemini-1.5-flash")
    }

    suspend fun getAiResponse(userMessage: String): Result<String> {
        val userId = auth.currentUser?.uid ?: "anonymous"
        return try {
            // Ghép prompt + lịch sử hội thoại
            val historyText = chatHistory.joinToString("\n") { msg ->
                if (msg.isMine) "User: ${msg.content}" else "AI: ${msg.content}"
            }

            val finalPrompt = "$systemPrompt\n$historyText\nUser: $userMessage\nAI:"

            // Gọi Firebase AI
            val response = model.generateContent(finalPrompt)
            val aiText = response.text ?: "AI không trả lời được"

            // Cập nhật lịch sử session
            chatHistory.add(AiChatMessage(content = userMessage, isMine = true))
            chatHistory.add(AiChatMessage(content = aiText, isMine = false))

            // Lưu log vào Firestore cho Admin
            val log = ChatbotLog(
                userId = userId,
                query = userMessage,
                response = aiText,
                status = "success",
                timestamp = System.currentTimeMillis()
            )
            firestore.collection("chatbot_logs").add(log)

            Result.success(aiText)
        } catch (e: Exception) {
            e.printStackTrace()
            // Log lỗi vào Firestore
            val errorLog = ChatbotLog(
                userId = userId,
                query = userMessage,
                response = "Error",
                status = "error",
                errorMessage = e.localizedMessage,
                timestamp = System.currentTimeMillis()
            )
            firestore.collection("chatbot_logs").add(errorLog)
            Result.failure(e)
        }
    }

    fun clearHistory() {
        chatHistory.clear()
    }

    fun getHistory(): List<AiChatMessage> = chatHistory.toList()
}
package com.example.chatly.data.repository

import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.example.chatly.data.model.AiChatMessage

class FirebaseAiChatRepository {

    // Lưu lịch sử chat
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
            .generativeModel("gemini-3-flash-preview")
    }

    suspend fun getAiResponse(userMessage: String): Result<String> {
        return try {
            // Ghép prompt + lịch sử hội thoại
            val historyText = chatHistory.joinToString("\n") { msg ->
                if (msg.isMine) "User: ${msg.content}" else "AI: ${msg.content}"
            }

            val finalPrompt = "$systemPrompt\n$historyText\nUser: $userMessage\nAI:"

            // Gọi Firebase AI
            val response = model.generateContent(finalPrompt)
            val aiText = response.text ?: "AI không trả lời được"

            // Cập nhật lịch sử
            chatHistory.add(AiChatMessage(content = userMessage, isMine = true))
            chatHistory.add(AiChatMessage(content = aiText, isMine = false))

            Result.success(aiText)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    fun clearHistory() {
        chatHistory.clear()
    }

    fun getHistory(): List<AiChatMessage> = chatHistory.toList()
}
package com.example.chatly.data.repository

import com.example.chatly.data.model.AiChatMessage
import com.example.chatly.data.model.Content
import com.example.chatly.data.model.GeminiRequest
import com.example.chatly.data.model.Part
import com.example.chatly.data.remote.GeminiApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

class AiChatRepository(private val apiService: GeminiApiService) {

    // Keep track of the conversation context
    private val chatHistory = mutableListOf<Content>()

    private val systemPrompt = """
        Bạn là một trợ lý học tập thông minh và thân thiện tích hợp trong ứng dụng Chatly.
        Nhiệm vụ của bạn:
        1. Trả lời bằng tiếng Việt một cách tự nhiên, dễ hiểu.
        2. Hỗ trợ giải bài tập, giải thích các khái niệm toán học, lý, hóa, văn học...
        3. Hỗ trợ viết code và giải thích code một cách chi tiết.
        4. Trình bày các công thức toán học hoặc đoạn code bằng định dạng Markdown.
        5. Luôn khích lệ người dùng học tập.
        6. Tuyệt đối không trả lời các nội dung độc hại, không phù hợp với lứa tuổi học sinh hoặc vi phạm pháp luật.
    """.trimIndent()

    suspend fun getAiResponse(userMessage: String, apiKey: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            // Build the request including history for context
            val historyToSend = chatHistory.takeLast(10)
            
            val request = GeminiRequest(
                contents = historyToSend + Content(
                    role = AiChatMessage.ROLE_USER,
                    parts = listOf(Part(text = userMessage))
                ),
                systemInstruction = Content(
                    role = "user", // Some versions of Gemini require "user" for system instructions if not supported as "system"
                    parts = listOf(Part(text = systemPrompt))
                )
            )

            val response = apiService.generateContent(apiKey, request)

            if (response.isSuccessful) {
                val body = response.body()
                val aiText = body?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                if (aiText != null) {
                    // Update history locally
                    chatHistory.add(Content(role = AiChatMessage.ROLE_USER, parts = listOf(Part(text = userMessage))))
                    chatHistory.add(Content(role = AiChatMessage.ROLE_AI, parts = listOf(Part(text = aiText))))
                    
                    Result.success(aiText)
                } else {
                    Result.failure(Exception("AI không trả về nội dung. Có thể do nội dung bị chặn."))
                }
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Lỗi kết nối API"
                Result.failure(Exception("API Error ${response.code()}: $errorMsg"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun clearHistory() {
        chatHistory.clear()
    }
}

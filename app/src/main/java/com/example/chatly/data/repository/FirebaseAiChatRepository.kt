package com.example.chatly.data.repository

import com.example.chatly.data.model.AiChatMessage
import com.example.chatly.data.model.admin.ChatbotLog
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.content
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FirebaseAiChatRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    // Session chat history
    private val chatHistory = mutableListOf<AiChatMessage>()

    // System prompt
    private val systemPrompt = """
        Bạn là một trợ lý học tập thông minh và thân thiện trong ứng dụng Chatly.

        Nhiệm vụ:
        1. Trả lời bằng tiếng Việt tự nhiên.
        2. Hỗ trợ học tập, code, bài tập.
        3. Trình bày code bằng Markdown.
        4. Khuyến khích người dùng học tập.
        5. Không trả lời nội dung nguy hiểm hoặc vi phạm pháp luật.
    """.trimIndent()

    // Firebase AI model
    private val model by lazy {
        Firebase.ai(backend = GenerativeBackend.googleAI())
            .generativeModel("gemini-3-flash-preview")
    }

    suspend fun getAiResponse(userMessage: String): Result<String> {

        val userId = auth.currentUser?.uid ?: "anonymous"

        return try {

            // Build history
            val historyText = chatHistory.joinToString("\n") { msg ->
                if (msg.isMine) {
                    "User: ${msg.content}"
                } else {
                    "AI: ${msg.content}"
                }
            }

            val finalPrompt = """
                $systemPrompt

                $historyText

                User: $userMessage
                AI:
            """.trimIndent()

            // Generate response
            val response = model.generateContent(
                content {
                    text(finalPrompt)
                }
            )

            val aiText = response.text ?: "AI không trả lời được"

            // Update session history
            chatHistory.add(
                AiChatMessage(
                    content = userMessage,
                    isMine = true
                )
            )

            chatHistory.add(
                AiChatMessage(
                    content = aiText,
                    isMine = false
                )
            )

            // Save log
            val log = ChatbotLog(
                userId = userId,
                query = userMessage,
                response = aiText,
                status = "success",
                timestamp = System.currentTimeMillis()
            )

            firestore.collection("chatbot_logs")
                .add(log)

            Result.success(aiText)

        } catch (e: Exception) {

            e.printStackTrace()

            val errorLog = ChatbotLog(
                userId = userId,
                query = userMessage,
                response = "Error",
                status = "error",
                errorMessage = e.localizedMessage,
                timestamp = System.currentTimeMillis()
            )

            firestore.collection("chatbot_logs")
                .add(errorLog)

            Result.failure(e)
        }
    }

    fun clearHistory() {
        chatHistory.clear()
    }

    fun getHistory(): List<AiChatMessage> {
        return chatHistory.toList()
    }
}
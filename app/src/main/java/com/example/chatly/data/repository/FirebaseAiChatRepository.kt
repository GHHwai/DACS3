package com.example.chatly.data.repository

import android.util.Log
import com.example.chatly.OllamaApi
import com.example.chatly.OllamaClient
import com.example.chatly.OllamaMessage
import com.example.chatly.OllamaRequest
import com.example.chatly.data.model.AiChatMessage
import com.example.chatly.data.model.ChatSession
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

    // System prompt
    private val systemPrompt = """
You are a smart and friendly learning assistant in the Chatly app.

Tasks:

1. Respond in natural English.

2. Provide support for learning, coding, and assignments.

3. Present code using Markdown.

4. Encourage users to learn.

5. Do not respond to dangerous or illegal content.
    """.trimIndent()

    // Firebase AI model
    private val model by lazy {
        Firebase.ai(backend = GenerativeBackend.googleAI())
            .generativeModel("gemini-3-flash-preview")
    }
    private suspend fun callFirebaseAI(
        finalPrompt: String
    ): String? {

        return try {

            Log.d("FIREBASE_AI", "🚀 START REQUEST")
            Log.d("FIREBASE_AI", "📦 PROMPT SIZE = ${finalPrompt.length}")

            val response = model.generateContent(
                content { text(finalPrompt) }
            )

            Log.d("FIREBASE_AI", "📩 RAW RESPONSE = $response")
            val text = response.candidates
                .firstOrNull()
                ?.content
                .toString()

            if (text == null) {
                Log.e("FIREBASE_AI", "⚠️ RESPONSE TEXT IS NULL")
            } else {
                Log.d("FIREBASE_AI", "💬 AI TEXT = $text")
            }

            return text

        } catch (e: Exception) {

            Log.e("FIREBASE_AI_ERROR", "❌ MESSAGE: ${e.message}")
            Log.e("FIREBASE_AI_ERROR", "❌ CLASS: ${e.javaClass}")
            Log.e("FIREBASE_AI_ERROR", "❌ CAUSE: ${e.cause}")
            Log.e("FIREBASE_AI_ERROR", "❌ STACKTRACE:\n${Log.getStackTraceString(e)}")

            // 🔥 detect quota / rate limit
            if (e.message?.contains("quota", ignoreCase = true) == true ||
                e.message?.contains("429", ignoreCase = true) == true
            ) {
                Log.e("FIREBASE_AI_ERROR", "🔥 POSSIBLE QUOTA EXCEEDED")
            }

            // 🔥 detect network
            if (e is java.net.UnknownHostException) {
                Log.e("FIREBASE_AI_ERROR", "🌐 NETWORK ERROR")
            }

            // 🔥 detect timeout
            if (e is java.net.SocketTimeoutException) {
                Log.e("FIREBASE_AI_ERROR", "⏰ TIMEOUT ERROR")
            }

            return null
        }
    }
    private suspend fun callOllamaAI(
        messages: List<AiChatMessage>,
        userMessage: String
    ): String? {

        try {
            Log.d("OLLAMA_DEBUG", "🚀 START REQUEST")

            val history = messages.map {
                OllamaMessage(
                    role = if (it.isMine) "user" else "assistant",
                    content = it.content
                )
            }

            val request = OllamaRequest(
                model = "llama2",
                messages = history + OllamaMessage(
                    role = "user",
                    content = userMessage
                )
            )

            Log.d("OLLAMA_DEBUG", "📦 REQUEST = $request")

            val response = OllamaClient.api.chat(request)

            Log.d("OLLAMA_DEBUG", "📩 RAW RESPONSE = $response")

            val raw = response.message?.content

            Log.d("OLLAMA_RAW", "raw = $raw")

            if (raw.isNullOrBlank()) return null

            val cleaned = cleanOllamaResponse(raw)

            Log.d("OLLAMA_DEBUG", "💬 CONTENT = $cleaned")
            return cleaned
        } catch (e: Exception) {

            Log.e("OLLAMA_ERROR", "❌ MESSAGE: ${e.message}")
            Log.e("OLLAMA_ERROR", "❌ CLASS: ${e.javaClass}")
            Log.e("OLLAMA_ERROR", "❌ STACKTRACE:\n${Log.getStackTraceString(e)}")

            return null
        }
    }
    suspend fun generateQuizContent(
        prompt: String
    ): Result<String> {

        // Gemini
        callFirebaseAI(prompt)?.let {
            return Result.success(it)
        }

        Log.d("QUIZ_AI", "Gemini failed -> Ollama fallback")

        // Ollama
        try {

            val response = OllamaClient.api.chat(
                OllamaRequest(
                    model = "llama2",
                    messages = listOf(
                        OllamaMessage(
                            role = "user",
                            content = prompt
                        )
                    )
                )
            )

            val text = response.message?.content

            if (!text.isNullOrBlank()) {
                return Result.success(text)
            }

        } catch (e: Exception) {

            Log.e(
                "QUIZ_AI",
                "Ollama failed",
                e
            )
        }

        // Chỉ tới đây mới coi là lỗi thật sự
        return Result.failure(
            Exception("Both Gemini and Ollama failed")
        )
    }
    suspend fun getAiResponse(
        messages: List<AiChatMessage>,
        userMessage: String
    ): Result<String> {

        val userId =
            auth.currentUser?.uid
                ?: "anonymous"

        return try {

            val historyText =
                messages.joinToString("\n") { msg ->

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

            val firebase = callFirebaseAI(finalPrompt)
            val ollama = callOllamaAI(messages, userMessage)

            Log.d("AI_DEBUG", "ollama=$ollama")

            val aiText = firebase ?: ollama ?: "Errors when calling the AI"

            val log = ChatbotLog(
                userId = userId,
                query = userMessage,
                response = aiText,
                status = "success",
                timestamp = System.currentTimeMillis()
            )

            firestore
                .collection("chatbot_logs")
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

            firestore
                .collection("chatbot_logs")
                .add(errorLog)

            Result.failure(e)
        }
    }
    fun saveChatSession(session: ChatSession) {
        firestore
            .collection("users")
            .document(session.userId)
            .collection("sessions")
            .document(session.id)
            .set(session)
    }
    fun saveMessage(userId: String, sessionId: String, message: AiChatMessage) {

        val msg = message.copy(
            timestamp = System.currentTimeMillis()
        )

        firestore
            .collection("users")
            .document(userId)
            .collection("sessions")
            .document(sessionId)
            .collection("messages")
            .add(
                mapOf(
                    "content" to msg.content,
                    "isMine" to msg.isMine,
                    "timestamp" to msg.timestamp
                )
            )
    }
    fun getMessages(userId: String, sessionId: String, callback: (List<AiChatMessage>) -> Unit) {

        firestore
            .collection("users")
            .document(userId)
            .collection("sessions")
            .document(sessionId)
            .collection("messages")
            .get()
            .addOnSuccessListener { snap ->

                val list = snap.documents.mapNotNull { doc ->
                    AiChatMessage(
                        content = doc.getString("content") ?: "",
                        isMine = doc.getBoolean("isMine") ?: false,
                        timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis()
                    )
                }

                callback(list)
            }
    }
    fun getChatSessions(userId: String, callback: (List<ChatSession>) -> Unit) {

        firestore
            .collection("users")
            .document(userId)
            .collection("sessions")
            .get()
            .addOnSuccessListener { snap ->

                val list = snap.documents.mapNotNull {
                    it.toObject(ChatSession::class.java)
                }

                callback(list)
            }
    }
    fun updateSessionTitle(uid: String, sessionId: String, title: String){
        firestore.collection("users")
            .document(uid)
            .collection("sessions")
            .document(sessionId)
            .update("title", title)
    }
    fun deleteSession(userId: String, sessionId: String) {

        val sessionRef = firestore
            .collection("users")
            .document(userId)
            .collection("sessions")
            .document(sessionId)

        // delete messages trước
        sessionRef.collection("messages")
            .get()
            .addOnSuccessListener { snap ->
                val batch = firestore.batch()

                snap.documents.forEach {
                    batch.delete(it.reference)
                }

                batch.commit().addOnSuccessListener {
                    sessionRef.delete()
                }
            }
    }
    private fun cleanOllamaResponse(input: String): String {

        var text = input.trim()

        // 1. remove markdown code block
        text = text.replace("```json", "")
            .replace("```", "")
            .trim()

        // 2. remove \n artifacts (không bắt buộc nhưng sạch log)
        text = text.replace("\\n", "")
            .replace("\n", "")
            .trim()

        // 3. extract JSON block (quan trọng nhất)
        val start = text.indexOf("{")
        val end = text.lastIndexOf("}")

        if (start != -1 && end != -1 && end > start) {
            text = text.substring(start, end + 1)
        }

        // 4. fix missing braces (AI bị cắt)
        val open = text.count { it == '{' }
        val close = text.count { it == '}' }

        if (close < open) {
            text += "}".repeat(open - close)
        }

        return text
    }
}
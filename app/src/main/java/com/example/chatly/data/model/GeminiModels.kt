package com.example.chatly.data.model

import com.google.gson.annotations.SerializedName
import java.util.UUID

/**
 * Request models for Gemini API
 */
data class GeminiRequest(
    @SerializedName("contents") val contents: List<Content>,
    @SerializedName("generationConfig") val generationConfig: GenerationConfig = GenerationConfig(),
    @SerializedName("systemInstruction") val systemInstruction: Content? = null
)

data class Content(
    @SerializedName("role") val role: String,
    @SerializedName("parts") val parts: List<Part>
)

data class Part(
    @SerializedName("text") val text: String
)

data class GenerationConfig(
    @SerializedName("temperature") val temperature: Double = 0.7,
    @SerializedName("topK") val topK: Int = 40,
    @SerializedName("topP") val topP: Double = 0.95,
    @SerializedName("maxOutputTokens") val maxOutputTokens: Int = 2048,
    @SerializedName("responseMimeType") val responseMimeType: String = "text/plain"
)

/**
 * Response models for Gemini API
 */
data class GeminiResponse(
    @SerializedName("candidates") val candidates: List<Candidate>?,
    @SerializedName("usageMetadata") val usageMetadata: UsageMetadata?
)

data class Candidate(
    @SerializedName("content") val content: Content?,
    @SerializedName("finishReason") val finishReason: String?
)

data class UsageMetadata(
    @SerializedName("promptTokenCount") val promptTokenCount: Int,
    @SerializedName("candidatesTokenCount") val candidatesTokenCount: Int,
    @SerializedName("totalTokenCount") val totalTokenCount: Int
)

/**
 * Internal App Model for UI
 */
data class AiChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val message: String,
    val role: String, // "user" or "model"
    val timestamp: Long = System.currentTimeMillis(),
    val isError: Boolean = false,
    val isLoading: Boolean = false
) {
    companion object {
        const val ROLE_USER = "user"
        const val ROLE_AI = "model"
    }
}

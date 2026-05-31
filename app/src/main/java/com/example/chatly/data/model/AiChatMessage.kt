package com.example.chatly.data.model

data class AiChatMessage(
    val id: String = "",
    val content: String = "",
    val isMine: Boolean = false,
    val timestamp: Long = 0L
)
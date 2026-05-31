package com.example.chatly.data.model

data class ChatSession(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
package com.example.chatly.data.model

data class AiChatMessage(
    val id: String = System.currentTimeMillis().toString(),  // id duy nhất
    val content: String,          // nội dung tin nhắn
    val isMine: Boolean,          // true nếu là user, false nếu là AI
    val timestamp: Long = System.currentTimeMillis()  // thời gian gửi
)
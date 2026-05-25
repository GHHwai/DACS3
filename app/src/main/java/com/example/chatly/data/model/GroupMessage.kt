package com.example.chatly.data.model

data class GroupMessage(
    val id: String = "",
    val groupId: String = "",
    val senderName: String = "",
    val content: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    // --- THÊM 2 DÒNG NÀY ĐỂ LƯU ĐƯỜNG DẪN FILE/ẢNH ---
    val imageUrl: String? = null,
    val fileUrl: String? = null
)
package com.example.chatly.data.model

data class Group(
    val id: String = "",
    val groupName: String = "",
    val createdBy: String = "",
    val members: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
)
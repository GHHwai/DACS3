package com.example.chatly.ui.admin.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.chatly.data.model.admin.ChatbotLog
import com.example.chatly.data.repository.admin.AdminRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AdminChatbotViewModel(private val repository: AdminRepository) : ViewModel() {
    private val _logs = MutableStateFlow<List<ChatbotLog>>(emptyList())
    val logs: StateFlow<List<ChatbotLog>> = _logs

    init {
        loadLogs()
    }

    private fun loadLogs() {
        viewModelScope.launch {
            repository.getChatbotLogs().collect { chatbotLogs ->
                _logs.value = chatbotLogs
            }
        }
    }

    class Factory(private val repository: AdminRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AdminChatbotViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return AdminChatbotViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

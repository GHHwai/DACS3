package com.example.chatly.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.chatly.data.model.AiChatMessage
import com.example.chatly.data.repository.AiChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AiChatViewModel(private val repository: AiChatRepository) : ViewModel() {

    private val _messages = MutableStateFlow<List<AiChatMessage>>(emptyList())
    val messages: StateFlow<List<AiChatMessage>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun sendMessage(text: String, apiKey: String) {
        if (text.trim().isEmpty()) return

        // 1. Add user message to list
        val userMsg = AiChatMessage(message = text, role = AiChatMessage.ROLE_USER)
        _messages.value = _messages.value + userMsg

        // 2. Add a temporary loading message for AI
        val loadingMsg = AiChatMessage(message = "...", role = AiChatMessage.ROLE_AI, isLoading = true)
        _messages.value = _messages.value + loadingMsg

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            val result = repository.getAiResponse(text, apiKey)

            // Remove loading message
            _messages.value = _messages.value.filter { !it.isLoading }

            result.onSuccess { aiResponse ->
                val aiMsg = AiChatMessage(message = aiResponse, role = AiChatMessage.ROLE_AI)
                _messages.value = _messages.value + aiMsg
            }.onFailure { exception ->
                _error.value = "Lỗi: ${exception.message}"
                // Add error message to chat
                val errorMsg = AiChatMessage(
                    message = "Rất tiếc, đã có lỗi xảy ra. Vui lòng kiểm tra kết nối mạng hoặc API key.",
                    role = AiChatMessage.ROLE_AI,
                    isError = true
                )
                _messages.value = _messages.value + errorMsg
            }

            _isLoading.value = false
        }
    }

    fun clearChat() {
        _messages.value = emptyList()
        repository.clearHistory()
    }

    class Factory(private val repository: AiChatRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AiChatViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return AiChatViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

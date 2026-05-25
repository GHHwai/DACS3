package com.example.chatly.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatly.data.model.AiChatMessage
import com.example.chatly.data.repository.FirebaseAiChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AiChatViewModel(private val repository: FirebaseAiChatRepository) : ViewModel() {

    private val _messages = MutableStateFlow<List<AiChatMessage>>(emptyList())
    val messages: StateFlow<List<AiChatMessage>> = _messages

    fun sendUserMessage(message: String) {
        // Thêm tin nhắn user
        _messages.value = _messages.value + AiChatMessage(content = message, isMine = true)

        viewModelScope.launch {
            val result = repository.getAiResponse(message)
            val aiMessage = result.getOrElse { "Đã có lỗi khi gọi AI" }
            _messages.value = _messages.value + AiChatMessage(content = aiMessage, isMine = false)
        }
    }

    fun clearChat() {
        repository.clearHistory()
        _messages.value = emptyList()
    }

    class Factory(private val repository: FirebaseAiChatRepository) :
        androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AiChatViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return AiChatViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
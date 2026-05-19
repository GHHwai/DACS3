package com.example.chatly.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatly.data.model.Message
import com.example.chatly.data.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest

class ChatViewModel(
    private val repository: ChatRepository
) : ViewModel() {
    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    private var chatUserId: String? = null
    private var myUserId: String? = null

    fun setChatUserIds(chatWithUserId: String, currentUserId: String) {
        if (chatUserId == chatWithUserId && myUserId == currentUserId) return
        chatUserId = chatWithUserId
        myUserId = currentUserId
        
        viewModelScope.launch {
            repository.getMessagesWithUser(chatWithUserId, currentUserId)
                .collectLatest { 
                    _messages.value = it
                }
        }
    }

    // Insert into Room AND remote on send
    fun sendMessage(msg: Message) {
        viewModelScope.launch {
            repository.sendMessage(msg) // inserts to Room and sends to Firestore
        }
    }

    fun sync() {
        if (chatUserId != null && myUserId != null) {
            viewModelScope.launch {
                repository.syncMessages(chatUserId!!, myUserId!!)
            }
        }
    }

    fun deleteMessage(messageId: String) {
        viewModelScope.launch {
            repository.deleteMessage(messageId)
        }
    }
}

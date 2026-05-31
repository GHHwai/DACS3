package com.example.chatly.ui.chat

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.chatly.data.model.AiChatMessage
import com.example.chatly.data.model.ChatSession
import com.example.chatly.data.repository.FirebaseAiChatRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AiChatViewModel(private val repository: FirebaseAiChatRepository) : ViewModel() {

    private val _messages =
        MutableStateFlow<List<AiChatMessage>>(emptyList())
    private val _sessions =
        MutableStateFlow<List<ChatSession>>(emptyList())
    private val _currentSessionId =
        MutableStateFlow<String?>(null)

    val currentSessionId =
        _currentSessionId

    val sessions: StateFlow<List<ChatSession>>
            = _sessions
    val messages: StateFlow<List<AiChatMessage>> = _messages
    private val userId =
        FirebaseAuth.getInstance().currentUser?.uid ?: "guest"
    init {
        loadSessions()
    }
    fun loadSessions() {

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        repository.getChatSessions(uid) { list ->
            _sessions.value = list.sortedByDescending { it.updatedAt }
        }
    }

    fun createNewChat(userId: String) {

        val sessionId = System.currentTimeMillis().toString()

        val session = ChatSession(
            id = sessionId,
            userId = userId,
            title = "New Chat",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        repository.saveChatSession(session)

        _sessions.value = listOf(session) + _sessions.value
        _currentSessionId.value = sessionId
        _messages.value = emptyList()
    }

    fun selectSession(sessionId: String) {

        _currentSessionId.value = sessionId

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        repository.getMessages(uid, sessionId) { list ->
            _messages.value = list.sortedBy { it.timestamp }
        }
    }
    fun sendUserMessage(message: String) {

        var sessionId = _currentSessionId.value
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        if (sessionId == null) {

            sessionId = System.currentTimeMillis().toString()

            val newSession = ChatSession(
                id = sessionId,
                userId = uid,
                title = message.take(30),
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )

            repository.saveChatSession(newSession)

            _currentSessionId.value = sessionId
        }

        val userMessage = AiChatMessage(
            content = message,
            isMine = true
        )

        _messages.value = _messages.value + userMessage
        repository.saveMessage(uid, sessionId, userMessage)

        val currentSession = _sessions.value.find { it.id == sessionId }

        if (currentSession?.title == "New Chat") {
            updateSessionTitle(sessionId, message)
        }

        _sessions.value = _sessions.value.map {
            if (it.id == sessionId) {
                it.copy(updatedAt = System.currentTimeMillis())
            } else it
        }.sortedByDescending { it.updatedAt }

        viewModelScope.launch {

            val result = repository.getAiResponse(
                messages = _messages.value,
                userMessage = message
            )

            result.onSuccess {
                Log.d("AI", "OK: $it")
            }.onFailure {
                Log.e("AI", "FAIL: ", it)
            }

            val aiMessage = AiChatMessage(
                content = result.getOrElse { "Đã có lỗi khi gọi AI" },
                isMine = false
            )

            _messages.value = _messages.value + aiMessage
            repository.saveMessage(uid, sessionId, aiMessage)
        }
    }
    private fun updateSessionTitle(
        sessionId: String,
        firstMessage: String
    ) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        _sessions.value = _sessions.value.map { session ->
            if (session.id == sessionId && session.title == "New Chat") {

                val newTitle = firstMessage.take(30)

                repository.updateSessionTitle(uid, sessionId, newTitle)

                session.copy(title = newTitle)
            } else session
        }
    }
    fun renameSession(sessionId: String, newTitle: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        repository.updateSessionTitle(uid, sessionId, newTitle)

        _sessions.value = _sessions.value.map {
            if (it.id == sessionId) {
                it.copy(title = newTitle)
            } else it
        }
    }
    fun deleteSession(sessionId: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        repository.deleteSession(uid, sessionId)

        _sessions.value = _sessions.value.filter {
            it.id != sessionId
        }

        if (_currentSessionId.value == sessionId) {
            _currentSessionId.value = null
            _messages.value = emptyList()
        }
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
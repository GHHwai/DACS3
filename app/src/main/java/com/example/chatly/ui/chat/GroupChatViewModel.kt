package com.example.chatly.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.chatly.data.model.Group
import com.example.chatly.data.model.GroupMessage
import com.example.chatly.data.repository.GroupChatRepository
import kotlinx.coroutines.flow.* // DÙNG DẤU CHẤM SAO ĐỂ IMPORT TẤT CẢ TÍNH NĂNG FLOW
import kotlinx.coroutines.launch

class GroupChatViewModel(
    private val repository: GroupChatRepository
) : ViewModel() {

    // Danh sách tất cả người dùng trên hệ thống để phục vụ việc chọn/mời vào nhóm
    private val _allUsers = MutableStateFlow<List<com.example.chatly.data.model.User>>(emptyList())
    val allUsers: StateFlow<List<com.example.chatly.data.model.User>> = _allUsers

    // Danh sách nhóm chat mà user tham gia
    private val _groups = MutableStateFlow<List<Group>>(emptyList())
    val groups: StateFlow<List<Group>> = _groups

    // Luồng tin nhắn trong phòng chat hiện tại
    private val _messages = MutableStateFlow<List<GroupMessage>>(emptyList())
    val messages: StateFlow<List<GroupMessage>> = _messages

    // Tải danh sách user
    fun loadAllUsers() {
        viewModelScope.launch {
            repository.getAllUsers().collect { users ->
                _allUsers.value = users
            }
        }
    }

    // Thêm thành viên hoặc rời nhóm
    fun updateMembers(groupId: String, newMembers: List<String>, onComplete: (Boolean) -> Unit = {}) {
        repository.updateGroupMembers(groupId, newMembers, onComplete)
    }

    // Tạo một nhóm chat mới
    fun createGroup(group: Group) {
        repository.createGroup(group)
    }

    // Tải danh sách nhóm chat
    fun loadGroups() {
        viewModelScope.launch {
            repository.getGroups().collect { groupList ->
                _groups.value = groupList
            }
        }
    }

    // Tải tin nhắn của một nhóm cụ thể
    fun loadMessages(groupId: String) {
        viewModelScope.launch {
            repository.getMessages(groupId).collect { messageList ->
                _messages.value = messageList
            }
        }
    }

    // Gửi tin nhắn mới
    fun sendMessage(message: GroupMessage) {
        repository.sendMessage(message)
    }

    // Factory để hỗ trợ khởi tạo ViewModel có tham số truyền vào
    class Factory(private val repository: GroupChatRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(GroupChatViewModel::class.java)) {
                return GroupChatViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
    // Thêm vào trong class GroupChatViewModel

    fun deleteGroup(groupId: String, onComplete: (Boolean) -> Unit = {}) {
        repository.deleteGroup(groupId, onComplete)
    }
}
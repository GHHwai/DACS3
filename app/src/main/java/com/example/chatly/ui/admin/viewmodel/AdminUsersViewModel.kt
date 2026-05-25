package com.example.chatly.ui.admin.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.chatly.data.model.User
import com.example.chatly.data.repository.admin.AdminRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AdminUsersViewModel(private val repository: AdminRepository) : ViewModel() {
    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users

    init {
        loadUsers()
    }

    private fun loadUsers() {
        viewModelScope.launch {
            repository.getAllUsers().collect { userList ->
                _users.value = userList
            }
        }
    }

    fun toggleUserStatus(user: User) {
        val newStatus = if (user.status == "active") "banned" else "active"
        viewModelScope.launch {
            repository.updateUserStatus(user.uid, newStatus)
        }
    }

    class Factory(private val repository: AdminRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AdminUsersViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return AdminUsersViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

package com.example.chatly.ui.admin.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.chatly.data.model.admin.Report
import com.example.chatly.data.repository.admin.AdminRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AdminChatViewModel(private val repository: AdminRepository) : ViewModel() {
    private val _reports = MutableStateFlow<List<Report>>(emptyList())
    val reports: StateFlow<List<Report>> = _reports

    init {
        loadReports()
    }

    private fun loadReports() {
        viewModelScope.launch {
            repository.getReports().collect { reportList ->
                _reports.value = reportList
            }
        }
    }

    fun resolveReport(id: String) {
        viewModelScope.launch {
            repository.resolveReport(id)
        }
    }

    class Factory(private val repository: AdminRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AdminChatViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return AdminChatViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

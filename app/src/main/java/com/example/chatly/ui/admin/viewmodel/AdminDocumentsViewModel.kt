package com.example.chatly.ui.admin.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.chatly.data.model.admin.Document
import com.example.chatly.data.repository.admin.AdminRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AdminDocumentsViewModel(private val repository: AdminRepository) : ViewModel() {
    private val _documents = MutableStateFlow<List<Document>>(emptyList())
    val documents: StateFlow<List<Document>> = _documents

    init {
        loadDocuments()
    }

    private fun loadDocuments() {
        viewModelScope.launch {
            repository.getDocuments().collect { docs ->
                _documents.value = docs
            }
        }
    }

    fun approveDocument(id: String) {
        viewModelScope.launch {
            repository.updateDocumentStatus(id, "approved")
        }
    }

    fun rejectDocument(id: String) {
        viewModelScope.launch {
            repository.updateDocumentStatus(id, "rejected")
        }
    }

    fun deleteDocument(id: String) {
        viewModelScope.launch {
            repository.deleteDocument(id)
        }
    }

    class Factory(private val repository: AdminRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AdminDocumentsViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return AdminDocumentsViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

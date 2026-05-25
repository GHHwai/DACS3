package com.example.chatly.ui.admin.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.chatly.data.model.admin.SystemData
import com.example.chatly.data.repository.admin.AdminRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class AdminSystemDataViewModel(private val repository: AdminRepository) : ViewModel() {
    private val _systemDataList = MutableStateFlow<List<SystemData>>(emptyList())
    val systemDataList: StateFlow<List<SystemData>> = _systemDataList

    private var currentType = "subject" // default

    init {
        loadData(currentType)
    }

    fun loadData(type: String) {
        currentType = type
        viewModelScope.launch {
            repository.getSystemData(type).collect { data ->
                _systemDataList.value = data
            }
        }
    }

    fun addData(name: String, description: String, date: String) {
        val data = SystemData(
            id = UUID.randomUUID().toString(),
            type = currentType,
            name = name,
            description = description,
            date = date
        )
        viewModelScope.launch {
            repository.addSystemData(data)
        }
    }

    fun deleteData(id: String) {
        viewModelScope.launch {
            repository.deleteSystemData(id)
        }
    }

    class Factory(private val repository: AdminRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AdminSystemDataViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return AdminSystemDataViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

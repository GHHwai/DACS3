package com.example.chatly.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatly.data.model.ExamSchedule
import com.example.chatly.data.model.StudySchedule
import com.example.chatly.data.repository.ScheduleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ScheduleUiState(

    val studySchedules: List<StudySchedule> = emptyList(),

    val examSchedules: List<ExamSchedule> = emptyList(),

    val searchQuery: String = "",

    val selectedDay: String = "Monday",

    val selectedTab: Int = 0,

    val isLoading: Boolean = false,

    val error: String? = null
)

class ScheduleViewModel(
    private val repository: ScheduleRepository = ScheduleRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScheduleUiState())
    val uiState: StateFlow<ScheduleUiState> = _uiState.asStateFlow()

    init {
        observeStudySchedules()
        observeExamSchedules()
    }

    // -------------------------
    // OBSERVE STUDY
    // -------------------------
    private fun observeStudySchedules() {
        viewModelScope.launch {
            repository.getStudySchedules()
                .collectLatest { schedules ->
                    _uiState.update {
                        it.copy(studySchedules = schedules)
                    }
                }
        }
    }

    // -------------------------
    // OBSERVE EXAM
    // -------------------------
    private fun observeExamSchedules() {
        viewModelScope.launch {
            repository.getExamSchedules()
                .collectLatest { exams ->
                    _uiState.update {
                        it.copy(
                            examSchedules = exams.sortedBy { e -> e.examDate }
                        )
                    }
                }
        }
    }

    // -------------------------
    // UI STATE
    // -------------------------
    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun onDaySelected(day: String) {
        _uiState.update { it.copy(selectedDay = day) }
    }

    fun onTabSelected(index: Int) {
        _uiState.update { it.copy(selectedTab = index) }
    }

    // -------------------------
    // STUDY CRUD
    // -------------------------
    fun addStudySchedule(
        schedule: StudySchedule,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                repository.addStudySchedule(schedule)
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Error")
            }
        }
    }

    fun updateStudySchedule(
        schedule: StudySchedule,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                repository.updateStudySchedule(schedule)
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Error")
            }
        }
    }

    fun deleteStudySchedule(id: String) {
        viewModelScope.launch {
            repository.deleteStudySchedule(id)
        }
    }

    // -------------------------
    // EXAM CRUD
    // -------------------------
    fun addExamSchedule(
        exam: ExamSchedule,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                repository.addExamSchedule(exam)
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Error")
            }
        }
    }

    fun updateExamSchedule(
        exam: ExamSchedule,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                repository.updateExamSchedule(exam)
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Error")
            }
        }
    }

    fun deleteExamSchedule(id: String) {
        viewModelScope.launch {
            repository.deleteExamSchedule(id)
        }
    }
}
package com.example.chatly.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.chatly.ui.components.*
import com.example.chatly.ui.viewmodel.ScheduleViewModel

@Composable
fun ScheduleScreen(
    onAddStudyClick: () -> Unit,
    onAddExamClick: () -> Unit,
    onEditStudyClick: (String) -> Unit,
    onEditExamClick: (String) -> Unit,
    viewModel: ScheduleViewModel = viewModel()
) {

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val filteredSchedules = uiState.studySchedules.filter {

        it.subject.contains(uiState.searchQuery, true)
                &&
                it.dayOfWeek == uiState.selectedDay
    }

    val filteredExams = uiState.examSchedules.filter {

        it.subject.contains(uiState.searchQuery, true)
    }

    Scaffold(

        topBar = {

            ChatlyTopAppBar(
                title = "Schedules"
            )
        },

        floatingActionButton = {

            FloatingActionButton(
                onClick = {
                    if (uiState.selectedTab == 0)
                        onAddStudyClick()
                    else
                        onAddExamClick()
                }
            ) {
                Icon(Icons.Default.Add, null)
            }
        }

    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {

            ScheduleSearchBar(
                query = uiState.searchQuery,
                onQueryChange = viewModel::onSearchQueryChange
            )

            Spacer(modifier = Modifier.height(16.dp))

            ScheduleTabs(
                selectedTab = uiState.selectedTab,
                onTabSelected = viewModel::onTabSelected
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (uiState.selectedTab == 0) {

                DayFilterChips(
                    selectedDay = uiState.selectedDay,
                    onDaySelected = viewModel::onDaySelected
                )

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    items(filteredSchedules) { schedule ->

                        ScheduleCard(
                            schedule = schedule,
                            onEdit = {
                                onEditStudyClick(schedule.id)
                            },
                            onDelete = {
                                viewModel.deleteStudySchedule(schedule.id)
                            }
                        )
                    }
                }

            } else {

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    items(filteredExams) { exam ->

                        ExamCard(
                            exam = exam,
                            onEdit = {
                                onEditExamClick(exam.id)
                            },
                            onDelete = {
                                viewModel.deleteExamSchedule(exam.id)
                            }
                        )
                    }                }
            }
        }
    }
}
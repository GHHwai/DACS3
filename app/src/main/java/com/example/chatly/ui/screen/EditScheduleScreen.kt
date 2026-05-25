package com.example.chatly.ui.screen

import com.example.chatly.ui.components.DayDropdown
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.chatly.data.model.StudySchedule
import com.example.chatly.ui.components.ChatlyButton
import com.example.chatly.ui.components.ChatlyTextField
import com.example.chatly.ui.components.ChatlyTopAppBar
import com.example.chatly.ui.viewmodel.ScheduleViewModel

@Composable
fun EditScheduleScreen(
    scheduleId: String,
    onBackClick: () -> Unit,
    viewModel: ScheduleViewModel = viewModel()
) {

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val schedule = uiState.studySchedules.find {
        it.id == scheduleId
    }

    var subject by remember {
        mutableStateOf(schedule?.subject ?: "")
    }

    var room by remember {
        mutableStateOf(schedule?.room ?: "")
    }

    var teacher by remember {
        mutableStateOf(schedule?.teacher ?: "")
    }

    var day by remember {
        mutableStateOf(schedule?.dayOfWeek ?: "")
    }

    var startTime by remember {
        mutableStateOf(schedule?.startTime ?: "")
    }

    var endTime by remember {
        mutableStateOf(schedule?.endTime ?: "")
    }

    Scaffold(

        topBar = {

            ChatlyTopAppBar(
                title = "Edit Schedule",
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }

    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            ChatlyTextField(
                value = subject,
                onValueChange = {
                    subject = it
                },
                label = "Subject"
            )

            ChatlyTextField(
                value = room,
                onValueChange = {
                    room = it
                },
                label = "Room"
            )

            ChatlyTextField(
                value = teacher,
                onValueChange = {
                    teacher = it
                },
                label = "Teacher"
            )

            DayDropdown(
                selectedDay = day,
                onDaySelected = {
                    day = it
                }
            )

            ChatlyTextField(
                value = startTime,
                onValueChange = {
                    startTime = it
                },
                label = "Start Time"
            )

            ChatlyTextField(
                value = endTime,
                onValueChange = {
                    endTime = it
                },
                label = "End Time"
            )

            ChatlyButton(

                text = "Update",

                onClick = {

                    viewModel.updateStudySchedule(

                        StudySchedule(
                            id = scheduleId,
                            subject = subject,
                            room = room,
                            teacher = teacher,
                            dayOfWeek = day,
                            startTime = startTime,
                            endTime = endTime
                        )
                    )

                    onBackClick()
                }
            )
        }
    }
}
package com.example.chatly.ui.screen

import DayDropdown
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.chatly.data.model.StudySchedule
import com.example.chatly.ui.components.ChatlyButton
import com.example.chatly.ui.components.ChatlyTextField
import com.example.chatly.ui.components.ChatlyTopAppBar
import com.example.chatly.ui.viewmodel.ScheduleViewModel

@Composable
fun AddScheduleScreen(
    onBackClick: () -> Unit,
    viewModel: ScheduleViewModel = viewModel()
) {

    var subject by remember { mutableStateOf("") }
    var room by remember { mutableStateOf("") }
    var teacher by remember { mutableStateOf("") }
    var day by remember { mutableStateOf("") }
    var startTime by remember { mutableStateOf("") }
    var endTime by remember { mutableStateOf("") }

    Scaffold(

        topBar = {

            ChatlyTopAppBar(

                title = "Add Schedule",

                navigationIcon = {

                    IconButton(
                        onClick = onBackClick
                    ) {

                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = null
                        )
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

                text = "Save",

                onClick = {

                    viewModel.addStudySchedule(

                        StudySchedule(
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
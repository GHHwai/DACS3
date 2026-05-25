package com.example.chatly.ui.screen

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
import com.example.chatly.data.model.ExamSchedule
import com.example.chatly.ui.components.ChatlyButton
import com.example.chatly.ui.components.ChatlyTextField
import com.example.chatly.ui.components.ChatlyTopAppBar
import com.example.chatly.ui.viewmodel.ScheduleViewModel

@Composable
fun AddExamScreen(
    onBackClick: () -> Unit,
    viewModel: ScheduleViewModel = viewModel()
) {

    var subject by remember { mutableStateOf("") }
    var room by remember { mutableStateOf("") }
    var examDate by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            ChatlyTopAppBar(
                title = "Add Exam",
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            ChatlyTextField(
                value = subject,
                onValueChange = { subject = it },
                label = "Subject"
            )

            ChatlyTextField(
                value = room,
                onValueChange = { room = it },
                label = "Room"
            )

            ChatlyTextField(
                value = examDate,
                onValueChange = { examDate = it },
                label = "Exam Date (dd/MM/yyyy)"
            )

            ChatlyTextField(
                value = note,
                onValueChange = { note = it },
                label = "Note"
            )

            ChatlyButton(
                text = if (uiState.isLoading) "Saving..." else "Save",
                onClick = {

                    if (subject.isBlank() || examDate.isBlank()) return@ChatlyButton

                    viewModel.addExamSchedule(
                        ExamSchedule(
                            subject = subject,
                            room = room,
                            examDate = examDate,
                            note = note
                        ),
                        onSuccess = {
                            onBackClick()
                        },
                        onError = {
                            println("Error: $it")
                        }
                    )
                }
            )
        }
    }
}
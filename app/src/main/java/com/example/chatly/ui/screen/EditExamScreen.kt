package com.example.chatly.ui.screen
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.chatly.data.model.ExamSchedule
import com.example.chatly.ui.components.ChatlyButton
import com.example.chatly.ui.components.ChatlyTextField
import com.example.chatly.ui.components.ChatlyTopAppBar
import com.example.chatly.ui.viewmodel.ScheduleViewModel
@Composable
fun EditExamScreen(
    examId: String,
    onBackClick: () -> Unit,
    viewModel: ScheduleViewModel = viewModel()
) {

    val uiState by viewModel.uiState.collectAsState()

    val exam = uiState.examSchedules.find { it.id == examId }

    if (exam == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    var subject by remember { mutableStateOf(exam.subject) }
    var room by remember { mutableStateOf(exam.room) }
    var examDate by remember { mutableStateOf(exam.examDate) }
    var note by remember { mutableStateOf(exam.note) }

    Scaffold(
        topBar = {
            ChatlyTopAppBar(
                title = "Edit Exam",
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
                label = "Exam Date"
            )

            ChatlyTextField(
                value = note,
                onValueChange = { note = it },
                label = "Note"
            )

            ChatlyButton(
                text = "Update",
                onClick = {

                    viewModel.updateExamSchedule(
                        ExamSchedule(
                            id = exam.id,
                            subject = subject,
                            room = room,
                            examDate = examDate,
                            note = note
                        ),
                        onSuccess = {
                            onBackClick()
                        },
                        onError = {
                            println(it)
                        }
                    )
                }
            )
        }
    }
}
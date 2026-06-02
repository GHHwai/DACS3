package com.example.chatly.ui.screen

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.platform.LocalContext
import com.example.chatly.utils.NotificationScheduler
import java.util.UUID

@Composable
fun AddExamScreen(
    onBackClick: () -> Unit,
    viewModel: ScheduleViewModel = viewModel()
) {
    val context = LocalContext.current

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
                    // 1. Kiểm tra không được bỏ trống các trường bắt buộc
                    if (subject.isBlank() || examDate.isBlank()) {
                        Toast.makeText(context, "Vui lòng nhập đầy đủ Môn học và Ngày thi", Toast.LENGTH_SHORT).show()
                        return@ChatlyButton
                    }

                    // 2. Chuyển đổi và kiểm tra định dạng ngày gõ tay (dd/MM/yyyy)
                    val exactExamTime = NotificationScheduler.convertExamDateToMillis(examDate)
                    if (exactExamTime == 0L) {
                        Toast.makeText(context, "Ngày thi không đúng định dạng dd/MM/yyyy", Toast.LENGTH_LONG).show()
                        return@ChatlyButton
                    }

                    val currentTime = System.currentTimeMillis()
                    val examId = UUID.randomUUID().toString()

                    // 3. Tiến hành gọi ViewModel để lưu vào Database
                    viewModel.addExamSchedule(
                        ExamSchedule(
                            id = examId,
                            subject = subject,
                            room = room,
                            examDate = examDate,
                            note = note
                        ),
                        onSuccess = {
                            // Khoảng thời gian 1 ngày tính bằng mili-giây
                            val oneDayInMillis = 24 * 60 * 60 * 1000L

                            // MỐC 1: Thông báo trước hẳn 1 ngày (vào lúc 7:00 sáng ngày hôm trước)
                            val triggerOneDayBefore = exactExamTime - oneDayInMillis
                            if (triggerOneDayBefore > currentTime) {
                                NotificationScheduler.scheduleNotification(
                                    context = context,
                                    id = "${examId}_1day", // Truyền ID dạng chuỗi biệt lập
                                    title = "Lịch thi: Ngày mai thi môn $subject",
                                    message = "Nhắc nhở: Ngày mai bạn có lịch thi lúc 07:00 tại phòng $room. Nhớ chuẩn bị thẻ sinh viên nhé!",
                                    triggerTimeInMillis = triggerOneDayBefore
                                )
                            }

                            // MỐC 2: Thông báo đúng ngày thi vào lúc 7:00 sáng
                            if (exactExamTime > currentTime) {
                                NotificationScheduler.scheduleNotification(
                                    context = context,
                                    id = "${examId}_7am", // Truyền ID dạng chuỗi biệt lập
                                    title = "Lịch thi: Hôm nay thi môn $subject",
                                    message = "Phòng thi: $room. Ghi chú: $note. Bình tĩnh làm bài thật tốt nhé!",
                                    triggerTimeInMillis = exactExamTime
                                )
                            }

                            // Quay trở lại màn hình trước sau khi hoàn tất mọi thủ tục
                            onBackClick()
                        },
                        onError = { error ->
                            Toast.makeText(context, "Lỗi: $error", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            )
        }
    }
}
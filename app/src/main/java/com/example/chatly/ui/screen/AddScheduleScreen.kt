package com.example.chatly.ui.screen

import android.widget.Toast // Đừng quên import Toast
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.chatly.data.model.StudySchedule
import com.example.chatly.ui.components.ChatlyButton
import com.example.chatly.ui.components.ChatlyTextField
import com.example.chatly.ui.components.ChatlyTopAppBar
import com.example.chatly.ui.viewmodel.ScheduleViewModel
import androidx.compose.ui.platform.LocalContext
import com.example.chatly.utils.NotificationScheduler
import java.util.UUID

@Composable
fun AddScheduleScreen(
    onBackClick: () -> Unit,
    viewModel: ScheduleViewModel = viewModel()
) {
    val context = LocalContext.current

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
                onValueChange = { subject = it },
                label = "Subject"
            )

            ChatlyTextField(
                value = room,
                onValueChange = { room = it },
                label = "Room"
            )

            ChatlyTextField(
                value = teacher,
                onValueChange = { teacher = it },
                label = "Teacher"
            )

            DayDropdown(
                selectedDay = day,
                onDaySelected = { day = it }
            )

            ChatlyTextField(
                value = startTime,
                onValueChange = { startTime = it },
                label = "Start Time (HH:mm)" // Thêm gợi ý để người dùng nhập đúng
            )

            ChatlyTextField(
                value = endTime,
                onValueChange = { endTime = it },
                label = "End Time"
            )

            ChatlyButton(
                text = "Save",
                onClick = {
                    // 1. CHẶN LƯU KHI ĐỂ TRỐNG THÔNG TIN QUAN TRỌNG
                    if (subject.isBlank() || day.isBlank() || startTime.isBlank()) {
                        Toast.makeText(context, "Vui lòng nhập Môn học, Thứ và Giờ bắt đầu", Toast.LENGTH_SHORT).show()
                        return@ChatlyButton
                    }

                    // 2. TÍNH TOÁN VÀ KIỂM TRA ĐỊNH DẠNG GIỜ (Phải làm trước khi lưu DB)
                    val exactStudyTime = NotificationScheduler.convertStudyTimeToMillis(day, startTime)
                    if (exactStudyTime == 0L) {
                        Toast.makeText(context, "Giờ bắt đầu sai định dạng. Vui lòng nhập HH:mm (VD: 07:30)", Toast.LENGTH_LONG).show()
                        return@ChatlyButton
                    }

                    // 3. THÔNG TIN HỢP LỆ -> LƯU VÀO DATABASE
                    val scheduleId = UUID.randomUUID().toString()
                    viewModel.addStudySchedule(
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

                    // 4. HẸN GIỜ ĐỔ CHUÔNG
                    val currentTime = System.currentTimeMillis()

                    // MỐC 1: Thông báo trước hẳn 1 ngày
                    val oneDayInMillis = 24 * 60 * 60 * 1000L
                    val triggerOneDayBefore = exactStudyTime - oneDayInMillis

                    if (triggerOneDayBefore > currentTime) {
                        NotificationScheduler.scheduleNotification(
                            context = context,
                            id = "${scheduleId}_1day",
                            title = "Ngày mai có lịch học: $subject",
                            message = "Nhắc trước để chuẩn bị bài: Ngày mai bạn có tiết học lúc $startTime tại phòng $room.",
                            triggerTimeInMillis = triggerOneDayBefore
                        )
                    }

                    // MỐC 2: Thông báo trước 15 phút
                    val fifteenMinsInMillis = 15 * 60 * 1000L
                    val triggerFifteenMinsBefore = exactStudyTime - fifteenMinsInMillis

                    if (triggerFifteenMinsBefore > currentTime) {
                        NotificationScheduler.scheduleNotification(
                            context = context,
                            id = "${scheduleId}_15mins",
                            title = "Sắp đến giờ học môn: $subject",
                            message = "Phòng học: $room - GV: $teacher. Vào lớp sau 15 phút nữa, lúc $startTime nhé!",
                            triggerTimeInMillis = triggerFifteenMinsBefore
                        )
                    }

                    // 5. Quay lại màn hình trước đó
                    onBackClick()
                }
            )
        }
    }
}
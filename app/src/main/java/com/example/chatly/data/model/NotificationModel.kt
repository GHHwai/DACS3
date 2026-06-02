package com.example.chatly.data.model

data class NotificationModel(
    val id: String = "",           // ID duy nhất của thông báo (ví dụ: examId_1day, scheduleId_15mins)
    val title: String = "",        // Tiêu đề: "Ngày mai bạn có lịch thi môn:..."
    val message: String = "",      // Nội dung lời nhắn chi tiết
    val timestamp: Long = 0L,      // Mốc thời gian thông báo được lưu (dùng để sắp xếp từ mới đến cũ)
    val isRead: Boolean = false    // Trạng thái đã đọc hay chưa (để làm hiệu ứng tô đậm/nhạt nếu cần)
)
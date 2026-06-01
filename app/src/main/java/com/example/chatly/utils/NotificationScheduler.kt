package com.example.chatly.utils

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.chatly.receiver.ScheduleAlarmReceiver
import java.text.SimpleDateFormat
import java.util.*

object NotificationScheduler {

    @SuppressLint("ScheduleExactAlarm")
    fun scheduleNotification(
        context: Context,
        id: String,
        title: String,
        message: String,
        triggerTimeInMillis: Long
    ) {
        // Nếu thời gian đặt lịch đã trôi qua ở quá khứ, không đặt nữa
        if (triggerTimeInMillis <= System.currentTimeMillis()) return

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // ĐÃ BỎ dòng action thừa để tránh lệch bộ lọc Intent nhận ở Receiver
        val intent = Intent(context, ScheduleAlarmReceiver::class.java).apply {
            putExtra("title", title)
            putExtra("message", message)
            putExtra("notificationId", id.hashCode())
        }

        // Dùng hàm mở rộng .or() chuẩn số nguyên để tránh xung đột cờ hệ thống
        val flags = PendingIntent.FLAG_UPDATE_CURRENT.or(PendingIntent.FLAG_IMMUTABLE)

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            id.hashCode(),
            intent,
            flags
        )

        // Sử dụng setAlarmClock giúp hệ thống ưu tiên đánh thức máy dậy chuẩn xác nhất
        val alarmInfo = AlarmManager.AlarmClockInfo(triggerTimeInMillis, pendingIntent)
        alarmManager.setAlarmClock(alarmInfo, pendingIntent)
    }

    // --- HÀM THỬ NGHIỆM: Bắn thông báo sau X giây để bạn test xem tính năng chạy ổn chưa ---
    fun scheduleTestNotification(context: Context, subject: String, secondsInFuture: Int) {
        val triggerTime = System.currentTimeMillis() + (secondsInFuture * 1000)
        scheduleNotification(
            context = context,
            id = "test_id_${System.currentTimeMillis()}",
            title = "Lịch học môn: $subject",
            message = "Đã đến giờ vào lớp rồi bạn ơi! Mau chuẩn bị sách vở nào.",
            triggerTimeInMillis = triggerTime
        )
    }

    // Hàm chuyển đổi dữ liệu ngày thi (dd/MM/yyyy) thành mili-giây (Mặc định báo lúc 7:00 sáng ngày thi)
    fun convertExamDateToMillis(examDateStr: String): Long {
        return try {
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).apply {
                isLenient = false // Chặn gõ ngày ảo dạng 35/12/2026
            }
            val date = sdf.parse("$examDateStr 07:00")
            date?.time ?: 0L
        } catch (e: Exception) {
            e.printStackTrace()
            0L
        }
    }

    // --- HÀM GỐC: Trả về mốc thời gian chính xác của buổi học (Ví dụ: Thứ 2 lúc 14:42) ---
    fun convertStudyTimeToMillis(dayOfWeekStr: String, startTimeStr: String): Long {
        return try {
            val calendar = Calendar.getInstance()
            val timeParts = startTimeStr.split(":")

            val hour = timeParts[0].toInt()
            val minute = timeParts[1].toInt()

            val targetDay = when (dayOfWeekStr.trim().lowercase()) {
                "monday", "thứ hai", "t2", "mon", "thứ 2" -> Calendar.MONDAY
                "tuesday", "thứ ba", "t3", "tue", "thứ 3" -> Calendar.TUESDAY
                "wednesday", "thứ tư", "t4", "wed", "thứ 4" -> Calendar.WEDNESDAY
                "thursday", "thứ năm", "t5", "thu", "thứ 5" -> Calendar.THURSDAY
                "friday", "thứ sáu", "t6", "fri", "thứ 6" -> Calendar.FRIDAY
                "saturday", "thứ bảy", "t7", "sat", "thứ 7" -> Calendar.SATURDAY
                "sunday", "chủ nhật", "cn", "sun" -> Calendar.SUNDAY
                else -> calendar.get(Calendar.DAY_OF_WEEK)
            }

            calendar.set(Calendar.HOUR_OF_DAY, hour)
            calendar.set(Calendar.MINUTE, minute)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)

            if (calendar.get(Calendar.DAY_OF_WEEK) != targetDay) {
                calendar.set(Calendar.DAY_OF_WEEK, targetDay)
            }

            // Nếu thời gian tính toán của tuần này đã trôi qua, tự động dời sang tuần sau
            if (calendar.timeInMillis <= System.currentTimeMillis()) {
                calendar.add(Calendar.WEEK_OF_YEAR, 1)
            }

            calendar.timeInMillis
        } catch (e: Exception) {
            e.printStackTrace()
            0L
        }
    }
}
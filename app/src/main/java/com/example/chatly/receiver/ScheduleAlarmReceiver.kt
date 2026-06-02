package com.example.chatly.receiver

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.example.chatly.data.model.NotificationModel
import com.example.chatly.ui.main.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ScheduleAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent == null) return

        val title = intent.getStringExtra("title") ?: "Schedule Reminder"
        val message = intent.getStringExtra("message") ?: "You have an upcoming event to check!"
        val notificationId = intent.getIntExtra("notificationId", 1)

        // 1. LƯU VÀO FIRESTORE (Chạy ngầm bất đồng bộ bởi Firebase)
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserId != null) {
            val db = FirebaseFirestore.getInstance()
            val newNotificationRef = db.collection("users")
                .document(currentUserId)
                .collection("notifications")
                .document()

            val notificationData = NotificationModel(
                id = newNotificationRef.id,
                title = title,
                message = message,
                timestamp = System.currentTimeMillis(),
                isRead = false
            )
            newNotificationRef.set(notificationData)
        }

        // Kiểm tra quyền hiển thị thông báo trên Android 13+ trước khi xử lý tiếp
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                return // Không có quyền thì dừng lại để tránh rác log
            }
        }

        // 2. PHÂN LOẠI ĐỂ CẤU HÌNH THÔNG BÁO CHUÔNG
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val isScheduleOrExam = title.contains("lịch học", ignoreCase = true) ||
                title.contains("lịch thi", ignoreCase = true) ||
                title.contains("schedule", ignoreCase = true) ||
                title.contains("exam", ignoreCase = true)

        val mainIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, notificationId, mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        if (isScheduleOrExam) {
            // ================================================================
            // DÀNH RIÊNG CHO LỊCH HỌC / LỊCH THI: CHỈ CHUÔNG - KHÔNG BANNER TRƯỢT
            // ================================================================
            val soundChannelId = "schedule_channel_sound_v3"

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    soundChannelId,
                    "Schedule Alarm Sound",
                    NotificationManager.IMPORTANCE_DEFAULT // Mức DEFAULT: Kêu chuông nhưng không nhảy banner xổ xuống
                ).apply {
                    enableLights(true)
                    enableVibration(true)
                    setSound(
                        defaultSoundUri,
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                }
                notificationManager.createNotificationChannel(channel)
            }

            val builder = NotificationCompat.Builder(context, soundChannelId)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setSound(defaultSoundUri) // Hỗ trợ các máy Android cũ dưới API 26
            // ĐÃ BỎ setDefaults(DEFAULT_ALL) để tránh ghi đè âm thanh tùy chỉnh

            notificationManager.notify(notificationId, builder.build())

        } else {
            // ================================================================
            // CÁC THÔNG BÁO KHÁC: TRƯỢT NỔI LÊN TRÊN VÀ TỰ TẮT SAU 7 GIÂY
            // ================================================================
            val urgentChannelId = "schedule_reminders_channel_urgent_v3"

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    urgentChannelId,
                    "Urgent Notifications",
                    NotificationManager.IMPORTANCE_HIGH // Mức HIGH: Ép bung banner trượt lên màn hình
                ).apply {
                    enableLights(true)
                    enableVibration(true)
                }
                notificationManager.createNotificationChannel(channel)
            }

            val builder = NotificationCompat.Builder(context, urgentChannelId)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH) // Match với IMPORTANCE_HIGH của Channel
                .setDefaults(NotificationCompat.DEFAULT_ALL) // Dùng mặc định hệ thống cho loại này ổn
                .setTimeoutAfter(7000) // Tự biến mất sau 7 giây nếu người dùng không bấm

            notificationManager.notify(notificationId, builder.build())
        }
    }
}
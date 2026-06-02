package com.example.chatly.ui.screen

import androidx.compose.animation.*
import androidx.compose.material.icons.filled.Logout
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import com.example.chatly.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Notifications
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onUserClick: (User) -> Unit,
    onAiChatClick: () -> Unit,
    onGroupChatClick: () -> Unit,
    onScheduleClick: () -> Unit,
    onProfileClick: () -> Unit,
    onNotificationClick: () -> Unit,
    onLogout: () -> Unit,
    viewModel: com.example.chatly.ui.main.MainViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val users by viewModel.users.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var currentTitle by remember { mutableStateOf("") }
    var currentMessage by remember { mutableStateOf("") }
    var hasNewNotification by remember { mutableStateOf(false) }
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    LaunchedEffect(currentUserId) {
        if (currentUserId != null) {
            val db = FirebaseFirestore.getInstance()
            db.collection("users")
                .document(currentUserId)
                .collection("notifications")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(1)
                .addSnapshotListener { snapshot, error ->
                    if (error == null && snapshot != null && !snapshot.isEmpty) {
                        val latestDoc = snapshot.documents.firstOrNull()
                        if (latestDoc != null) {
                            val title = latestDoc.getString("title") ?: ""
                            val message = latestDoc.getString("message") ?: ""
                            val timestamp = latestDoc.getLong("timestamp") ?: 0L

                            if (System.currentTimeMillis() - timestamp < 10000) {
                                currentTitle = title
                                currentMessage = message
                                hasNewNotification = true
                            }
                        }
                    }
                }
        }
    }

    Scaffold(
        containerColor = Color(0xFFEFF1F5), // Nền xám dịu xuống để tôn khối trắng lên tối đa
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Chatly",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF2563EB), // Xanh dương hiện đại hơn
                        modifier = Modifier.padding(start = 4.dp)
                    )
                },
                actions = {
                    Row(
                        modifier = Modifier.padding(end = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            onClick = onNotificationClick,
                            shape = CircleShape,
                            color = Color.White,
                            shadowElevation = 4.dp,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Notifications, contentDescription = "Notifications", modifier = Modifier.size(20.dp), tint = Color(0xFF1E293B))
                            }
                        }

                        Surface(
                            onClick = onScheduleClick,
                            shape = CircleShape,
                            color = Color.White,
                            shadowElevation = 4.dp,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.CalendarMonth, contentDescription = "Schedule", modifier = Modifier.size(20.dp), tint = Color(0xFF1E293B))
                            }
                        }

                        Surface(
                            onClick = onProfileClick,
                            shape = CircleShape,
                            color = Color.White,
                            shadowElevation = 4.dp,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Person, contentDescription = "Profile", modifier = Modifier.size(20.dp), tint = Color(0xFF1E293B))
                            }
                        }

                        Surface(
                            onClick = { viewModel.logout(); onLogout() },
                            shape = CircleShape,
                            color = Color(0xFFFFF1F1),
                            shadowElevation = 4.dp,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Logout, contentDescription = "Logout", modifier = Modifier.size(18.dp), tint = Color(0xFFEF4444))
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFEFF1F5)
                )
            )
        },
        floatingActionButton = {
            // --- 🛠️ THAY ĐỔI: HAI NÚT TO BẰNG NHAU, MÀU XANH DƯƠNG PASTEL NHẠT ---
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.padding(bottom = 12.dp, end = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // 1. NÚT CHAT AI TO RỘNG
                Surface(
                    onClick = onAiChatClick,
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF69A2EF), // Màu xanh dương nhạt cực soft
                    modifier = Modifier
                        .width(135.dp)
                        .height(48.dp)
                        .shadow(8.dp, RoundedCornerShape(16.dp), ambientColor = Color(0xFF2563EB).copy(alpha = 0.25f)),
                    border = BorderStroke(1.dp, Color(0xFFBFDBFE))
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, tint = Color(
                            0xFF2A4BB6
                        ), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("AI Chat", fontWeight = FontWeight.Black, color = Color.White, fontSize = 17.sp)
                    }
                }

                // 2. NÚT GROUPS TO BẰNG KHÍT NÚT AI CHAT
                Surface(
                    onClick = onGroupChatClick,
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF69A2EF), // Màu xanh dương nhẹ khác tông một tí cho đẹp
                    modifier = Modifier
                        .width(135.dp)
                        .height(48.dp)
                        .shadow(8.dp, RoundedCornerShape(16.dp), ambientColor = Color(0xFF2563EB).copy(alpha = 0.25f)),
                    border = BorderStroke(1.dp, Color(0xFFDBEAFE))
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Groups, contentDescription = null, tint = Color(0xFF1D4ED8), modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Groups", fontWeight = FontWeight.Black, color = Color.White, fontSize = 17.sp)
                    }
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(strokeWidth = 3.dp)
                }
            } else {
                Column(modifier = Modifier.padding(padding)) {
                    Text(
                        text = "Direct Messages",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(start = 22.dp, top = 16.dp, bottom = 12.dp),
                        color = Color(0xFF1E3A8A).copy(alpha = 0.85f)
                    )
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 140.dp) // Chừa khoảng trống rộng hơn cho 2 nút to
                    ) {
                        items(users, key = { it.uid }) { user ->
                            UserItem(user = user, onClick = { onUserClick(user) })
                        }
                    }
                }
            }

            if (hasNewNotification && currentTitle.isNotBlank()) {
                NotificationController(
                    notificationTitle = currentTitle,
                    notificationMessage = currentMessage,
                    onDismiss = { hasNewNotification = false }
                )
            }
        }
    }
}

@Composable
fun UserItem(user: User, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            // Ép đổ bóng tầng sâu nhiều lớp thực tế
            .shadow(
                elevation = 10.dp,
                shape = RoundedCornerShape(20.dp),
                clip = false,
                ambientColor = Color(0xFF0F172A).copy(alpha = 0.28f),
                spotColor = Color(0xFF0F172A).copy(alpha = 0.32f)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        // Thêm viền xám đậm nhẹ phủ quanh mép để chặn mắt nhìn, tạo cảm giác card nổi hẳn lên
        border = BorderStroke(1.5.dp, Color(0xFFE2E8F0))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .shadow(3.dp, CircleShape)
                    .background(Color.White, CircleShape)
                    .padding(2.dp)
            ) {
                AsyncImage(
                    model = user.photoUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop,
                    error = painterResource(com.example.chatly.R.drawable.logo_icon)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.displayName?.ifEmpty { "User" } ?: "User",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = user.email,
                    fontSize = 13.sp,
                    color = Color(0xFF475569),
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun NotificationController(
    notificationTitle: String,
    notificationMessage: String,
    onDismiss: () -> Unit
) {
    val isScheduleOrExam = notificationTitle.contains("lịch học", ignoreCase = true) ||
            notificationTitle.contains("lịch thi", ignoreCase = true) ||
            notificationTitle.contains("schedule", ignoreCase = true) ||
            notificationTitle.contains("exam", ignoreCase = true)||
            notificationTitle.contains("giờ học", ignoreCase = true)

    if (isScheduleOrExam) {
        AlertDialog(
            onDismissRequest = onDismiss,
            shape = RoundedCornerShape(24.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color(0xFFE3F2FD), shape = CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = Color(0xFF1E88E5),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "New Reminder",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Text(
                        text = notificationTitle,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.primary,
                        lineHeight = 22.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(
                            text = notificationMessage,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(12.dp),
                            lineHeight = 20.sp
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 10.dp),
                    modifier = Modifier.padding(bottom = 4.dp, end = 4.dp)
                ) {
                    Text(
                        text = "Got it",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        )
    } else {
        var isVisible by remember { mutableStateOf(true) }

        LaunchedEffect(notificationTitle) {
            delay(4000)
            isVisible = false
            onDismiss()
        }

        Box(modifier = Modifier.fillMaxSize()) {
            AnimatedVisibility(
                visible = isVisible,
                enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp, start = 16.dp, end = 16.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(8.dp, shape = RoundedCornerShape(16.dp)),
                    color = MaterialTheme.colorScheme.inverseSurface,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(MaterialTheme.colorScheme.inverseOnSurface.copy(alpha = 0.2f), shape = CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Notifications,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.inverseOnSurface,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = notificationTitle,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.inverseSurface,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = notificationMessage,
                                color = MaterialTheme.colorScheme.inverseOnSurface.copy(alpha = 0.8f),
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
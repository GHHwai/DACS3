package com.example.chatly.ui.chat

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.chatly.data.model.Group
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupsScreen(
    viewModel: GroupChatViewModel, // Nhận vào ViewModel ở đây
    onGroupClick: (Group) -> Unit,
    onBackClick: () -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    var newGroupName by remember { mutableStateOf("") }

    // Tự động load danh sách nhóm từ Firebase/Repository khi màn hình mở lên
    LaunchedEffect(Unit) {
        viewModel.loadGroups()
    }

    // Quan sát danh sách nhóm từ ViewModel (Dữ liệu thay đổi UI tự động cập nhật)
    val groups by viewModel.groups.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = "Group") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showDialog = true }) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Thêm nhóm"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            items(groups) { group ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .clickable { onGroupClick(group) }
                ) {
                    Text(
                        text = group.groupName,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = {
                    showDialog = false
                    newGroupName = ""
                },
                title = { Text(text = "Tạo nhóm mới") },
                text = {
                    OutlinedTextField(
                        value = newGroupName,
                        onValueChange = { newGroupName = it },
                        label = { Text("Nhập tên nhóm") },
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newGroupName.isNotBlank()) {
                                // 1. Lấy UID của chính bạn đang đăng nhập
                                val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: "unknown"

                                // 2. Điền đầy đủ thông tin vào Object Group trước khi gửi lên Firebase
                                val newGroup = Group(
                                    id = "", // Để trống vì bên Repository bạn đã dùng doc.id rồi, rất tốt!
                                    groupName = newGroupName,
                                    createdBy = currentUserId,                      // Người tạo nhóm
                                    members = listOf(currentUserId),                // Người tạo tự động là thành viên đầu tiên
                                    createdAt = System.currentTimeMillis()
                                )

                                viewModel.createGroup(newGroup)
                                showDialog = false
                                newGroupName = ""
                            }
                        }
                    ) {
                        Text("Tạo")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showDialog = false
                        newGroupName = ""
                    }) {
                        Text("Hủy")
                    }
                }
            )
        }
    }
}
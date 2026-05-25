package com.example.chatly.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.chatly.data.model.Group
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupsScreen(
    viewModel: GroupChatViewModel,
    onGroupClick: (Group) -> Unit,
    onBackClick: () -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    var newGroupName by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.loadGroups()
    }

    val groups by viewModel.groups.collectAsState()
    val bubbleBrush = Brush.linearGradient(colors = listOf(Color(0xFF9FF3E8), Color(0xFFC0E0FF)))

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = "Group") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                actions = {
                    IconButton(onClick = { showDialog = true }) {
                        Icon(imageVector = Icons.Filled.Add, contentDescription = "Thêm nhóm")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding).background(Color(0xFFFAFAFA)),
            contentPadding = PaddingValues(top = 8.dp)
        ) {
            items(groups) { group ->
                // --- ĐÃ BIẾN THÀNH CARD GRADIENT VIỀN KIM LOẠI ĐỒNG BỘ ---
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .shadow(4.dp, shape = RoundedCornerShape(12.dp))
                        .border(width = 2.dp, color = Color(0xFFB0C4DE), shape = RoundedCornerShape(12.dp))
                        .background(brush = bubbleBrush, shape = RoundedCornerShape(12.dp))
                        .clickable { onGroupClick(group) }
                        .padding(16.dp)
                ) {
                    Text(
                        text = group.groupName,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF2C3E50)
                    )
                }
            }
        }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false; newGroupName = "" },
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
                                val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: "unknown"
                                val newGroup = Group(
                                    id = "",
                                    groupName = newGroupName,
                                    createdBy = currentUserId,
                                    members = listOf(currentUserId),
                                    createdAt = System.currentTimeMillis()
                                )
                                viewModel.createGroup(newGroup)
                                showDialog = false; newGroupName = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7B3399))
                    ) { Text("Tạo") }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false; newGroupName = "" }) { Text("Hủy") }
                }
            )
        }
    }
}
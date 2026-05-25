package com.example.chatly.ui.chat

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.chatly.data.model.GroupMessage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Query

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupChatScreen(
    groupId: String,
    groupName: String,
    viewModel: GroupChatViewModel,
    onBackClick: () -> Unit
) {
    var text by remember { mutableStateOf("") }
    var showMenu by remember { mutableStateOf(false) }
    var showInviteDialog by remember { mutableStateOf(false) }
    var showRemoveDialog by remember { mutableStateOf(false) }

    LaunchedEffect(groupId) {
        viewModel.loadMessages(groupId)
        viewModel.loadAllUsers()
    }

    val messages by viewModel.messages.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState()

    val currentUser = FirebaseAuth.getInstance().currentUser
    val currentUserId = currentUser?.uid ?: ""
    val currentUserName = currentUser?.displayName ?: "Ai đó"

    val currentGroup = viewModel.groups.collectAsState().value.find { it.id == groupId }
    val currentGroupMembers = currentGroup?.members ?: emptyList()
    val groupCreatorId = currentGroup?.createdBy ?: ""

    val isStillInGroup = currentGroupMembers.contains(currentUserId)
    val isCreator = currentUserId == groupCreatorId

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = groupName) },
                // --- ĐÂY LÀ CHỖ THÊM ĐỂ ĐỔI MÀU XANH DƯƠNG NHẠT ---
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = androidx.compose.ui.graphics.Color(0xFFD0E8FF)
                ),
                // ------------------------------------------------
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                actions = {
                    if (isStillInGroup) {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(imageVector = Icons.Default.MoreVert, contentDescription = "Tùy chọn")
                        }
                    }

                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(
                            text = { Text("Mời thêm thành viên") },
                            onClick = {
                                showMenu = false
                                showInviteDialog = true
                            }
                        )

                        if (isCreator) {
                            DropdownMenuItem(
                                text = { Text("Xóa thành viên", color = androidx.compose.ui.graphics.Color(0xFFFFA500)) },
                                onClick = {
                                    showMenu = false
                                    showRemoveDialog = true
                                }
                            )
                        }

                        DropdownMenuItem(
                            text = { Text("Rời khỏi nhóm", color = androidx.compose.ui.graphics.Color.Red) },
                            onClick = {
                                showMenu = false

                                val leaveNotification = GroupMessage(
                                    id = System.currentTimeMillis().toString(),
                                    groupId = groupId,
                                    senderName = "Hệ thống",
                                    content = "$currentUserName đã rời khỏi nhóm"
                                )
                                viewModel.sendMessage(leaveNotification)

                                val updatedMembers = currentGroupMembers.filter { it != currentUserId }

                                if (updatedMembers.isEmpty()) {
                                    viewModel.deleteGroup(groupId) { success ->
                                        if (success) onBackClick()
                                    }
                                } else {
                                    viewModel.updateMembers(groupId, updatedMembers)
                                }
                            }
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
                items(messages) { msg ->
                    Card(modifier = Modifier.padding(8.dp).fillMaxWidth()) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(
                                text = msg.senderName,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (msg.senderName == "Hệ thống") androidx.compose.ui.graphics.Color.Gray else MaterialTheme.colorScheme.primary
                            )
                            Text(text = msg.content, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }

            if (isStillInGroup) {
                Row(modifier = Modifier.padding(8.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    TextField(
                        value = text,
                        onValueChange = { text = it },
                        placeholder = { Text("Nhập tin nhắn...") },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = {
                        if (text.isNotBlank()) {
                            val newMessage = GroupMessage(
                                id = System.currentTimeMillis().toString(),
                                groupId = groupId,
                                senderName = "Me",
                                content = text
                            )
                            viewModel.sendMessage(newMessage)
                            text = ""
                        }
                    }) { Text("Gửi") }
                }
            } else {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Bạn đã rời khỏi nhóm này. Không thể gửi tin nhắn.",
                        color = androidx.compose.ui.graphics.Color.Red,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(16.dp)
                    )
                }
            }
        }
    }

    // --- DIALOG MỜI THÀNH VIÊN CÓ THANH TÌM KIẾM ---
    if (showInviteDialog) {
        val usersNotInGroup = allUsers.filter { !currentGroupMembers.contains(it.uid) }
        var searchQuery by remember { mutableStateOf("") }
        val membersToInvite = remember { mutableStateListOf<String>() }

        val filteredUsers = usersNotInGroup.filter {
            (it.displayName ?: "Ẩn danh").contains(searchQuery, ignoreCase = true)
        }

        AlertDialog(
            onDismissRequest = { showInviteDialog = false; membersToInvite.clear(); searchQuery = "" },
            title = { Text("Mời thành viên mới") },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Tìm tên thành viên...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Tìm kiếm") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    if (filteredUsers.isEmpty()) {
                        Text(
                            text = if (searchQuery.isEmpty()) "Tất cả mọi người đã ở trong nhóm." else "Không tìm thấy thành viên nào khớp.",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 250.dp)) {
                            items(filteredUsers) { user ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            if (membersToInvite.contains(user.uid)) membersToInvite.remove(user.uid)
                                            else membersToInvite.add(user.uid)
                                        }
                                        .padding(vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = membersToInvite.contains(user.uid),
                                        onCheckedChange = { isChecked ->
                                            if (isChecked) membersToInvite.add(user.uid)
                                            else membersToInvite.remove(user.uid)
                                        }
                                    )
                                    Text(text = user.displayName ?: "Ẩn danh", modifier = Modifier.padding(start = 8.dp))
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (membersToInvite.isNotEmpty()) {
                            val updatedMembers = currentGroupMembers.toMutableList()
                            updatedMembers.addAll(membersToInvite)
                            viewModel.updateMembers(groupId, updatedMembers)
                        }
                        showInviteDialog = false
                        membersToInvite.clear()
                        searchQuery = ""
                    }
                ) { Text("Mời") }
            },
            dismissButton = {
                TextButton(onClick = { showInviteDialog = false; membersToInvite.clear(); searchQuery = "" }) { Text("Hủy") }
            }
        )
    }

    // --- DIALOG XÓA THÀNH VIÊN DÀNH RIÊNG CHO TRƯỞNG NHÓM ---
    if (showRemoveDialog) {
        val usersInGroup = allUsers.filter { currentGroupMembers.contains(it.uid) && it.uid != currentUserId }
        var removeSearchQuery by remember { mutableStateOf("") }
        val membersToRemove = remember { mutableStateListOf<String>() }

        val filteredGroupUsers = usersInGroup.filter {
            (it.displayName ?: "Ẩn danh").contains(removeSearchQuery, ignoreCase = true)
        }

        AlertDialog(
            onDismissRequest = { showRemoveDialog = false; membersToRemove.clear(); removeSearchQuery = "" },
            title = { Text("Xóa thành viên khỏi nhóm") },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = removeSearchQuery,
                        onValueChange = { removeSearchQuery = it },
                        placeholder = { Text("Tìm thành viên cần xóa...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Tìm kiếm") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    if (filteredGroupUsers.isEmpty()) {
                        Text(
                            text = if (removeSearchQuery.isEmpty()) "Không có thành viên nào khác để xóa." else "Không tìm thấy thành viên trùng khớp.",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 250.dp)) {
                            items(filteredGroupUsers) { user ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            if (membersToRemove.contains(user.uid)) membersToRemove.remove(user.uid)
                                            else membersToRemove.add(user.uid)
                                        }
                                        .padding(vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = membersToRemove.contains(user.uid),
                                        onCheckedChange = { isChecked ->
                                            if (isChecked) membersToRemove.add(user.uid)
                                            else membersToRemove.remove(user.uid)
                                        }
                                    )
                                    Text(text = user.displayName ?: "Ẩn danh", modifier = Modifier.padding(start = 8.dp))
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    onClick = {
                        if (membersToRemove.isNotEmpty()) {
                            membersToRemove.forEach { uid ->
                                val targetUser = allUsers.find { it.uid == uid }
                                val targetName = targetUser?.displayName ?: "Thành viên"
                                val removeNotification = GroupMessage(
                                    id = System.currentTimeMillis().toString() + uid,
                                    groupId = groupId,
                                    senderName = "Hệ thống",
                                    content = "$targetName đã bị xóa khỏi nhóm bởi Trưởng nhóm"
                                )
                                viewModel.sendMessage(removeNotification)
                            }

                            val updatedMembers = currentGroupMembers.filter { !membersToRemove.contains(it) }
                            viewModel.updateMembers(groupId, updatedMembers)
                        }
                        showRemoveDialog = false
                        membersToRemove.clear()
                        removeSearchQuery = ""
                    }
                ) { Text("Xóa", color = androidx.compose.ui.graphics.Color.White) }
            },
            dismissButton = {
                TextButton(onClick = { showRemoveDialog = false; membersToRemove.clear(); removeSearchQuery = "" }) { Text("Hủy") }
            }
        )
    }
}
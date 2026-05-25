package com.example.chatly.ui.screen

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.chatly.data.model.GroupMessage
import com.example.chatly.ui.chat.GroupChatViewModel
import com.example.chatly.ui.components.ChatlyTopAppBar
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupChatScreen(
    groupId: String,
    groupName: String,
    viewModel: GroupChatViewModel,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    var text by remember { mutableStateOf("") }
    var showMenu by remember { mutableStateOf(false) }
    var showInviteDialog by remember { mutableStateOf(false) }
    var showRemoveDialog by remember { mutableStateOf(false) }
    var showMembersDialog by remember { mutableStateOf(false) }

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

    val listState = rememberLazyListState()

    // Định nghĩa màu Gradient cho bong bóng chat và Menu giống ảnh mẫu
    val bubbleBrush = Brush.linearGradient(colors = listOf(Color(0xFF9FF3E8), Color(0xFFC0E0FF)))
    val menuBrush = Brush.linearGradient(colors = listOf(Color(0xFF7B3399), Color(0xFF269DAB)))
    val menuBorderBrush = Brush.linearGradient(colors = listOf(Color(0xFFFFCCFF), Color(0xFF9FF3E8)))

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.uploadFileToFreeService(context, groupId, currentUserName, it, isImage = true) }
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.uploadFileToFreeService(context, groupId, currentUserName, it, isImage = false) }
    }

    LaunchedEffect(groupId) {
        viewModel.loadMessages(groupId)
        viewModel.loadAllUsers()
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            ChatlyTopAppBar(
                title = groupName,
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        viewModel.getGroupMembersDetail(currentGroupMembers)
                        showMembersDialog = true
                    }) {
                        Icon(imageVector = Icons.Default.People, contentDescription = "View Members")
                    }

                    if (isStillInGroup) {
                        Box {
                            IconButton(onClick = { showMenu = true }) {
                                Icon(imageVector = Icons.Default.MoreVert, contentDescription = "Options")
                            }

                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false },
                                modifier = Modifier
                                    .width(200.dp)
                                    .border(width = 2.dp, brush = menuBorderBrush, shape = RoundedCornerShape(12.dp))
                                    .background(brush = menuBrush, shape = RoundedCornerShape(12.dp))
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Invite members", color = Color.White) },
                                    onClick = { showMenu = false; showInviteDialog = true }
                                )
                                if (isCreator) {
                                    DropdownMenuItem(
                                        text = { Text("Remove members", color = Color(0xFFFFA500)) },
                                        onClick = { showMenu = false; showRemoveDialog = true }
                                    )
                                }
                                DropdownMenuItem(
                                    text = { Text("Leave group", color = Color(0xFFFF4D4D)) },
                                    onClick = {
                                        showMenu = false
                                        val leaveNotification = GroupMessage(
                                            id = System.currentTimeMillis().toString(),
                                            groupId = groupId, senderName = "System", content = "$currentUserName left the group"
                                        )
                                        viewModel.sendMessage(leaveNotification)

                                        val updatedMembers = currentGroupMembers.filter { it != currentUserId }
                                        if (updatedMembers.isEmpty()) {
                                            viewModel.deleteGroup(groupId) { success -> if (success) onBackClick() }
                                        } else {
                                            viewModel.updateMembers(groupId, updatedMembers)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding).background(Color(0xFFFAFAFA))) {

            // --- DANH SÁCH TIN NHẮN ---
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(12.dp)
            ) {
                items(messages) { msg ->
                    val isMe = msg.senderName == currentUserName

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                    ) {
                        // --- ĐÃ ĐỔI THÀNH BONG BÓNG CHAT GRADIENT VIỀN KIM LOẠI BẠC ---
                        Box(
                            modifier = Modifier
                                .widthIn(max = 280.dp)
                                .shadow(4.dp, shape = RoundedCornerShape(16.dp))
                                .border(width = 2.dp, color = Color(0xFFB0C4DE), shape = RoundedCornerShape(16.dp)) // Viền bạc xám kim loại
                                .background(brush = bubbleBrush, shape = RoundedCornerShape(16.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                if (!isMe) {
                                    Text(
                                        text = msg.senderName,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (msg.senderName == "Hệ thống") Color.Gray else Color(0xFF1E6F7D)
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                }

                                Text(text = msg.content, style = MaterialTheme.typography.bodyLarge, color = Color(0xFF2C3E50))

                                if (!msg.imageUrl.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    AsyncImage(
                                        model = msg.imageUrl,
                                        contentDescription = "Hình ảnh tin nhắn",
                                        modifier = Modifier.fillMaxWidth().heightIn(max = 200.dp).clip(RoundedCornerShape(8.dp)),
                                        contentScale = ContentScale.Fit
                                    )
                                }

                                if (!msg.fileUrl.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "📥 Nhấn vào đây để tải tập tin",
                                        color = Color(0xFF0066CC),
                                        style = MaterialTheme.typography.bodyMedium.copy(textDecoration = TextDecoration.Underline),
                                        modifier = Modifier.clickable {
                                            try {
                                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(msg.fileUrl))
                                                context.startActivity(intent)
                                            } catch (e: Exception) { e.printStackTrace() }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // --- THANH NHẬP LIỆU PHÍA DƯỚI TÙY BIẾN BO TRÒN TRẮNG ---
            if (isStillInGroup) {
                Row(
                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { imagePickerLauncher.launch("image/*") }) {
                        Icon(imageVector = Icons.Default.Image, contentDescription = "Chọn ảnh", tint = Color(0xFF269DAB))
                    }

                    IconButton(onClick = { filePickerLauncher.launch("*/*") }) {
                        Icon(imageVector = Icons.Default.AttachFile, contentDescription = "Chọn tài liệu", tint = Color(0xFF269DAB))
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    TextField(
                        value = text,
                        onValueChange = { text = it },
                        placeholder = { Text("Nhập tin nhắn...") },
                        modifier = Modifier.weight(1f),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF0F4F8),
                            unfocusedContainerColor = Color(0xFFF0F4F8),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (text.isNotBlank()) {
                                val newMessage = GroupMessage(
                                    id = System.currentTimeMillis().toString(),
                                    groupId = groupId, senderName = currentUserName, content = text
                                )
                                viewModel.sendMessage(newMessage)
                                text = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7B3399)), // Nút màu tím thẫm của menu
                        shape = RoundedCornerShape(24.dp)
                    ) { Text("Gửi") }
                }
            } else {
                Surface(color = Color(0xFFFFE6E6), modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Bạn đã rời khỏi nhóm này. Không thể gửi tin nhắn.",
                        color = Color.Red, style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(16.dp)
                    )
                }
            }
        }
    }

    // Các Khối Dialog (Invite, Remove, Members) giữ nguyên toàn bộ logic của bạn...
    if (showInviteDialog) {
        val usersNotInGroup = allUsers.filter { !currentGroupMembers.contains(it.uid) }
        var searchQuery by remember { mutableStateOf("") }
        val membersToInvite = remember { mutableStateListOf<String>() }
        val filteredUsers = usersNotInGroup.filter { (it.displayName ?: "Ẩn danh").contains(searchQuery, ignoreCase = true) }

        AlertDialog(
            onDismissRequest = { showInviteDialog = false; membersToInvite.clear(); searchQuery = "" },
            title = { Text("Mời thành viên mới") },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = searchQuery, onValueChange = { searchQuery = it },
                        placeholder = { Text("Tìm tên thành viên...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Tìm kiếm") },
                        modifier = Modifier.fillMaxWidth(), singleLine = true
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    if (filteredUsers.isEmpty()) {
                        Text(text = if (searchQuery.isEmpty()) "Tất cả mọi người đã ở trong nhóm." else "Không tìm thấy thành viên nào khớp.", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(vertical = 16.dp))
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 250.dp)) {
                            items(filteredUsers) { user ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().clickable {
                                        if (membersToInvite.contains(user.uid)) membersToInvite.remove(user.uid) else membersToInvite.add(user.uid)
                                    }.padding(vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(checked = membersToInvite.contains(user.uid), onCheckedChange = { isChecked -> if (isChecked) membersToInvite.add(user.uid) else membersToInvite.remove(user.uid) })
                                    Text(text = user.displayName ?: "Ẩn danh", modifier = Modifier.padding(start = 8.dp))
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (membersToInvite.isNotEmpty()) {
                        val updatedMembers = currentGroupMembers.toMutableList().apply { addAll(membersToInvite) }
                        viewModel.updateMembers(groupId, updatedMembers)
                    }
                    showInviteDialog = false; membersToInvite.clear(); searchQuery = ""
                }) { Text("Mời") }
            },
            dismissButton = { TextButton(onClick = { showInviteDialog = false; membersToInvite.clear(); searchQuery = "" }) { Text("Hủy") } }
        )
    }

    if (showRemoveDialog) {
        val usersInGroup = allUsers.filter { currentGroupMembers.contains(it.uid) && it.uid != currentUserId }
        var removeSearchQuery by remember { mutableStateOf("") }
        val membersToRemove = remember { mutableStateListOf<String>() }
        val filteredGroupUsers = usersInGroup.filter { (it.displayName ?: "Ẩn danh").contains(removeSearchQuery, ignoreCase = true) }

        AlertDialog(
            onDismissRequest = { showRemoveDialog = false; membersToRemove.clear(); removeSearchQuery = "" },
            title = { Text("Xóa thành viên khỏi nhóm") },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = removeSearchQuery, onValueChange = { removeSearchQuery = it },
                        placeholder = { Text("Tìm thành viên cần xóa...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Tìm kiếm") },
                        modifier = Modifier.fillMaxWidth(), singleLine = true
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    if (filteredGroupUsers.isEmpty()) {
                        Text(text = if (removeSearchQuery.isEmpty()) "Không có thành viên nào khác để xóa." else "Không tìm thấy thành viên trùng khớp.", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(vertical = 16.dp))
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 250.dp)) {
                            items(filteredGroupUsers) { user ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().clickable {
                                        if (membersToRemove.contains(user.uid)) membersToRemove.remove(user.uid) else membersToRemove.add(user.uid)
                                    }.padding(vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(checked = membersToRemove.contains(user.uid), onCheckedChange = { isChecked -> if (isChecked) membersToRemove.add(user.uid) else membersToRemove.remove(user.uid) })
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
                                    groupId = groupId, senderName = "Hệ thống", content = "$targetName đã bị xóa khỏi nhóm bởi Trưởng nhóm"
                                )
                                viewModel.sendMessage(removeNotification)
                            }
                            val updatedMembers = currentGroupMembers.filter { !membersToRemove.contains(it) }
                            viewModel.updateMembers(groupId, updatedMembers)
                        }
                        showRemoveDialog = false; membersToRemove.clear(); removeSearchQuery = ""
                    }
                ) { Text("Xóa", color = Color.White) }
            },
            dismissButton = { TextButton(onClick = { showRemoveDialog = false; membersToRemove.clear(); removeSearchQuery = "" }) { Text("Hủy") } }
        )
    }

    if (showMembersDialog) {
        AlertDialog(
            onDismissRequest = { showMembersDialog = false },
            title = { Text(text = "Thành viên nhóm (${currentGroupMembers.size})") },
            text = {
                Box(modifier = Modifier.heightIn(max = 300.dp)) {
                    LazyColumn {
                        items(viewModel.groupMembersInfo) { member ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AsyncImage(
                                    model = member.photoUrl ?: Icons.Default.Person,
                                    contentDescription = null,
                                    modifier = Modifier.size(40.dp).clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(text = member.displayName ?: "Thành viên Chatly", style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showMembersDialog = false }) { Text("Đóng") } }
        )
    }
}
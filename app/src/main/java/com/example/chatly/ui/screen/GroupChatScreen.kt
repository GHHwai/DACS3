package com.example.chatly.ui.screen

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.chatly.data.model.GroupMessage
import com.example.chatly.ui.chat.GroupChatViewModel
import com.example.chatly.ui.components.ChatlyTopAppBar
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupChatScreen(
    groupId: String,
    groupName: String,
    viewModel: GroupChatViewModel,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var text by remember { mutableStateOf("") }
    var showMenu by remember { mutableStateOf(false) }
    var showInviteDialog by remember { mutableStateOf(false) }
    var showRemoveDialog by remember { mutableStateOf(false) }
    var showMembersDialog by remember { mutableStateOf(false) }

    val messages by viewModel.messages.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState()

    val currentUser = FirebaseAuth.getInstance().currentUser
    val currentUserId = currentUser?.uid ?: ""

    val myUserDetail = allUsers.find { it.uid == currentUserId }
    val currentUserName = myUserDetail?.displayName ?: currentUser?.displayName ?: "Tôi"

    val currentGroup = viewModel.groups.collectAsState().value.find { it.id == groupId }
    val currentGroupMembers = currentGroup?.members ?: emptyList()
    val groupCreatorId = currentGroup?.createdBy ?: ""

    val isStillInGroup = currentGroupMembers.contains(currentUserId)
    val isCreator = currentUserId == groupCreatorId

    val listState = rememberLazyListState()

    val sortedMessages = remember(messages) {
        messages.sortedBy { it.timestamp }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.uploadFileToFreeService(context, groupId, currentUserId, it, isImage = true) }
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.uploadFileToFreeService(context, groupId, currentUserId, it, isImage = false) }
    }

    LaunchedEffect(groupId) {
        viewModel.loadMessages(groupId)
        viewModel.loadAllUsers()
    }

    LaunchedEffect(sortedMessages.size) {
        if (sortedMessages.isNotEmpty()) {
            listState.animateScrollToItem(sortedMessages.size - 1)
        }
    }

    Scaffold(
        containerColor = Color(0xFFEFF1F5),
        topBar = {
            ChatlyTopAppBar(
                title = groupName,
                navigationIcon = {
                    IconButton(onClick = { onBackClick() }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color(0xFF1E293B))
                    }
                },
                actions = {
                    IconButton(onClick = {
                        viewModel.getGroupMembersDetail(currentGroupMembers)
                        showMembersDialog = true
                    }) {
                        Icon(imageVector = Icons.Default.People, contentDescription = "View Members", tint = Color(0xFF1E293B))
                    }

                    if (isStillInGroup) {
                        Box {
                            IconButton(onClick = { showMenu = true }) {
                                Icon(imageVector = Icons.Default.MoreVert, contentDescription = "Options", tint = Color(0xFF1E293B))
                            }

                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false },
                                modifier = Modifier
                                    .width(200.dp)
                                    .shadow(12.dp, shape = RoundedCornerShape(16.dp))
                                    .background(Color.White, shape = RoundedCornerShape(16.dp))
                                    .border(BorderStroke(1.dp, Color(0xFFE2E8F0)), shape = RoundedCornerShape(16.dp))
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Mời thành viên", color = Color(0xFF0F172A), fontWeight = FontWeight.Medium) },
                                    onClick = { showMenu = false; showInviteDialog = true }
                                )
                                if (isCreator) {
                                    DropdownMenuItem(
                                        text = { Text("Xóa thành viên", color = Color(0xFFEF4444), fontWeight = FontWeight.Medium) },
                                        onClick = { showMenu = false; showRemoveDialog = true }
                                    )
                                }
                                DropdownMenuItem(
                                    text = { Text("Rời nhóm", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold) },
                                    onClick = {
                                        showMenu = false
                                        val leaveNotification = GroupMessage(
                                            id = System.currentTimeMillis().toString(),
                                            groupId = groupId, senderName = "System", content = "$currentUserName đã rời nhóm"
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
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {

            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(sortedMessages, key = { it.id }) { msg ->
                    val senderUser = allUsers.find { it.uid == msg.senderName || it.displayName == msg.senderName }
                    val finalSenderName = senderUser?.displayName ?: msg.senderName

                    val isMe = msg.senderName == currentUserId || msg.senderName == currentUserName || finalSenderName == currentUserName
                    val isSystem = msg.senderName == "Hệ thống" || msg.senderName == "System"

                    MessageBubble(
                        msg = msg,
                        finalSenderName = finalSenderName,
                        isMe = isMe,
                        isSystem = isSystem,
                        context = context
                    )
                }
            }

            if (isStillInGroup) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.White,
                    shadowElevation = 16.dp
                ) {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { imagePickerLauncher.launch("image/*") }) {
                            Icon(imageVector = Icons.Default.Image, contentDescription = "Chọn ảnh", tint = Color(0xFF475569))
                        }

                        IconButton(onClick = { filePickerLauncher.launch("*/*") }) {
                            Icon(imageVector = Icons.Default.AttachFile, contentDescription = "Chọn tài liệu", tint = Color(0xFF475569))
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        TextField(
                            value = text,
                            onValueChange = { text = it },
                            placeholder = { Text("Nhập tin nhắn...", color = Color(0xFF94A3B8)) },
                            modifier = Modifier.weight(1f),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFFF1F5F9),
                                unfocusedContainerColor = Color(0xFFF1F5F9),
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                focusedTextColor = Color(0xFF0F172A)
                            ),
                            shape = RoundedCornerShape(24.dp)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = {
                                if (text.isNotBlank()) {
                                    val newMessage = GroupMessage(
                                        id = System.currentTimeMillis().toString(),
                                        groupId = groupId,
                                        senderName = currentUserId,
                                        content = text
                                    )
                                    // CHỈ GỬI TIN NHẮN, KHÔNG GỬI THÔNG BÁO FIRESTORE NỮA
                                    viewModel.sendMessage(newMessage)
                                    text = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                            shape = RoundedCornerShape(24.dp),
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
                        ) {
                            Text("Gửi", fontWeight = FontWeight.Black, fontSize = 15.sp)
                        }
                    }
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
    // ... (Phần Dialog giữ nguyên, bỏ bớt phần hiển thị Dialog ở đây cho gọn tin nhắn)
}

// Sub-composable hiển thị bong bóng tin nhắn (Giữ nguyên gốc)
@Composable
fun MessageBubble(
    msg: GroupMessage,
    finalSenderName: String,
    isMe: Boolean,
    isSystem: Boolean,
    context: Context
) {
    if (isSystem) {
        Box(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), contentAlignment = Alignment.Center) {
            Surface(color = Color(0xFFE2E8F0), shape = RoundedCornerShape(12.dp)) {
                Text(text = msg.content, fontSize = 12.sp, color = Color(0xFF475569), modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp))
            }
        }
    } else {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start) {
            Card(
                modifier = Modifier.widthIn(max = 290.dp).shadow(elevation = if (isMe) 2.dp else 4.dp, shape = RoundedCornerShape(18.dp)),
                colors = CardDefaults.cardColors(containerColor = if (isMe) Color(0xFF2563EB) else Color.White),
                border = if (isMe) null else BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    if (!isMe) {
                        Text(text = finalSenderName, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1D4ED8))
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    Text(text = msg.content, fontSize = 15.sp, color = if (isMe) Color.White else Color(0xFF0F172A))

                    if (!msg.imageUrl.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        AsyncImage(model = msg.imageUrl, contentDescription = null, modifier = Modifier.fillMaxWidth().heightIn(max = 200.dp).clip(RoundedCornerShape(12.dp)), contentScale = ContentScale.Crop)
                    }
                    if (!msg.fileUrl.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "📥 Tải tập tin đính kèm",
                            color = if (isMe) Color(0xFF93C5FD) else Color(0xFF2563EB),
                            style = MaterialTheme.typography.bodyMedium.copy(textDecoration = TextDecoration.Underline),
                            modifier = Modifier.clickable {
                                try { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(msg.fileUrl))) } catch (e: Exception) { e.printStackTrace() }
                            }
                        )
                    }
                }
            }
        }
    }
}
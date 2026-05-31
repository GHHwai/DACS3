package com.example.chatly.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.chatly.data.model.AiChatMessage
import com.example.chatly.data.repository.FirebaseAiChatRepository
import com.example.chatly.ui.chat.AiChatViewModel
import com.example.chatly.ui.components.ChatBubble
import com.example.chatly.ui.components.ChatlyTopAppBar
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiChatScreen(
    viewModel: AiChatViewModel =
        viewModel(
            factory = AiChatViewModel.Factory(
                FirebaseAiChatRepository()
            )
        ),
    onBackClick: () -> Unit
) {
    var menuExpandedId by remember { mutableStateOf<String?>(null) }
    var editingSessionId by remember { mutableStateOf<String?>(null) }
    var editText by remember { mutableStateOf("") }
    val messages by viewModel.messages.collectAsState()
    val currentSessionId by
    viewModel.currentSessionId
        .collectAsState()
    val currentUserId =
        FirebaseAuth
            .getInstance()
            .currentUser
            ?.uid
            ?: "guest"
    var input by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val sessions by viewModel.sessions.collectAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    ModalNavigationDrawer(

        drawerState = drawerState,

        drawerContent = {

            ModalDrawerSheet(

                modifier = Modifier.width(300.dp)

            ) {

                Text(
                    text = "Chat History",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(16.dp)
                )

                Button(
                    onClick = {

                        viewModel.createNewChat(
                            currentUserId
                        )

                        scope.launch {
                            drawerState.close()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                ) {
                    Text("New Chat")
                }

                Spacer(
                    modifier = Modifier.height(12.dp)
                )

                LazyColumn {

                    items(sessions) { session ->

                        var expanded by remember { mutableStateOf(false) }

                        NavigationDrawerItem(
                            label = {

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {

                                    if (editingSessionId == session.id) {

                                        TextField(
                                            value = editText,
                                            onValueChange = { editText = it },
                                            singleLine = true,
                                            modifier = Modifier.weight(1f),
                                            colors = TextFieldDefaults.colors(
                                                focusedContainerColor = Color.Transparent,
                                                unfocusedContainerColor = Color.Transparent
                                            )
                                        )

                                        IconButton(onClick = {
                                            viewModel.renameSession(session.id, editText)
                                            editingSessionId = null
                                        }) {
                                            Icon(Icons.Default.Send, contentDescription = "Save")
                                        }

                                    } else {

                                        Text(
                                            text = session.title,
                                            modifier = Modifier.weight(1f)
                                        )

                                        Box {

                                            IconButton(onClick = {
                                                menuExpandedId = session.id
                                            }) {
                                                Icon(Icons.Default.Menu, contentDescription = "More")
                                            }

                                            DropdownMenu(
                                                expanded = menuExpandedId == session.id,
                                                onDismissRequest = {
                                                    menuExpandedId = null
                                                }
                                            ) {

                                                DropdownMenuItem(
                                                    text = { Text("Rename") },
                                                    onClick = {
                                                        editingSessionId = session.id
                                                        editText = session.title
                                                        menuExpandedId = null
                                                    }
                                                )

                                                DropdownMenuItem(
                                                    text = { Text("Delete") },
                                                    onClick = {
                                                        viewModel.deleteSession(session.id)
                                                        menuExpandedId = null
                                                    }
                                                )
                                            }
                                        }                                    }
                                }
                            },
                            selected = session.id == currentSessionId,
                            onClick = {
                                if (editingSessionId == null) {
                                    viewModel.selectSession(session.id)
                                    scope.launch { drawerState.close() }
                                }
                            }
                        )
                    }
                }
            }
        }

    ) {

        Scaffold(
        topBar = {
            ChatlyTopAppBar(
                title = "AI Chat Assistant",
                navigationIcon = {

                    Row {

                        IconButton(
                            onClick = {
                                scope.launch {
                                    drawerState.open()
                                }
                            }
                        ) {
                            Icon(
                                Icons.Default.Menu,
                                contentDescription = "Menu"
                            )
                        }

                        IconButton(
                            onClick = onBackClick
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
        ) {
            // Message List
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                state = listState,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(messages) { message ->
                    ChatBubble(text = message.content, isMine = message.isMine)
                }
            }

            // Input Area
            Surface(
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = input,
                        onValueChange = { input = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Ask me anything...") },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                        )
                    )
                    IconButton(
                        onClick = {
                            if (input.isNotBlank()) {
                                viewModel.sendUserMessage(input)
                                input = ""
                                scope.launch {
                                    if (messages.isNotEmpty()) {
                                        listState.animateScrollToItem(messages.size)
                                    }
                                }
                            }
                        },
                        enabled = input.isNotBlank()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send",
                            tint = if (input.isNotBlank()) MaterialTheme.colorScheme.primary else Color.Gray
                        )
                    }
                }
            }
        }
    }
}
}

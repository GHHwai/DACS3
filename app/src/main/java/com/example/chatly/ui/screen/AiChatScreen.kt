package com.example.chatly.ui.screen

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.chatly.data.repository.FirebaseAiChatRepository
import com.example.chatly.ui.chat.AiChatViewModel
import com.example.chatly.ui.chat.QuizViewModel
import com.example.chatly.ui.components.ChatBubble
import com.example.chatly.ui.components.ChatlyTopAppBar
import com.example.chatly.ui.components.QuizLoadingCard
import com.example.chatly.ui.components.QuizQuestionCard
import com.example.chatly.ui.components.QuizResultScreen
import com.example.chatly.ui.components.QuizTopicDialog
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiChatScreen(
    viewModel: AiChatViewModel = viewModel(
        factory = AiChatViewModel.Factory(FirebaseAiChatRepository())
    ),
    quizViewModel: QuizViewModel = viewModel(factory = QuizViewModel.Factory()),
    onBackClick: () -> Unit
) {
    // ── Chat state ────────────────────────────────────────────────────────────
    var menuExpandedId by remember { mutableStateOf<String?>(null) }
    var editingSessionId by remember { mutableStateOf<String?>(null) }
    var editText by remember { mutableStateOf("") }
    val messages by viewModel.messages.collectAsState()
    val currentSessionId by viewModel.currentSessionId.collectAsState()
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: "guest"
    var input by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val sessions by viewModel.sessions.collectAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // ── Quiz state ────────────────────────────────────────────────────────────
    val quizUiState by quizViewModel.uiState.collectAsState()

    // Auto-scroll when new messages arrive
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1)
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(modifier = Modifier.width(300.dp)) {
                Text(
                    text = "Chat History",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(16.dp)
                )

                // New Chat
                Button(
                    onClick = {
                        quizViewModel.resetQuiz()
                        viewModel.createNewChat(currentUserId)
                        scope.launch { drawerState.close() }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                ) { Text("New Chat") }

                Spacer(modifier = Modifier.height(8.dp))

                // ── Quiz Mode button ──────────────────────────────────────────
                OutlinedButton(
                    onClick = {
                        // Close drawer first, THEN show the dialog once drawer is fully shut
                        scope.launch {
                            drawerState.close()
                            quizViewModel.startQuizSetup()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.tertiary
                    )
                ) {
                    Icon(Icons.Default.Quiz, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Quiz Mode")
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(4.dp))

                // Session list
                LazyColumn {
                    items(sessions) { session ->
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
                                        Text(text = session.title, modifier = Modifier.weight(1f))
                                        Box {
                                            IconButton(onClick = { menuExpandedId = session.id }) {
                                                Icon(Icons.Default.Menu, contentDescription = "More")
                                            }
                                            DropdownMenu(
                                                expanded = menuExpandedId == session.id,
                                                onDismissRequest = { menuExpandedId = null }
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
                                        }
                                    }
                                }
                            },
                            selected = session.id == currentSessionId,
                            onClick = {
                                if (editingSessionId == null) {
                                    quizViewModel.resetQuiz()
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
                    title = if (quizUiState.isQuizMode) "Quiz · ${quizUiState.topicInput}"
                    else "AI Chat Assistant",
                    navigationIcon = {
                        Row {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Default.Menu, contentDescription = "Menu")
                            }
                            IconButton(onClick = onBackClick) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                            }
                        }
                    },
                    actions = {
                        if (quizUiState.isQuizMode) {
                            IconButton(onClick = quizViewModel::resetQuiz) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Exit Quiz",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                )
            }
        ) { padding ->

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // ── Main content: chat or quiz ────────────────────────────────
                AnimatedContent(
                    targetState = quizUiState.isQuizMode,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(300)) togetherWith
                                fadeOut(animationSpec = tween(300))
                    },
                    label = "mode_switch",
                    modifier = Modifier.fillMaxSize()
                ) { isQuizMode ->
                    if (isQuizMode) {
                        // ── QUIZ UI ───────────────────────────────────────────
                        when {
                            quizUiState.isFinished -> QuizResultScreen(
                                uiState = quizUiState,
                                onPlayAgain = quizViewModel::startQuizSetup,
                                onExit = quizViewModel::resetQuiz
                            )
                            quizUiState.isLoading -> QuizLoadingCard()
                            quizUiState.error != null -> QuizErrorCard(
                                message = quizUiState.error!!,
                                onRetry = { quizViewModel.beginQuiz(quizUiState.topicInput) }
                            )
                            quizUiState.currentQuestion != null -> QuizQuestionCard(
                                uiState = quizUiState,
                                onSelectAnswer = quizViewModel::selectAnswer,
                                onNext = quizViewModel::nextQuestion
                            )
                            else -> QuizLoadingCard()
                        }
                    } else {
                        // ── CHAT UI ───────────────────────────────────────────
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .imePadding()
                        ) {
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
                                        placeholder = { Text("Ask me anything…") },
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
                                            tint = if (input.isNotBlank())
                                                MaterialTheme.colorScheme.primary
                                            else Color.Gray
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // ── Topic dialog rendered INSIDE Scaffold Box, on top of everything ──
                if (quizUiState.isTopicSelectionStep) {
                    QuizTopicDialog(
                        uiState = quizUiState,
                        onTopicChange = quizViewModel::onTopicInputChange,
                        onConfirm = { topic ->
                            if (topic.isNotBlank()) quizViewModel.beginQuiz(topic)
                        },
                        onDismiss = quizViewModel::dismissTopicSelection
                    )
                }
            }
        }
    }
}

// ─── Error card ───────────────────────────────────────────────────────────────

@Composable
private fun QuizErrorCard(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("⚠️", fontSize = 48.sp)
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Something went wrong",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onRetry, shape = RoundedCornerShape(50.dp)) {
            Text("Retry")
        }
    }
}

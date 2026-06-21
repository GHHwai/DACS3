package com.example.chatly.ui.screen

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.chatly.data.model.QuizSession
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
import java.text.SimpleDateFormat
import java.util.*

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
    var menuExpandedId by remember { mutableStateOf<String?>(null) }
    val session by quizViewModel.sessionState.collectAsState()
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
    val quizUiState by quizViewModel.uiState.collectAsState()

    // Quiz history
    val quizHistory by quizViewModel.quizHistory.collectAsState()
    val isHistoryLoading by quizViewModel.isHistoryLoading.collectAsState()
    val historySheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showHistorySheet by remember { mutableStateOf(false) }

    // Load history once on entry
    LaunchedEffect(Unit) {
        quizViewModel.loadQuizHistory()
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1)
    }

    // Quiz History Bottom Sheet
    if (showHistorySheet) {
        ModalBottomSheet(
            onDismissRequest = { showHistorySheet = false },
            sheetState = historySheetState,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            QuizHistorySheet(
                history = quizHistory,
                isLoading = isHistoryLoading,
                onRefresh = { quizViewModel.loadQuizHistory() },
                onDismiss = { showHistorySheet = false }
            )
        }
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

                OutlinedButton(
                    onClick = {
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

                Spacer(modifier = Modifier.height(8.dp))

                // Quiz History button in drawer
                OutlinedButton(
                    onClick = {
                        scope.launch {
                            drawerState.close()
                            quizViewModel.loadQuizHistory()
                            showHistorySheet = true
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Icon(Icons.Default.History, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Quiz History")
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(4.dp))

                LazyColumn {
                    items(sessions) { chatSession ->
                        NavigationDrawerItem(
                            label = {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    if (editingSessionId == chatSession.id) {
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
                                            viewModel.renameSession(chatSession.id, editText)
                                            editingSessionId = null
                                        }) {
                                            Icon(Icons.Default.Send, contentDescription = "Save")
                                        }
                                    } else {
                                        Text(
                                            text = chatSession.title,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Box {
                                            IconButton(onClick = { menuExpandedId = chatSession.id }) {
                                                Icon(Icons.Default.Menu, contentDescription = "More")
                                            }
                                            DropdownMenu(
                                                expanded = menuExpandedId == chatSession.id,
                                                onDismissRequest = { menuExpandedId = null }
                                            ) {
                                                DropdownMenuItem(
                                                    text = { Text("Rename") },
                                                    onClick = {
                                                        editingSessionId = chatSession.id
                                                        editText = chatSession.title
                                                        menuExpandedId = null
                                                    }
                                                )
                                                DropdownMenuItem(
                                                    text = { Text("Delete") },
                                                    onClick = {
                                                        viewModel.deleteSession(chatSession.id)
                                                        menuExpandedId = null
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            },
                            selected = chatSession.id == currentSessionId,
                            onClick = {
                                if (editingSessionId == null) {
                                    quizViewModel.resetQuiz()
                                    viewModel.selectSession(chatSession.id)
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
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back"
                                )
                            }
                        }
                    },
                    actions = {
                        // History icon always visible in top bar
                        IconButton(onClick = {
                            quizViewModel.loadQuizHistory()
                            showHistorySheet = true
                        }) {
                            Icon(
                                Icons.Default.History,
                                contentDescription = "Quiz History",
                                tint = MaterialTheme.colorScheme.secondary
                            )
                        }
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
                        Column(modifier = Modifier.fillMaxSize()) {
                            session?.let { quizSession ->
                                QuizSessionBanner(session = quizSession)
                            }
                            Box(modifier = Modifier.weight(1f)) {
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
                            }
                        }
                    } else {
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

// ─── Quiz History Bottom Sheet ────────────────────────────────────────────────

@Composable
private fun QuizHistorySheet(
    history: List<QuizSession>,
    isLoading: Boolean,
    onRefresh: () -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 32.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Quiz History",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Row {
                TextButton(onClick = onRefresh) { Text("Refresh") }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            history.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🎯", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No quiz sessions yet",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Complete a quiz and it will appear here",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 500.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(history) { quizSession ->
                        QuizHistoryCard(session = quizSession)
                    }
                }
            }
        }
    }
}

@Composable
private fun QuizHistoryCard(session: QuizSession) {
    val pct = if (session.totalQuestions > 0)
        (session.correctCount.toFloat() / session.totalQuestions * 100).toInt()
    else 0

    val scoreColor = when {
        pct >= 80 -> Color(0xFF22C55E)
        pct >= 60 -> MaterialTheme.colorScheme.primary
        pct >= 40 -> Color(0xFFF59E0B)
        else -> Color(0xFFEF4444)
    }

    val dateStr = remember(session.createdAt) {
        SimpleDateFormat("MMM dd, yyyy  HH:mm", Locale.getDefault())
            .format(Date(session.createdAt))
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Score circle
            Box(
                modifier = Modifier.size(56.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    progress = { pct / 100f },
                    modifier = Modifier.size(56.dp),
                    strokeWidth = 5.dp,
                    color = scoreColor,
                    trackColor = scoreColor.copy(alpha = 0.15f)
                )
                Text(
                    text = "$pct%",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = scoreColor
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = session.topic,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${session.correctCount} / ${session.totalQuestions} correct",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = dateStr,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }

            // Finished badge
            if (session.isFinished) {
                Surface(
                    shape = RoundedCornerShape(50.dp),
                    color = Color(0xFF22C55E).copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "Done",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF22C55E)
                    )
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(50.dp),
                    color = Color(0xFFF5A70B).copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "In progress",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFAF744C)
                    )
                }
            }
        }
    }
}

// ─── Quiz Session Banner ──────────────────────────────────────────────────────

@Composable
private fun QuizSessionBanner(session: QuizSession) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.primaryContainer,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = session.topic,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    maxLines = 1
                )
                Text(
                    text = "Question ${(session.currentIndex + 1).coerceAtMost(session.totalQuestions)} of ${session.totalQuestions}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(50.dp),
                    color = MaterialTheme.colorScheme.primary
                ) {
                    Text(
                        text = "✓ ${session.correctCount}",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
                Surface(
                    shape = RoundedCornerShape(50.dp),
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = "/ ${session.totalQuestions}",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}

// ─── Error Card ───────────────────────────────────────────────────────────────

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

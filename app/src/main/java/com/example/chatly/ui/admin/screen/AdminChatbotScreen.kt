package com.example.chatly.ui.admin.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.chatly.ui.admin.viewmodel.AdminChatbotViewModel
import com.example.chatly.data.model.admin.ChatbotLog
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminChatbotScreen(
    viewModel: AdminChatbotViewModel,
    onBackClick: () -> Unit
) {
    val logs by viewModel.logs.collectAsState()

    Scaffold(
        topBar = { AdminTopBar(title = "Chatbot Monitor", onBackClick = onBackClick) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(logs.sortedByDescending { it.timestamp }) { log ->
                ChatbotLogCard(log = log)
            }
        }
    }
}

@Composable
fun ChatbotLogCard(log: ChatbotLog) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
    val dateString = dateFormat.format(Date(log.timestamp))

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "User: ${log.userId}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                Text(text = dateString, style = MaterialTheme.typography.labelSmall)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Q: ${log.query}", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "A: ${log.response}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
            
            if (log.status == "error") {
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Error: ${log.errorMessage}", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

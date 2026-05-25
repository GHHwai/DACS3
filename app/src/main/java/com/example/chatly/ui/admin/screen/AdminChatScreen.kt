package com.example.chatly.ui.admin.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.chatly.ui.admin.viewmodel.AdminChatViewModel
import com.example.chatly.data.model.admin.Report
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminChatScreen(
    viewModel: AdminChatViewModel,
    onBackClick: () -> Unit
) {
    val reports by viewModel.reports.collectAsState()

    Scaffold(
        topBar = { AdminTopBar(title = "Chat Reports", onBackClick = onBackClick) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(reports.sortedByDescending { it.timestamp }) { report ->
                ReportCard(
                    report = report,
                    onResolve = { viewModel.resolveReport(report.id) }
                )
            }
        }
    }
}

@Composable
fun ReportCard(report: Report, onResolve: () -> Unit) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    val dateString = dateFormat.format(Date(report.timestamp))

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Reported User: ${report.reportedUserId}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                Text(text = "Reporter: ${report.reporterId}", style = MaterialTheme.typography.labelSmall)
                Text(text = "Reason: ${report.reason}", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 4.dp))
                Text(text = dateString, style = MaterialTheme.typography.labelSmall)
                Text(
                    text = "Status: ${report.status.uppercase()}", 
                    style = MaterialTheme.typography.labelSmall,
                    color = if (report.status == "pending") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            if (report.status == "pending") {
                IconButton(onClick = onResolve) {
                    Icon(Icons.Default.Check, contentDescription = "Resolve", tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

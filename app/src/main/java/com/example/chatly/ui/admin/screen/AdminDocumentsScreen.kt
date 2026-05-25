package com.example.chatly.ui.admin.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.chatly.ui.admin.viewmodel.AdminDocumentsViewModel
import com.example.chatly.data.model.admin.Document

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDocumentsScreen(
    viewModel: AdminDocumentsViewModel,
    onBackClick: () -> Unit
) {
    val documents by viewModel.documents.collectAsState()

    Scaffold(
        topBar = { AdminTopBar(title = "Manage Documents", onBackClick = onBackClick) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(documents) { doc ->
                DocumentCard(
                    document = doc,
                    onApprove = { viewModel.approveDocument(doc.id) },
                    onReject = { viewModel.rejectDocument(doc.id) },
                    onDelete = { viewModel.deleteDocument(doc.id) }
                )
            }
        }
    }
}

@Composable
fun DocumentCard(
    document: Document,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    onDelete: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = document.title, fontWeight = FontWeight.Bold)
                Text(text = "Status: ${document.status.uppercase()}", style = MaterialTheme.typography.labelMedium)
                Text(text = "Uploader: ${document.uploaderId}", style = MaterialTheme.typography.labelSmall)
            }
            if (document.status == "pending") {
                IconButton(onClick = onApprove) {
                    Icon(Icons.Default.Check, contentDescription = "Approve", tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = onReject) {
                    Icon(Icons.Default.Close, contentDescription = "Reject", tint = MaterialTheme.colorScheme.error)
                }
            } else {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

package com.example.chatly.ui.admin.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.chatly.ui.admin.viewmodel.AdminUsersViewModel
import com.example.chatly.data.model.User

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminUsersScreen(
    viewModel: AdminUsersViewModel,
    onBackClick: () -> Unit
) {
    val users by viewModel.users.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    
    val filteredUsers = users.filter { 
        it.email.contains(searchQuery, ignoreCase = true) || 
        (it.displayName?.contains(searchQuery, ignoreCase = true) == true) 
    }

    Scaffold(
        topBar = { AdminTopBar(title = "Manage Users", onBackClick = onBackClick) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Search users by email or name...") },
                singleLine = true
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredUsers) { user ->
                    UserAdminCard(
                        user = user,
                        onToggleStatus = { viewModel.toggleUserStatus(user) }
                    )
                }
            }
        }
    }
}

@Composable
fun UserAdminCard(
    user: User,
    onToggleStatus: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = user.displayName ?: "Unknown", fontWeight = FontWeight.Bold)
                Text(text = user.email, style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = "Status: ${user.status.uppercase()}", 
                    style = MaterialTheme.typography.labelSmall,
                    color = if (user.status == "active") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
            }
            IconButton(onClick = onToggleStatus) {
                if (user.status == "active") {
                    Icon(imageVector = Icons.Default.Block, contentDescription = "Ban User", tint = MaterialTheme.colorScheme.error)
                } else {
                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Unban User", tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

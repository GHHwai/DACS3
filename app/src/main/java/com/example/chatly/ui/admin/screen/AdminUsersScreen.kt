package com.example.chatly.ui.admin.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Search
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
    initialFilter: String = "all",
    onBackClick: () -> Unit
) {
    val users by viewModel.users.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var statusFilter by remember { mutableStateOf(initialFilter) }
    
    val totalCount = users.size
    val activeCount = users.count { it.status == "active" }
    val bannedCount = users.count { it.status == "banned" }

    val filteredUsers = users.filter { 
        val matchesSearch = it.email.contains(searchQuery, ignoreCase = true) || 
                          (it.displayName?.contains(searchQuery, ignoreCase = true) == true)
        val matchesStatus = when (statusFilter) {
            "active" -> it.status == "active"
            "banned" -> it.status == "banned"
            else -> true
        }
        matchesSearch && matchesStatus
    }

    Scaffold(
        topBar = { AdminTopBar(title = "User Management", onBackClick = onBackClick) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Stats Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                UserMiniStat(label = "Total", count = totalCount, modifier = Modifier.weight(1f))
                UserMiniStat(label = "Active", count = activeCount, modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.primary)
                UserMiniStat(label = "Banned", count = bannedCount, modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.error)
            }

            // Search and Filter Bar
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search by name or email...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = statusFilter == "all",
                        onClick = { statusFilter = "all" },
                        label = { Text("All") }
                    )
                    FilterChip(
                        selected = statusFilter == "active",
                        onClick = { statusFilter = "active" },
                        label = { Text("Active") }
                    )
                    FilterChip(
                        selected = statusFilter == "banned",
                        onClick = { statusFilter = "banned" },
                        label = { Text("Banned") }
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
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
fun UserMiniStat(
    label: String,
    count: Int,
    modifier: Modifier = Modifier,
    color: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = count.toString(), style = MaterialTheme.typography.titleLarge, color = color, fontWeight = FontWeight.Bold)
            Text(text = label, style = MaterialTheme.typography.labelSmall)
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
                Surface(
                    color = if (user.status == "active") MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = user.status.uppercase(), 
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (user.status == "active") MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer
                    )
                }
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

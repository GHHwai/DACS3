package com.example.chatly.ui.admin.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.chatly.ui.admin.viewmodel.AdminDashboardViewModel
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    viewModel: AdminDashboardViewModel,
    onNavigateToUsers: () -> Unit,
    onNavigateToSystemData: () -> Unit,
    onNavigateToDocuments: () -> Unit,
    onNavigateToChatbot: () -> Unit,
    onNavigateToChatSystem: () -> Unit,
    onLogout: () -> Unit,
    onBackClick: () -> Unit
) {
    val stats by viewModel.stats.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            AdminTopBar(
                title = "Admin Dashboard",
                onBackClick = onBackClick,
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(
                            Icons.Default.Logout,
                            contentDescription = "Logout"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                Text(
                    text = "System Overview",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        StatCard(
                            title = "Users",
                            value = "${stats?.totalUsers ?: 0}",
                            icon = Icons.Default.People,
                            onClick = onNavigateToUsers
                        )
                    }
                    item {
                        StatCard(
                            title = "Active Users",
                            value = "${stats?.activeUsers ?: 0}",
                            icon = Icons.Default.PersonSearch,
                            onClick = onNavigateToUsers
                        )
                    }
                    item {
                        StatCard(
                            title = "System Data",
                            value = "Manage",
                            icon = Icons.Default.School,
                            onClick = onNavigateToSystemData
                        )
                    }
                    item {
                        StatCard(
                            title = "Documents",
                            value = "${stats?.totalDocuments ?: 0}",
                            icon = Icons.Default.LibraryBooks,
                            onClick = onNavigateToDocuments
                        )
                    }
                    item {
                        StatCard(
                            title = "Chatbot Logs",
                            value = "${stats?.totalChatbotRequests ?: 0}",
                            icon = Icons.Default.SmartToy,
                            onClick = onNavigateToChatbot
                        )
                    }
                    item {
                        StatCard(
                            title = "Reports",
                            value = "${stats?.totalReports ?: 0}",
                            icon = Icons.Default.Warning,
                            onClick = onNavigateToChatSystem
                        )
                    }
                }
            }
        }
    }
}

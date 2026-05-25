package com.example.chatly.ui.admin.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.chatly.ui.admin.viewmodel.AdminSystemDataViewModel
import com.example.chatly.data.model.admin.SystemData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminSystemDataScreen(
    viewModel: AdminSystemDataViewModel,
    onBackClick: () -> Unit
) {
    val dataList by viewModel.systemDataList.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Subject", "Schedule", "Exam")

    var showAddDialog by remember { mutableStateOf(false) }

    LaunchedEffect(selectedTab) {
        viewModel.loadData(tabs[selectedTab].lowercase())
    }

    Scaffold(
        topBar = { AdminTopBar(title = "System Data", onBackClick = onBackClick) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(dataList) { item ->
                    SystemDataCard(
                        item = item,
                        onDelete = { viewModel.deleteData(item.id) }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddSystemDataDialog(
            type = tabs[selectedTab],
            onDismiss = { showAddDialog = false },
            onAdd = { name, desc, date ->
                viewModel.addData(name, desc, date)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun SystemDataCard(item: SystemData, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = item.name, fontWeight = FontWeight.Bold)
                Text(text = item.description, style = MaterialTheme.typography.bodyMedium)
                Text(text = "Date: ${item.date}", style = MaterialTheme.typography.labelSmall)
            }
            IconButton(onClick = onDelete) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
fun AddSystemDataDialog(
    type: String,
    onDismiss: () -> Unit,
    onAdd: (String, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add new $type") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, singleLine = true)
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, singleLine = true)
                OutlinedTextField(value = date, onValueChange = { date = it }, label = { Text("Date") }, singleLine = true)
            }
        },
        confirmButton = {
            Button(onClick = { onAdd(name, description, date) }) { Text("Add") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

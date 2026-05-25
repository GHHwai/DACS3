package com.example.chatly.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.chatly.ui.components.ChatlyButton
import com.example.chatly.ui.components.ChatlyTopAppBar
import com.example.chatly.ui.components.ProfileInfoItem
import com.example.chatly.ui.profile.ProfileViewModel

@Composable
fun ProfileScreen(
    onEditProfileClick: () -> Unit,
    onAdminClick: () -> Unit,
    onBackClick: () -> Unit,
    onLogout: () -> Unit,
    viewModel: ProfileViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.refreshProfile()
    }

    Scaffold(
        topBar = {
            ChatlyTopAppBar(
                title = "Profile",
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (!uiState.photoUrl.isNullOrEmpty()) {

                    AsyncImage(
                        model = uiState.photoUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )

                } else {

                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(120.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                ProfileInfoItem(label = "Name", value = uiState.displayName.ifEmpty { "N/A" })
                ProfileInfoItem(label = "Email", value = uiState.email)
                ProfileInfoItem(label = "Mobile", value = uiState.mobile)
                ProfileInfoItem(label = "Date of Birth", value = uiState.dob)
                ProfileInfoItem(label = "Gender", value = uiState.gender)
                
                Spacer(modifier = Modifier.weight(1f))
                
                ChatlyButton(
                    text = "Edit Profile",
                    onClick = onEditProfileClick
                )

                if (uiState.role == "admin") {
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedButton(
                        onClick = onAdminClick,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = MaterialTheme.shapes.medium,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.tertiary
                        )
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Admin Dashboard")
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                TextButton(
                    onClick = {
                        viewModel.logout()
                        onLogout()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Logout", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

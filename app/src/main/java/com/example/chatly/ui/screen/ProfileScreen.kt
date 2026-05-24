package com.example.chatly.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
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
import com.example.chatly.ui.profile.ProfileViewModel

@Composable
fun ProfileScreen(
    onEditProfileClick: () -> Unit,
    onBackClick: () -> Unit,
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
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
            }
        }
    }
}

@Composable
fun ProfileInfoItem(label: String, value: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 4.dp)
        )
        HorizontalDivider(modifier = Modifier.padding(top = 8.dp))
    }
}

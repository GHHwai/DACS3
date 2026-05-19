package com.example.chatly.ui.screen

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.chatly.ui.components.ChatlyButton
import com.example.chatly.ui.components.ChatlyTextField
import com.example.chatly.ui.components.ChatlyTopAppBar
import com.example.chatly.ui.profile.ProfileViewModel
import com.example.chatly.util.CloudinaryUploader

@Composable
fun EditProfileScreen(
    onBackClick: () -> Unit,
    viewModel: ProfileViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    val CLOUDINARY_CLOUD_NAME = com.example.chatly.BuildConfig.CLOUDINARY_CLOUD_NAME
    val CLOUDINARY_UNSIGNED_PRESET = com.example.chatly.BuildConfig.CLOUDINARY_UNSIGNED_PRESET

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        viewModel.onImageSelected(uri)
    }

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            Toast.makeText(context, "Profile updated!", Toast.LENGTH_SHORT).show()
            viewModel.resetSaveStatus()
            onBackClick()
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            ChatlyTopAppBar(
                title = "Edit Profile",
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
                .imePadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AsyncImage(
                model = uiState.profileImageUri ?: uiState.photoUrl ?: Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )

            TextButton(onClick = { pickImageLauncher.launch("image/*") }) {
                Text("Change Photo")
            }

            Spacer(modifier = Modifier.height(16.dp))

            ChatlyTextField(
                value = uiState.displayName,
                onValueChange = { viewModel.onDisplayNameChange(it) },
                label = "Full Name",
                modifier = Modifier.padding(bottom = 16.dp)
            )

            ChatlyTextField(
                value = uiState.mobile,
                onValueChange = { viewModel.onMobileChange(it) },
                label = "Mobile Number",
                modifier = Modifier.padding(bottom = 16.dp)
            )

            ChatlyTextField(
                value = uiState.dob,
                onValueChange = { viewModel.onDobChange(it) },
                label = "Date of Birth",
                modifier = Modifier.padding(bottom = 16.dp)
            )

            ChatlyTextField(
                value = uiState.gender,
                onValueChange = { viewModel.onGenderChange(it) },
                label = "Gender",
                modifier = Modifier.padding(bottom = 24.dp)
            )

            ChatlyButton(
                text = "Save Profile",
                isLoading = uiState.isLoading,
                onClick = {
                    if (uiState.profileImageUri != null) {
                        CloudinaryUploader.uploadImage(
                            context = context,
                            imageUri = uiState.profileImageUri!!,
                            cloudName = CLOUDINARY_CLOUD_NAME,
                            unsignedPreset = CLOUDINARY_UNSIGNED_PRESET,
                            onSuccess = { imageUrl ->
                                viewModel.setPhotoUrl(imageUrl)
                                viewModel.saveProfile()
                            },
                            onFailure = { e ->
                                Toast.makeText(context, "Image upload failed: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        )
                    } else {
                        viewModel.saveProfile()
                    }
                }
            )
        }
    }
}

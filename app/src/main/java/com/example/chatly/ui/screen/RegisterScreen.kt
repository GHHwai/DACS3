package com.example.chatly.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.chatly.ui.auth.AuthViewModel
import com.example.chatly.ui.components.ChatlyButton
import com.example.chatly.ui.components.ChatlyTextField

@Composable
fun RegisterScreen(
    onNavigateToLogin: () -> Unit,
    onRegisterSuccess: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    var name by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    val authState by viewModel.authState.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(authState, error) {
        if (authState || error != null) {
            isLoading = false
        }

        if (authState) {
            onRegisterSuccess()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
            .imePadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Create Account",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 32.sp
            ),
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Join Chatly today",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        ChatlyTextField(
            value = name,
            onValueChange = { name = it },
            label = "Full Name",
            leadingIcon = Icons.Default.Person,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        ChatlyTextField(
            value = mobile,
            onValueChange = { mobile = it },
            label = "Mobile Number",
            leadingIcon = Icons.Default.Phone,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        ChatlyTextField(
            value = email,
            onValueChange = { email = it },
            label = "Email",
            leadingIcon = Icons.Default.Email,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        ChatlyTextField(
            value = password,
            onValueChange = { password = it },
            label = "Password",
            leadingIcon = Icons.Default.Lock,
            visualTransformation =
                if (passwordVisible) VisualTransformation.None
                else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector =
                            if (passwordVisible) Icons.Default.VisibilityOff
                            else Icons.Default.Visibility,
                        contentDescription = null
                    )
                }
            },
            modifier = Modifier.padding(bottom = 24.dp)
        )

        if (error != null) {
            Text(
                text = error ?: "",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        ChatlyButton(
            text = "Register",
            isLoading = isLoading,
            onClick = {
                if (name.isNotBlank() && mobile.isNotBlank() && email.isNotBlank() && password.isNotBlank()) {
                    isLoading = true
                    viewModel.register(name, mobile, email, password)
                }
            },
            modifier = Modifier.padding(bottom = 16.dp)
        )

        TextButton(onClick = onNavigateToLogin) {
            Text(text = "Already have an account? Login")
        }
    }
}

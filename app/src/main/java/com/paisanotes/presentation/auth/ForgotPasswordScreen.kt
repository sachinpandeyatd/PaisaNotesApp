package com.paisanotes.presentation.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    viewModel: AuthViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.resetSuccess) {
        if (state.resetSuccess) onNavigateBack() // Go back to login when done!
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reset Password") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            if (!state.isOtpSent) {
                // STEP 1: REQUEST OTP
                Text("Enter your email to receive a 6-digit OTP.", style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(24.dp))
                
                OutlinedTextField(
                    value = state.email, onValueChange = viewModel::onEmailChange,
                    label = { Text("Email") }, modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )
                Spacer(modifier = Modifier.height(24.dp))
                
                Button(onClick = viewModel::requestOtp, modifier = Modifier.fillMaxWidth(), enabled = !state.isLoading) {
                    if (state.isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp)) else Text("Send OTP")
                }
            } else {
                // STEP 2: VERIFY OTP AND RESET
                Text("An OTP has been sent to your email.", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = state.otp, onValueChange = viewModel::onOtpChange,
                    label = { Text("6-Digit OTP") }, modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = state.password, onValueChange = viewModel::onPasswordChange,
                    label = { Text("New Password") }, modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                )
                Spacer(modifier = Modifier.height(24.dp))

                Button(onClick = viewModel::resetPassword, modifier = Modifier.fillMaxWidth(), enabled = !state.isLoading) {
                    if (state.isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp)) else Text("Reset Password")
                }
            }

            if (state.error != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = state.error!!, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
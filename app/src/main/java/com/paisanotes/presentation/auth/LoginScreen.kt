package com.paisanotes.presentation.auth

import android.app.Activity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch
import java.security.SecureRandom
import android.util.Base64

@Composable
fun LoginScreen(
    viewModel: AuthViewModel = hiltViewModel(),
    onLoginSuccess: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Grab the Client ID from strings.xml
    val webClientId = com.paisanotes.BuildConfig.GOOGLE_WEB_CLIENT_ID

    LaunchedEffect(state.loginSuccess) {
        if (state.loginSuccess) onLoginSuccess()
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "PaisaNotes", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = state.email, onValueChange = viewModel::onEmailChange,
            label = { Text("Email") }, modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = state.password, onValueChange = viewModel::onPasswordChange,
            label = { Text("Password") }, modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )
        Spacer(modifier = Modifier.height(24.dp))

        if (state.error != null) {
            Text(text = state.error!!, color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(8.dp))
        }

        Button(
            onClick = viewModel::login, modifier = Modifier.fillMaxWidth(), enabled = !state.isLoading
        ) {
            if (state.isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
            else Text("Login")
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text("OR", style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(24.dp))

        // 🚨 GOOGLE SIGN-IN BUTTON
        OutlinedButton(
            onClick = {
                coroutineScope.launch {
                    try {
                        val credentialManager = CredentialManager.create(context)
                        val randomBytes = ByteArray(32)
                        SecureRandom().nextBytes(randomBytes)
                        val hashedNonce = Base64.encodeToString(randomBytes, Base64.NO_WRAP or Base64.URL_SAFE or Base64.NO_PADDING)

                        android.util.Log.d("Auth", "Web Client ID: $webClientId")

                        val signInWithGoogleOption = GetSignInWithGoogleOption.Builder(
                            serverClientId = webClientId
                        ).setNonce(hashedNonce)
                            .build()

                        val request = GetCredentialRequest.Builder()
                            .addCredentialOption(signInWithGoogleOption)
                            .build()

                        // This pops up the beautiful Android bottom sheet!
                        val result = credentialManager.getCredential(context as Activity, request)

                        // Extract the token
                        val credential = result.credential
                        if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                            val idToken = googleIdTokenCredential.idToken

                            // Send token to our Spring Boot backend!
                            viewModel.googleLogin(idToken)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        // User cancelled or no Google accounts found
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isLoading
        ) {
            Text("Sign in with Google")
        }
    }
}
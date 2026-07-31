package com.paisanotes.presentation.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onAccountDeleted: () -> Unit // 🚨 Navigate to Login
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }

    val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
    var isIgnoringBattery by remember { mutableStateOf(powerManager.isIgnoringBatteryOptimizations(context.packageName)) }
    var hasNotificationAccess by remember { mutableStateOf(NotificationManagerCompat.getEnabledListenerPackages(context).contains(context.packageName)) }

    LaunchedEffect(state.deleteSuccess) {
        if (state.deleteSuccess) onAccountDeleted()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            Text("Auto-Capture Reliability", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(16.dp), color = MaterialTheme.colorScheme.primary)

            ListItem(
                headlineContent = { Text("Read Notifications") },
                supportingContent = { Text("Required to auto-capture payments") },
                leadingContent = { Icon(Icons.Default.NotificationsActive, null) },
                trailingContent = {
                    Switch(
                        checked = hasNotificationAccess,
                        onCheckedChange = { context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)) }
                    )
                }
            )

            HorizontalDivider()

            ListItem(
                headlineContent = { Text("Run in Background") },
                supportingContent = { Text("Prevents phone from killing auto-capture.") },
                leadingContent = { Icon(Icons.Default.BatteryAlert, null) },
                trailingContent = {
                    Switch(
                        checked = isIgnoringBattery,
                        onCheckedChange = {
                            if (!isIgnoringBattery) {
                                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply { data = Uri.parse("package:${context.packageName}") }
                                context.startActivity(intent)
                            } else {
                                context.startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
                            }
                        }
                    )
                }
            )

            Spacer(modifier = Modifier.weight(1f))

            // 🚨 DELETE ACCOUNT BUTTON
            if (state.error != null) {
                Text(state.error!!, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(16.dp))
            }

            Button(
                onClick = { showDeleteDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.fillMaxWidth().padding(16.dp).padding(bottom = 32.dp),
                enabled = !state.isDeleting
            ) {
                if (state.isDeleting) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onError)
                else {
                    Icon(Icons.Default.DeleteForever, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Delete Account Permanently")
                }
            }
        }

        // 🚨 CONFIRMATION DIALOG
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                icon = { Icon(Icons.Default.DeleteForever, null, tint = MaterialTheme.colorScheme.error) },
                title = { Text("Delete Account?") },
                text = { Text("This will permanently delete your account and all your financial data from our servers. This action cannot be undone.") },
                confirmButton = {
                    Button(
                        onClick = { showDeleteDialog = false; viewModel.deleteAccount() },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) { Text("Yes, Delete Everything") }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
                }
            )
        }
    }
}
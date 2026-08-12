package com.paisanotes.presentation.onboarding

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

// Helper function to check the Listener permission
fun isNotificationListenerEnabled(context: Context): Boolean {
    return NotificationManagerCompat.getEnabledListenerPackages(context).contains(context.packageName)
}

@Composable
fun PermissionsGate(
    content: @Composable () -> Unit // The actual app (MainScreen)
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // 1. Define required standard permissions dynamically based on Android version
    val requiredPermissions = remember {
        mutableListOf<String>().apply {
            if (Build.VERSION.SDK_INT >= 36) add(Manifest.permission.ACCESS_LOCAL_NETWORK)
            if (Build.VERSION.SDK_INT >= 33) add(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    // 2. Track States
    var runtimeGranted by remember {
        mutableStateOf(requiredPermissions.all { ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED })
    }

    var listenerGranted by remember {
        mutableStateOf(isNotificationListenerEnabled(context))
    }

    // 3. Auto-refresh Listener state when the user returns from Android Settings!
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                listenerGranted = isNotificationListenerEnabled(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // 4. Permission Launcher for standard permissions
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissionsMap ->
        runtimeGranted = requiredPermissions.all { ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED }
    }

    // 5. The UX Flow
    if (!runtimeGranted && requiredPermissions.isNotEmpty()) {
        // SCREEN 1: Standard Permissions (Network & Alert Notifications)
        PermissionScreen(
            title = "Welcome to PaisaNotes",
            description = "To keep your finances perfectly synced and alert you when transactions happen, we need basic setup permissions.",
            icon1 = Icons.Default.CloudSync,
            reason1 = "Local Network: To securely sync your offline data to the cloud.",
            icon2 = Icons.Default.NotificationsActive,
            reason2 = "Notifications: To instantly alert you when we auto-capture a payment.",
            buttonText = "Grant Permissions",
            onButtonClick = { launcher.launch(requiredPermissions.toTypedArray()) }
        )
    } else if (!listenerGranted) {
        // SCREEN 2: The "Scary" Notification Listener Permission
        PermissionScreen(
            title = "The Magic Step \uD83E\uDE84",
            description = "To fully automate your expense tracking, PaisaNotes needs to read transaction alerts from Banks, GPay, and Paytm.",
            icon1 = Icons.Default.AutoAwesome,
            reason1 = "Auto-Capture: We read the notification tray to instantly log your expenses without you typing anything.",
            icon2 = Icons.Default.PrivacyTip,
            reason2 = "Privacy Promise: We strictly filter for financial keywords (like 'debited' or 'spent') and completely ignore all personal chats.",
            buttonText = "Enable Auto-Capture",
            onButtonClick = { 
                context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)) 
            }
        )
    } else {
        // BOTH GRANTED! Show the actual app.
        content()
    }
}

// Reusable UI template for the permission screens
@Composable
private fun PermissionScreen(
    title: String, description: String, 
    icon1: androidx.compose.ui.graphics.vector.ImageVector, reason1: String, 
    icon2: androidx.compose.ui.graphics.vector.ImageVector, reason2: String, 
    buttonText: String, onButtonClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = description, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
        
        Spacer(modifier = Modifier.height(48.dp))

        // Reason 1
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
                Icon(icon1, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(reason1, style = MaterialTheme.typography.bodyMedium)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Reason 2
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(MaterialTheme.colorScheme.secondaryContainer), contentAlignment = Alignment.Center) {
                Icon(icon2, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(reason2, style = MaterialTheme.typography.bodyMedium)
        }

        Spacer(modifier = Modifier.height(48.dp))

        Button(onClick = onButtonClick, modifier = Modifier.fillMaxWidth().height(56.dp)) {
            Text(buttonText, style = MaterialTheme.typography.titleMedium)
        }
    }
}
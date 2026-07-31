package com.paisanotes

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class QuickAddActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                var showSheet by remember { mutableStateOf(true) }

                if (showSheet) {
                    ModalBottomSheet(
                        onDismissRequest = {
                            showSheet = false
                            finish() // 🚨 Close the invisible activity if they tap outside!
                        },
                        sheetState = sheetState
                    ) {
                        Column(modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp)) {
                            Text(
                                "What would you like to add?",
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier.padding(16.dp)
                            )

                            ListItem(
                                headlineContent = { Text("Transaction") },
                                leadingContent = { Icon(Icons.Default.List, null, tint = MaterialTheme.colorScheme.primary) },
                                modifier = Modifier.clickable { launchApp("TRANSACTION") }
                            )
                            ListItem(
                                headlineContent = { Text("Loan / Ledger Entry") },
                                leadingContent = { Icon(Icons.Default.Money, null, tint = MaterialTheme.colorScheme.error) },
                                modifier = Modifier.clickable { launchApp("LOAN") }
                            )
                            ListItem(
                                headlineContent = { Text("Proxy EMI") },
                                leadingContent = { Icon(Icons.Default.Person, null, tint = MaterialTheme.colorScheme.tertiary) },
                                modifier = Modifier.clickable { launchApp("EMI") }
                            )
                        }
                    }
                }
            }
        }
    }

    private fun launchApp(action: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("QUICK_ACTION", action)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish() // Kill the transparent activity
    }
}
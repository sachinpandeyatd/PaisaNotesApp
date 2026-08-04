package com.paisanotes.presentation.account_detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountDetailScreen(
    viewModel: AccountDetailViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val formatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    val sdf = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())

    var showPayDialog by remember { mutableStateOf(false) }
    var payAmount by remember { mutableStateOf("") }
    var sourceAccountId by remember { mutableStateOf<String?>(null) }
    var sourceAccountName by remember { mutableStateOf("") }
    var expandedSource by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.account?.name ?: "Account") },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } }
            )
        }
    ) { paddingValues ->
        if (state.isLoading) return@Scaffold
        val account = state.account

        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            
            // --- HEADER CARD ---
            Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Current Balance", style = MaterialTheme.typography.labelLarge)
                    Text(
                        text = formatter.format(account?.currentBalance ?: 0.0),
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = if ((account?.currentBalance ?: 0.0) < 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                    
                    // 🚨 REMINDER TEXT
                    if (account?.type == "CREDIT_CARD" && account.statementDay != null) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Reminder: Statement on ${account.statementDay}th, Due on ${account.dueDay}th",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }

                    Spacer(Modifier.height(16.dp))
                    
                    // 🚨 ACTION BUTTONS
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        Button(onClick = { showPayDialog = true }) { Text("Pay / Add Funds") }
                        OutlinedButton(onClick = viewModel::resetBalance) { Text("Reset to ₹0") }
                    }
                }
            }

            Text("History", modifier = Modifier.padding(start = 16.dp, top = 8.dp), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            // --- TIMELINE ---
            LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.transactions, key = { it.id }) { txn ->
                    // Determine if money came IN or OUT of THIS specific account
                    val isMoneyOut = (txn.accountId == account?.id && txn.transactionType != "INCOME") || txn.transactionType == "EXPENSE"
                    val amountColor = if (isMoneyOut) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    val sign = if (isMoneyOut) "-" else "+"

                    ListItem(
                        headlineContent = { Text(txn.category, fontWeight = FontWeight.Bold) },
                        supportingContent = { Text(sdf.format(Date(txn.transactionDate))) },
                        trailingContent = { Text("$sign ${formatter.format(txn.amount)}", color = amountColor, fontWeight = FontWeight.Bold) },
                        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest)
                    )
                }
            }
        }

        // --- PAY DIALOG ---
        if (showPayDialog) {
            AlertDialog(
                onDismissRequest = { showPayDialog = false },
                title = { Text("Record Payment") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        OutlinedTextField(
                            value = payAmount, onValueChange = { payAmount = it },
                            label = { Text("Amount") }, prefix = { Text("₹") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                        // Dropdown for Source Account
                        ExposedDropdownMenuBox(expanded = expandedSource, onExpandedChange = { expandedSource = it }) {
                            OutlinedTextField(
                                value = sourceAccountName, onValueChange = {}, readOnly = true,
                                label = { Text("Pay From") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedSource) },
                                modifier = Modifier.menuAnchor().fillMaxWidth()
                            )
                            ExposedDropdownMenu(expanded = expandedSource, onDismissRequest = { expandedSource = false }) {
                                state.allAccounts.forEach { acc ->
                                    DropdownMenuItem(text = { Text("${acc.name} (${formatter.format(acc.currentBalance)})") }, onClick = { 
                                        sourceAccountId = acc.id; sourceAccountName = acc.name; expandedSource = false 
                                    })
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        val amt = payAmount.toDoubleOrNull()
                        if (amt != null && amt > 0 && sourceAccountId != null) {
                            viewModel.recordPayment(amt, sourceAccountId!!)
                            showPayDialog = false
                            payAmount = ""; sourceAccountId = null; sourceAccountName = ""
                        }
                    }, enabled = sourceAccountId != null) { Text("Confirm") }
                },
                dismissButton = { TextButton(onClick = { showPayDialog = false }) { Text("Cancel") } }
            )
        }
    }
}
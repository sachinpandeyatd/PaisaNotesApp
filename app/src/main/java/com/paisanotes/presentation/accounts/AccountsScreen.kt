package com.paisanotes.presentation.accounts

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountsScreen(
    viewModel: AccountsViewModel = hiltViewModel(),
    onOpenDrawer: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val formatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))

    var showSheet by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }
    var newType by remember { mutableStateOf("CASH") }
    var newBalance by remember { mutableStateOf("") }
    val sheetState = rememberModalBottomSheetState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Wallets & Accounts") },
                navigationIcon = { IconButton(onClick = onOpenDrawer) { Icon(Icons.Default.Menu, "Menu") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showSheet = true }) { Icon(Icons.Default.Add, "Add Account") }
        }
    ) { paddingValues ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            return@Scaffold
        }

        LazyColumn(contentPadding = PaddingValues(16.dp), modifier = Modifier.padding(paddingValues)) {
            items(state.accounts) { account ->
                val icon = when (account.type) {
                    "CASH" -> Icons.Default.Money
                    "CREDIT_CARD" -> Icons.Default.CreditCard
                    "WALLET" -> Icons.Default.AccountBalanceWallet
                    else -> Icons.Default.AccountBalance
                }
                ListItem(
                    headlineContent = { Text(account.name, fontWeight = FontWeight.Bold) },
                    supportingContent = { Text(account.type) },
                    leadingContent = { Icon(icon, null, tint = MaterialTheme.colorScheme.primary) },
                    trailingContent = { 
                        Text(formatter.format(account.currentBalance), fontWeight = FontWeight.Bold, color = if(account.currentBalance < 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface)
                    },
                    colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.padding(bottom = 8.dp).fillMaxWidth()
                )
            }
        }

        if (showSheet) {
            ModalBottomSheet(onDismissRequest = { showSheet = false }, sheetState = sheetState) {
                Column(Modifier.padding(16.dp).padding(bottom = 32.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Add Account", style = MaterialTheme.typography.titleLarge)
                    OutlinedTextField(value = newName, onValueChange = { newName = it }, label = { Text("Account Name (e.g. HDFC)") }, modifier = Modifier.fillMaxWidth())
                    
                    // Type selector
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        listOf("CASH", "SAVINGS", "CREDIT_CARD", "WALLET").forEach { type ->
                            FilterChip(
                                selected = newType == type,
                                onClick = { newType = type },
                                label = { Text(type.replace("_", " ")) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = newBalance, onValueChange = { newBalance = it }, 
                        label = { Text("Initial Balance (₹)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth()
                    )
                    
                    Button(
                        onClick = {
                            val bal = newBalance.toDoubleOrNull() ?: 0.0
                            if (newName.isNotBlank()) {
                                viewModel.saveAccount(newName, newType, bal)
                                showSheet = false
                                newName = ""; newBalance = ""
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Save Account") }
                }
            }
        }
    }
}
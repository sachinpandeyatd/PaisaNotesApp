package com.paisanotes.presentation.accounts

import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.paisanotes.domain.model.CreditCardBill
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountsScreen(
    viewModel: AccountsViewModel = hiltViewModel(),
    onOpenDrawer: () -> Unit,
    onNavigateToAccountDetail: (String) -> Unit
) {
    val state by viewModel.state.collectAsState()
    val formatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))

    // --- ADD ACCOUNT SHEET STATE ---
    var showAddSheet by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }
    var newType by remember { mutableStateOf("CASH") }
    var newBalance by remember { mutableStateOf("") }
    var newStatementDay by remember { mutableStateOf("") }
    var newDueDay by remember { mutableStateOf("") }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // --- PAY BILL DIALOG STATE ---
    var billToPay by remember { mutableStateOf<CreditCardBill?>(null) }
    var paymentAmount by remember { mutableStateOf("") }
    var sourceAccountId by remember { mutableStateOf<String?>(null) }
    var sourceAccountName by remember { mutableStateOf("") }
    var expandedSourceAccount by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Wallets & Accounts") },
                navigationIcon = { IconButton(onClick = onOpenDrawer) { Icon(Icons.Default.Menu, "Menu") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddSheet = true }) { Icon(Icons.Default.Add, "Add Account") }
        }
    ) { paddingValues ->

        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            return@Scaffold
        }

        // ==========================================
        // 1. MAIN LIST OF ACCOUNTS
        // ==========================================
        LazyColumn(contentPadding = PaddingValues(16.dp), modifier = Modifier.padding(paddingValues)) {
            items(state.accounts, key = { it.id }) { account ->
                val icon = when (account.type) {
                    "CASH" -> Icons.Default.Money
                    "CREDIT_CARD" -> Icons.Default.CreditCard
                    "WALLET" -> Icons.Default.AccountBalanceWallet
                    else -> Icons.Default.AccountBalance
                }

                // Find if this specific account has an active bill!
                val activeBill = state.activeBills.find { it.accountId == account.id && it.status != "CLEARED" }

                ElevatedCard(
                    modifier = Modifier.padding(bottom = 12.dp).fillMaxWidth().clickable {
                        onNavigateToAccountDetail(account.id)
                    },
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column {
                        ListItem(
                            headlineContent = { Text(account.name, fontWeight = FontWeight.Bold) },
                            supportingContent = {
                                Column {
                                    Text(account.type.replace("_", " "))
                                    if (account.type == "CREDIT_CARD" && account.statementDay != null) {
                                        Text("Generates on ${account.statementDay}th • Due on ${account.dueDay}th", style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            },
                            leadingContent = { Icon(icon, null, tint = MaterialTheme.colorScheme.primary) },
                            trailingContent = {
                                Text(
                                    text = formatter.format(account.currentBalance),
                                    fontWeight = FontWeight.Bold,
                                    color = if (account.currentBalance < 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                                )
                            },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                        )

                        // THE BILL ALERT STRIP
                        if (activeBill != null) {
                            HorizontalDivider()
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("${activeBill.billingMonth} Bill", style = MaterialTheme.typography.labelMedium)
                                    val remaining = activeBill.totalBilledAmount - activeBill.amountPaid
                                    Text("Due: ${formatter.format(remaining)}", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                                }
                                Button(onClick = {
                                    // Open the Pay Bill Dialog!
                                    billToPay = activeBill
                                    paymentAmount = (activeBill.totalBilledAmount - activeBill.amountPaid).toString()
                                }) {
                                    Text("Pay Bill")
                                }
                            }
                        }
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }

        // ==========================================
        // 2. ADD ACCOUNT BOTTOM SHEET
        // ==========================================
        if (showAddSheet) {
            ModalBottomSheet(onDismissRequest = { showAddSheet = false }, sheetState = sheetState) {
                Column(Modifier.padding(16.dp).padding(bottom = 32.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Add Account", style = MaterialTheme.typography.titleLarge)

                    OutlinedTextField(
                        value = newName, onValueChange = { newName = it },
                        label = { Text("Account Name (e.g. HDFC)") }, modifier = Modifier.fillMaxWidth(), singleLine = true
                    )

                    // Type selector
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        listOf("CASH", "SAVINGS", "CREDIT_CARD", "WALLET").forEach { type ->
                            FilterChip(
                                selected = newType == type, onClick = { newType = type }, label = { Text(type.replace("_", " ")) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = newBalance, onValueChange = { newBalance = it },
                        label = { Text(if (newType == "CREDIT_CARD") "Current Outstanding (₹)" else "Initial Balance (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth(), singleLine = true
                    )

                    // DYNAMIC FIELDS: Only show Statement/Due dates if it's a Credit Card!
                    if (newType == "CREDIT_CARD") {
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            OutlinedTextField(
                                value = newStatementDay, onValueChange = { newStatementDay = it },
                                label = { Text("Statement Day (1-31)") }, modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true
                            )
                            OutlinedTextField(
                                value = newDueDay, onValueChange = { newDueDay = it },
                                label = { Text("Due Day (1-31)") }, modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            val bal = newBalance.toDoubleOrNull() ?: 0.0
                            val sDay = newStatementDay.toIntOrNull()
                            val dDay = newDueDay.toIntOrNull()
                            if (newName.isNotBlank()) {
                                viewModel.saveAccount(newName, newType, bal, sDay, dDay)
                                showAddSheet = false
                                newName = ""; newBalance = ""; newStatementDay = ""; newDueDay = ""; newType = "CASH"
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Save Account") }
                }
            }
        }

        // ==========================================
        // 3. PAY BILL DIALOG
        // ==========================================
        if (billToPay != null) {
            AlertDialog(
                onDismissRequest = { billToPay = null },
                title = { Text("Pay Credit Card Bill") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text("Billing Month: ${billToPay!!.billingMonth}", style = MaterialTheme.typography.labelLarge)

                        OutlinedTextField(
                            value = paymentAmount,
                            onValueChange = { paymentAmount = it },
                            label = { Text("Amount to Pay") },
                            prefix = { Text("₹") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // DROPDOWN TO SELECT WHICH ACCOUNT TO PAY FROM
                        ExposedDropdownMenuBox(
                            expanded = expandedSourceAccount,
                            onExpandedChange = { expandedSourceAccount = it }
                        ) {
                            OutlinedTextField(
                                value = sourceAccountName,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Pay From Account") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedSourceAccount) },
                                modifier = Modifier.menuAnchor().fillMaxWidth()
                            )
                            ExposedDropdownMenu(expanded = expandedSourceAccount, onDismissRequest = { expandedSourceAccount = false }) {
                                // Filter out the credit card itself from the list of payment sources!
                                state.accounts.filter { it.id != billToPay!!.accountId }.forEach { acc ->
                                    DropdownMenuItem(
                                        text = { Text("${acc.name} (${formatter.format(acc.currentBalance)})") },
                                        onClick = {
                                            sourceAccountId = acc.id
                                            sourceAccountName = acc.name
                                            expandedSourceAccount = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val amt = paymentAmount.toDoubleOrNull()
                            if (amt != null && amt > 0 && sourceAccountId != null) {
                                viewModel.payCreditCardBill(billToPay!!.id, amt, sourceAccountId!!)
                                billToPay = null
                                sourceAccountId = null
                                sourceAccountName = ""
                            }
                        },
                        enabled = sourceAccountId != null // Disable until they pick an account to pay from!
                    ) { Text("Confirm Payment") }
                },
                dismissButton = { TextButton(onClick = { billToPay = null }) { Text("Cancel") } }
            )
        }
    }
}
package com.paisanotes.presentation.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.paisanotes.presentation.transactions.TransactionItem
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale
import androidx.compose.ui.platform.LocalLocale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onOpenDrawer: () -> Unit,
    onNavigateToAddTransaction: () -> Unit,
    onNavigateToEditTransaction: (String) -> Unit
) {
    val state by viewModel.state.collectAsState()
    val formatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    val currentMonth = LocalDate.now().month.getDisplayName(TextStyle.FULL, LocalLocale.current.platformLocale)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dashboard") },
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) { Icon(Icons.Default.Menu, "Open Menu") }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToAddTransaction) {
                Icon(Icons.Default.Add, "Add Transaction")
            }
        }
    ) { paddingValues ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            // 1. MONTHLY SUMMARY CARD
            item {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("$currentMonth Overview", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(16.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Income", style = MaterialTheme.typography.labelMedium)
                                Text(formatter.format(state.thisMonthIncome), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Expense", style = MaterialTheme.typography.labelMedium)
                                Text(formatter.format(state.thisMonthExpense), color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                            }
                        }
                        Divider(Modifier.padding(vertical = 12.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Net Savings", style = MaterialTheme.typography.titleSmall)
                            val savings = state.thisMonthIncome - state.thisMonthExpense
                            Text(
                                text = formatter.format(savings),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (savings >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            // 2. EXPOSURE CARD
            item {
                OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Total Market Exposure", style = MaterialTheme.typography.titleSmall)
                        Text(
                            text = formatter.format(state.totalExposure),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (state.totalExposure < 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            // 3. RECENT TRANSACTIONS HEADER
            item {
                Spacer(Modifier.height(8.dp))
                Text("Recent Activity", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            // 4. RECENT TRANSACTIONS LIST
            if (state.recentTransactions.isEmpty()) {
                item { Text("No transactions yet.", modifier = Modifier.padding(vertical = 16.dp)) }
            } else {
                items(state.recentTransactions, key = { it.id }) { txn ->
                    TransactionItem(transaction = txn, onClick = { onNavigateToEditTransaction(txn.id) })
                }
            }
            
            item { Spacer(modifier = Modifier.height(80.dp)) } // Padding for Bottom Nav
        }
    }
}
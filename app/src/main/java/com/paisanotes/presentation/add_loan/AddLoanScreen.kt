package com.paisanotes.presentation.add_loan

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddLoanScreen(
    viewModel: AddLoanViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.saveSuccess) {
        if (state.saveSuccess) onNavigateBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (state.isEditing) "Edit Ledger Entry" else "Add Ledger Entry") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
                },
                actions = {
                    if (state.isEditing) {
                        IconButton(onClick = viewModel::deleteLoan) {
                            Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                FilterChip(
                    selected = state.type == "LENT",
                    onClick = { viewModel.onTypeChange("LENT") },
                    label = { Text("Given") },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.errorContainer)
                )
                FilterChip(
                    selected = state.type == "BORROWED",
                    onClick = { viewModel.onTypeChange("BORROWED") },
                    label = { Text("Taken") },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.primaryContainer)
                )
            }
            OutlinedTextField(
                value = state.amount,
                onValueChange = viewModel::onAmountChange,
                label = { Text("Amount Lent (₹)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                prefix = { Text("₹") }
            )
            OutlinedTextField(
                value = state.notes,
                onValueChange = viewModel::onNotesChange,
                label = { Text("Notes (e.g., 'For dinner')") },
                modifier = Modifier.fillMaxWidth()
            )

            if (!state.isEditing) {
                var expandedTxn by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(expanded = expandedTxn, onExpandedChange = { expandedTxn = it }) {
                    OutlinedTextField(
                        value = state.linkedTxnName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Link to Auto-Captured Payment") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedTxn) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = expandedTxn, onDismissRequest = { expandedTxn = false }) {
                        DropdownMenuItem(text = { Text("None (Create New)") }, onClick = {
                            viewModel.onLinkedTxnSelect(null, "None (Create New)", null)
                            expandedTxn = false
                        })

                        // Smart filtering: If you lent money, find recent expenses. If you borrowed, find incomes!
                        val targetType = if (state.type == "LENT") "EXPENSE" else "INCOME"

                        state.recentAutoCaptures.filter { it.transactionType == targetType }.forEach { txn ->
                            val dateStr = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(
                                Date(txn.transactionDate)
                            )
                            DropdownMenuItem(
                                text = { Text("₹${txn.amount} - $dateStr") },
                                onClick = {
                                    viewModel.onLinkedTxnSelect(txn.id, "₹${txn.amount}", txn.amount)
                                    expandedTxn = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = viewModel::saveLoan,
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isSaving
            ) {
                if (state.isSaving) CircularProgressIndicator(modifier = Modifier.size(24.dp))
                else Text("Save Loan")
            }
        }
    }
}
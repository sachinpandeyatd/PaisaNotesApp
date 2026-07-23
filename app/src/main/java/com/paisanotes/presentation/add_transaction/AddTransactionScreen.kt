package com.paisanotes.presentation.add_transaction

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.paisanotes.presentation.util.getCategoryIcon
import com.paisanotes.presentation.util.parseHexColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    viewModel: AddTransactionViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit // Callback to go back to the previous screen
) {
    val state by viewModel.state.collectAsState()
    var showCategorySheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    // This listens for the saveSuccess flag. If it becomes true, we automatically pop the screen!
    LaunchedEffect(state.saveSuccess) {
        if (state.saveSuccess) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Transaction") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Type Selector (Income vs Expense)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                FilterChip(
                    selected = state.transactionType == "EXPENSE",
                    onClick = { viewModel.onTypeChange("EXPENSE") },
                    label = { Text("Expense") },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.errorContainer)
                )
                FilterChip(
                    selected = state.transactionType == "INCOME",
                    onClick = { viewModel.onTypeChange("INCOME") },
                    label = { Text("Income") },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.primaryContainer)
                )
            }

            // Amount Field
            OutlinedTextField(
                value = state.amount,
                onValueChange = viewModel::onAmountChange,
                label = { Text("Amount (₹)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                prefix = { Text("₹") }
            )

            // Category Field
            OutlinedTextField(
                value = state.category,
                onValueChange = {}, // Read-only
                label = { Text("Category") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) },
                interactionSource = remember { MutableInteractionSource() }
                    .also { interactionSource ->
                        LaunchedEffect(interactionSource) {
                            interactionSource.interactions.collect {
                                if (it is androidx.compose.foundation.interaction.PressInteraction.Release) {
                                    showCategorySheet = true
                                }
                            }
                        }
                    }
            )

            // Notes Field
            OutlinedTextField(
                value = state.notes,
                onValueChange = viewModel::onNotesChange,
                label = { Text("Notes (Optional)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.weight(1f))

            // Save Button
            Button(
                onClick = viewModel::saveTransaction,
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isSaving
            ) {
                if (state.isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Save Transaction")
                }
            }
        }

        if (showCategorySheet) {
            ModalBottomSheet(
                onDismissRequest = { showCategorySheet = false },
                sheetState = sheetState
            ) {
                Column(Modifier.fillMaxWidth().padding(16.dp)) {
                    Text("Select Category", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(16.dp))

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(4), // 4 icons per row
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(bottom = 32.dp)
                    ) {
                        items(state.categories) { category ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.clickable {
                                    viewModel.onCategorySelect(category)
                                    showCategorySheet = false
                                }
                            ) {
                                Surface(
                                    shape = androidx.compose.foundation.shape.CircleShape,
                                    color = parseHexColor(category.color).copy(alpha = 0.2f),
                                    modifier = Modifier.size(56.dp)
                                ) {
                                    Icon(
                                        imageVector = getCategoryIcon(category.icon),
                                        contentDescription = category.name,
                                        tint = parseHexColor(category.color),
                                        modifier = Modifier.padding(12.dp)
                                    )
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(category.name, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }
        }
    }
}
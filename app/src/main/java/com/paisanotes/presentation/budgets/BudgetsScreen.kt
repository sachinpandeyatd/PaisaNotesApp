package com.paisanotes.presentation.budgets

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.paisanotes.domain.model.BudgetProgress
import com.paisanotes.domain.model.Category
import com.paisanotes.presentation.util.getCategoryIcon
import com.paisanotes.presentation.util.parseHexColor
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetsScreen(
    viewModel: BudgetsViewModel = hiltViewModel(),
    onOpenDrawer: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val formatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))

    // Bottom Sheet State
    var showSheet by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    var amountInput by remember { mutableStateOf("") }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Monthly Budgets") },
                navigationIcon = { IconButton(onClick = onOpenDrawer) { Icon(Icons.Default.Menu, "Menu") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showSheet = true }) { Icon(Icons.Default.Add, "Add Budget") }
        }
    ) { paddingValues ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            return@Scaffold
        }

        if (state.budgets.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No budgets set. Click + to create one!")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(state.budgets, key = { it.budgetId }) { budget ->
                    BudgetCard(budget, formatter) {
                        // On Click, open sheet to edit!
                        selectedCategory = state.availableCategories.find { it.id == budget.categoryId }
                        amountInput = budget.monthlyLimit.toString()
                        showSheet = true
                    }
                }
            }
        }

        // --- ADD / EDIT BUDGET BOTTOM SHEET ---
        if (showSheet) {
            ModalBottomSheet(onDismissRequest = { showSheet = false; selectedCategory = null; amountInput = "" }, sheetState = sheetState) {
                Column(Modifier.fillMaxWidth().padding(16.dp).padding(bottom = 32.dp)) {
                    Text("Set Budget Limit", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(16.dp))

                    if (selectedCategory == null) {
                        Text("1. Select a Category", style = MaterialTheme.typography.labelMedium)
                        Spacer(Modifier.height(8.dp))
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(4),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.height(200.dp)
                        ) {
                            items(state.availableCategories) { cat ->
                                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { selectedCategory = cat }) {
                                    Surface(shape = CircleShape, color = parseHexColor(cat.color).copy(alpha = 0.2f), modifier = Modifier.size(48.dp)) {
                                        Icon(getCategoryIcon(cat.icon), null, tint = parseHexColor(cat.color), modifier = Modifier.padding(12.dp))
                                    }
                                    Text(cat.name, style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    } else {
                        // Category Selected, enter amount!
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(getCategoryIcon(selectedCategory!!.icon), null, tint = parseHexColor(selectedCategory!!.color))
                            Spacer(Modifier.width(8.dp))
                            Text(selectedCategory!!.name, style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.weight(1f))
                            TextButton(onClick = { selectedCategory = null }) { Text("Change") }
                        }
                        Spacer(Modifier.height(16.dp))
                        OutlinedTextField(
                            value = amountInput, onValueChange = { amountInput = it },
                            label = { Text("Monthly Limit (₹)") }, modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                        Spacer(Modifier.height(24.dp))
                        Button(
                            onClick = {
                                val limit = amountInput.toDoubleOrNull()
                                if (limit != null && limit > 0) {
                                    viewModel.saveBudget(selectedCategory!!.id, limit)
                                    showSheet = false
                                    selectedCategory = null
                                    amountInput = ""
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Save Budget") }
                    }
                }
            }
        }
    }
}

@Composable
fun BudgetCard(budget: BudgetProgress, formatter: NumberFormat, onClick: () -> Unit) {
    val progressColor = if (budget.isExceeded) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary

    ElevatedCard(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = CircleShape, color = parseHexColor(budget.categoryColor).copy(alpha = 0.2f), modifier = Modifier.size(40.dp)) {
                    Icon(getCategoryIcon(budget.categoryIcon), null, tint = parseHexColor(budget.categoryColor), modifier = Modifier.padding(8.dp))
                }
                Spacer(Modifier.width(16.dp))
                Text(budget.categoryName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                
                // Spent vs Limit text
                Column(horizontalAlignment = Alignment.End) {
                    Text(formatter.format(budget.spentAmount), fontWeight = FontWeight.Bold, color = progressColor)
                    Text("of ${formatter.format(budget.monthlyLimit)}", style = MaterialTheme.typography.labelSmall)
                }
            }
            Spacer(Modifier.height(16.dp))
            
            // The actual Progress Bar
            LinearProgressIndicator(
                progress = { budget.percentage },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                color = progressColor,
                trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
            )
            
            Spacer(Modifier.height(8.dp))
            val remaining = budget.monthlyLimit - budget.spentAmount
            if (budget.isExceeded) {
                Text("Over budget by ${formatter.format(-remaining)}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
            } else {
                Text("${formatter.format(remaining)} remaining", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
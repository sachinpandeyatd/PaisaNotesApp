package com.paisanotes.presentation.person_detail

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.paisanotes.domain.model.Emi
import com.paisanotes.domain.model.Loan
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.ui.platform.LocalLocale
import com.paisanotes.domain.model.AuditLog
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PersonDetailScreen(
    viewModel: PersonDetailViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToAddLoan: (String, String?) -> Unit,
    onNavigateToAddEmi: (String, String?) -> Unit
) {
    val state by viewModel.state.collectAsState()

    var showBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    // Pager State for Tabs
    val tabs = listOf("Loans", "Proxy EMIs")
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.person?.name ?: "Loading...") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            // We will hook this up to Add Loan/EMI forms later!
            FloatingActionButton(onClick = { showBottomSheet = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Loan or EMI")
            }
        }
    ) { paddingValues ->
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // --- HEADER: TOTAL EXPOSURE ---
            ExposureHeader(
                totalExposure = state.totalExposure,
                phone = state.person?.phoneNumber
            )

            // --- TABS ---
            TabRow(
                selectedTabIndex = pagerState.currentPage,
                indicator = { tabPositions ->
                    if (pagerState.currentPage < tabPositions.size) {
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage])
                        )
                    }
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        },
                        text = { Text(title) }
                    )
                }
            }

            // --- SWIPEABLE CONTENT ---
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                when (page) {
                    0 -> LoansList(
                        loans = state.loans,
                        onEditLoan = { loanId ->
                            state.person?.id?.let { personId ->
                                onNavigateToAddLoan(personId, loanId)
                            }
                        }
                    )
                    1 -> EmisList(
                        emis = state.proxyEmis,
                        onRecordEmiPayment = viewModel::recordEmiPayment,
                        onEditEmi = { emiId ->
                            state.person?.id?.let { personId ->
                                onNavigateToAddEmi(personId, emiId)
                            }
                        },
                        getEmiHistory = viewModel::getEmiHistory
                    )
                }
            }

            if (showBottomSheet) {
                ModalBottomSheet(
                    onDismissRequest = { showBottomSheet = false },
                    sheetState = sheetState
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("What do you want to add?", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(16.dp))

                        ListItem(
                            headlineContent = { Text("Lend Money (Loan)") },
                            supportingContent = { Text("Simple cash loan given to this friend.") },
                            modifier = Modifier.clickable {
                                showBottomSheet = false
                                // Trigger navigation to Loan Form
                                state.person?.id?.let { onNavigateToAddLoan(it, null) }
                            }
                        )
                        Divider()
                        ListItem(
                            headlineContent = { Text("Proxy EMI") },
                            supportingContent = { Text("You bought something for them using your card.") },
                            modifier = Modifier.clickable {
                                showBottomSheet = false
                                state.person?.id?.let { onNavigateToAddEmi(it, null) }
                            }
                        )
                    }
                }
            }

        }
    }
}

@Composable
fun ExposureHeader(totalExposure: Double, phone: String?) {
    val formatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = "Total Exposure", style = MaterialTheme.typography.labelLarge)
                Text(
                    text = formatter.format(totalExposure),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error // Red, because they owe you this money!
                )
                if (!phone.isNullOrBlank()) {
                    Text(text = phone, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
fun LoansList(loans: List<Loan>, onEditLoan: (String) -> Unit) {
    if (loans.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No history with this person yet.")
        }
    } else {
        // Sort by date descending so the newest events are at the top!
        val sortedTimeline = loans.sortedByDescending { it.dateGiven }

        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(sortedTimeline, key = { it.id }) { entry ->
                val formatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
                val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", LocalLocale.current.platformLocale)

                val isLent = entry.type == "LENT"
                val amountColor = if (isLent) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                val headlineText = if (isLent) "You Gave: ${formatter.format(entry.amountLent)}" else "You Got: ${formatter.format(entry.amountLent)}"
                val sign = if (isLent) "-" else "+"

                ListItem(
                    modifier = Modifier.clickable { onEditLoan(entry.id) },
                    headlineContent = { Text(headlineText, fontWeight = FontWeight.Bold, color = amountColor) },
                    supportingContent = {
                        Column {
                            if (!entry.notes.isNullOrBlank()) {
                                Text(entry.notes, style = MaterialTheme.typography.bodyMedium)
                            }
                            Text(sdf.format(Date(entry.dateGiven)), style = MaterialTheme.typography.labelSmall)
                        }
                    },
                    trailingContent = {
                        Text(
                            text = "$sign ${formatter.format(entry.amountLent)}",
                            fontWeight = FontWeight.Bold,
                            color = amountColor
                        )
                    },
                    colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmisList(emis: List<Emi>, onRecordEmiPayment: (String, Double, String) -> Unit, onEditEmi: (String) -> Unit, getEmiHistory: (String) -> Flow<List<AuditLog>>) {
    var selectedEmi by remember { mutableStateOf<Emi?>(null) }
    var paymentAmount by remember { mutableStateOf("") }
    var selectedMonth by remember { mutableStateOf("") }

    var historyEmiId by remember { mutableStateOf<String?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    if (emis.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No EMIs found.") }
    } else {
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(emis, key = { it.id }) { emi ->
                val formatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
                ListItem(
                    modifier = Modifier.clickable { onEditEmi(emi.id) },
                    headlineContent = { Text(emi.itemName, fontWeight = FontWeight.Bold) },
                    supportingContent = {
                        Column {
                            Text("${formatter.format(emi.monthlyEmiAmount)} / month  •  Paid: ${emi.completedMonths}/${emi.totalMonths}")
                            // Show total progress
                            Text("Total Paid: ${formatter.format(emi.amountPaid)} of ${formatter.format(emi.totalAmountWithInterest)}", style = MaterialTheme.typography.labelSmall)
                        }
                    },
                    trailingContent = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { historyEmiId = emi.id }) {
                                Icon(Icons.Default.History, contentDescription = "View History", tint = MaterialTheme.colorScheme.primary)
                            }

                            if (emi.status == "ACTIVE") {
                                Button(onClick = {
                                    selectedEmi = emi
                                    paymentAmount = emi.monthlyEmiAmount.toString()
                                    selectedMonth = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("MMM yyyy"))
                                }) { Text("Pay") }
                            } else {
                                Badge(containerColor = MaterialTheme.colorScheme.primary) { Text("CLOSED") }
                            }
                        }
                    },
                    colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest)
                )
            }
        }
    }

    // --- PAYMENT DIALOG ---
    if (selectedEmi != null) {
        AlertDialog(
            onDismissRequest = { selectedEmi = null },
            title = { Text("Record EMI Payment") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedTextField(
                        value = paymentAmount,
                        onValueChange = { paymentAmount = it },
                        label = { Text("Amount Paid") },
                        prefix = { Text("₹") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = selectedMonth,
                        onValueChange = { selectedMonth = it },
                        label = { Text("For Month (e.g. Jul 2026)") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val amt = paymentAmount.toDoubleOrNull()
                    if (amt != null && amt > 0 && selectedMonth.isNotBlank()) {
                        onRecordEmiPayment(selectedEmi!!.id, amt, selectedMonth)
                        selectedEmi = null
                    }
                }) { Text("Confirm") }
            },
            dismissButton = { TextButton(onClick = { selectedEmi = null }) { Text("Cancel") } }
        )
    }

    // --- REPAYMENT HISTORY BOTTOM SHEET ---
    if (historyEmiId != null) {
        // Collect the Flow specifically for the selected EMI
        val historyFlow = remember(historyEmiId) { getEmiHistory(historyEmiId!!) }
        val historyLogs by historyFlow.collectAsState(initial = emptyList())

        ModalBottomSheet(onDismissRequest = { historyEmiId = null }, sheetState = sheetState) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp).padding(bottom = 32.dp)) {
                Text("Repayment History", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(16.dp))

                // Filter out creation/edits and only show actual Repayments (logs containing "month")
                val repaymentLogs = historyLogs.filter { it.metadata.containsKey("month") }

                if (repaymentLogs.isEmpty()) {
                    Text("No payments recorded yet.", modifier = Modifier.padding(16.dp))
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(repaymentLogs, key = { it.id }) { log ->
                            val formatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
                            val sdf =
                                SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())

                            // Safely extract the JSON metadata
                            val monthStr = log.metadata["month"]?.toString() ?: "Unknown"
                            val amtStr =
                                log.metadata["amountPaid"]?.toString()?.toDoubleOrNull() ?: 0.0

                            ListItem(
                                headlineContent = {
                                    Text(
                                        monthStr,
                                        fontWeight = FontWeight.Bold
                                    )
                                },
                                supportingContent = {
                                    Text(
                                        "Recorded on: ${sdf.format(Date(log.createdAt))}",
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                },
                                trailingContent = {
                                    Text(
                                        "+ ${formatter.format(amtStr)}",
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                },
                                colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            )
                        }
                    }
                }
            }
        }
    }
}
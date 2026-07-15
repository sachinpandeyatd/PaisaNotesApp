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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PersonDetailScreen(
    viewModel: PersonDetailViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToAddLoan: (String) -> Unit,
    onNavigateToAddEmi: (String) -> Unit
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
                    0 -> LoansList(loans = state.loans)
                    1 -> EmisList(emis = state.proxyEmis)
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
                                state.person?.id?.let { onNavigateToAddLoan(it) }
                            }
                        )
                        Divider()
                        ListItem(
                            headlineContent = { Text("Proxy EMI") },
                            supportingContent = { Text("You bought something for them using your card.") },
                            modifier = Modifier.clickable {
                                showBottomSheet = false
                                state.person?.id?.let { onNavigateToAddEmi(it) }
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
fun LoansList(loans: List<Loan>) {
    if (loans.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No active loans.")
        }
    } else {
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(loans, key = { it.id }) { loan ->
                val formatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
                ListItem(
                    headlineContent = { Text(formatter.format(loan.amountLent), fontWeight = FontWeight.Bold) },
                    supportingContent = {
                        val sdf = SimpleDateFormat("dd MMM yyyy", LocalLocale.current.platformLocale)
                        Text("Given on: ${sdf.format(Date(loan.dateGiven))}")
                    },
                    trailingContent = {
                        Badge(containerColor = if (loan.status == "ACTIVE") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary) {
                            Text(loan.status)
                        }
                    },
                    colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest)
                )
            }
        }
    }
}

@Composable
fun EmisList(emis: List<Emi>) {
    if (emis.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No proxy EMIs linked to this person.")
        }
    } else {
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(emis, key = { it.id }) { emi ->
                val formatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
                ListItem(
                    headlineContent = { Text(emi.itemName, fontWeight = FontWeight.Bold) },
                    supportingContent = { Text("${formatter.format(emi.monthlyEmiAmount)} / month") },
                    trailingContent = {
                        Text("${emi.totalMonths} Months", style = MaterialTheme.typography.labelMedium)
                    },
                    colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest)
                )
            }
        }
    }
}
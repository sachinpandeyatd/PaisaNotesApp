package com.paisanotes.presentation.my_emis

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.paisanotes.presentation.add_emi.MyEmisViewModel
import com.paisanotes.presentation.person_detail.EmisList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyEmisScreen(
    viewModel: MyEmisViewModel = hiltViewModel(),
    onOpenDrawer: () -> Unit,
    onNavigateToAddEmi: (String?) -> Unit
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My EMIs") },
                navigationIcon = { IconButton(onClick = onOpenDrawer) { Icon(Icons.Default.Menu, "Menu") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    onNavigateToAddEmi(null)
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add EMI"
                )
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                EmisList(
                    emis = state.emis,
                    onRecordEmiPayment = viewModel::recordEmiPayment,
                    onEditEmi = { emiId -> onNavigateToAddEmi(emiId) }
                )
            }
        }
    }
}
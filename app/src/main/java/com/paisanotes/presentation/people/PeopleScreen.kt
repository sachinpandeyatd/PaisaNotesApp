package com.paisanotes.presentation.people

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.paisanotes.domain.model.Person
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PeopleScreen(
    viewModel: PeopleViewModel = hiltViewModel(),
    onNavigateToPersonDetail: (String) -> Unit // Will use this later to view their loans/EMIs!
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("People & Friends") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.showAddDialog() }) {
                Icon(Icons.Default.Add, contentDescription = "Add Person")
            }
        }
    ) { paddingValues ->
        
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (state.people.isEmpty()) {
                Text(
                    text = "No people added yet.\nAdd friends to track loans and EMIs!",
                    modifier = Modifier.align(Alignment.Center),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.people, key = { it.id }) { person ->
                        PersonItemCard(person = person) {
                            onNavigateToPersonDetail(person.id)
                        }
                    }
                }
            }
        }

        // --- INLINE ADD DIALOG ---
        if (state.isAddDialogVisible) {
            AlertDialog(
                onDismissRequest = { viewModel.hideAddDialog() },
                title = { Text("Add Friend") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = state.newPersonName,
                            onValueChange = viewModel::onNameChange,
                            label = { Text("Name") },
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = state.newPersonPhone,
                            onValueChange = viewModel::onPhoneChange,
                            label = { Text("Phone Number (Optional)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true
                        )
                    }
                },
                confirmButton = {
                    Button(onClick = { viewModel.savePerson() }) { Text("Save") }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.hideAddDialog() }) { Text("Cancel") }
                }
            )
        }
    }
}

@Composable
fun PersonItemCard(person: Person, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.padding(12.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(text = person.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                // 🚨 DYNAMIC CALCULATION FORMATTING
                val formatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
                val formattedExposure = formatter.format(person.totalExposure)

                // If exposure is > 0, we highlight it in Red!
                val exposureColor = if (person.totalExposure > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant

                Text(
                    text = "Total Exposure: $formattedExposure",
                    style = MaterialTheme.typography.bodySmall,
                    color = exposureColor,
                    fontWeight = if (person.totalExposure > 0) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}
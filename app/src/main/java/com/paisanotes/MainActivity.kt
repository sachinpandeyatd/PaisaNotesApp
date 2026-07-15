package com.paisanotes

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.paisanotes.data.local.TokenManager
import com.paisanotes.presentation.add_transaction.AddTransactionScreen
import com.paisanotes.presentation.auth.LoginScreen
import com.paisanotes.presentation.navigation.AddTransactionRoute
import com.paisanotes.presentation.navigation.LoginRoute
import com.paisanotes.presentation.navigation.PeopleRoute
import com.paisanotes.presentation.navigation.TransactionsRoute
import com.paisanotes.presentation.people.PeopleScreen
import com.paisanotes.presentation.transactions.TransactionsScreen
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var tokenManager: TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val startScreen = if (tokenManager.getToken() != null) TransactionsRoute else LoginRoute

        setContent {
            MaterialTheme {
                Surface {

                    // --- Local Network permission gate (Android 17 / SDK 37+) ---
                    var permissionGranted by remember {
                        mutableStateOf(
                            if (Build.VERSION.SDK_INT >= 36) {
                                ContextCompat.checkSelfPermission(
                                    this, Manifest.permission.ACCESS_LOCAL_NETWORK
                                ) == PackageManager.PERMISSION_GRANTED
                            } else true // permission doesn't exist below API 36
                        )
                    }

                    val launcher = rememberLauncherForActivityResult(
                        ActivityResultContracts.RequestPermission()
                    ) { granted -> permissionGranted = granted }

                    LaunchedEffect(Unit) {
                        if (Build.VERSION.SDK_INT >= 36 && !permissionGranted) {
                            launcher.launch(Manifest.permission.ACCESS_LOCAL_NETWORK)
                        }
                    }

                    if (!permissionGranted && Build.VERSION.SDK_INT >= 36) {
                        // Simple blocking screen until granted — replace with your own rationale UI
                        Column(
                            modifier = androidx.compose.ui.Modifier.fillMaxSize(),
                            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                            verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
                        ) {
                            Text("PaisaNotes needs local network access to sync with your server.")
                            Spacer(modifier = androidx.compose.ui.Modifier.height(16.dp))
                            Button(onClick = { launcher.launch(Manifest.permission.ACCESS_LOCAL_NETWORK) }) {
                                Text("Grant permission")
                            }
                        }
                    } else {
                        val navController = rememberNavController()
                        NavHost(navController = navController, startDestination = PeopleRoute) {
                            composable<PeopleRoute> {
                                PeopleScreen (
                                    onNavigateToPersonDetail = { personId ->
                                        // We will build the detail screen next!
                                    }
                                )
                            }
                            composable<TransactionsRoute> {
                                TransactionsScreen(onNavigateToAddTransaction = {
                                    navController.navigate(AddTransactionRoute())
                                })
                            }
                            composable<AddTransactionRoute> {
                                AddTransactionScreen(onNavigateBack = { navController.popBackStack() })
                            }
                        }
                    }
                }
            }
        }
    }
}
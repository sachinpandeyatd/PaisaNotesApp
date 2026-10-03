package com.paisanotes

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.paisanotes.data.local.TokenManager
import com.paisanotes.presentation.add_transaction.AddTransactionScreen
import com.paisanotes.presentation.auth.LoginScreen
import com.paisanotes.presentation.main.MainScreen
import com.paisanotes.presentation.navigation.AddTransactionRoute
import com.paisanotes.presentation.navigation.HomeRoute
import com.paisanotes.presentation.navigation.LoginRoute
import com.paisanotes.presentation.navigation.MyEmisRoute
import com.paisanotes.presentation.navigation.PeopleRoute
import com.paisanotes.presentation.navigation.TransactionsRoute
import com.paisanotes.presentation.transactions.TransactionsScreen
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var tokenManager: TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val quickAction = intent.getStringExtra("QUICK_ACTION")
        val intentAction = intent.action
        val editTxnId = intent.getStringExtra("TXN_ID")

        val startScreen = if (tokenManager.getToken() != null) {
            if (intentAction == "ACTION_EDIT_TXN" && editTxnId != null) {
                AddTransactionRoute(transactionId = editTxnId)
            }
            when (quickAction) {
                "TRANSACTION" -> AddTransactionRoute(null)
                "LOAN" -> PeopleRoute // (Or AddLoanRoute if you want to pass a default person)
                "EMI" -> MyEmisRoute
                else -> HomeRoute
            }
        } else {
            LoginRoute
        }

        setContent {
            MaterialTheme {
                Surface {
                    // Wrap the entire app in our new PermissionsGate!
                    com.paisanotes.presentation.onboarding.PermissionsGate {

                        // If all permissions are granted, it renders the App!
                        MainScreen(startDestination = startScreen)

                    }
                }
            }
        }
    }
}
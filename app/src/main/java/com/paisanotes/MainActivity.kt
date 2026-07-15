package com.paisanotes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.paisanotes.data.local.TokenManager
import com.paisanotes.presentation.add_transaction.AddTransactionScreen
import com.paisanotes.presentation.auth.LoginScreen
import com.paisanotes.presentation.navigation.AddTransactionRoute
import com.paisanotes.presentation.navigation.LoginRoute
import com.paisanotes.presentation.navigation.TransactionsRoute
import com.paisanotes.presentation.transactions.TransactionsScreen
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // Inject TokenManager to check login status before drawing UI
    @Inject
    lateinit var tokenManager: TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Determine start destination
        val startScreen = if (tokenManager.getToken() != null) TransactionsRoute else LoginRoute

        setContent {
            MaterialTheme {
                Surface {
                    val navController = rememberNavController()

                    NavHost(
                        navController = navController,
                        startDestination = startScreen // <--- THE AUTH GATE
                    ) {

                        composable<LoginRoute> {
                            LoginScreen(
                                onLoginSuccess = {
                                    // Pop the login screen off the backstack so user can't press 'Back' to return to it
                                    navController.navigate(TransactionsRoute) {
                                        popUpTo(LoginRoute) { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable<TransactionsRoute> {
                            TransactionsScreen(
                                onNavigateToAddTransaction = {
                                    navController.navigate(AddTransactionRoute())
                                }
                            )
                        }

                        composable<AddTransactionRoute> {
                            AddTransactionScreen(
                                onNavigateBack = {
                                    navController.popBackStack()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
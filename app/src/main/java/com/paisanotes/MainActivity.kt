package com.paisanotes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.paisanotes.presentation.add_transaction.AddTransactionScreen
import com.paisanotes.presentation.navigation.AddTransactionRoute
import com.paisanotes.presentation.navigation.TransactionsRoute
import com.paisanotes.presentation.transactions.TransactionsScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                Surface {
                    // 1. Create the Navigation Controller
                    val navController = rememberNavController()

                    // 2. Define the Navigation Graph
                    NavHost(
                        navController = navController,
                        startDestination = TransactionsRoute // Start on the Transactions screen
                    ) {

                        // Screen 1: Transactions List
                        composable<TransactionsRoute> {
                            TransactionsScreen(
                                onNavigateToAddTransaction = {
                                    // When FAB is clicked, navigate to Add screen!
                                    navController.navigate(AddTransactionRoute())
                                }
                            )
                        }

                        // Screen 2: Add Transaction Form
                        composable<AddTransactionRoute> {
                            AddTransactionScreen(
                                onNavigateBack = {
                                    // When Saved or Back arrow is clicked, pop back to the list!
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
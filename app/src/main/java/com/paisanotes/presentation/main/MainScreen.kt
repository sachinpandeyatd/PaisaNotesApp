package com.paisanotes.presentation.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.paisanotes.presentation.add_transaction.AddTransactionScreen
import com.paisanotes.presentation.auth.LoginScreen
import com.paisanotes.presentation.navigation.AddTransactionRoute
import com.paisanotes.presentation.navigation.LoginRoute
import com.paisanotes.presentation.navigation.PeopleRoute
import com.paisanotes.presentation.navigation.TransactionsRoute
import com.paisanotes.presentation.navigation.bottomNavItems
import com.paisanotes.presentation.people.PeopleScreen
import com.paisanotes.presentation.transactions.TransactionsScreen

@Composable
fun MainScreen(startDestination: Any) {
    val navController = rememberNavController()

    // Track the current route to conditionally show/hide the BottomBar
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Hide BottomBar on Login and Add Transaction screens
    val hideBottomBar = currentDestination?.hasRoute(LoginRoute::class) == true ||
            currentDestination?.hasRoute(AddTransactionRoute::class) == true

    Scaffold(
        bottomBar = {
            if (!hideBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { topLevelRoute ->

                        // Check if the current route matches this tab
                        val isSelected = currentDestination?.hierarchy?.any {
                            it.hasRoute(topLevelRoute.routeClass)
                        } == true

                        NavigationBarItem(
                            icon = { Icon(topLevelRoute.icon, contentDescription = topLevelRoute.name) },
                            label = { Text(topLevelRoute.name) },
                            selected = isSelected,
                            onClick = {
                                navController.navigate(topLevelRoute.route) {
                                    // 🚨 INTERVIEW MAGIC: Prevent backstack bloat!
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->

        // This is the container where all our screens will be injected!
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            // 1. LOGIN
            composable<LoginRoute> {
                LoginScreen(onLoginSuccess = {
                    navController.navigate(TransactionsRoute) {
                        popUpTo(LoginRoute) { inclusive = true }
                    }
                })
            }

            // 2. TRANSACTIONS TAB
            composable<TransactionsRoute> {
                TransactionsScreen(onNavigateToAddTransaction = {
                    navController.navigate(AddTransactionRoute())
                })
            }

            // 3. PEOPLE TAB
            composable<PeopleRoute> {
                PeopleScreen(onNavigateToPersonDetail = { personId ->
                    // We will add the PersonDetailRoute later!
                })
            }

            // 4. ADD TRANSACTION FORM
            composable<AddTransactionRoute> {
                AddTransactionScreen(onNavigateBack = {
                    navController.popBackStack()
                })
            }
        }
    }
}
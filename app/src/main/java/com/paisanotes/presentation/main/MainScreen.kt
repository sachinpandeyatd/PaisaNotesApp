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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.paisanotes.presentation.add_emi.AddEmiScreen
import com.paisanotes.presentation.add_loan.AddLoanScreen
import com.paisanotes.presentation.add_transaction.AddTransactionScreen
import com.paisanotes.presentation.auth.LoginScreen
import com.paisanotes.presentation.auth.RegisterScreen
import com.paisanotes.presentation.navigation.AddEmiRoute
import com.paisanotes.presentation.navigation.AddLoanRoute
import com.paisanotes.presentation.navigation.AddTransactionRoute
import com.paisanotes.presentation.navigation.LoginRoute
import com.paisanotes.presentation.navigation.PeopleRoute
import com.paisanotes.presentation.navigation.PersonDetailRoute
import com.paisanotes.presentation.navigation.RegisterRoute
import com.paisanotes.presentation.navigation.TransactionsRoute
import com.paisanotes.presentation.navigation.bottomNavItems
import com.paisanotes.presentation.people.PeopleScreen
import com.paisanotes.presentation.person_detail.PersonDetailScreen
import com.paisanotes.presentation.transactions.TransactionsScreen

@Composable
fun MainScreen(startDestination: Any) {
    val navController = rememberNavController()

    // Track the current route to conditionally show/hide the BottomBar
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Hide BottomBar on Login and Add Transaction screens
    val hideBottomBar = currentDestination?.hasRoute(LoginRoute::class) == true ||
            currentDestination?.hasRoute(AddTransactionRoute::class) == true ||
            currentDestination?.hasRoute(RegisterRoute::class) == true ||
            currentDestination?.hasRoute(AddLoanRoute::class) == true ||
            currentDestination?.hasRoute(AddEmiRoute::class) == true

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
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate(TransactionsRoute) {
                            popUpTo(LoginRoute) { inclusive = true }
                        }
                    },
                    onNavigateToRegister = {
                        navController.navigate(RegisterRoute)
                    }
                )
            }

            // 2. REGISTER
            composable<RegisterRoute> {
                RegisterScreen(
                    onRegisterSuccess = {
                        navController.navigate(TransactionsRoute) {
                            // If they registered, pop everything off so they can't go back to login/register
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onNavigateToLogin = {
                        navController.popBackStack()
                    }
                )
            }

            // 2. TRANSACTIONS TAB
            composable<TransactionsRoute> {
                TransactionsScreen(
                    onNavigateToAddTransaction = { id ->
                        navController.navigate(AddTransactionRoute(transactionId = id))
                    }
                )
            }

            // 3. PEOPLE TAB
            composable<PeopleRoute> {
                PeopleScreen(onNavigateToPersonDetail = { personId ->
                    navController.navigate(PersonDetailRoute(personId = personId))
                })
            }

            // 4. ADD TRANSACTION FORM
            composable<AddTransactionRoute> {
                AddTransactionScreen(onNavigateBack = {
                    navController.popBackStack()
                })
            }

            // 5. PERSON DETAIL SCREEN
            composable<PersonDetailRoute> {
                PersonDetailScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToAddLoan = { personId ->
                        navController.navigate(AddLoanRoute(personId))
                    },
                    onNavigateToAddEmi = { personId ->
                        navController.navigate(AddEmiRoute(personId))
                    }
                )
            }

            // 6. ADD LOAN
            composable<AddLoanRoute> {
                AddLoanScreen(onNavigateBack = { navController.popBackStack() })
            }

            // 7. ADD EMI
            composable<AddEmiRoute> {
                AddEmiScreen(onNavigateBack = { navController.popBackStack() })
            }
        }
    }
}
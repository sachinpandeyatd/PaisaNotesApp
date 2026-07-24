package com.paisanotes.presentation.main

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
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
import com.paisanotes.presentation.auth.ForgotPasswordScreen
import com.paisanotes.presentation.auth.LoginScreen
import com.paisanotes.presentation.auth.RegisterScreen
import com.paisanotes.presentation.navigation.*
import com.paisanotes.presentation.people.PeopleScreen
import com.paisanotes.presentation.person_detail.PersonDetailScreen
import com.paisanotes.presentation.transactions.TransactionsScreen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    startDestination: Any,
    viewModel: MainViewModel = hiltViewModel() // 🚨 Inject the ViewModel
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Drawer State
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    val hideBottomBar = currentDestination?.hasRoute(LoginRoute::class) == true ||
            currentDestination?.hasRoute(RegisterRoute::class) == true ||
            currentDestination?.hasRoute(AddTransactionRoute::class) == true ||
            currentDestination?.hasRoute(AddLoanRoute::class) == true ||
            currentDestination?.hasRoute(AddEmiRoute::class) == true ||
            currentDestination?.hasRoute(SettingsRoute::class) == true ||
            currentDestination?.hasRoute(ForgotPasswordRoute::class) == true ||
            currentDestination?.hasRoute(BudgetsRoute::class) == true

    // 🚨 WRAP EVERYTHING IN THE DRAWER
    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = !hideBottomBar, // Disable swiping drawer open on Login/Forms
        drawerContent = {
            ModalDrawerSheet {
                Spacer(Modifier.height(24.dp))
                Text(
                    text = "PaisaNotes",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                HorizontalDivider()
                Spacer(Modifier.height(16.dp))

                // LOGOUT BUTTON
                NavigationDrawerItem(
                    icon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout") },
                    label = { Text("Logout") },
                    selected = false,
                    onClick = {
                        coroutineScope.launch { drawerState.close() }
                        viewModel.logout()
                        // Route back to login and DESTROY the backstack
                        navController.navigate(LoginRoute) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                NavigationDrawerItem(
                    icon = { Icon(androidx.compose.material.icons.Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") },
                    selected = false,
                    onClick = {
                        coroutineScope.launch { drawerState.close() }
                        navController.navigate(SettingsRoute)
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                NavigationDrawerItem(
                    icon = { Icon(androidx.compose.material.icons.Icons.Default.PieChart, "Budgets") },
                    label = { Text("Budgets") },
                    selected = false,
                    onClick = {
                        coroutineScope.launch { drawerState.close() }
                        navController.navigate(BudgetsRoute)
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
            }
        }
    ) {
        Scaffold(
            bottomBar = {
                if (!hideBottomBar) {
                    NavigationBar {
                        bottomNavItems.forEach { topLevelRoute ->
                            val isSelected = currentDestination?.hierarchy?.any { it.hasRoute(topLevelRoute.routeClass) } == true
                            NavigationBarItem(
                                icon = { Icon(topLevelRoute.icon, contentDescription = topLevelRoute.name) },
                                label = { Text(topLevelRoute.name) },
                                selected = isSelected,
                                onClick = {
                                    navController.navigate(topLevelRoute.route) {
                                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
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
            NavHost(
                navController = navController,
                startDestination = startDestination,
                modifier = Modifier.padding(innerPadding)
            ) {
                // 1. LOGIN & REGISTER
                composable<LoginRoute> {
                    LoginScreen(
                        onLoginSuccess = {
                            navController.navigate(TransactionsRoute) { popUpTo(LoginRoute) { inclusive = true } }
                        },
                        onNavigateToRegister = {
                            navController.navigate(RegisterRoute)
                        },
                        onNavigateToForgotPassword = {
                            navController.navigate(ForgotPasswordRoute)
                        }
                    )
                }
                composable<RegisterRoute> {
                    RegisterScreen(
                        onRegisterSuccess = {
                            navController.navigate(TransactionsRoute) { popUpTo(0) { inclusive = true } }
                        },
                        onNavigateToLogin = { navController.popBackStack() }
                    )
                }

                // 2. TRANSACTIONS TAB
                composable<TransactionsRoute> {
                    TransactionsScreen(
                        onNavigateToAddTransaction = { id -> navController.navigate(AddTransactionRoute(id)) },
                        onOpenDrawer = { coroutineScope.launch { drawerState.open() } } // 🚨 PASS DRAWER CALLBACK
                    )
                }

                // 3. PEOPLE TAB
                composable<PeopleRoute> {
                    PeopleScreen(
                        onNavigateToPersonDetail = { id -> navController.navigate(PersonDetailRoute(id)) },
                        onOpenDrawer = { coroutineScope.launch { drawerState.open() } } // 🚨 PASS DRAWER CALLBACK
                    )
                }

                // 4. PERSON DETAIL & FORMS (Keep your existing ones)
                composable<PersonDetailRoute> {
                    PersonDetailScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToAddLoan = { id -> navController.navigate(AddLoanRoute(id)) },
                        onNavigateToAddEmi = { id -> navController.navigate(AddEmiRoute(id)) }
                    )
                }
                composable<AddTransactionRoute> { AddTransactionScreen(onNavigateBack = { navController.popBackStack() }) }
                composable<AddLoanRoute> { AddLoanScreen(onNavigateBack = { navController.popBackStack() }) }
                composable<AddEmiRoute> { AddEmiScreen(onNavigateBack = { navController.popBackStack() }) }
                composable<SettingsRoute> {
                    com.paisanotes.presentation.settings.SettingsScreen(
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable<HomeRoute> {
                    com.paisanotes.presentation.home.HomeScreen(
                        onOpenDrawer = { coroutineScope.launch { drawerState.open() } },
                        onNavigateToAddTransaction = { navController.navigate(AddTransactionRoute(null)) },
                        onNavigateToEditTransaction = { id -> navController.navigate(AddTransactionRoute(id)) }
                    )
                }

                composable<LogsRoute> {
                    com.paisanotes.presentation.logs.LogsScreen(
                        onOpenDrawer = { coroutineScope.launch { drawerState.open() } }
                    )
                }

                composable<ForgotPasswordRoute> {
                    ForgotPasswordScreen(onNavigateBack = { navController.popBackStack() })
                }
                composable<BudgetsRoute> {
                    com.paisanotes.presentation.budgets.BudgetsScreen(
                        onOpenDrawer = { coroutineScope.launch { drawerState.open() } }
                    )
                }
            }
        }
    }
}
package com.paisanotes.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.serialization.Serializable
import kotlin.reflect.KClass

// An object because this screen doesn't need any arguments
@Serializable
data object LoginRoute

@Serializable
data object TransactionsRoute

@Serializable
data class AddTransactionRoute(val transactionId: String? = null)

@Serializable
data class PersonDetailRoute(val personId: String)

@Serializable
data object PeopleRoute

@Serializable
data class AddLoanRoute(val personId: String)

@Serializable
data class AddEmiRoute(val personId: String)

// Add this below LoginRoute
@Serializable
data object RegisterRoute

@Serializable
data object SettingsRoute

@Serializable
data object HomeRoute

@Serializable
data object LogsRoute

// --- NEW: Bottom Navigation Helper ---
data class TopLevelRoute<T : Any>(
    val name: String,
    val route: T,
    val routeClass: KClass<T>,
    val icon: ImageVector
)

val bottomNavItems = listOf(
    TopLevelRoute("Transactions", TransactionsRoute, TransactionsRoute::class, Icons.Default.List),
    TopLevelRoute("People", PeopleRoute, PeopleRoute::class, Icons.Default.Person),
    TopLevelRoute("Home", HomeRoute, HomeRoute::class, Icons.Default.Home),
    TopLevelRoute("Logs", LogsRoute, LogsRoute::class, Icons.Default.Info)
)
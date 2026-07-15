package com.paisanotes.presentation.navigation

import androidx.compose.material.icons.Icons
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
data object PeopleRoute

// --- NEW: Bottom Navigation Helper ---
data class TopLevelRoute<T : Any>(
    val name: String,
    val route: T,
    val routeClass: KClass<T>,
    val icon: ImageVector
)

val bottomNavItems = listOf(
    TopLevelRoute("Transactions", TransactionsRoute, TransactionsRoute::class, Icons.Default.List),
    TopLevelRoute("People", PeopleRoute, PeopleRoute::class, Icons.Default.Person)
    // We will add EMIs and Logs here later!
)
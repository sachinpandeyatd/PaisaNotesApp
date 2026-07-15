package com.paisanotes.presentation.navigation

import kotlinx.serialization.Serializable

// An object because this screen doesn't need any arguments
@Serializable
data object TransactionsRoute

@Serializable
data object LoginRoute

// A data class because we might pass an ID later if we want to Edit a transaction!
@Serializable
data class AddTransactionRoute(val transactionId: String? = null)
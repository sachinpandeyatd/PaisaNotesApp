package com.paisanotes.service

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.paisanotes.domain.model.Transaction
import com.paisanotes.domain.parser.NotificationParser
import com.paisanotes.domain.repository.TransactionRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

// Tell Hilt to inject dependencies into this Android component!
@AndroidEntryPoint
class PaisaNotificationListener : NotificationListenerService() {

    @Inject
    lateinit var parser: NotificationParser

    @Inject
    lateinit var repository: TransactionRepository

    // A coroutine scope specifically for this service
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn == null) return

        val packageName = sbn.packageName
        val notification = sbn.notification
        val extras = notification.extras

        val title = extras.getString(Notification.EXTRA_TITLE) ?: ""
        val text = extras.getString(Notification.EXTRA_TEXT) ?: ""

        Log.d("PaisaListener", "Notification from $packageName: $title - $text")

        // 1. Pass to our Parser
        val parsedData = parser.parse(packageName, title, text)

        if (parsedData != null) {
            Log.d("PaisaListener", "Parsed successfully: $parsedData")

            // 2. Automatically save the transaction to the database
            serviceScope.launch {
                val transaction = Transaction(
                    id = UUID.randomUUID().toString(),
                    amount = parsedData.amount,
                    transactionType = parsedData.type,
                    merchant = parsedData.merchant,
                    category = "Auto-Captured", // We can add ML categorization later!
                    transactionDate = System.currentTimeMillis(),
                    paymentMethod = "UPI",
                    source = "NOTIFICATION", // Audit trail magic!
                    notes = "Captured from $packageName"
                )

                // 🚨 This single line of code saves it to Room AND triggers the WorkManager
                // to push it to Spring Boot!
                repository.saveTransaction(transaction)
            }
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        // We don't care when they swipe the notification away
    }
}
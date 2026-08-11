package com.paisanotes.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.core.app.NotificationCompat
import com.paisanotes.MainActivity
import com.paisanotes.domain.model.Transaction
import com.paisanotes.domain.parser.NotificationParser
import com.paisanotes.domain.repository.AccountRepository
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

    @Inject
    lateinit var accountRepository: AccountRepository

    // A coroutine scope specifically for this service
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val processedHashes = java.util.Collections.newSetFromMap(object : java.util.LinkedHashMap<Int, Boolean>() {
        override fun removeEldestEntry(eldest: Map.Entry<Int, Boolean>): Boolean {
            return size > 50
        }
    })

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn == null) return

        val packageName = sbn.packageName
        val notification = sbn.notification
        val extras = notification.extras

        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: ""
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""

        Log.d("PaisaListener", "Notification from $packageName: $title - $text")

        val payloadHash = "$packageName:$title:$text".hashCode()
        if (processedHashes.contains(payloadHash)) return
        processedHashes.add(payloadHash)

        // 1. Pass to our Parser
        val parsedData = parser.parse(packageName, title, text)

        if (parsedData != null) {
            Log.d("PaisaListener", "Parsed successfully: $parsedData")

            serviceScope.launch {

                val matchedAccountId = accountRepository.getOrCreateAccountId(parsedData.accountName)
                val notesText = "Captured from $packageName"

                // DEDUPLICATION CHECK (e.g., 5-minute window = 5 * 60 * 1000 milliseconds)
                val isDuplicate = repository.hasRecentDuplicate(
                    amount = parsedData.amount,
                    type = parsedData.type,
                    notes = notesText,
                    timeWindowMs = 120_000L
                )

                if (isDuplicate) {
                    Log.d("PaisaListener", "Duplicate notification ignored for amount: ${parsedData.amount}")
                    return@launch // Stop execution! Do not save!
                }


                // 2. If it's unique, save it as usual!
                val transaction = Transaction(
                    id = UUID.randomUUID().toString(),
                    amount = parsedData.amount,
                    transactionType = parsedData.type,
                    merchant = parsedData.merchant,
                    category = "Auto-Captured",
                    categoryId = null,
                    accountId = matchedAccountId,
                    transferAccountId = null,
                    transactionDate = System.currentTimeMillis(),
                    paymentMethod = "UPI",
                    source = "NOTIFICATION",
                    notes = "Captured from $packageName"
                )

                repository.saveTransaction(transaction)

                showSuccessAlert(parsedData.amount, parsedData.type, parsedData.accountName)
            }
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        // We don't care when they swipe the notification away
    }

    private fun showSuccessAlert(amount: Double, type: String, sourceApp: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // 1. Create the Notification Channel (Required for modern Android)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "paisa_auto_capture",
                "Auto-Capture Alerts",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        // 2. Create the Intent so clicking the notification opens the app!
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        // 3. Build and fire the notification
        val builder = NotificationCompat.Builder(this, "paisa_auto_capture")
            .setSmallIcon(android.R.drawable.ic_menu_save) // A standard save icon
            .setContentTitle("PaisaNotes: Auto-Captured $type")
            .setContentText("Successfully auto-captured transaction of ₹$amount from $sourceApp")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        // Use a random ID so multiple captures don't overwrite each other
        notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
    }
}
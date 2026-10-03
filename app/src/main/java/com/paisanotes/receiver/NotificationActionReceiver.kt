package com.paisanotes.receiver

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.paisanotes.domain.repository.TransactionRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class NotificationActionReceiver : BroadcastReceiver() {

    @Inject
    lateinit var repository: TransactionRepository

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "ACTION_DELETE_TXN") {
            val txnId = intent.getStringExtra("TXN_ID") ?: return
            val notifId = intent.getIntExtra("NOTIF_ID", -1)

            // goAsync() tells Android to keep this receiver alive long enough to finish the database operation
            val pendingResult = goAsync()

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    // 1. Delete from Room (This also generates the Audit Log and triggers the Sync Worker!)
                    repository.deleteTransaction(txnId)

                    // 2. Dismiss the notification from the user's screen
                    if (notifId != -1) {
                        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                        nm.cancel(notifId)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
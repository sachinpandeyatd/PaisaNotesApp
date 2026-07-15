package com.paisanotes.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.paisanotes.domain.repository.TransactionRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: TransactionRepository // Hilt injects this magically!
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            // We call the exact same sync logic the manual Refresh button uses!
            val success = repository.syncWithServer()

            if (success) {
                Result.success()
            } else {
                // If the server returns a 500, or network drops mid-sync, tell the OS to retry later.
                Result.retry()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Using retry() uses exponential backoff (retries in 10s, then 20s, 40s...)
            Result.retry()
        }
    }
}
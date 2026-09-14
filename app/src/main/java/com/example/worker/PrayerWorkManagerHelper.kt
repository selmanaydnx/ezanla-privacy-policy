package com.example.worker

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object PrayerWorkManagerHelper {

    /**
     * Enqueues a periodic background sync worker that runs every 15 minutes (or as permitted by Android OS).
     * Uses ExistingPeriodicWorkPolicy.KEEP so it preserves existing work schedules without thrashing.
     */
    fun enqueuePeriodicPrayerSync(context: Context) {
        val workManager = WorkManager.getInstance(context)

        // Flexible periodic work: 15 minutes interval, 5 minutes flex interval
        val periodicRequest = PeriodicWorkRequestBuilder<PrayerSyncWorker>(
            15, TimeUnit.MINUTES,
            5, TimeUnit.MINUTES
        )
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 1, TimeUnit.MINUTES)
            .build()

        workManager.enqueueUniquePeriodicWork(
            PrayerSyncWorker.WORK_NAME_PERIODIC,
            ExistingPeriodicWorkPolicy.UPDATE,
            periodicRequest
        )
    }

    /**
     * Enqueues an immediate one-time sync worker.
     * Useful when city changes, alarms are updated, or user requests immediate sync.
     */
    fun enqueueImmediateSync(context: Context) {
        val workManager = WorkManager.getInstance(context)

        val oneTimeRequest = OneTimeWorkRequestBuilder<PrayerSyncWorker>()
            .setBackoffCriteria(BackoffPolicy.LINEAR, 10, TimeUnit.SECONDS)
            .build()

        workManager.enqueueUniqueWork(
            PrayerSyncWorker.WORK_NAME_ONE_TIME,
            ExistingWorkPolicy.REPLACE,
            oneTimeRequest
        )
    }

    /**
     * Cancels all scheduled prayer sync background workers.
     */
    fun cancelAllPrayerSync(context: Context) {
        val workManager = WorkManager.getInstance(context)
        workManager.cancelUniqueWork(PrayerSyncWorker.WORK_NAME_PERIODIC)
        workManager.cancelUniqueWork(PrayerSyncWorker.WORK_NAME_ONE_TIME)
    }

    /**
     * Formats the last background sync time for display in the UI.
     */
    fun formatLastSyncTime(lastSyncMillis: Long): String {
        if (lastSyncMillis <= 0L) return "Henüz senkronize edilmedi"
        val diffMinutes = (System.currentTimeMillis() - lastSyncMillis) / (60 * 1000L)
        return when {
            diffMinutes < 1 -> "Az önce"
            diffMinutes < 60 -> "$diffMinutes dakika önce"
            else -> {
                val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
                "Bugün ${sdf.format(Date(lastSyncMillis))}"
            }
        }
    }
}

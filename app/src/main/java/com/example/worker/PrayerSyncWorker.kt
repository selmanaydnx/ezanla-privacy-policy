package com.example.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.alarm.PrayerAlarmScheduler
import com.example.alarm.PrayerNotificationHelper
import com.example.audio.AdhanAudioPlayer
import com.example.calc.PrayerCalculationEngine
import com.example.data.AppPreferencesRepository
import com.example.model.NotificationMode
import com.example.model.PrayerTimeItem
import com.example.model.PrayerType
import com.example.widget.PrayerWidgetProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar

/**
 * High-reliability WorkManager Worker for prayer time synchronization and notification guarantees.
 * Designed especially for devices with aggressive battery savers (Xiaomi/MIUI, Huawei, Samsung, Oppo).
 *
 * It runs periodically in the background:
 * 1. Calculates prayer times for today and tomorrow.
 * 2. Re-arms exact alarms via AlarmManager in case they were cleared by system battery optimizations.
 * 3. Checks if any prayer alarm was missed due to deep sleep / aggressive killing within a tolerance window (15 mins),
 *    and delivers the notification immediately.
 * 4. Refreshes the Home Screen Widget.
 */
class PrayerSyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val context = applicationContext
            val prefs = AppPreferencesRepository(context)
            val city = prefs.getSelectedCity()
            val now = System.currentTimeMillis()

            // 1. Calculate today's prayer times
            val calToday = Calendar.getInstance()
            val todayYear = calToday.get(Calendar.YEAR)
            val todayMonth = calToday.get(Calendar.MONTH) + 1
            val todayDay = calToday.get(Calendar.DAY_OF_MONTH)

            val rawTodayTimes = PrayerCalculationEngine.calculateTimesForDate(
                todayYear, todayMonth, todayDay, city
            )

            // 2. Calculate tomorrow's prayer times to guarantee 24-48 hours coverage
            val calTomorrow = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }
            val rawTomorrowTimes = PrayerCalculationEngine.calculateTimesForDate(
                calTomorrow.get(Calendar.YEAR),
                calTomorrow.get(Calendar.MONTH) + 1,
                calTomorrow.get(Calendar.DAY_OF_MONTH),
                city
            )

            val precaution = prefs.getPrecautionMinutes()
            val adjustList: (List<PrayerTimeItem>) -> List<PrayerTimeItem> = { list ->
                if (precaution == 0) list else list.map { item ->
                    val adjustedMillis = item.timestampMillis + (precaution * 60 * 1000L)
                    val c = Calendar.getInstance().apply { timeInMillis = adjustedMillis }
                    val formatted = String.format("%02d:%02d", c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE))
                    item.copy(timeFormatted = formatted, timestampMillis = adjustedMillis)
                }
            }

            val todayAdjusted = adjustList(rawTodayTimes)
            val tomorrowAdjusted = adjustList(rawTomorrowTimes)
            val combinedList = todayAdjusted + tomorrowAdjusted

            val notifSettings = prefs.getNotificationSettings()
            val isEarlyReminderEnabled = prefs.isEarlyReminderEnabled()

            // 3. Re-schedule upcoming exact alarms for both today and tomorrow
            PrayerAlarmScheduler.scheduleUpcomingPrayerAlarms(
                context = context,
                prayerTimes = combinedList,
                cityName = city.name,
                notificationSettings = notifSettings,
                isEarlyReminderEnabled = isEarlyReminderEnabled
            )

            // 4. Safety net: Check if an alarm was killed by battery saver within the last 15 minutes
            val toleranceWindowMillis = 15 * 60 * 1000L
            val lastNotifiedKey = prefs.getLastNotifiedPrayerKey()

            for (item in todayAdjusted) {
                val timeDiff = now - item.timestampMillis
                // If the prayer time passed recently (0 to 15 mins ago)
                if (timeDiff in 0..toleranceWindowMillis) {
                    val prayerKey = "${todayYear}_${todayMonth}_${todayDay}_${item.type.name}"
                    if (lastNotifiedKey != prayerKey) {
                        val notifMode = notifSettings[item.type] ?: NotificationMode.EZAN
                        if (notifMode != NotificationMode.SILENT) {
                            val prayerVoices = prefs.getAllPrayerEzanVoices()
                            val voiceToPlay = when (notifMode) {
                                NotificationMode.BEEP -> "Nazik Bip & Çan Bildirim Tonu"
                                NotificationMode.EZAN -> prayerVoices[item.type] ?: prefs.getEzanVoice()
                                else -> prefs.getEzanVoice()
                            }

                            PrayerNotificationHelper.showPrayerEzanNotification(
                                context = context,
                                prayerType = item.type,
                                cityName = city.name,
                                timeFormatted = item.timeFormatted,
                                voiceName = voiceToPlay
                            )

                            // Play adhan audio
                            AdhanAudioPlayer.playAdhan(
                                context = context,
                                voiceName = voiceToPlay
                            )

                            prefs.setLastNotifiedPrayerKey(prayerKey)
                        }
                    }
                }
            }

            // 5. Update Home Screen Widget
            PrayerWidgetProvider.updateAllWidgets(context)

            // 6. Record last sync time
            prefs.setLastBackgroundSyncTime(now)

            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "PrayerSyncWorker execution failed: ${e.message}", e)
            Result.retry()
        }
    }

    companion object {
        const val TAG = "PrayerSyncWorker"
        const val WORK_NAME_PERIODIC = "prayer_time_periodic_sync"
        const val WORK_NAME_ONE_TIME = "prayer_time_one_time_sync"
    }
}

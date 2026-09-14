package com.example.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.model.NotificationMode
import com.example.model.PrayerTimeItem
import com.example.model.PrayerType
import com.example.worker.PrayerWorkManagerHelper
import java.util.Calendar

object PrayerAlarmScheduler {

    private const val TAG = "PrayerAlarmScheduler"
    const val ACTION_PRAYER_ALARM = "com.example.ACTION_PRAYER_ALARM"
    const val ACTION_PRAYER_EARLY_REMINDER = "com.example.ACTION_PRAYER_EARLY_REMINDER"

    const val EXTRA_PRAYER_TYPE = "extra_prayer_type"
    const val EXTRA_CITY_NAME = "extra_city_name"
    const val EXTRA_TIME_FORMATTED = "extra_time_formatted"

    /**
     * Schedules upcoming prayer alarms using AlarmManager with battery-saver resilient fallbacks
     * and WorkManager safety nets.
     */
    fun scheduleUpcomingPrayerAlarms(
        context: Context,
        prayerTimes: List<PrayerTimeItem>,
        cityName: String,
        notificationSettings: Map<PrayerType, NotificationMode>,
        isEarlyReminderEnabled: Boolean
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val now = System.currentTimeMillis()
        val canScheduleExact = AlarmPermissionHelper.canScheduleExactAlarms(context)

        for (item in prayerTimes) {
            val prayerType = item.type
            val notifMode = notificationSettings[prayerType] ?: NotificationMode.EZAN

            // Generate day-specific request code so multiple days don't override each other
            val cal = Calendar.getInstance().apply { timeInMillis = item.timestampMillis }
            val dayOfYear = cal.get(Calendar.DAY_OF_YEAR)
            val baseRequestCode = (dayOfYear * 10) + prayerType.ordinal

            // 1. Schedule Vakit Girdi Alarms (Ezan / Bildirim)
            if (notifMode != NotificationMode.SILENT && item.timestampMillis > now) {
                val intent = Intent(context, PrayerAlarmReceiver::class.java).apply {
                    action = ACTION_PRAYER_ALARM
                    putExtra(EXTRA_PRAYER_TYPE, prayerType.name)
                    putExtra(EXTRA_CITY_NAME, cityName)
                    putExtra(EXTRA_TIME_FORMATTED, item.timeFormatted)
                }

                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    baseRequestCode,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                try {
                    if (canScheduleExact) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            alarmManager.setExactAndAllowWhileIdle(
                                AlarmManager.RTC_WAKEUP,
                                item.timestampMillis,
                                pendingIntent
                            )
                        } else {
                            alarmManager.setExact(
                                AlarmManager.RTC_WAKEUP,
                                item.timestampMillis,
                                pendingIntent
                            )
                        }
                    } else {
                        // Exact alarm permission not available on this device; use inexact allow-while-idle
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            alarmManager.setAndAllowWhileIdle(
                                AlarmManager.RTC_WAKEUP,
                                item.timestampMillis,
                                pendingIntent
                            )
                        } else {
                            alarmManager.set(AlarmManager.RTC_WAKEUP, item.timestampMillis, pendingIntent)
                        }
                    }
                } catch (e: SecurityException) {
                    Log.w(TAG, "Exact alarm permission revoked or restricted: ${e.message}")
                    try {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, item.timestampMillis, pendingIntent)
                        } else {
                            alarmManager.set(AlarmManager.RTC_WAKEUP, item.timestampMillis, pendingIntent)
                        }
                    } catch (_: Exception) {}
                    // Ensure periodic WorkManager backup is running to catch prayer intervals
                    PrayerWorkManagerHelper.enqueuePeriodicPrayerSync(context)
                } catch (e: Exception) {
                    Log.e(TAG, "Error scheduling alarm: ${e.message}")
                }
            }

            // 2. Schedule Early Reminder (15 minutes prior)
            if (isEarlyReminderEnabled && notifMode != NotificationMode.SILENT) {
                val reminderTime = item.timestampMillis - (15 * 60 * 1000L)
                if (reminderTime > now) {
                    val reminderIntent = Intent(context, PrayerAlarmReceiver::class.java).apply {
                        action = ACTION_PRAYER_EARLY_REMINDER
                        putExtra(EXTRA_PRAYER_TYPE, prayerType.name)
                        putExtra(EXTRA_CITY_NAME, cityName)
                        putExtra(EXTRA_TIME_FORMATTED, item.timeFormatted)
                    }

                    val reminderPendingIntent = PendingIntent.getBroadcast(
                        context,
                        10000 + baseRequestCode,
                        reminderIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )

                    try {
                        if (canScheduleExact) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                alarmManager.setExactAndAllowWhileIdle(
                                    AlarmManager.RTC_WAKEUP,
                                    reminderTime,
                                    reminderPendingIntent
                                )
                            } else {
                                alarmManager.setExact(AlarmManager.RTC_WAKEUP, reminderTime, reminderPendingIntent)
                            }
                        } else {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, reminderTime, reminderPendingIntent)
                            } else {
                                alarmManager.set(AlarmManager.RTC_WAKEUP, reminderTime, reminderPendingIntent)
                            }
                        }
                    } catch (_: Exception) {}
                }
            }
        }
    }
}

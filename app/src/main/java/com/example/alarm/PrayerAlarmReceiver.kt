package com.example.alarm

import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.audio.AdhanAudioPlayer
import com.example.calc.PrayerCalculationEngine
import com.example.data.AppPreferencesRepository
import com.example.model.NotificationMode
import com.example.model.PrayerTimeItem
import com.example.model.PrayerType
import com.example.worker.PrayerWorkManagerHelper
import java.util.Calendar

class PrayerAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent == null) return
        val action = intent.action ?: return

        val prefs = AppPreferencesRepository(context)

        when (action) {
            PrayerAlarmScheduler.ACTION_PRAYER_ALARM -> {
                val prayerTypeName = intent.getStringExtra(PrayerAlarmScheduler.EXTRA_PRAYER_TYPE) ?: return
                val cityName = intent.getStringExtra(PrayerAlarmScheduler.EXTRA_CITY_NAME) ?: prefs.getSelectedCity().name
                val timeFormatted = intent.getStringExtra(PrayerAlarmScheduler.EXTRA_TIME_FORMATTED) ?: ""

                val prayerType = try {
                    PrayerType.valueOf(prayerTypeName)
                } catch (_: Exception) {
                    PrayerType.OGLE
                }

                // Mark as notified so background WorkManager sync does not duplicate
                val cal = Calendar.getInstance()
                val prayerKey = "${cal.get(Calendar.YEAR)}_${cal.get(Calendar.MONTH) + 1}_${cal.get(Calendar.DAY_OF_MONTH)}_${prayerType.name}"
                prefs.setLastNotifiedPrayerKey(prayerKey)

                // Check notification mode for this prayer
                val notifSettings = prefs.getNotificationSettings()
                val mode = notifSettings[prayerType] ?: NotificationMode.EZAN

                if (mode == NotificationMode.SILENT) {
                    return
                }

                // Determine voice to play
                val prayerVoices = prefs.getAllPrayerEzanVoices()
                val voiceToPlay = when (mode) {
                    NotificationMode.BEEP -> "Nazik Bip & Çan Bildirim Tonu"
                    NotificationMode.EZAN -> {
                        prayerVoices[prayerType] ?: prefs.getEzanVoice()
                    }
                    else -> prefs.getEzanVoice()
                }

                // 1. Show persistent high-priority notification with stop action
                PrayerNotificationHelper.showPrayerEzanNotification(
                    context = context,
                    prayerType = prayerType,
                    cityName = cityName,
                    timeFormatted = timeFormatted,
                    voiceName = voiceToPlay
                )

                // 2. Play Adhan audio
                AdhanAudioPlayer.playAdhan(
                    context = context,
                    voiceName = voiceToPlay
                )

                // 3. Kaza namazı borçlarını Öğle ve Yatsı vaktinde bildirme
                if (prayerType == PrayerType.OGLE || prayerType == PrayerType.YATSI) {
                    val kazaTracker = prefs.getKazaTracker()
                    if (kazaTracker.total > 0) {
                        PrayerNotificationHelper.showKazaReminderNotification(
                            context = context,
                            prayerType = prayerType,
                            kazaTracker = kazaTracker
                        )
                    }
                }
            }

            PrayerAlarmScheduler.ACTION_PRAYER_EARLY_REMINDER -> {
                val prayerTypeName = intent.getStringExtra(PrayerAlarmScheduler.EXTRA_PRAYER_TYPE) ?: return
                val cityName = intent.getStringExtra(PrayerAlarmScheduler.EXTRA_CITY_NAME) ?: prefs.getSelectedCity().name
                val timeFormatted = intent.getStringExtra(PrayerAlarmScheduler.EXTRA_TIME_FORMATTED) ?: ""

                val prayerType = try {
                    PrayerType.valueOf(prayerTypeName)
                } catch (_: Exception) {
                    PrayerType.OGLE
                }

                if (prefs.isEarlyReminderEnabled()) {
                    PrayerNotificationHelper.showEarlyReminderNotification(
                        context = context,
                        prayerType = prayerType,
                        cityName = cityName,
                        timeFormatted = timeFormatted
                    )
                }
            }

            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            "android.app.action.SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED" -> {
                rescheduleAllAlarmsAndSync(context, prefs)
            }
        }
    }

    private fun rescheduleAllAlarmsAndSync(context: Context, prefs: AppPreferencesRepository) {
        val city = prefs.getSelectedCity()

        // Calculate today and tomorrow
        val calToday = Calendar.getInstance()
        val rawToday = PrayerCalculationEngine.calculateTimesForDate(
            calToday.get(Calendar.YEAR),
            calToday.get(Calendar.MONTH) + 1,
            calToday.get(Calendar.DAY_OF_MONTH),
            city
        )

        val calTomorrow = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }
        val rawTomorrow = PrayerCalculationEngine.calculateTimesForDate(
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

        val allTimes = adjustList(rawToday) + adjustList(rawTomorrow)

        PrayerAlarmScheduler.scheduleUpcomingPrayerAlarms(
            context = context,
            prayerTimes = allTimes,
            cityName = city.name,
            notificationSettings = prefs.getNotificationSettings(),
            isEarlyReminderEnabled = prefs.isEarlyReminderEnabled()
        )

        // Ensure WorkManager periodic sync is registered
        PrayerWorkManagerHelper.enqueuePeriodicPrayerSync(context)
        PrayerWorkManagerHelper.enqueueImmediateSync(context)
    }
}

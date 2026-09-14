package com.example.alarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.model.PrayerType

object PrayerNotificationHelper {

    const val CHANNEL_EZAN = "prayer_ezan_channel_v2"
    const val CHANNEL_REMINDER = "prayer_reminder_channel_v2"
    const val CHANNEL_KAZA = "prayer_kaza_reminder_channel"

    private const val NOTIFICATION_ID_EZAN = 1001
    private const val NOTIFICATION_ID_REMINDER = 1002
    private const val NOTIFICATION_ID_KAZA = 3001

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // 1. Ezan & Vakit Girişi Kanalı (Yüksek Öncelik, Sesli & Titreşimli)
            val ezanChannel = NotificationChannel(
                CHANNEL_EZAN,
                "Ezanla Vakit & Ezan Sesleri",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Namaz vakti girdiğinde ezan sesi ve tam ekran bildirimi gönderir."
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 400, 200, 600, 200, 400)
                setShowBadge(true)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }

            // 2. Erken Hatırlatma Kanalı (15 dk önce)
            val reminderChannel = NotificationChannel(
                CHANNEL_REMINDER,
                "Vakit Öncesi Hatırlatıcı",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Namaz vaktinden 15 dakika önce abdest ve hazırlık bildirimi gönderir."
                enableVibration(true)
                setShowBadge(true)
            }

            // 3. Kaza Namazı Bildirim Kanalı (Öğle ve Yatsı Vakitlerinde İki Kez)
            val kazaChannel = NotificationChannel(
                CHANNEL_KAZA,
                "Kaza Namazı Hatırlatıcıları",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Öğle ve Yatsı vakitlerinde kaza namazı borçlarını hatırlatır."
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 300, 150, 300)
                setShowBadge(true)
            }

            notificationManager.createNotificationChannel(ezanChannel)
            notificationManager.createNotificationChannel(reminderChannel)
            notificationManager.createNotificationChannel(kazaChannel)
        }
    }

    fun showPrayerEzanNotification(
        context: Context,
        prayerType: PrayerType,
        cityName: String,
        timeFormatted: String,
        voiceName: String
    ) {
        createNotificationChannels(context)

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            context,
            prayerType.ordinal,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(context, PrayerStopAudioReceiver::class.java).apply {
            action = "com.example.ACTION_STOP_EZAN"
        }
        val stopPendingIntent = PendingIntent.getBroadcast(
            context,
            999,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = "🕋 ${prayerType.titleTr} Vakti Girdi ($timeFormatted)"
        val content = "$cityName için ${prayerType.titleTr} namazı vakti girdi. Ezan okunuyor: $voiceName"

        val notification = NotificationCompat.Builder(context, CHANNEL_EZAN)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(title)
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(openAppPendingIntent)
            .setDeleteIntent(stopPendingIntent) // Kullanıcı Talebi: Bildirim kapatıldığında ezan ve hatırlatmayı anında durdur
            .addAction(android.R.drawable.ic_media_pause, "Sesi Durdur", stopPendingIntent)
            .setVibrate(longArrayOf(0, 400, 200, 600))
            .build()

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_EZAN + prayerType.ordinal, notification)
        } catch (_: SecurityException) {}
    }

    fun showEarlyReminderNotification(
        context: Context,
        prayerType: PrayerType,
        cityName: String,
        timeFormatted: String
    ) {
        createNotificationChannels(context)

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            context,
            2000 + prayerType.ordinal,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = "⏰ ${prayerType.titleTr} Vaktine 15 Dakika Kaldı"
        val content = "$cityName için ${prayerType.titleTr} vakti saat $timeFormatted'da girecek. Hazırlık yapabilirsiniz."

        val notification = NotificationCompat.Builder(context, CHANNEL_REMINDER)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(openAppPendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_REMINDER + prayerType.ordinal, notification)
        } catch (_: SecurityException) {}
    }

    fun dismissEzanNotification(context: Context) {
        try {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            for (p in PrayerType.entries) {
                nm.cancel(NOTIFICATION_ID_EZAN + p.ordinal)
            }
        } catch (_: Exception) {}
    }

    fun showKazaReminderNotification(
        context: Context,
        prayerType: PrayerType,
        kazaTracker: com.example.model.KazaTracker
    ) {
        createNotificationChannels(context)

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID_KAZA + prayerType.ordinal,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = "📋 Kaza Namazı Borcu Hatırlatması (${prayerType.titleTr} Vakti)"
        val shortContent = "Toplam ${kazaTracker.total} vakit kaza namazı borcunuz bulunmaktadır."
        val bigContent = "Toplam ${kazaTracker.total} vakit kaza borcunuz var:\n" +
                "• Sabah: ${kazaTracker.fajr}  • Öğle: ${kazaTracker.dhuhr}  • İkindi: ${kazaTracker.asr}\n" +
                "• Akşam: ${kazaTracker.maghrib}  • Yatsı: ${kazaTracker.isha}  • Vitir: ${kazaTracker.witr}\n\n" +
                "Bu mübarek vakitte kaza namazınızı eda ederek borcunuzu hafifletebilirsiniz."

        val notification = NotificationCompat.Builder(context, CHANNEL_KAZA)
            .setSmallIcon(android.R.drawable.ic_menu_agenda)
            .setContentTitle(title)
            .setContentText(shortContent)
            .setStyle(NotificationCompat.BigTextStyle().bigText(bigContent))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(openAppPendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_KAZA + prayerType.ordinal, notification)
        } catch (_: SecurityException) {}
    }
}

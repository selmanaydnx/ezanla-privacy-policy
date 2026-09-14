package com.example

import android.app.Application
import com.example.worker.PrayerWorkManagerHelper

class PrayerApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize and ensure high-reliability periodic background sync for prayer notifications
        try {
            PrayerWorkManagerHelper.enqueuePeriodicPrayerSync(this)
            PrayerWorkManagerHelper.enqueueImmediateSync(this)
        } catch (_: Exception) {}
    }
}

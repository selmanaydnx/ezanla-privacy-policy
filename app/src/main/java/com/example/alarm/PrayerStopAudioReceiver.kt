package com.example.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.audio.AdhanAudioPlayer

class PrayerStopAudioReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        AdhanAudioPlayer.stop()
        PrayerNotificationHelper.dismissEzanNotification(context)
    }
}

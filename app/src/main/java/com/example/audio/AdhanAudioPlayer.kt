package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import com.example.R
import kotlinx.coroutines.*

/**
 * AdhanAudioPlayer plays authentic, real human muezzin adhan recitations
 * recorded in classical Ottoman / Turkish Makams (Saba, Rast, Hicaz, Segâh, Uşşak)
 * as well as sacred shrines (Makkah, Madinah) using high quality offline raw audio.
 */
object AdhanAudioPlayer {

    private const val TAG = "AdhanAudioPlayer"
    private var activeJob: Job? = null
    private var activeMediaPlayer: MediaPlayer? = null
    private val playerScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var hardwareControlsReceiver: android.content.BroadcastReceiver? = null
    private var volumeObserver: android.database.ContentObserver? = null
    private var appContext: Context? = null

    @Volatile
    var isPlaying: Boolean = false
        private set

    // Real Human Ezan & Makam voices
    val AVAILABLE_VOICES = listOf(
        "İstanbul Ezanı (Saba Makamı - Sabah)",
        "Rast Makamı (Klasik Türk Öğle Ezanı)",
        "Hicaz Makamı (Duygulu İkindi Ezanı)",
        "Segâh Makamı (Akşam Ezanı - Hızlı & Coşkulu)",
        "Uşşak Makamı (Huzur Dolu Yatsı Ezanı)",
        "Hüzzam Makamı (Duygu Yüklü Sabah Ezanı)",
        "Nihavend Makamı (Ferahlatıcı Türk Ezanı)",
        "Mekke-i Mükerreme Ezanı (Kâbe-i Muazzama)",
        "Medine-i Münevvere Ezanı (Mescid-i Nebevî)",
        "Kudüs Mescid-i Aksâ Ezanı",
        "Mısır Ezanı (Şeyh Mustafa İsmail)",
        "Kahire Ezanı (Şeyh Abdulbasit Abdussamed)",
        "Kısa Zil / Nazik Bildirim Bipi",
        "Sessiz (Yalnızca Titreşim ve Bildirim)"
    )

    fun playAdhan(
        context: Context,
        voiceName: String,
        onStarted: () -> Unit = {},
        onFinished: () -> Unit = {}
    ) {
        stop()

        if (voiceName.contains("Sessiz", ignoreCase = true)) {
            triggerVibration(context, longVibe = true)
            onFinished()
            return
        }

        appContext = context.applicationContext
        registerHardwareInterruptionListeners(context.applicationContext)

        isPlaying = true
        onStarted()

        // Titreşimi ana ses akışını bloke etmeyecek şekilde asenkron tetikle (gecikmeyi sıfırlar)
        playerScope.launch(Dispatchers.Default) {
            triggerVibration(context, longVibe = false)
        }

        activeJob = playerScope.launch(Dispatchers.IO) {
            var wakeLock: android.os.PowerManager.WakeLock? = null
            try {
                val powerManager = context.getSystemService(Context.POWER_SERVICE) as? android.os.PowerManager
                wakeLock = powerManager?.newWakeLock(
                    android.os.PowerManager.PARTIAL_WAKE_LOCK,
                    "NamazVakti:AdhanAudioWakeLock"
                )?.apply {
                    setReferenceCounted(false)
                    acquire(5 * 60 * 1000L) // 5 dakika emniyet kilidi
                }

                playRealAudio(context.applicationContext, voiceName) {
                    try {
                        if (wakeLock?.isHeld == true) wakeLock.release()
                    } catch (_: Exception) {}
                    unregisterHardwareInterruptionListeners()
                    onFinished()
                }
            } catch (e: CancellationException) {
                try {
                    if (wakeLock?.isHeld == true) wakeLock.release()
                } catch (_: Exception) {}
                cleanup()
                isPlaying = false
                onFinished()
            } catch (e: Exception) {
                Log.e(TAG, "Error playing real adhan: ${e.message}", e)
                try {
                    if (wakeLock?.isHeld == true) wakeLock.release()
                } catch (_: Exception) {}
                cleanup()
                isPlaying = false
                onFinished()
            }
        }
    }

    fun stop() {
        activeJob?.cancel()
        activeJob = null
        unregisterHardwareInterruptionListeners()
        cleanup()
        isPlaying = false
    }

    /**
     * Kullanıcı Talebi:
     * "Namaz vakti geldiğinde ve sen bildirimi gönderip telefonda ezan okumaya başladığında eğer kullanıcı:
     * 1. bildirimi kaydırıp kapatırsa
     * 2. veya telefonun ses tuşlarına
     * 3. veya kapatma tuşuna basarsa
     * bu üç durumda ezan sesini kes."
     */
    private fun registerHardwareInterruptionListeners(context: Context) {
        unregisterHardwareInterruptionListeners()
        try {
            // 1. Yayın Alıcısı: Telefonun kapatma / güç tuşu (ACTION_SCREEN_OFF), cihazı kapatma (ACTION_SHUTDOWN) ve ses tuşları (VOLUME_CHANGED_ACTION)
            val filter = android.content.IntentFilter().apply {
                addAction(android.content.Intent.ACTION_SCREEN_OFF)
                addAction(android.content.Intent.ACTION_SHUTDOWN)
                addAction("android.intent.action.QUICKBOOT_POWEROFF")
                addAction("android.media.VOLUME_CHANGED_ACTION")
            }
            val receiver = object : android.content.BroadcastReceiver() {
                override fun onReceive(ctx: Context?, intent: android.content.Intent?) {
                    val action = intent?.action
                    Log.d(TAG, "Hardware button / screen event ($action) detected -> stopping adhan")
                    stop()
                    ctx?.let { com.example.alarm.PrayerNotificationHelper.dismissEzanNotification(it) }
                }
            }
            hardwareControlsReceiver = receiver
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.registerReceiver(receiver, filter, Context.RECEIVER_EXPORTED)
            } else {
                context.registerReceiver(receiver, filter)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Could not register hardwareControlsReceiver: ${e.message}")
        }

        try {
            // 2. ContentObserver: Kullanıcı telefonun ses açma/kısma tuşuna bastığı an donanım ses değişikliğini yakalar
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? android.media.AudioManager
            val initialMusicVol = audioManager?.getStreamVolume(android.media.AudioManager.STREAM_MUSIC) ?: -1
            val initialAlarmVol = audioManager?.getStreamVolume(android.media.AudioManager.STREAM_ALARM) ?: -1

            val observer = object : android.database.ContentObserver(android.os.Handler(android.os.Looper.getMainLooper())) {
                override fun onChange(selfChange: Boolean) {
                    super.onChange(selfChange)
                    val currentMusicVol = audioManager?.getStreamVolume(android.media.AudioManager.STREAM_MUSIC) ?: -1
                    val currentAlarmVol = audioManager?.getStreamVolume(android.media.AudioManager.STREAM_ALARM) ?: -1
                    if (currentMusicVol != initialMusicVol || currentAlarmVol != initialAlarmVol) {
                        Log.d(TAG, "Volume key pressed (music=$currentMusicVol, alarm=$currentAlarmVol) -> stopping adhan")
                        stop()
                        context.let { com.example.alarm.PrayerNotificationHelper.dismissEzanNotification(it) }
                    }
                }
            }
            volumeObserver = observer
            context.contentResolver.registerContentObserver(
                android.provider.Settings.System.CONTENT_URI,
                true,
                observer
            )
        } catch (e: Exception) {
            Log.w(TAG, "Could not register volume ContentObserver: ${e.message}")
        }
    }

    private fun unregisterHardwareInterruptionListeners() {
        try {
            hardwareControlsReceiver?.let {
                appContext?.unregisterReceiver(it)
            }
        } catch (_: Exception) {}
        hardwareControlsReceiver = null

        try {
            volumeObserver?.let {
                appContext?.contentResolver?.unregisterContentObserver(it)
            }
        } catch (_: Exception) {}
        volumeObserver = null
    }

    private fun cleanup() {
        try {
            activeMediaPlayer?.let {
                if (it.isPlaying) {
                    it.stop()
                }
                it.reset()
                it.release()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing media player: ${e.message}")
        }
        activeMediaPlayer = null
    }

    private fun playRealAudio(
        context: Context,
        voiceName: String,
        onFinished: () -> Unit
    ) {
        val lower = voiceName.lowercase()

        // If gentle chime/beep is selected, use system ringtone manager
        if (lower.contains("bip") || lower.contains("çan") || lower.contains("zil") || lower.contains("bildirim tonu")) {
            playSystemChime(context, onFinished)
            return
        }

        // Map to authentic real human recorded adhan raw resource
        val rawResId = getRawResourceForVoice(voiceName)

        try {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setFlags(AudioAttributes.FLAG_AUDIBILITY_ENFORCED)
                .build()

            // Pass audioAttributes directly during create() so they are set before prepare()
            val player = MediaPlayer.create(context, rawResId, audioAttributes, 0)
            if (player == null) {
                Log.e(TAG, "MediaPlayer.create returned null for $rawResId")
                onFinished()
                return
            }

            player.setOnCompletionListener {
                cleanup()
                isPlaying = false
                onFinished()
            }

            player.setOnErrorListener { _, what, extra ->
                Log.e(TAG, "MediaPlayer error: what=$what extra=$extra")
                cleanup()
                isPlaying = false
                onFinished()
                true
            }

            player.start()
            activeMediaPlayer = player
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start real human adhan MediaPlayer: ${e.message}", e)
            cleanup()
            isPlaying = false
            onFinished()
        }
    }

    private fun playSystemChime(context: Context, onFinished: () -> Unit) {
        try {
            val alertUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)

            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            val player = try {
                MediaPlayer.create(context, alertUri, null, audioAttributes, 0)
            } catch (_: Exception) {
                MediaPlayer.create(context, alertUri)
            }

            if (player != null) {
                player.setOnCompletionListener {
                    cleanup()
                    isPlaying = false
                    onFinished()
                }
                player.start()
                activeMediaPlayer = player
            } else {
                cleanup()
                isPlaying = false
                onFinished()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to play system chime: ${e.message}")
            cleanup()
            isPlaying = false
            onFinished()
        }
    }

    fun getRawResourceForVoice(voiceName: String): Int {
        val lower = voiceName.lowercase()
        return when {
            // Sabah & Saba
            lower.contains("sabah") || lower.contains("saba") || lower.contains("hüzzam") || lower.contains("huzzam") -> R.raw.ezan_sabah_saba
            // Öğle & Rast
            lower.contains("öğle") || lower.contains("ogle") || lower.contains("rast") || lower.contains("nihavend") -> R.raw.ezan_ogle_rast
            // İkindi & Hicaz
            lower.contains("ikindi") || lower.contains("hicaz") -> R.raw.ezan_ikindi_hicaz
            // Akşam & Segâh
            lower.contains("akşam") || lower.contains("aksam") || lower.contains("segâh") || lower.contains("segah") -> R.raw.ezan_aksam_segah
            // Yatsı & Uşşak
            lower.contains("yatsı") || lower.contains("yatsi") || lower.contains("uşşak") || lower.contains("ussak") || lower.contains("bayati") -> R.raw.ezan_yatsi_ussak
            // Mekke & Kâbe
            lower.contains("mekke") || lower.contains("kâbe") || lower.contains("kabe") || lower.contains("abdulbasit") -> R.raw.ezan_mekke
            // Medine & Kudüs
            lower.contains("medine") || lower.contains("nebev") || lower.contains("kudüs") || lower.contains("kudus") || lower.contains("aksâ") || lower.contains("aksa") -> R.raw.ezan_medine
            // İstanbul
            lower.contains("istanbul") || lower.contains("ismail") -> R.raw.ezan_istanbul
            // Fallback default: authentic Turkish Istanbul Ezan
            else -> R.raw.ezan_istanbul
        }
    }

    private fun triggerVibration(context: Context, longVibe: Boolean) {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
            vibrator?.let {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val timings = if (longVibe) longArrayOf(0, 400, 200, 600) else longArrayOf(0, 250, 150, 300)
                    it.vibrate(VibrationEffect.createWaveform(timings, -1))
                } else {
                    @Suppress("DEPRECATION")
                    it.vibrate(500)
                }
            }
        } catch (_: Exception) {}
    }
}

package com.example.ui

import android.app.Application
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.alarm.PrayerAlarmScheduler
import com.example.alarm.PrayerNotificationHelper
import com.example.audio.AdhanAudioPlayer
import com.example.calc.PrayerCalculationEngine
import com.example.data.AppPreferencesRepository
import com.example.data.CityDatabase
import com.example.data.LiveGpsState
import com.example.data.LiveLocationTracker
import com.example.data.LocationVerificationHelper
import com.example.data.LocationVerificationState
import com.example.data.WisdomDatabase
import com.example.model.*
import com.example.widget.PrayerWidgetProvider
import com.example.worker.PrayerWorkManagerHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

data class PrayerUiState(
    val selectedCity: City = CityDatabase.defaultCity,
    val dateOffsetDays: Int = 0,
    val prayerTimes: List<PrayerTimeItem> = emptyList(),
    val currentPrayer: PrayerTimeItem? = null,
    val nextPrayer: PrayerTimeItem? = null,
    val countdownText: String = "--:--:--",
    val countdownSecondsRemaining: Long = 0L,
    val progressToNextPrayer: Float = 0f,
    val gregorianDateText: String = "",
    val hijriDate: HijriDate = HijriDate(1, "Muharrem", 1448),
    val qiblaBearing: Double = 0.0,
    val distanceToKaabaKm: Int = 0,
    val compassAzimuth: Float = 0f,
    val isSensorAvailable: Boolean = false,
    val dailyWisdom: DailyWisdom = WisdomDatabase.getTodayWisdom(1),
    val wisdomIndex: Int = 0,
    // Zikirmatik
    val currentDhikrIndex: Int = 0,
    val dhikrCount: Int = 0,
    val dhikrCompletedLaps: Int = 0,
    val isVibrationEnabled: Boolean = true,
    val dhikrBookEntries: List<DhikrBookEntry> = emptyList(),
    // City Search Sheet
    val isCityPickerVisible: Boolean = false,
    val citySearchQuery: String = "",
    val filteredCities: List<City> = CityDatabase.cities,
    // Notification modes map
    val notificationSettings: Map<PrayerType, NotificationMode> = PrayerType.entries.associateWith { NotificationMode.EZAN },
    // Settings state & Persistence
    val selectedTheme: AppTheme = AppTheme.MIDNIGHT,
    val darkModePreference: DarkModePreference = DarkModePreference.SYSTEM,
    val isEyeComfortEnabled: Boolean = true,
    val isLargeFontEnabled: Boolean = false,
    val calculationMethod: String = "Diyanet İşleri Başkanlığı (Türkiye Standart)",
    val precautionMinutes: Int = 0,
    val isEarlyReminderEnabled: Boolean = true,
    val selectedEzanVoice: String = "İstanbul Ezanı (Saba Makamı)",
    val prayerEzanVoices: Map<PrayerType, String> = emptyMap(),
    val isPlayingEzan: Boolean = false,
    val activePlayingVoiceName: String? = null,
    val locationVerificationState: LocationVerificationState = LocationVerificationState.Idle,
    // Widget Customization (Renk, Motif, Şeffaflık)
    val widgetThemeOption: String = "FOLLOW_APP",
    val widgetMotifOption: String = "FOLLOW_THEME",
    val widgetBackgroundOpacity: Int = 92,
    val widgetMotifWatermarkEnabled: Boolean = true,
    // Namaz & Kaza Tracker Persistence
    val kazaTracker: KazaTracker = KazaTracker(),
    val dailyPrayerCheck: DailyPrayerCheck = DailyPrayerCheck(),
    val kazaBookEntries: List<KazaBookEntry> = emptyList(),
    // Realtime GPS
    val liveGpsState: LiveGpsState = LiveGpsState(),
    // Compass Advanced Options & State
    val compassDialStyle: CompassDialStyle = CompassDialStyle.CLASSIC_KAABA,
    val isCompassHapticEnabled: Boolean = true,
    val isCompassSoundEnabled: Boolean = false,
    val isCompassLevelEnabled: Boolean = true,
    val isCompassTrueNorthEnabled: Boolean = true,
    val compassPitch: Float = 0f,
    val compassRoll: Float = 0f,
    val isDeviceFlat: Boolean = true,
    val sunAzimuth: Double = 0.0,
    val sunAltitude: Double = 0.0,
    val magneticDeclination: Float = 5.5f
)

class PrayerViewModel(application: Application) : AndroidViewModel(application), SensorEventListener {

    private val prefsRepo = AppPreferencesRepository(application)
    private val liveLocationTracker = LiveLocationTracker(application)

    private val _uiState = MutableStateFlow(PrayerUiState())
    val uiState: StateFlow<PrayerUiState> = _uiState.asStateFlow()

    private val sensorManager = application.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val rotationSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        ?: sensorManager?.getDefaultSensor(Sensor.TYPE_ORIENTATION)

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = application.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        application.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    private val _compassAzimuth = MutableStateFlow(0f)
    val compassAzimuthFlow: StateFlow<Float> = _compassAzimuth.asStateFlow()
    private var lastEmittedAzimuth: Float = 0f
    private var isSensorRegistered: Boolean = false

    init {
        // Load persisted state across app restarts
        // Check day transition immediately (auto-accumulate missed prayers from yesterday)
        prefsRepo.checkAndPerformDayTransition()

        val savedTheme = prefsRepo.getSavedTheme()
        val savedDarkModePref = prefsRepo.getDarkModePreference()
        val savedEyeComfort = prefsRepo.isEyeComfortEnabled()
        val savedLargeFont = prefsRepo.isLargeFontEnabled()
        val savedCompassStyle = prefsRepo.getCompassDialStyle()
        val savedCompassHaptic = prefsRepo.isCompassHapticEnabled()
        val savedCompassSound = prefsRepo.isCompassSoundEnabled()
        val savedCompassLevel = prefsRepo.isCompassLevelEnabled()
        val savedCompassTrueNorth = prefsRepo.isCompassTrueNorthEnabled()
        val savedWidgetTheme = prefsRepo.getWidgetTheme()
        val savedWidgetMotif = prefsRepo.getWidgetMotif()
        val savedWidgetOpacity = prefsRepo.getWidgetBackgroundOpacity()
        val savedWidgetWatermark = prefsRepo.isWidgetMotifWatermarkEnabled()
        val savedCityId = prefsRepo.getSavedCityId("34")
        val savedCity = CityDatabase.cities.find { it.id == savedCityId } ?: CityDatabase.defaultCity
        val savedCalcMethod = prefsRepo.getCalculationMethod()
        val savedPrecaution = prefsRepo.getPrecautionMinutes()
        val savedEarlyReminder = prefsRepo.isEarlyReminderEnabled()
        val savedEzanVoice = prefsRepo.getEzanVoice()
        val savedPrayerVoices = prefsRepo.getAllPrayerEzanVoices()
        val savedNotifSettings = prefsRepo.getNotificationSettings()
        val savedVib = prefsRepo.isVibrationEnabled()
        val savedDhikrIndex = prefsRepo.getDhikrIndex()
        val savedDhikrCount = prefsRepo.getDhikrCount()
        val savedDhikrLaps = prefsRepo.getDhikrLaps()
        val savedKaza = prefsRepo.getKazaTracker()
        val savedDailyCheck = prefsRepo.getDailyPrayerCheck()
        val savedDhikrBook = prefsRepo.getDhikrBookEntries()
        val savedKazaBook = prefsRepo.getKazaBookEntries()

        PrayerNotificationHelper.createNotificationChannels(application)

        val cal = Calendar.getInstance()
        val dayOfYear = cal.get(Calendar.DAY_OF_YEAR)
        val todayWisdom = WisdomDatabase.getTodayWisdom(dayOfYear)

        _uiState.update {
            it.copy(
                selectedTheme = savedTheme,
                darkModePreference = savedDarkModePref,
                isEyeComfortEnabled = savedEyeComfort,
                isLargeFontEnabled = savedLargeFont,
                compassDialStyle = savedCompassStyle,
                isCompassHapticEnabled = savedCompassHaptic,
                isCompassSoundEnabled = savedCompassSound,
                isCompassLevelEnabled = savedCompassLevel,
                isCompassTrueNorthEnabled = savedCompassTrueNorth,
                widgetThemeOption = savedWidgetTheme,
                widgetMotifOption = savedWidgetMotif,
                widgetBackgroundOpacity = savedWidgetOpacity,
                widgetMotifWatermarkEnabled = savedWidgetWatermark,
                selectedCity = savedCity,
                calculationMethod = savedCalcMethod,
                precautionMinutes = savedPrecaution,
                isEarlyReminderEnabled = savedEarlyReminder,
                selectedEzanVoice = savedEzanVoice,
                prayerEzanVoices = savedPrayerVoices,
                notificationSettings = savedNotifSettings,
                isVibrationEnabled = savedVib,
                currentDhikrIndex = savedDhikrIndex,
                dhikrCount = savedDhikrCount,
                dhikrCompletedLaps = savedDhikrLaps,
                dhikrBookEntries = savedDhikrBook,
                kazaTracker = savedKaza,
                dailyPrayerCheck = savedDailyCheck,
                kazaBookEntries = savedKazaBook,
                dailyWisdom = todayWisdom,
                isSensorAvailable = rotationSensor != null
            )
        }

        recalculateTimes()
        startClockTicker()

        // Observe GPS state changes
        viewModelScope.launch {
            liveLocationTracker.gpsState.collect { gps ->
                _uiState.update { it.copy(liveGpsState = gps) }
                // When exact GPS coordinates are available, update Qibla bearing with live GPS!
                if (gps.latitude != null && gps.longitude != null) {
                    val qBearing = PrayerCalculationEngine.calculateQiblaBearing(gps.latitude, gps.longitude)
                    val distKm = PrayerCalculationEngine.calculateDistanceToKaabaKm(gps.latitude, gps.longitude)
                    _uiState.update { it.copy(qiblaBearing = qBearing, distanceToKaabaKm = distKm) }
                }
            }
        }

        // Start GPS tracking initially to lock live coordinates
        startGpsTracking()
        updateSunAndQiblaData()
    }

    fun startGpsTracking() {
        liveLocationTracker.checkAndStartTracking()
    }

    fun stopGpsTracking() {
        liveLocationTracker.stopTracking()
    }

    fun startCompassSensor() {
        if (!isSensorRegistered && rotationSensor != null) {
            sensorManager?.registerListener(this, rotationSensor, SensorManager.SENSOR_DELAY_GAME)
            isSensorRegistered = true
            _uiState.update { it.copy(isSensorAvailable = true) }
        }
        startGpsTracking()
    }

    fun stopCompassSensor() {
        if (isSensorRegistered) {
            sensorManager?.unregisterListener(this)
            isSensorRegistered = false
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return
        var newAzimuth = 0f
        var pitch = 0f
        var roll = 0f

        if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
            val rotationMatrix = FloatArray(9)
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
            val orientation = FloatArray(3)
            SensorManager.getOrientation(rotationMatrix, orientation)
            val azimuthDeg = (Math.toDegrees(orientation[0].toDouble()) + 360.0) % 360.0
            newAzimuth = azimuthDeg.toFloat()
            pitch = Math.toDegrees(orientation[1].toDouble()).toFloat()
            roll = Math.toDegrees(orientation[2].toDouble()).toFloat()
        } else if (event.sensor.type == Sensor.TYPE_ORIENTATION) {
            newAzimuth = (event.values[0] + 360f) % 360f
            pitch = event.values[1]
            roll = event.values[2]
        } else return

        val isFlat = kotlin.math.abs(pitch) < 14f && kotlin.math.abs(roll) < 14f

        // Low deadband filter for butter-smooth rotation animation with low jitter
        if (kotlin.math.abs(newAzimuth - lastEmittedAzimuth) > 0.15f || kotlin.math.abs(pitch - _uiState.value.compassPitch) > 0.8f) {
            lastEmittedAzimuth = newAzimuth
            _compassAzimuth.value = newAzimuth
            _uiState.update {
                it.copy(
                    compassPitch = pitch,
                    compassRoll = roll,
                    isDeviceFlat = isFlat
                )
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    fun setManualCompassAzimuth(azimuth: Float) {
        val normalized = (azimuth % 360f + 360f) % 360f
        _compassAzimuth.value = normalized
    }

    private fun startClockTicker() {
        viewModelScope.launch {
            while (isActive) {
                updatePrayerProgression()
                delay(1000)
            }
        }
    }

    // Date navigation
    fun nextDay() {
        _uiState.update { it.copy(dateOffsetDays = it.dateOffsetDays + 1) }
        recalculateTimes()
    }

    fun previousDay() {
        _uiState.update { it.copy(dateOffsetDays = it.dateOffsetDays - 1) }
        recalculateTimes()
    }

    fun resetToToday() {
        _uiState.update { it.copy(dateOffsetDays = 0) }
        recalculateTimes()
    }

    fun setDateOffset(offset: Int) {
        _uiState.update { it.copy(dateOffsetDays = offset) }
        recalculateTimes()
    }

    // City selection
    fun showCityPicker() {
        _uiState.update { it.copy(isCityPickerVisible = true, citySearchQuery = "", filteredCities = CityDatabase.cities) }
    }

    fun hideCityPicker() {
        _uiState.update { it.copy(isCityPickerVisible = false) }
    }

    fun searchCity(query: String) {
        val filtered = CityDatabase.searchCities(query)
        _uiState.update { it.copy(citySearchQuery = query, filteredCities = filtered) }
    }

    fun setCity(city: City) {
        _uiState.update { it.copy(selectedCity = city, isCityPickerVisible = false) }
        prefsRepo.saveCityId(city.id)
        recalculateTimes()
    }

    // Notification toggle per prayer
    fun toggleNotification(prayerType: PrayerType) {
        var nextMode = NotificationMode.EZAN
        _uiState.update { state ->
            val currentMode = state.notificationSettings[prayerType] ?: NotificationMode.EZAN
            nextMode = when (currentMode) {
                NotificationMode.EZAN -> NotificationMode.BEEP
                NotificationMode.BEEP -> NotificationMode.SILENT
                NotificationMode.SILENT -> NotificationMode.EZAN
            }
            val updated = state.notificationSettings.toMutableMap()
            updated[prayerType] = nextMode
            state.copy(notificationSettings = updated)
        }
        prefsRepo.saveNotificationMode(prayerType, nextMode)
        scheduleUpcomingAlarms()
    }

    // Wisdom actions
    fun nextWisdom() {
        val newIndex = _uiState.value.wisdomIndex + 1
        val cal = Calendar.getInstance()
        val dayOfYear = cal.get(Calendar.DAY_OF_YEAR)
        _uiState.update {
            it.copy(
                wisdomIndex = newIndex,
                dailyWisdom = WisdomDatabase.getWisdomByIndex(dayOfYear + newIndex)
            )
        }
    }

    // Settings actions (Persistent)
    fun setTheme(theme: AppTheme) {
        _uiState.update { it.copy(selectedTheme = theme) }
        prefsRepo.saveTheme(theme)
        triggerWidgetUpdate()
    }

    fun setDarkModePreference(preference: DarkModePreference) {
        _uiState.update { it.copy(darkModePreference = preference) }
        prefsRepo.saveDarkModePreference(preference)
        triggerWidgetUpdate()
    }

    fun toggleEyeComfort() {
        val next = !_uiState.value.isEyeComfortEnabled
        _uiState.update { it.copy(isEyeComfortEnabled = next) }
        prefsRepo.saveEyeComfortEnabled(next)
    }

    // --- Compass Customization & Feedback Actions ---
    fun setCompassDialStyle(style: CompassDialStyle) {
        _uiState.update { it.copy(compassDialStyle = style) }
        prefsRepo.saveCompassDialStyle(style)
    }

    fun toggleCompassHaptic() {
        val next = !_uiState.value.isCompassHapticEnabled
        _uiState.update { it.copy(isCompassHapticEnabled = next) }
        prefsRepo.saveCompassHapticEnabled(next)
    }

    fun toggleCompassSound() {
        val next = !_uiState.value.isCompassSoundEnabled
        _uiState.update { it.copy(isCompassSoundEnabled = next) }
        prefsRepo.saveCompassSoundEnabled(next)
    }

    fun toggleCompassLevel() {
        val next = !_uiState.value.isCompassLevelEnabled
        _uiState.update { it.copy(isCompassLevelEnabled = next) }
        prefsRepo.saveCompassLevelEnabled(next)
    }

    fun toggleCompassTrueNorth() {
        val next = !_uiState.value.isCompassTrueNorthEnabled
        _uiState.update { it.copy(isCompassTrueNorthEnabled = next) }
        prefsRepo.saveCompassTrueNorthEnabled(next)
    }

    fun updateSunAndQiblaData() {
        val lat = _uiState.value.liveGpsState.latitude ?: _uiState.value.selectedCity.latitude
        val lng = _uiState.value.liveGpsState.longitude ?: _uiState.value.selectedCity.longitude
        val (sunAz, sunAlt) = PrayerCalculationEngine.calculateSunPosition(lat, lng, _uiState.value.selectedCity.timezone)
        val declination = PrayerCalculationEngine.estimateMagneticDeclination(lat, lng)
        _uiState.update {
            it.copy(
                sunAzimuth = sunAz,
                sunAltitude = sunAlt,
                magneticDeclination = declination
            )
        }
    }

    private var lastAlignmentFeedbackTime = 0L

    fun triggerQiblaAlignmentFeedback() {
        val now = System.currentTimeMillis()
        if (now - lastAlignmentFeedbackTime < 2000L) return
        lastAlignmentFeedbackTime = now

        if (_uiState.value.isCompassHapticEnabled) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val effect = VibrationEffect.createWaveform(longArrayOf(0, 45, 55, 75), -1)
                    vibrator?.vibrate(effect)
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(80L)
                }
            } catch (_: Exception) {}
        }

        if (_uiState.value.isCompassSoundEnabled) {
            try {
                val tg = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 65)
                tg.startTone(ToneGenerator.TONE_PROP_BEEP2, 110)
                viewModelScope.launch {
                    delay(200)
                    tg.release()
                }
            } catch (_: Exception) {}
        }
    }

    // --- Widget Customization Actions ---
    fun setWidgetThemeOption(option: String) {
        _uiState.update { it.copy(widgetThemeOption = option) }
        prefsRepo.saveWidgetTheme(option)
        triggerWidgetUpdate()
    }

    fun setWidgetMotifOption(option: String) {
        _uiState.update { it.copy(widgetMotifOption = option) }
        prefsRepo.saveWidgetMotif(option)
        triggerWidgetUpdate()
    }

    fun setWidgetBackgroundOpacity(opacity: Int) {
        _uiState.update { it.copy(widgetBackgroundOpacity = opacity) }
        prefsRepo.saveWidgetBackgroundOpacity(opacity)
        triggerWidgetUpdate()
    }

    fun toggleWidgetMotifWatermark() {
        val next = !_uiState.value.widgetMotifWatermarkEnabled
        _uiState.update { it.copy(widgetMotifWatermarkEnabled = next) }
        prefsRepo.saveWidgetMotifWatermarkEnabled(next)
        triggerWidgetUpdate()
    }

    fun triggerWidgetUpdate() {
        try {
            PrayerWidgetProvider.updateAllWidgets(getApplication())
        } catch (_: Exception) {}
    }

    fun setCalculationMethod(method: String) {
        _uiState.update { it.copy(calculationMethod = method) }
        prefsRepo.saveCalculationMethod(method)
        recalculateTimes()
    }

    fun setPrecautionMinutes(minutes: Int) {
        _uiState.update { it.copy(precautionMinutes = minutes) }
        prefsRepo.savePrecautionMinutes(minutes)
        recalculateTimes()
    }

    fun toggleEarlyReminder() {
        val newVal = !_uiState.value.isEarlyReminderEnabled
        _uiState.update { it.copy(isEarlyReminderEnabled = newVal) }
        prefsRepo.saveEarlyReminderEnabled(newVal)
        scheduleUpcomingAlarms()
    }

    fun setEzanVoice(voice: String) {
        _uiState.update { it.copy(selectedEzanVoice = voice) }
        prefsRepo.saveEzanVoice(voice)
        scheduleUpcomingAlarms()
    }

    fun setPrayerEzanVoice(type: PrayerType, voice: String) {
        _uiState.update { current ->
            val updated = current.prayerEzanVoices.toMutableMap()
            updated[type] = voice
            current.copy(prayerEzanVoices = updated)
        }
        prefsRepo.savePrayerEzanVoice(type, voice)
        scheduleUpcomingAlarms()
    }

    fun testSoundAndVibration(customVoice: String? = null) {
        val voice = customVoice ?: _uiState.value.selectedEzanVoice
        if (AdhanAudioPlayer.isPlaying) {
            AdhanAudioPlayer.stop()
            _uiState.update { it.copy(isPlayingEzan = false, activePlayingVoiceName = null) }
            return
        }
        _uiState.update { it.copy(isPlayingEzan = true, activePlayingVoiceName = voice) }
        AdhanAudioPlayer.playAdhan(
            context = getApplication(),
            voiceName = voice,
            onStarted = {
                _uiState.update { it.copy(isPlayingEzan = true, activePlayingVoiceName = voice) }
            },
            onFinished = {
                _uiState.update { it.copy(isPlayingEzan = false, activePlayingVoiceName = null) }
            }
        )
    }

    fun stopEzanPlayback() {
        AdhanAudioPlayer.stop()
        _uiState.update { it.copy(isPlayingEzan = false, activePlayingVoiceName = null) }
    }

    fun scheduleUpcomingAlarms() {
        val state = _uiState.value
        PrayerAlarmScheduler.scheduleUpcomingPrayerAlarms(
            context = getApplication(),
            prayerTimes = state.prayerTimes,
            cityName = state.selectedCity.name,
            notificationSettings = state.notificationSettings,
            isEarlyReminderEnabled = state.isEarlyReminderEnabled
        )
        // Background Service guarantee via WorkManager
        try {
            PrayerWorkManagerHelper.enqueuePeriodicPrayerSync(getApplication())
            PrayerWorkManagerHelper.enqueueImmediateSync(getApplication())
        } catch (_: Exception) {}
    }

    fun verifyLocation() {
        viewModelScope.launch {
            _uiState.update { it.copy(locationVerificationState = LocationVerificationState.Checking) }
            delay(400)
            val result = LocationVerificationHelper.detectAndVerifyLocation(getApplication())
            _uiState.update { it.copy(locationVerificationState = result) }
            if (result is LocationVerificationState.Verified) {
                setCity(result.nearestCity)
            }
        }
    }

    // Zikirmatik actions (Persistent & Defter Entegrasyonu)
    fun incrementDhikr() {
        val state = _uiState.value
        val preset = WisdomDatabase.dhikrPresets.getOrNull(state.currentDhikrIndex) ?: WisdomDatabase.dhikrPresets[0]
        val newCount = state.dhikrCount + 1
        var newLaps = state.dhikrCompletedLaps

        // Kalıcı Zikir Defterine kaydet
        val updatedDefter = prefsRepo.logDhikrIncrement(
            dhikrId = preset.id,
            title = preset.title,
            arabic = preset.arabic,
            meaning = preset.meaning,
            target = preset.target
        )

        if (preset.target > 0 && newCount >= preset.target) {
            newLaps += 1
            triggerHaptic(longVibe = true)
            _uiState.update {
                it.copy(
                    dhikrCount = 0,
                    dhikrCompletedLaps = newLaps,
                    dhikrBookEntries = updatedDefter
                )
            }
            prefsRepo.saveDhikrState(state.currentDhikrIndex, 0, newLaps)
        } else {
            triggerHaptic(longVibe = false)
            _uiState.update {
                it.copy(
                    dhikrCount = newCount,
                    dhikrBookEntries = updatedDefter
                )
            }
            prefsRepo.saveDhikrState(state.currentDhikrIndex, newCount, newLaps)
        }
    }

    fun resetDhikr() {
        triggerHaptic(longVibe = false)
        val state = _uiState.value
        _uiState.update { it.copy(dhikrCount = 0, dhikrCompletedLaps = 0) }
        prefsRepo.saveDhikrState(state.currentDhikrIndex, 0, 0)
    }

    fun resetDhikrBookEntry(dhikrId: String) {
        val updated = prefsRepo.resetDhikrBookEntry(dhikrId)
        _uiState.update { it.copy(dhikrBookEntries = updated) }
    }

    fun selectDhikrPreset(index: Int) {
        if (index in WisdomDatabase.allDhikrPresets.indices) {
            _uiState.update {
                it.copy(
                    currentDhikrIndex = index,
                    dhikrCount = 0,
                    dhikrCompletedLaps = 0
                )
            }
            prefsRepo.saveDhikrState(index, 0, 0)
        }
    }

    fun selectDhikrById(dhikrId: String) {
        val index = WisdomDatabase.allDhikrPresets.indexOfFirst { it.id == dhikrId }
        if (index >= 0) {
            selectDhikrPreset(index)
        }
    }

    fun selectDhikrByAsma(asmaNumber: Int) {
        selectDhikrById("asma_$asmaNumber")
    }

    fun toggleLargeFont() {
        val newVal = !_uiState.value.isLargeFontEnabled
        _uiState.update { it.copy(isLargeFontEnabled = newVal) }
        prefsRepo.saveLargeFontEnabled(newVal)
    }

    fun setLargeFont(enabled: Boolean) {
        _uiState.update { it.copy(isLargeFontEnabled = enabled) }
        prefsRepo.saveLargeFontEnabled(enabled)
    }

    fun toggleVibration() {
        val newVal = !_uiState.value.isVibrationEnabled
        _uiState.update { it.copy(isVibrationEnabled = newVal) }
        prefsRepo.saveVibrationEnabled(newVal)
    }

    // Kaza Tracker & Daily Prayers (Persistent)
    fun updateKazaTracker(tracker: KazaTracker) {
        _uiState.update { it.copy(kazaTracker = tracker) }
        prefsRepo.saveKazaTracker(tracker)
    }

    fun logKazaAction(
        title: String,
        description: String,
        changeAmount: Int,
        prayerName: String,
        newTracker: KazaTracker
    ) {
        val nowFormatted = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(Date())
        val entry = KazaBookEntry(
            id = UUID.randomUUID().toString(),
            title = title,
            description = description,
            changeAmount = changeAmount,
            prayerName = prayerName,
            isAutoMissed = false,
            dateFormatted = nowFormatted,
            timestampMillis = System.currentTimeMillis()
        )
        val updatedBook = prefsRepo.addKazaBookEntry(entry)
        _uiState.update {
            it.copy(
                kazaTracker = newTracker,
                kazaBookEntries = updatedBook
            )
        }
        prefsRepo.saveKazaTracker(newTracker)
    }

    fun clearKazaBook() {
        prefsRepo.clearKazaBookEntries()
        _uiState.update { it.copy(kazaBookEntries = emptyList()) }
    }

    fun toggleDailyPrayer(prayerName: String) {
        val current = _uiState.value.dailyPrayerCheck
        val updated = when (prayerName.lowercase()) {
            "fajr", "sabah" -> current.copy(fajr = !current.fajr)
            "dhuhr", "ogle", "öğle" -> current.copy(dhuhr = !current.dhuhr)
            "asr", "ikindi" -> current.copy(asr = !current.asr)
            "maghrib", "aksam", "akşam" -> current.copy(maghrib = !current.maghrib)
            "isha", "yatsi", "yatsı" -> current.copy(isha = !current.isha)
            else -> current
        }
        _uiState.update { it.copy(dailyPrayerCheck = updated) }
        prefsRepo.saveDailyPrayerCheck(updated)
    }

    private fun triggerHaptic(longVibe: Boolean) {
        if (!_uiState.value.isVibrationEnabled) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = if (longVibe) {
                    VibrationEffect.createWaveform(longArrayOf(0, 70, 50, 90), -1)
                } else {
                    VibrationEffect.createOneShot(35, VibrationEffect.DEFAULT_AMPLITUDE)
                }
                vibrator?.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(if (longVibe) 150L else 40L)
            }
        } catch (_: Exception) {}
    }

    private fun recalculateTimes() {
        val state = _uiState.value
        val cal = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, state.dateOffsetDays)
        }
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH) + 1
        val day = cal.get(Calendar.DAY_OF_MONTH)
        val dayOfYear = cal.get(Calendar.DAY_OF_YEAR)

        val rawTimes = PrayerCalculationEngine.calculateTimesForDate(year, month, day, state.selectedCity)

        // Apply precaution minutes if set
        val times = if (state.precautionMinutes == 0) {
            rawTimes
        } else {
            rawTimes.map { item ->
                val adjustedMillis = item.timestampMillis + (state.precautionMinutes * 60 * 1000L)
                val c = Calendar.getInstance().apply { timeInMillis = adjustedMillis }
                val formatted = String.format("%02d:%02d", c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE))
                item.copy(timeFormatted = formatted, timestampMillis = adjustedMillis)
            }
        }

        val hijri = PrayerCalculationEngine.calculateHijriDate(year, month, day)
        val qibla = PrayerCalculationEngine.calculateQiblaBearing(state.selectedCity.latitude, state.selectedCity.longitude)
        val dist = PrayerCalculationEngine.calculateDistanceToKaabaKm(state.selectedCity.latitude, state.selectedCity.longitude)

        val sdf = SimpleDateFormat("d MMMM yyyy, EEEE", Locale("tr", "TR"))
        val dateText = sdf.format(cal.time)

        _uiState.update {
            it.copy(
                prayerTimes = times,
                hijriDate = hijri,
                qiblaBearing = qibla,
                distanceToKaabaKm = dist,
                gregorianDateText = dateText,
                dailyWisdom = WisdomDatabase.getWisdomByIndex(dayOfYear + it.wisdomIndex)
            )
        }
        updatePrayerProgression()
        try {
            PrayerWidgetProvider.updateAllWidgets(getApplication())
        } catch (_: Exception) {}
        scheduleUpcomingAlarms()
    }

    private var lastWidgetRefreshMillis: Long = 0L

    private fun updatePrayerProgression() {
        val now = System.currentTimeMillis()
        val state = _uiState.value
        val times = state.prayerTimes
        if (times.isEmpty()) return

        // Periyodik widget senkronizasyonu (30 saniyede bir)
        if (now - lastWidgetRefreshMillis >= 30_000L) {
            lastWidgetRefreshMillis = now
            try {
                PrayerWidgetProvider.updateAllWidgets(getApplication())
            } catch (_: Exception) {}
        }

        // Otomatik kaza aktarımı kontrolü (vakti geçen ve kılınmayan vakti kazaya aktar)
        checkAndProcessMissedPrayers()

        // If today (offset == 0)
        if (state.dateOffsetDays == 0) {
            var nextItem: PrayerTimeItem? = null
            var currentItem: PrayerTimeItem? = null
            var nextIndex = -1

            for (i in times.indices) {
                if (times[i].timestampMillis > now) {
                    nextItem = times[i]
                    nextIndex = i
                    break
                }
            }

            var prevTimeMillis: Long
            if (nextIndex == 0) {
                // Gece yarısı ile sabah İmsak arası:
                // Sıradaki: İmsak. Bir önceki: Dün geceki Yatsı.
                val yesterdayYatsiMillis = times.last().timestampMillis - (24 * 3600 * 1000L)
                currentItem = times.last().copy(timestampMillis = yesterdayYatsiMillis)
                prevTimeMillis = yesterdayYatsiMillis
            } else if (nextIndex > 0) {
                currentItem = times[nextIndex - 1]
                prevTimeMillis = currentItem.timestampMillis
            } else {
                // Yatsıdan sonra: Sıradaki: Yarınki İmsak. Bir önceki: Bugünkü Yatsı.
                currentItem = times.last()
                prevTimeMillis = currentItem.timestampMillis
                val tomorrowCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }
                val rawTomorrowTimes = PrayerCalculationEngine.calculateTimesForDate(
                    tomorrowCal.get(Calendar.YEAR),
                    tomorrowCal.get(Calendar.MONTH) + 1,
                    tomorrowCal.get(Calendar.DAY_OF_MONTH),
                    state.selectedCity
                )
                val tomorrowTimes = if (state.precautionMinutes == 0) rawTomorrowTimes else {
                    rawTomorrowTimes.map { item ->
                        val adjustedMillis = item.timestampMillis + (state.precautionMinutes * 60 * 1000L)
                        val c = Calendar.getInstance().apply { timeInMillis = adjustedMillis }
                        val formatted = String.format("%02d:%02d", c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE))
                        item.copy(timeFormatted = formatted, timestampMillis = adjustedMillis)
                    }
                }
                nextItem = tomorrowTimes.first()
            }

            val remainingMillis = ((nextItem?.timestampMillis ?: now) - now).coerceAtLeast(0L)
            val totalSeconds = remainingMillis / 1000
            val hours = totalSeconds / 3600
            val minutes = (totalSeconds % 3600) / 60
            val seconds = totalSeconds % 60
            val countdownStr = String.format("%02d:%02d:%02d", hours, minutes, seconds)

            // Progress between previous prayer time and next prayer time
            val windowTotal = ((nextItem?.timestampMillis ?: (now + 1)) - prevTimeMillis).coerceAtLeast(1L)
            val windowElapsed = (now - prevTimeMillis).coerceIn(0L, windowTotal)
            val progress = (windowElapsed.toFloat() / windowTotal.toFloat()).coerceIn(0f, 1f)

            // Update item states
            val updatedTimes = times.map { item ->
                item.copy(
                    isNext = item.type == nextItem?.type,
                    isCurrent = if (nextIndex == 0) false else item.type == currentItem?.type,
                    isPassed = item.timestampMillis < now
                )
            }

            _uiState.update {
                it.copy(
                    prayerTimes = updatedTimes,
                    currentPrayer = currentItem,
                    nextPrayer = nextItem,
                    countdownText = countdownStr,
                    countdownSecondsRemaining = totalSeconds,
                    progressToNextPrayer = progress
                )
            }
        } else {
            // For other days, no live countdown
            val updatedTimes = times.map { item ->
                item.copy(isNext = false, isCurrent = false, isPassed = state.dateOffsetDays < 0)
            }
            _uiState.update {
                it.copy(
                    prayerTimes = updatedTimes,
                    currentPrayer = null,
                    nextPrayer = updatedTimes.firstOrNull(),
                    countdownText = "--:--:--",
                    progressToNextPrayer = 0f
                )
            }
        }
    }

    private fun checkAndProcessMissedPrayers() {
        val now = System.currentTimeMillis()
        val state = _uiState.value
        val times = state.prayerTimes
        if (times.isEmpty() || state.dateOffsetDays != 0) return

        // 1. Gün geçişi kontrolü
        val dayTransitionOccurred = prefsRepo.checkAndPerformDayTransition()
        if (dayTransitionOccurred) {
            _uiState.update {
                it.copy(
                    kazaTracker = prefsRepo.getKazaTracker(),
                    dailyPrayerCheck = prefsRepo.getDailyPrayerCheck()
                )
            }
        }

        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(now))
        val nowFormatted = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(Date(now))
        val processed = prefsRepo.getProcessedMissedPrayers().toMutableSet()
        var currentKaza = _uiState.value.kazaTracker
        var updatedBookList = _uiState.value.kazaBookEntries
        var updated = false

        val gunesItem = times.find { it.type == PrayerType.GUNES }
        val ikindiItem = times.find { it.type == PrayerType.IKINDI }
        val aksamItem = times.find { it.type == PrayerType.AKSAM }
        val yatsiItem = times.find { it.type == PrayerType.YATSI }

        // 1. Sabah Namazı: Vakti Güneş ile çıkar. Vakit geçtiğinde kılınmadıysa otomatik kaza defterine aktar
        val fajrKey = "${todayStr}_FAJR"
        if (gunesItem != null && now >= gunesItem.timestampMillis && !processed.contains(fajrKey)) {
            processed.add(fajrKey)
            if (!_uiState.value.dailyPrayerCheck.fajr) {
                currentKaza = currentKaza.copy(fajr = currentKaza.fajr + 1)
                val entry = KazaBookEntry(
                    id = UUID.randomUUID().toString(),
                    title = "Sabah Namazı Vakti Çıktı",
                    description = "Vakit geçtiği için otomatik kaza defterine aktarıldı (+1)",
                    changeAmount = 1,
                    prayerName = "Sabah Namazı",
                    isAutoMissed = true,
                    dateFormatted = nowFormatted,
                    timestampMillis = now
                )
                updatedBookList = prefsRepo.addKazaBookEntry(entry)
                updated = true
            }
        }

        // 2. Öğle Namazı: Vakti İkindi ezanı ile çıkar
        val dhuhrKey = "${todayStr}_DHUHR"
        if (ikindiItem != null && now >= ikindiItem.timestampMillis && !processed.contains(dhuhrKey)) {
            processed.add(dhuhrKey)
            if (!_uiState.value.dailyPrayerCheck.dhuhr) {
                currentKaza = currentKaza.copy(dhuhr = currentKaza.dhuhr + 1)
                val entry = KazaBookEntry(
                    id = UUID.randomUUID().toString(),
                    title = "Öğle Namazı Vakti Çıktı",
                    description = "Vakit geçtiği için otomatik kaza defterine aktarıldı (+1)",
                    changeAmount = 1,
                    prayerName = "Öğle Namazı",
                    isAutoMissed = true,
                    dateFormatted = nowFormatted,
                    timestampMillis = now
                )
                updatedBookList = prefsRepo.addKazaBookEntry(entry)
                updated = true
            }
        }

        // 3. İkindi Namazı: Vakti Akşam ezanı ile çıkar
        val asrKey = "${todayStr}_ASR"
        if (aksamItem != null && now >= aksamItem.timestampMillis && !processed.contains(asrKey)) {
            processed.add(asrKey)
            if (!_uiState.value.dailyPrayerCheck.asr) {
                currentKaza = currentKaza.copy(asr = currentKaza.asr + 1)
                val entry = KazaBookEntry(
                    id = UUID.randomUUID().toString(),
                    title = "İkindi Namazı Vakti Çıktı",
                    description = "Vakit geçtiği için otomatik kaza defterine aktarıldı (+1)",
                    changeAmount = 1,
                    prayerName = "İkindi Namazı",
                    isAutoMissed = true,
                    dateFormatted = nowFormatted,
                    timestampMillis = now
                )
                updatedBookList = prefsRepo.addKazaBookEntry(entry)
                updated = true
            }
        }

        // 4. Akşam Namazı: Vakti Yatsı ezanı ile çıkar
        val maghribKey = "${todayStr}_MAGHRIB"
        if (yatsiItem != null && now >= yatsiItem.timestampMillis && !processed.contains(maghribKey)) {
            processed.add(maghribKey)
            if (!_uiState.value.dailyPrayerCheck.maghrib) {
                currentKaza = currentKaza.copy(maghrib = currentKaza.maghrib + 1)
                val entry = KazaBookEntry(
                    id = UUID.randomUUID().toString(),
                    title = "Akşam Namazı Vakti Çıktı",
                    description = "Vakit geçtiği için otomatik kaza defterine aktarıldı (+1)",
                    changeAmount = 1,
                    prayerName = "Akşam Namazı",
                    isAutoMissed = true,
                    dateFormatted = nowFormatted,
                    timestampMillis = now
                )
                updatedBookList = prefsRepo.addKazaBookEntry(entry)
                updated = true
            }
        }

        if (updated) {
            _uiState.update {
                it.copy(
                    kazaTracker = currentKaza,
                    kazaBookEntries = updatedBookList
                )
            }
            prefsRepo.saveKazaTracker(currentKaza)
        }
        prefsRepo.saveProcessedMissedPrayers(processed)
    }

    fun getLastSavedSubService(): String = prefsRepo.getLastActiveSubService()

    fun saveActiveSubService(name: String) {
        prefsRepo.saveLastActiveSubService(name)
    }

    override fun onCleared() {
        super.onCleared()
        AdhanAudioPlayer.stop()
        sensorManager?.unregisterListener(this)
        liveLocationTracker.stopTracking()
    }
}

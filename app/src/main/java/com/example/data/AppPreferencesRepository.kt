package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.AppTheme
import com.example.model.DailyPrayerCheck
import com.example.model.DarkModePreference
import com.example.model.DhikrBookEntry
import com.example.model.KazaBookEntry
import com.example.model.KazaTracker
import com.example.model.NotificationMode
import com.example.model.PrayerType
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class AppPreferencesRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("ezan_vakti_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_THEME = "app_theme"
        private const val KEY_CITY_ID = "selected_city_id"
        private const val KEY_CALC_METHOD = "calculation_method"
        private const val KEY_PRECAUTION_MINUTES = "precaution_minutes"
        private const val KEY_EARLY_REMINDER = "early_reminder_enabled"
        private const val KEY_EZAN_VOICE = "ezan_voice"
        private const val KEY_VIBRATION = "vibration_enabled"

        // Zikirmatik
        private const val KEY_DHIKR_INDEX = "dhikr_index"
        private const val KEY_DHIKR_COUNT = "dhikr_count"
        private const val KEY_DHIKR_LAPS = "dhikr_laps"

        // Kaza Debts Tracker (Boş/0 başlar, kullanıcı girdikçe kalıcı saklanır)
        private const val KEY_KAZA_FAJR = "kaza_fajr"
        private const val KEY_KAZA_DHUHR = "kaza_dhuhr"
        private const val KEY_KAZA_ASR = "kaza_asr"
        private const val KEY_KAZA_MAGHRIB = "kaza_maghrib"
        private const val KEY_KAZA_ISHA = "kaza_isha"
        private const val KEY_KAZA_WITR = "kaza_witr"

        // Daily Prayer Checklist
        private const val KEY_PRAYER_CHECK_DATE = "prayer_check_date"
        private const val KEY_PRAYER_CHECK_FAJR = "prayer_check_fajr"
        private const val KEY_PRAYER_CHECK_DHUHR = "prayer_check_dhuhr"
        private const val KEY_PRAYER_CHECK_ASR = "prayer_check_asr"
        private const val KEY_PRAYER_CHECK_MAGHRIB = "prayer_check_maghrib"
        private const val KEY_PRAYER_CHECK_ISHA = "prayer_check_isha"

        // Otomatik kaza aktarımı ve son kalınan alt menü
        private const val KEY_PROCESSED_MISSED_PRAYERS = "processed_missed_prayers"
        private const val KEY_LAST_SUB_SERVICE = "last_active_sub_service"
        private const val KEY_KAZA_BOOK_DATA = "kaza_book_journal_data"

        // Widget Özelleştirme (Renk, Motif, Şeffaflık)
        private const val KEY_WIDGET_THEME = "widget_theme_preference"
        private const val KEY_WIDGET_MOTIF = "widget_motif_preference"
        private const val KEY_WIDGET_OPACITY = "widget_bg_opacity"
        private const val KEY_WIDGET_MOTIF_WATERMARK = "widget_motif_watermark_enabled"

        // Koyu Mod & Gece Göz Dinlendirme
        private const val KEY_DARK_MODE_PREF = "dark_mode_preference"
        private const val KEY_EYE_COMFORT = "eye_comfort_enabled"
        private const val KEY_LARGE_FONT = "large_font_enabled"

        // Kıble Pusulası Gelişmiş Seçenekleri
        private const val KEY_COMPASS_DIAL_STYLE = "compass_dial_style"
        private const val KEY_COMPASS_HAPTIC = "compass_haptic_enabled"
        private const val KEY_COMPASS_SOUND = "compass_sound_enabled"
        private const val KEY_COMPASS_LEVEL = "compass_level_enabled"
        private const val KEY_COMPASS_TRUE_NORTH = "compass_true_north_enabled"
    }

    // --- Compass Options Persistence ---
    fun getCompassDialStyle(): com.example.model.CompassDialStyle {
        val raw = prefs.getString(KEY_COMPASS_DIAL_STYLE, com.example.model.CompassDialStyle.CLASSIC_KAABA.name)
        return try {
            com.example.model.CompassDialStyle.valueOf(raw ?: com.example.model.CompassDialStyle.CLASSIC_KAABA.name)
        } catch (_: Exception) {
            com.example.model.CompassDialStyle.CLASSIC_KAABA
        }
    }

    fun saveCompassDialStyle(style: com.example.model.CompassDialStyle) {
        prefs.edit().putString(KEY_COMPASS_DIAL_STYLE, style.name).apply()
    }

    fun isCompassHapticEnabled(): Boolean = prefs.getBoolean(KEY_COMPASS_HAPTIC, true)
    fun saveCompassHapticEnabled(enabled: Boolean) = prefs.edit().putBoolean(KEY_COMPASS_HAPTIC, enabled).apply()

    fun isCompassSoundEnabled(): Boolean = prefs.getBoolean(KEY_COMPASS_SOUND, false)
    fun saveCompassSoundEnabled(enabled: Boolean) = prefs.edit().putBoolean(KEY_COMPASS_SOUND, enabled).apply()

    fun isCompassLevelEnabled(): Boolean = prefs.getBoolean(KEY_COMPASS_LEVEL, true)
    fun saveCompassLevelEnabled(enabled: Boolean) = prefs.edit().putBoolean(KEY_COMPASS_LEVEL, enabled).apply()

    fun isCompassTrueNorthEnabled(): Boolean = prefs.getBoolean(KEY_COMPASS_TRUE_NORTH, true)
    fun saveCompassTrueNorthEnabled(enabled: Boolean) = prefs.edit().putBoolean(KEY_COMPASS_TRUE_NORTH, enabled).apply()

    // --- Dark Mode & Eye Comfort Persistence ---
    fun getDarkModePreference(): DarkModePreference {
        val raw = prefs.getString(KEY_DARK_MODE_PREF, DarkModePreference.SYSTEM.name)
        return try {
            DarkModePreference.valueOf(raw ?: DarkModePreference.SYSTEM.name)
        } catch (_: Exception) {
            DarkModePreference.SYSTEM
        }
    }

    fun saveDarkModePreference(pref: DarkModePreference) {
        prefs.edit().putString(KEY_DARK_MODE_PREF, pref.name).apply()
    }

    fun isEyeComfortEnabled(): Boolean = prefs.getBoolean(KEY_EYE_COMFORT, true)

    fun saveEyeComfortEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_EYE_COMFORT, enabled).apply()
    }

    fun isLargeFontEnabled(): Boolean = prefs.getBoolean(KEY_LARGE_FONT, false)

    fun saveLargeFontEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_LARGE_FONT, enabled).apply()
    }

    // --- Widget Customization Persistence ---
    fun getWidgetTheme(): String = prefs.getString(KEY_WIDGET_THEME, "FOLLOW_APP") ?: "FOLLOW_APP"

    fun saveWidgetTheme(themeOption: String) {
        prefs.edit().putString(KEY_WIDGET_THEME, themeOption).apply()
    }

    fun getWidgetMotif(): String = prefs.getString(KEY_WIDGET_MOTIF, "FOLLOW_THEME") ?: "FOLLOW_THEME"

    fun saveWidgetMotif(motifOption: String) {
        prefs.edit().putString(KEY_WIDGET_MOTIF, motifOption).apply()
    }

    fun getWidgetBackgroundOpacity(): Int = prefs.getInt(KEY_WIDGET_OPACITY, 92)

    fun saveWidgetBackgroundOpacity(opacity: Int) {
        prefs.edit().putInt(KEY_WIDGET_OPACITY, opacity).apply()
    }

    fun isWidgetMotifWatermarkEnabled(): Boolean = prefs.getBoolean(KEY_WIDGET_MOTIF_WATERMARK, true)

    fun saveWidgetMotifWatermarkEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_WIDGET_MOTIF_WATERMARK, enabled).apply()
    }

    // --- Theme Persistence ---
    fun getSavedTheme(): AppTheme {
        val themeName = prefs.getString(KEY_THEME, AppTheme.MIDNIGHT.name)
        return try {
            AppTheme.valueOf(themeName ?: AppTheme.MIDNIGHT.name)
        } catch (e: Exception) {
            AppTheme.MIDNIGHT
        }
    }

    fun saveTheme(theme: AppTheme) {
        prefs.edit().putString(KEY_THEME, theme.name).apply()
    }

    // --- Selected City ID ---
    fun getSavedCityId(defaultId: String = "34"): String {
        return prefs.getString(KEY_CITY_ID, defaultId) ?: defaultId
    }

    fun getSelectedCity(): com.example.model.City {
        val id = getSavedCityId("34")
        return CityDatabase.cities.find { it.id == id } ?: CityDatabase.defaultCity
    }

    fun saveCityId(cityId: String) {
        prefs.edit().putString(KEY_CITY_ID, cityId).apply()
    }

    fun saveSelectedCity(city: com.example.model.City) {
        saveCityId(city.id)
    }

    // --- Settings Persistence ---
    fun getCalculationMethod(default: String = "Diyanet İşleri Başkanlığı (Türkiye Standart)"): String =
        prefs.getString(KEY_CALC_METHOD, default) ?: default

    fun saveCalculationMethod(method: String) {
        prefs.edit().putString(KEY_CALC_METHOD, method).apply()
    }

    fun getPrecautionMinutes(): Int = prefs.getInt(KEY_PRECAUTION_MINUTES, 0)

    fun savePrecautionMinutes(minutes: Int) {
        prefs.edit().putInt(KEY_PRECAUTION_MINUTES, minutes).apply()
    }

    fun isEarlyReminderEnabled(): Boolean = prefs.getBoolean(KEY_EARLY_REMINDER, true)

    fun saveEarlyReminderEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_EARLY_REMINDER, enabled).apply()
    }

    fun getEzanVoice(default: String = "İstanbul Ezanı (Saba Makamı)"): String =
        prefs.getString(KEY_EZAN_VOICE, default) ?: default

    fun saveEzanVoice(voice: String) {
        prefs.edit().putString(KEY_EZAN_VOICE, voice).apply()
    }

    fun getPrayerEzanVoice(type: PrayerType): String {
        val defaultVoice = when (type) {
            PrayerType.IMSAK -> "Saba Makamı (Sabah Ezanı)"
            PrayerType.GUNES -> "Kısa Ney Sesi (Kerâhat Çıkışı)"
            PrayerType.OGLE -> "Rast Makamı (Öğle Ezanı)"
            PrayerType.IKINDI -> "Hicaz Makamı (İkindi Ezanı)"
            PrayerType.AKSAM -> "Segâh Makamı (Akşam Ezanı)"
            PrayerType.YATSI -> "Uşşak Makamı (Yatsı Ezanı)"
        }
        return prefs.getString("KEY_PRAYER_VOICE_${type.name}", defaultVoice) ?: defaultVoice
    }

    fun savePrayerEzanVoice(type: PrayerType, voice: String) {
        prefs.edit().putString("KEY_PRAYER_VOICE_${type.name}", voice).apply()
    }

    fun getAllPrayerEzanVoices(): Map<PrayerType, String> {
        return PrayerType.entries.associateWith { getPrayerEzanVoice(it) }
    }

    fun isVibrationEnabled(): Boolean = prefs.getBoolean(KEY_VIBRATION, true)

    fun saveVibrationEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_VIBRATION, enabled).apply()
    }

    // --- Zikirmatik Persistence ---
    fun getDhikrIndex(): Int = prefs.getInt(KEY_DHIKR_INDEX, 0)
    fun getDhikrCount(): Int = prefs.getInt(KEY_DHIKR_COUNT, 0)
    fun getDhikrLaps(): Int = prefs.getInt(KEY_DHIKR_LAPS, 0)

    fun saveDhikrState(index: Int, count: Int, laps: Int) {
        prefs.edit()
            .putInt(KEY_DHIKR_INDEX, index)
            .putInt(KEY_DHIKR_COUNT, count)
            .putInt(KEY_DHIKR_LAPS, laps)
            .apply()
    }

    // --- Kaza Tracker Persistence (Kullanıcı talebi: varsayılan 0/boş başlasın ve kalıcı kalsın) ---
    fun getKazaTracker(): KazaTracker {
        return KazaTracker(
            fajr = prefs.getInt(KEY_KAZA_FAJR, 0),
            dhuhr = prefs.getInt(KEY_KAZA_DHUHR, 0),
            asr = prefs.getInt(KEY_KAZA_ASR, 0),
            maghrib = prefs.getInt(KEY_KAZA_MAGHRIB, 0),
            isha = prefs.getInt(KEY_KAZA_ISHA, 0),
            witr = prefs.getInt(KEY_KAZA_WITR, 0)
        )
    }

    fun saveKazaTracker(tracker: KazaTracker) {
        prefs.edit()
            .putInt(KEY_KAZA_FAJR, tracker.fajr)
            .putInt(KEY_KAZA_DHUHR, tracker.dhuhr)
            .putInt(KEY_KAZA_ASR, tracker.asr)
            .putInt(KEY_KAZA_MAGHRIB, tracker.maghrib)
            .putInt(KEY_KAZA_ISHA, tracker.isha)
            .putInt(KEY_KAZA_WITR, tracker.witr)
            .apply()
    }

    // --- Daily Prayer Check Persistence ---
    fun getDailyPrayerCheck(): DailyPrayerCheck {
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val savedDate = prefs.getString(KEY_PRAYER_CHECK_DATE, "")
        if (savedDate != todayStr) {
            return DailyPrayerCheck(fajr = false, dhuhr = false, asr = false, maghrib = false, isha = false)
        }
        return DailyPrayerCheck(
            fajr = prefs.getBoolean(KEY_PRAYER_CHECK_FAJR, false),
            dhuhr = prefs.getBoolean(KEY_PRAYER_CHECK_DHUHR, false),
            asr = prefs.getBoolean(KEY_PRAYER_CHECK_ASR, false),
            maghrib = prefs.getBoolean(KEY_PRAYER_CHECK_MAGHRIB, false),
            isha = prefs.getBoolean(KEY_PRAYER_CHECK_ISHA, false)
        )
    }

    fun saveDailyPrayerCheck(check: DailyPrayerCheck) {
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        prefs.edit()
            .putString(KEY_PRAYER_CHECK_DATE, todayStr)
            .putBoolean(KEY_PRAYER_CHECK_FAJR, check.fajr)
            .putBoolean(KEY_PRAYER_CHECK_DHUHR, check.dhuhr)
            .putBoolean(KEY_PRAYER_CHECK_ASR, check.asr)
            .putBoolean(KEY_PRAYER_CHECK_MAGHRIB, check.maghrib)
            .putBoolean(KEY_PRAYER_CHECK_ISHA, check.isha)
            .apply()
    }

    // --- Otomatik Kaza Aktarımı Takibi ---
    fun getProcessedMissedPrayers(): Set<String> {
        return prefs.getStringSet(KEY_PROCESSED_MISSED_PRAYERS, emptySet()) ?: emptySet()
    }

    fun saveProcessedMissedPrayers(keys: Set<String>) {
        prefs.edit().putStringSet(KEY_PROCESSED_MISSED_PRAYERS, keys).apply()
    }

    fun checkAndPerformDayTransition(): Boolean {
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val savedDate = prefs.getString(KEY_PRAYER_CHECK_DATE, "")
        if (savedDate.isNullOrEmpty()) {
            prefs.edit().putString(KEY_PRAYER_CHECK_DATE, todayStr).apply()
            return false
        }
        if (savedDate != todayStr) {
            val fajrDone = prefs.getBoolean(KEY_PRAYER_CHECK_FAJR, false)
            val dhuhrDone = prefs.getBoolean(KEY_PRAYER_CHECK_DHUHR, false)
            val asrDone = prefs.getBoolean(KEY_PRAYER_CHECK_ASR, false)
            val maghribDone = prefs.getBoolean(KEY_PRAYER_CHECK_MAGHRIB, false)
            val ishaDone = prefs.getBoolean(KEY_PRAYER_CHECK_ISHA, false)

            var kaza = getKazaTracker()
            val processed = getProcessedMissedPrayers().toMutableSet()
            var changed = false

            if (!fajrDone && !processed.contains("${savedDate}_FAJR")) {
                kaza = kaza.copy(fajr = kaza.fajr + 1)
                processed.add("${savedDate}_FAJR")
                changed = true
            }
            if (!dhuhrDone && !processed.contains("${savedDate}_DHUHR")) {
                kaza = kaza.copy(dhuhr = kaza.dhuhr + 1)
                processed.add("${savedDate}_DHUHR")
                changed = true
            }
            if (!asrDone && !processed.contains("${savedDate}_ASR")) {
                kaza = kaza.copy(asr = kaza.asr + 1)
                processed.add("${savedDate}_ASR")
                changed = true
            }
            if (!maghribDone && !processed.contains("${savedDate}_MAGHRIB")) {
                kaza = kaza.copy(maghrib = kaza.maghrib + 1)
                processed.add("${savedDate}_MAGHRIB")
                changed = true
            }
            if (!ishaDone && !processed.contains("${savedDate}_ISHA")) {
                kaza = kaza.copy(isha = kaza.isha + 1)
                processed.add("${savedDate}_ISHA")
                changed = true
            }

            if (changed) {
                saveKazaTracker(kaza)
                saveProcessedMissedPrayers(processed)
            }

            prefs.edit()
                .putString(KEY_PRAYER_CHECK_DATE, todayStr)
                .putBoolean(KEY_PRAYER_CHECK_FAJR, false)
                .putBoolean(KEY_PRAYER_CHECK_DHUHR, false)
                .putBoolean(KEY_PRAYER_CHECK_ASR, false)
                .putBoolean(KEY_PRAYER_CHECK_MAGHRIB, false)
                .putBoolean(KEY_PRAYER_CHECK_ISHA, false)
                .apply()
            return true
        }
        return false
    }

    // --- Son Kalınan Hizmet Menüsü (Başa dönmeme kuralı) ---
    fun getLastActiveSubService(): String = prefs.getString(KEY_LAST_SUB_SERVICE, "NONE") ?: "NONE"

    fun saveLastActiveSubService(serviceName: String) {
        prefs.edit().putString(KEY_LAST_SUB_SERVICE, serviceName).apply()
    }

    // --- Zikir Defteri & Çetele Kayıtları (Kalıcı Zikir Günlüğü) ---
    private val KEY_DHIKR_BOOK_DATA = "dhikr_book_entries_json"

    fun getDhikrBookEntries(): List<DhikrBookEntry> {
        val jsonStr = prefs.getString(KEY_DHIKR_BOOK_DATA, null)
        if (jsonStr.isNullOrEmpty()) {
            return emptyList()
        }
        val result = mutableListOf<DhikrBookEntry>()
        try {
            val jsonArray = JSONArray(jsonStr)
            val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val lastDate = obj.optString("lastDate", "")
                val savedTodayCount = obj.optInt("todayCount", 0)
                // Yeni güne geçildiyse todayCount 0 olarak gösterilir ama totalCount korunur
                val effectiveTodayCount = if (lastDate == todayStr) savedTodayCount else 0

                result.add(
                    DhikrBookEntry(
                        dhikrId = obj.optString("id", ""),
                        title = obj.optString("title", ""),
                        arabic = obj.optString("arabic", ""),
                        meaning = obj.optString("meaning", ""),
                        totalCount = obj.optInt("totalCount", 0),
                        todayCount = effectiveTodayCount,
                        completedLaps = obj.optInt("completedLaps", 0),
                        lastUpdatedDate = lastDate,
                        customTarget = obj.optInt("customTarget", 33)
                    )
                )
            }
        } catch (_: Exception) {}
        return result
    }

    fun saveDhikrBookEntries(entries: List<DhikrBookEntry>) {
        val jsonArray = JSONArray()
        entries.forEach { entry ->
            val obj = JSONObject().apply {
                put("id", entry.dhikrId)
                put("title", entry.title)
                put("arabic", entry.arabic)
                put("meaning", entry.meaning)
                put("totalCount", entry.totalCount)
                put("todayCount", entry.todayCount)
                put("completedLaps", entry.completedLaps)
                put("lastDate", entry.lastUpdatedDate)
                put("customTarget", entry.customTarget)
            }
            jsonArray.put(obj)
        }
        prefs.edit().putString(KEY_DHIKR_BOOK_DATA, jsonArray.toString()).apply()
    }

    fun logDhikrIncrement(
        dhikrId: String,
        title: String,
        arabic: String,
        meaning: String,
        target: Int
    ): List<DhikrBookEntry> {
        val currentEntries = getDhikrBookEntries().toMutableList()
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val index = currentEntries.indexOfFirst { it.dhikrId == dhikrId }

        if (index >= 0) {
            val old = currentEntries[index]
            val newToday = if (old.lastUpdatedDate == todayStr) old.todayCount + 1 else 1
            val newTotal = old.totalCount + 1
            val newLaps = if (target > 0) newTotal / target else old.completedLaps
            currentEntries[index] = old.copy(
                totalCount = newTotal,
                todayCount = newToday,
                completedLaps = newLaps,
                lastUpdatedDate = todayStr,
                customTarget = target
            )
        } else {
            currentEntries.add(
                DhikrBookEntry(
                    dhikrId = dhikrId,
                    title = title,
                    arabic = arabic,
                    meaning = meaning,
                    totalCount = 1,
                    todayCount = 1,
                    completedLaps = if (target > 0) 1 / target else 0,
                    lastUpdatedDate = todayStr,
                    customTarget = target
                )
            )
        }

        saveDhikrBookEntries(currentEntries)
        return currentEntries
    }

    fun resetDhikrBookEntry(dhikrId: String): List<DhikrBookEntry> {
        val currentEntries = getDhikrBookEntries().toMutableList()
        val index = currentEntries.indexOfFirst { it.dhikrId == dhikrId }
        if (index >= 0) {
            currentEntries[index] = currentEntries[index].copy(
                totalCount = 0,
                todayCount = 0,
                completedLaps = 0
            )
            saveDhikrBookEntries(currentEntries)
        }
        return currentEntries
    }

    // --- Kaza İşlem Defteri (Uygulamadan çıkıldığında asla sıfırlanmayan kalıcı kayıtlar) ---
    fun getKazaBookEntries(): List<KazaBookEntry> {
        val raw = prefs.getString(KEY_KAZA_BOOK_DATA, null) ?: return emptyList()
        return try {
            val jsonArray = JSONArray(raw)
            val list = mutableListOf<KazaBookEntry>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    KazaBookEntry(
                        id = obj.optString("id", UUID.randomUUID().toString()),
                        title = obj.optString("title", ""),
                        description = obj.optString("description", ""),
                        changeAmount = obj.optInt("changeAmount", 0),
                        prayerName = obj.optString("prayerName", ""),
                        isAutoMissed = obj.optBoolean("isAutoMissed", false),
                        dateFormatted = obj.optString("dateFormatted", ""),
                        timestampMillis = obj.optLong("timestampMillis", System.currentTimeMillis())
                    )
                )
            }
            list.sortedByDescending { it.timestampMillis }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun saveKazaBookEntries(entries: List<KazaBookEntry>) {
        val jsonArray = JSONArray()
        entries.take(200).forEach { entry ->
            val obj = JSONObject().apply {
                put("id", entry.id)
                put("title", entry.title)
                put("description", entry.description)
                put("changeAmount", entry.changeAmount)
                put("prayerName", entry.prayerName)
                put("isAutoMissed", entry.isAutoMissed)
                put("dateFormatted", entry.dateFormatted)
                put("timestampMillis", entry.timestampMillis)
            }
            jsonArray.put(obj)
        }
        prefs.edit().putString(KEY_KAZA_BOOK_DATA, jsonArray.toString()).apply()
    }

    fun addKazaBookEntry(entry: KazaBookEntry): List<KazaBookEntry> {
        val current = getKazaBookEntries().toMutableList()
        current.add(0, entry)
        val trimmed = current.take(200)
        saveKazaBookEntries(trimmed)
        return trimmed
    }

    fun clearKazaBookEntries() {
        prefs.edit().remove(KEY_KAZA_BOOK_DATA).apply()
    }

    fun getNotificationMode(prayerType: PrayerType): NotificationMode {
        val name = prefs.getString("notif_mode_${prayerType.name}", null) ?: return NotificationMode.EZAN
        return try {
            NotificationMode.valueOf(name)
        } catch (_: Exception) {
            NotificationMode.EZAN
        }
    }

    fun saveNotificationMode(prayerType: PrayerType, mode: NotificationMode) {
        prefs.edit().putString("notif_mode_${prayerType.name}", mode.name).apply()
    }

    fun getNotificationSettings(): Map<PrayerType, NotificationMode> {
        return PrayerType.entries.associateWith { getNotificationMode(it) }
    }

    fun saveNotificationSettings(settings: Map<PrayerType, NotificationMode>) {
        val editor = prefs.edit()
        settings.forEach { (type, mode) ->
            editor.putString("notif_mode_${type.name}", mode.name)
        }
        editor.apply()
    }

    fun getLastBackgroundSyncTime(): Long {
        return prefs.getLong("key_last_background_sync_time", 0L)
    }

    fun setLastBackgroundSyncTime(timestampMillis: Long) {
        prefs.edit().putLong("key_last_background_sync_time", timestampMillis).apply()
    }

    fun getLastNotifiedPrayerKey(): String? {
        return prefs.getString("key_last_notified_prayer", null)
    }

    fun setLastNotifiedPrayerKey(key: String) {
        prefs.edit().putString("key_last_notified_prayer", key).apply()
    }
}

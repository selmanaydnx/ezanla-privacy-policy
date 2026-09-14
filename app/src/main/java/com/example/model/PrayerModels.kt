package com.example.model

enum class PrayerType(
    val titleTr: String,
    val descriptionTr: String
) {
    IMSAK("İmsak", "Sabah namazı vakti"),
    GUNES("Güneş", "Güneş doğuşu, kerahet başlangıcı"),
    OGLE("Öğle", "Öğle namazı vakti"),
    IKINDI("İkindi", "İkindi namazı vakti"),
    AKSAM("Akşam", "Akşam namazı vakti"),
    YATSI("Yatsı", "Yatsı ve vitir namazı vakti");

    /**
     * Kullanıcı Talebi: "Akşam namazında iftar vakti sadece ramazan ayının bulunduğu günler olacak şekilde ayarla."
     */
    fun getDisplayName(isRamadan: Boolean = false): String {
        return if (this == AKSAM && isRamadan) "Akşam (İftar)" else titleTr
    }

    fun getDescription(isRamadan: Boolean = false): String {
        return when (this) {
            IMSAK -> if (isRamadan) "Sabah namazı ve sahur sonu" else "Sabah namazı vakti"
            AKSAM -> if (isRamadan) "Akşam namazı ve iftar vakti" else "Akşam namazı vakti"
            else -> descriptionTr
        }
    }
}

data class PrayerTimeItem(
    val type: PrayerType,
    val timeFormatted: String,
    val timestampMillis: Long,
    val isNext: Boolean = false,
    val isCurrent: Boolean = false,
    val isPassed: Boolean = false,
    val notificationMode: NotificationMode = NotificationMode.EZAN
)

enum class NotificationMode {
    EZAN,
    BEEP,
    SILENT
}

data class City(
    val id: String,
    val name: String,
    val country: String,
    val latitude: Double,
    val longitude: Double,
    val timezone: Double = 3.0 // Turkey UTC+3
)

data class HijriDate(
    val day: Int,
    val monthName: String,
    val year: Int
) {
    fun format(): String = "$day $monthName $year"
}

data class DhikrPreset(
    val id: String,
    val title: String,
    val arabic: String,
    val meaning: String,
    val target: Int = 33,
    val virtues: String = ""
)

data class DhikrBookEntry(
    val dhikrId: String,
    val title: String,
    val arabic: String,
    val meaning: String = "",
    val totalCount: Int = 0,
    val todayCount: Int = 0,
    val completedLaps: Int = 0,
    val lastUpdatedDate: String = "",
    val customTarget: Int = 33
)

data class DailyWisdom(
    val ayahArabic: String,
    val ayahTurkish: String,
    val ayahSurah: String,
    val hadithTurkish: String,
    val hadithSource: String,
    val duaArabic: String,
    val duaTurkish: String
)

data class KazaBookEntry(
    val id: String,
    val title: String,
    val description: String,
    val changeAmount: Int, // e.g. -1 (kılındı), +1 (eklendi/aktarıldı)
    val prayerName: String,
    val isAutoMissed: Boolean,
    val dateFormatted: String,
    val timestampMillis: Long = System.currentTimeMillis()
)


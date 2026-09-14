package com.example.model

data class Surah(
    val number: Int,
    val nameTr: String,
    val nameAr: String,
    val meaningTr: String,
    val ayahCount: Int,
    val revelationType: String, // "Mekke" or "Medine"
    val audioUrl: String,
    val ayahs: List<Ayah> = emptyList()
)

data class Ayah(
    val surahNumber: Int,
    val ayahNumber: Int,
    val textAr: String,
    val textTr: String
)

data class TajweedRule(
    val title: String,
    val titleAr: String,
    val description: String,
    val colorHex: Long,
    val examples: List<TajweedExample>
)

data class TajweedExample(
    val arabicWord: String,
    val highlightedPart: String,
    val explanation: String
)

data class NearbyMosque(
    val id: String,
    val name: String,
    val city: String,
    val distanceMeters: Int,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val features: List<String>,
    val isHistorical: Boolean = false
)

enum class JuzStatus {
    AVAILABLE,
    IN_PROGRESS,
    COMPLETED
}

data class HatimJuz(
    val number: Int,
    val startSurah: String,
    val endSurah: String,
    val pageStart: Int,
    val pageEnd: Int,
    var status: JuzStatus = JuzStatus.AVAILABLE,
    var readerName: String = ""
)

data class ReligiousDay(
    val id: String,
    val title: String,
    val hijriDate: String,
    val gregorianDate: String,
    val daysRemaining: Int,
    val significance: String,
    val worshipRecommendation: String
)

data class AsmaUlHusna(
    val number: Int,
    val arabic: String,
    val transliteration: String,
    val turkishMeaning: String,
    val ebcedValue: Int,
    val virtue: String
)

data class IslamicVideo(
    val id: String,
    val title: String,
    val speaker: String,
    val duration: String,
    val category: String,
    val thumbnailUrl: String,
    val videoUrl: String,
    val description: String
)

data class IslamicWallpaper(
    val id: String,
    val title: String,
    val category: String,
    val imageUrl: String,
    val accentColorHex: Long
)

data class KazaTracker(
    val fajr: Int = 0,
    val dhuhr: Int = 0,
    val asr: Int = 0,
    val maghrib: Int = 0,
    val isha: Int = 0,
    val witr: Int = 0
) {
    val total: Int get() = fajr + dhuhr + asr + maghrib + isha + witr
    val totalDays: Int get() = total / 6
}

data class DailyPrayerCheck(
    val fajr: Boolean = false,
    val dhuhr: Boolean = false,
    val asr: Boolean = false,
    val maghrib: Boolean = false,
    val isha: Boolean = false
) {
    val completedCount: Int get() = listOf(fajr, dhuhr, asr, maghrib, isha).count { it }
    val progress: Float get() = completedCount / 5f
}

enum class DuaCategory(val titleTr: String) {
    TUMU("Tümü"),
    SABAH_AKSAM("Sabah & Akşam"),
    NAMAZ_SONRASI("Namaz Sonrası"),
    SIFA_KORUNMA("Şifâ & Korunma"),
    GUNLUK_HAYAT("Günlük Hayat"),
    PEYGAMBER_DUALARI("Peygamber Duaları"),
    TEVBE_ISTIGFAR("Tevbe & İstiğfar")
}

data class CategorizedDua(
    val id: String,
    val title: String,
    val category: DuaCategory,
    val arabic: String,
    val transcription: String,
    val turkish: String,
    val sourceOrVirtue: String,
    val repeatCount: Int = 1
) {
    val arabicText: String get() = arabic
    val transliteration: String get() = transcription
    val turkishMeaning: String get() = turkish
    val virtueNotes: String get() = sourceOrVirtue
    val recommendedCount: String get() = if (repeatCount > 1) "${repeatCount} Defa" else ""
}

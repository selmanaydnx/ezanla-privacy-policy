package com.example.calc

import com.example.model.City
import com.example.model.HijriDate
import com.example.model.PrayerTimeItem
import com.example.model.PrayerType
import java.util.Calendar
import kotlin.math.*

object PrayerCalculationEngine {

    // Kaaba coordinates
    private const val KAABA_LAT = 21.422487
    private const val KAABA_LNG = 39.826206

    // Turkish Diyanet method angles
    private const val FAJR_ANGLE = 18.0
    private const val ISHA_ANGLE = 17.0
    private const val SUN_ALTITUDE_SUNRISE = -0.8333 // Refraction & sun disk

    fun calculateTimesForDate(
        year: Int,
        month: Int, // 1-12
        day: Int,   // 1-31
        city: City
    ): List<PrayerTimeItem> {
        val jd = julianDate(year, month, day)
        val d = jd - 2451545.0

        // Sun parameters
        val g = fixAngle(357.529 + 0.98560028 * d)
        val q = fixAngle(280.459 + 0.98564736 * d)
        val l = fixAngle(q + 1.915 * sin(Math.toRadians(g)) + 0.020 * sin(Math.toRadians(2 * g)))

        val e = 23.439 - 0.00000036 * d
        val ra = fixAngle(Math.toDegrees(atan2(cos(Math.toRadians(e)) * sin(Math.toRadians(l)), cos(Math.toRadians(l))))) / 15.0

        // Solar Declination & Equation of Time
        val declination = Math.toDegrees(asin(sin(Math.toRadians(e)) * sin(Math.toRadians(l))))
        val eqTime = (q / 15.0) - ra

        // Solar Noon in local time
        val noon = 12.0 + city.timezone - (city.longitude / 15.0) - eqTime

        // Sunrise & Sunset angle
        val sunriseHourAngle = calculateHourAngle(city.latitude, declination, SUN_ALTITUDE_SUNRISE)
        val sunrise = noon - sunriseHourAngle
        val sunset = noon + sunriseHourAngle

        // Fajr (İmsak)
        val fajrHourAngle = calculateHourAngle(city.latitude, declination, -FAJR_ANGLE)
        val fajr = noon - fajrHourAngle

        // Asr (İkindi) - Shadow factor = 1 (Standard)
        val asrAltitude = Math.toDegrees(atan(1.0 / (1.0 + tan(Math.toRadians(abs(city.latitude - declination))))))
        val asrHourAngle = calculateHourAngle(city.latitude, declination, asrAltitude)
        val asr = noon + asrHourAngle

        // Isha (Yatsı)
        val ishaHourAngle = calculateHourAngle(city.latitude, declination, -ISHA_ANGLE)
        val isha = noon + ishaHourAngle

        // Diyanet standard precautions (+ few minutes for safety and horizon elevation)
        val imsakAdjusted = fajr // Diyanet standardı 18° fecr-i sadık
        val gunesAdjusted = sunrise // Güneşin doğuşu (ufuk kırılması dahil)
        val ogleAdjusted = noon + (5.0 / 60.0) // 5 min
        val ikindiAdjusted = asr + (4.0 / 60.0) // 4 min
        val aksamAdjusted = sunset + (7.0 / 60.0) // 7 min
        val yatsiAdjusted = isha + (2.0 / 60.0) // 2 min

        return listOf(
            createItem(PrayerType.IMSAK, year, month, day, imsakAdjusted),
            createItem(PrayerType.GUNES, year, month, day, gunesAdjusted),
            createItem(PrayerType.OGLE, year, month, day, ogleAdjusted),
            createItem(PrayerType.IKINDI, year, month, day, ikindiAdjusted),
            createItem(PrayerType.AKSAM, year, month, day, aksamAdjusted),
            createItem(PrayerType.YATSI, year, month, day, yatsiAdjusted)
        )
    }

    private fun createItem(
        type: PrayerType,
        year: Int,
        month: Int,
        day: Int,
        decimalHours: Double
    ): PrayerTimeItem {
        val totalMinutes = (decimalHours * 60.0).roundToInt()
        val normalizedMinutes = ((totalMinutes % (24 * 60)) + (24 * 60)) % (24 * 60)
        val hour = normalizedMinutes / 60
        val minute = normalizedMinutes % 60

        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month - 1)
            set(Calendar.DAY_OF_MONTH, day)
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val formatted = String.format("%02d:%02d", hour, minute)
        return PrayerTimeItem(
            type = type,
            timeFormatted = formatted,
            timestampMillis = cal.timeInMillis
        )
    }

    private fun calculateHourAngle(latitude: Double, declination: Double, angle: Double): Double {
        val latRad = Math.toRadians(latitude)
        val decRad = Math.toRadians(declination)
        val angRad = Math.toRadians(angle)

        val cosH = (sin(angRad) - sin(latRad) * sin(decRad)) / (cos(latRad) * cos(decRad))
        val clampedCosH = cosH.coerceIn(-1.0, 1.0)
        return Math.toDegrees(acos(clampedCosH)) / 15.0
    }

    private fun fixAngle(angle: Double): Double {
        var a = angle - 360.0 * floor(angle / 360.0)
        if (a < 0) a += 360.0
        return a
    }

    private fun julianDate(year: Int, month: Int, day: Int): Double {
        var y = year
        var m = month
        if (m <= 2) {
            y -= 1
            m += 12
        }
        val a = floor(y / 100.0)
        val b = 2 - a + floor(a / 4.0)
        return floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + day + b - 1524.5
    }

    // Hijri Date Calculation
    private val HIJRI_MONTHS_TR = listOf(
        "Muharrem", "Safer", "Rebiülevvel", "Rebiülahir",
        "Cemaziyelevvel", "Cemaziyelahir", "Recep", "Şaban",
        "Ramazan", "Şevval", "Zilkade", "Zilhicce"
    )

    fun calculateHijriDate(year: Int, month: Int, day: Int): HijriDate {
        val jd = julianDate(year, month, day).toInt()
        val l = jd - 1948440 + 10632
        val n = ((l - 1) / 10631)
        val l2 = l - 10631 * n + 354
        val j = ((10985 - l2) / 5316) * ((50 * l2) / 17719) + (l2 / 5670) * ((43 * l2) / 15238)
        val l3 = l2 - ((30 - j) / 15) * ((17719 * j) / 50) - (j / 16) * ((15238 * j) / 43) + 29
        val hMonth = ((24 * l3) / 709)
        val hDay = l3 - ((709 * hMonth) / 24)
        val hYear = 30 * n + j - 30

        val safeMonth = ((hMonth - 1).coerceIn(0, 11))
        return HijriDate(
            day = hDay.coerceIn(1, 30),
            monthName = HIJRI_MONTHS_TR[safeMonth],
            year = hYear
        )
    }

    /**
     * Kullanıcı Talebi: "Akşam namazında iftar vakti sadece ramazan ayının bulunduğu günler olacak şekilde ayarla."
     * Belirtilen tarihin Ramazan ayı içerisinde olup olmadığını döndürür.
     */
    fun isRamadan(calendar: Calendar = Calendar.getInstance(java.util.TimeZone.getTimeZone("GMT+3"))): Boolean {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) // 0-based
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        // Diyanet resmi takvimine göre Ramazan ayları
        if (year == 2025 && month == Calendar.MARCH && day in 1..29) return true
        if (year == 2026 && ((month == Calendar.FEBRUARY && day >= 18) || (month == Calendar.MARCH && day <= 19))) return true
        if (year == 2027 && ((month == Calendar.FEBRUARY && day >= 8) || (month == Calendar.MARCH && day <= 18))) return true
        if (year == 2028 && ((month == Calendar.JANUARY && day >= 28) || (month == Calendar.FEBRUARY && day <= 26))) return true

        val hijri = calculateHijriDate(year, month + 1, day)
        return hijri.monthName == "Ramazan"
    }

    // Calculate Qibla angle from True North (0 to 360 degrees)
    fun calculateQiblaBearing(lat: Double, lng: Double): Double {
        val latRad = Math.toRadians(lat)
        val kaabaLatRad = Math.toRadians(KAABA_LAT)
        val dLngRad = Math.toRadians(KAABA_LNG - lng)

        val y = sin(dLngRad) * cos(kaabaLatRad)
        val x = cos(latRad) * sin(kaabaLatRad) - sin(latRad) * cos(kaabaLatRad) * cos(dLngRad)
        val bearingRad = atan2(y, x)
        val bearingDeg = Math.toDegrees(bearingRad)
        return (bearingDeg + 360.0) % 360.0
    }

    // Calculate Great-circle distance to Mecca in km
    fun calculateDistanceToKaabaKm(lat: Double, lng: Double): Int {
        return calculateDistanceBetweenKm(lat, lng, KAABA_LAT, KAABA_LNG).roundToInt()
    }

    // Calculate distance in km between any two geographical points
    fun calculateDistanceBetweenKm(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        val r = 6371.0 // Earth radius in km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLng = Math.toRadians(lng2 - lng1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLng / 2) * sin(dLng / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }

    // Calculate Sun's instantaneous azimuth (compass bearing in degrees) and altitude above horizon
    fun calculateSunPosition(
        lat: Double,
        lng: Double,
        timezone: Double = 3.0,
        cal: Calendar = Calendar.getInstance()
    ): Pair<Double, Double> {
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH) + 1
        val day = cal.get(Calendar.DAY_OF_MONTH)
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        val minute = cal.get(Calendar.MINUTE)
        val second = cal.get(Calendar.SECOND)

        val decimalHour = hour + minute / 60.0 + second / 3600.0
        val jd = julianDate(year, month, day) + (decimalHour - timezone) / 24.0
        val d = jd - 2451545.0

        val g = fixAngle(357.529 + 0.98560028 * d)
        val q = fixAngle(280.459 + 0.98564736 * d)
        val l = fixAngle(q + 1.915 * sin(Math.toRadians(g)) + 0.020 * sin(Math.toRadians(2 * g)))

        val e = 23.439 - 0.00000036 * d
        val ra = fixAngle(Math.toDegrees(atan2(cos(Math.toRadians(e)) * sin(Math.toRadians(l)), cos(Math.toRadians(l))))) / 15.0

        val declination = Math.toDegrees(asin(sin(Math.toRadians(e)) * sin(Math.toRadians(l))))
        val eqTime = (q / 15.0) - ra

        val localSolarTime = decimalHour + (lng / 15.0) - timezone + eqTime
        val hourAngleDeg = (localSolarTime - 12.0) * 15.0

        val latRad = Math.toRadians(lat)
        val decRad = Math.toRadians(declination)
        val haRad = Math.toRadians(hourAngleDeg)

        val sinAlt = sin(latRad) * sin(decRad) + cos(latRad) * cos(decRad) * cos(haRad)
        val altitudeDeg = Math.toDegrees(asin(sinAlt.coerceIn(-1.0, 1.0)))

        val cosAlt = cos(Math.toRadians(altitudeDeg))
        val cosAz = (sin(decRad) - sin(latRad) * sinAlt) / (cos(latRad) * cosAlt).coerceAtLeast(0.00001)
        val azRad = acos(cosAz.coerceIn(-1.0, 1.0))
        var azimuthDeg = Math.toDegrees(azRad)
        if (sin(haRad) > 0) {
            azimuthDeg = 360.0 - azimuthDeg
        }

        return Pair((azimuthDeg + 360.0) % 360.0, altitudeDeg)
    }

    // Approximate magnetic declination (degrees difference between magnetic north and true north)
    fun estimateMagneticDeclination(lat: Double, lng: Double): Float {
        val approx = ((lng - 30.0) * 0.15 + (lat - 35.0) * 0.1 + 5.5).toFloat()
        return approx.coerceIn(-25f, 25f)
    }
}

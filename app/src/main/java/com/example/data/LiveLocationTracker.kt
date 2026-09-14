package com.example.data

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import androidx.core.content.ContextCompat
import com.example.calc.PrayerCalculationEngine
import com.example.model.City
import com.example.model.NearbyMosque
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class LiveGpsState(
    val hasPermission: Boolean = false,
    val isGpsEnabled: Boolean = false,
    val isTracking: Boolean = false,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val accuracyMeters: Float? = null,
    val speedKmh: Float? = null,
    val provider: String? = null,
    val nearestCity: City? = null,
    val statusMessage: String = "Konum bekleniyor...",
    val lastUpdateTimestamp: Long = 0L
)

class LiveLocationTracker(private val context: Context) {

    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager

    private val _gpsState = MutableStateFlow(LiveGpsState())
    val gpsState: StateFlow<LiveGpsState> = _gpsState.asStateFlow()

    private val locationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            updateWithLocation(location)
        }

        @Deprecated("Deprecated in Java")
        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}

        override fun onProviderEnabled(provider: String) {
            checkAndStartTracking()
        }

        override fun onProviderDisabled(provider: String) {
            _gpsState.value = _gpsState.value.copy(
                statusMessage = "Konum servisi ($provider) kapalı"
            )
        }
    }

    fun hasPermission(): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    @SuppressLint("MissingPermission")
    fun checkAndStartTracking() {
        val perm = hasPermission()
        if (!perm) {
            _gpsState.value = _gpsState.value.copy(
                hasPermission = false,
                isTracking = false,
                statusMessage = "Konum izni verilmedi"
            )
            return
        }

        val lm = locationManager
        if (lm == null) {
            _gpsState.value = _gpsState.value.copy(
                hasPermission = true,
                statusMessage = "Cihaz konum servisi bulunamadı"
            )
            return
        }

        val isGpsOn = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val isNetOn = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        if (!isGpsOn && !isNetOn) {
            _gpsState.value = _gpsState.value.copy(
                hasPermission = true,
                isGpsEnabled = false,
                isTracking = false,
                statusMessage = "GPS kapalı. Lütfen cihaz konumunu açınız"
            )
            return
        }

        // Cihazdaki tüm aktif sağlayıcılardan (GPS, Ağ, Pasif) en doğru son bilinen konumu hemen al
        var bestLastLoc: Location? = null
        try {
            val providers = lm.getProviders(true)
            for (p in providers) {
                val loc = lm.getLastKnownLocation(p) ?: continue
                if (bestLastLoc == null || loc.accuracy < bestLastLoc.accuracy || (loc.time > bestLastLoc.time && loc.accuracy <= (bestLastLoc.accuracy * 1.5f))) {
                    bestLastLoc = loc
                }
            }
        } catch (_: Exception) {}

        if (bestLastLoc != null) {
            updateWithLocation(bestLastLoc)
        } else {
            // Gerçek konum uydudan/ağdan alınana kadar sahte koordinat üretme
            _gpsState.value = _gpsState.value.copy(
                hasPermission = true,
                isGpsEnabled = true,
                isTracking = true,
                latitude = null,
                longitude = null,
                accuracyMeters = null,
                statusMessage = "Uydular aranıyor... Lütfen açık alanda bekleyiniz"
            )
        }

        // Canlı sürekli GPS dinlemeyi başlat (1 saniyede veya 0.5 metrede bir canlı güncelleme)
        try {
            if (isGpsOn) {
                lm.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    1000L,
                    0.5f,
                    locationListener,
                    Looper.getMainLooper()
                )
            }
            if (isNetOn) {
                lm.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    1000L,
                    0.5f,
                    locationListener,
                    Looper.getMainLooper()
                )
            }
            try {
                if (lm.isProviderEnabled(LocationManager.PASSIVE_PROVIDER)) {
                    lm.requestLocationUpdates(
                        LocationManager.PASSIVE_PROVIDER,
                        1000L,
                        0.5f,
                        locationListener,
                        Looper.getMainLooper()
                    )
                }
            } catch (_: Exception) {}
            _gpsState.value = _gpsState.value.copy(
                isTracking = true,
                isGpsEnabled = true
            )
        } catch (_: Exception) {}
    }

    private fun updateWithLocation(location: Location) {
        val lat = location.latitude
        val lng = location.longitude
        val nearest = LocationVerificationHelper.findNearestCity(lat, lng)
        val speedKmh = if (location.hasSpeed()) location.speed * 3.6f else null
        val accuracy = if (location.hasAccuracy()) location.accuracy else null

        _gpsState.value = _gpsState.value.copy(
            hasPermission = true,
            isGpsEnabled = true,
            isTracking = true,
            latitude = lat,
            longitude = lng,
            accuracyMeters = accuracy,
            speedKmh = speedKmh,
            provider = location.provider ?: "GPS",
            nearestCity = nearest,
            statusMessage = "Canlı GPS Aktif (±${accuracy?.toInt() ?: 10}m)",
            lastUpdateTimestamp = System.currentTimeMillis()
        )
    }

    fun stopTracking() {
        try {
            locationManager?.removeUpdates(locationListener)
            _gpsState.value = _gpsState.value.copy(isTracking = false)
        } catch (_: Exception) {}
    }

    /**
     * Verilen cami listesini kullanıcının canlı GPS koordinatlarına göre gerçek zamanlı mesafesini
     * hesaplayarak en yakından en uzağa doğru sıralar.
     */
    fun getMosquesSortedByRealtimeDistance(
        baseMosques: List<NearbyMosque>,
        userLat: Double?,
        userLng: Double?
    ): List<NearbyMosque> {
        if (userLat == null || userLng == null) {
            return baseMosques
        }

        return baseMosques.map { mosque ->
            val results = FloatArray(1)
            Location.distanceBetween(
                userLat,
                userLng,
                mosque.latitude,
                mosque.longitude,
                results
            )
            val distanceM = results[0].toInt()
            mosque.copy(distanceMeters = distanceM)
        }.sortedBy { it.distanceMeters }
    }
}

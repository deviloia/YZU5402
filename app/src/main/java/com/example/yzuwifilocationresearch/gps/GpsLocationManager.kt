package com.example.yzuwifilocationresearch.gps

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import android.os.Looper
import kotlinx.coroutines.suspendCancellableCoroutine

data class GpsReading(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float,
    val timestamp: Long
)

class GpsLocationManager(private val context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): GpsReading? {
        if (!hasLocationPermission()) return null

        return suspendCancellableCoroutine { continuation ->
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener { location ->
                    val reading = location?.let {
                        GpsReading(
                            latitude = it.latitude,
                            longitude = it.longitude,
                            accuracy = it.accuracy,
                            timestamp = it.time
                        )
                    }
                    continuation.resume(reading) { _, _, _ -> }
                }
                .addOnFailureListener {
                    continuation.resume(null) { _, _, _ -> }
                }
        }
    }

    @SuppressLint("MissingPermission")
    fun startLocationUpdates(onLocation: (GpsReading) -> Unit): () -> Unit {
        if (!hasLocationPermission()) return {}

        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1_000L)
            .setMinUpdateIntervalMillis(500L)
            .setWaitForAccurateLocation(false)
            .build()
        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    onLocation(
                        GpsReading(
                            latitude = location.latitude,
                            longitude = location.longitude,
                            accuracy = location.accuracy,
                            timestamp = location.time
                        )
                    )
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(request, callback, Looper.getMainLooper())
        return {
            fusedLocationClient.removeLocationUpdates(callback)
        }
    }
}

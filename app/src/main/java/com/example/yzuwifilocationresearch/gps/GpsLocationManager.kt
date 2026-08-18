package com.example.yzuwifilocationresearch.gps

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.suspendCancellableCoroutine

/** 一次成功的 GPS 讀值，拿不到定位時整個回傳 null，不用假座標。 */
data class GpsReading(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float,
    val timestamp: Long
)

class GpsLocationManager(context: Context) {

    // Fused 定位服務入口，整合 GPS / WiFi / 基地台訊號。
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    // 建立當下先檢查一次有沒有定位權限，之後呼叫 API 前用這個判斷。
    private val hasPermission: Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED

    // 已經手動檢查 hasPermission，這裡跟 Lint 說不用再警告缺權限檢查。
    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): GpsReading? {
        // 沒權限就直接回傳 null，不要呼叫定位 API（會 crash）。
        if (!hasPermission) return null

        // 把舊式 callback 風格的定位 API 包裝成 suspend fun。
        return suspendCancellableCoroutine { continuation ->
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener { location ->
                    // location 本身也可能是 null（成功但拿不到座標）。
                    val reading = location?.let {
                        GpsReading(
                            latitude = it.latitude,
                            longitude = it.longitude,
                            accuracy = it.accuracy,
                            timestamp = it.time
                        )
                    }
                    // 把結果交還給暫停中的函式，繼續往下執行。
                    continuation.resume(reading) { _, _, _ -> }
                }
                .addOnFailureListener {
                    // 定位失敗（GPS 關閉、逾時等），一樣回傳 null。
                    continuation.resume(null) { _, _, _ -> }
                }
        }
    }
}

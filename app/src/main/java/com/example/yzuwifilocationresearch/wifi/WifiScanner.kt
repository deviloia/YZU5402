package com.example.yzuwifilocationresearch.wifi

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import androidx.core.content.ContextCompat
import com.example.yzuwifilocationresearch.model.WifiScanResult
import kotlinx.coroutines.suspendCancellableCoroutine

/** 觸發一次系統 WiFi 掃描，回傳這一次看到的原始結果（未分組、未統計）。 */
class WifiScanner(context: Context) {

    private val appContext = context.applicationContext

    private val wifiManager: WifiManager =
        appContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

    private val hasPermission: Boolean =
        ContextCompat.checkSelfPermission(appContext, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED

    /** 觸發一次掃描，等系統廣播「掃描完成」後回傳結果。沒權限或啟動失敗回傳空清單。 */
    @SuppressLint("MissingPermission")
    suspend fun scanOnce(): List<WifiScanResult> {
        if (!hasPermission) return emptyList()

        return suspend  CancellableCoroutine { continuation ->
            val receiver = object : BroadcastReceiver() {
                override fun onReceive(receivedContext: Context, intent: Intent) {
                    runCatching { appContext.unregisterReceiver(this) }
                    val results = wifiManager.scanResults.map { result ->
                        WifiScanResult(
                            ssid = result.SSID ?: "",
                            bssid = result.BSSID ?: "",
                            rssi = result.level,
                            frequency = result.frequency
                        )
                    }
                    if (continuation.isActive) {
                        continuation.resume(results) { _, _, _ -> }
                    }
                }
            }

            ContextCompat.registerReceiver(
                appContext,
                receiver,
                IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION),
                ContextCompat.RECEIVER_EXPORTED
            )

            continuation.invokeOnCancellation {
                runCatching { appContext.unregisterReceiver(receiver) }
            }

            val started = wifiManager.startScan()
            if (!started) {
                runCatching { appContext.unregisterReceiver(receiver) }
                if (continuation.isActive) {
                    continuation.resume(emptyList()) { _, _, _ -> }
                }
            }
        }
    }
}

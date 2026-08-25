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
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine

/** 觸發一次系統 WiFi 掃描，回傳這一次看到的原始結果（未分組、未統計）。 */
class WifiScanner(context: Context) {

    private val appContext = context.applicationContext

    private val wifiManager: WifiManager =
        appContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

    private val hasPermission: Boolean =
        ContextCompat.checkSelfPermission(appContext, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED

    /**
     * 觸發一次掃描，等系統廣播「掃描完成」後回傳結果。
     * Android 對前景 App 有「2 分鐘內最多 4 次」的掃描節流限制，超過額度時 startScan() 會直接回傳
     * false（不是錯誤，是系統拒絕這次請求）。這裡遇到節流會每隔 [retryDelayMillis] 重試一次，
     * 直到成功或等超過 [maxWaitMillis]，確保拿到的是真的新掃描結果，不是放棄後回傳空清單。
     * onThrottled 在每次被節流、準備重試前呼叫，方便畫面顯示「被節流，等待中」這類狀態。
     */
    @SuppressLint("MissingPermission")
    suspend fun scanOnce(
        maxWaitMillis: Long = 40_000L,
        retryDelayMillis: Long = 5_000L,
        onThrottled: (() -> Unit)? = null
    ): List<WifiScanResult> {
        if (!hasPermission) return emptyList()

        val deadline = System.currentTimeMillis() + maxWaitMillis
        while (true) {
            val result = attemptScan()
            if (result != null) return result

            if (System.currentTimeMillis() >= deadline) return emptyList()
            onThrottled?.invoke()
            delay(retryDelayMillis)
        }
    }

    /** 嘗試一次掃描：null 代表被節流拒絕（可以重試），非 null（可能是空清單）代表真的掃完了。 */
    private suspend fun attemptScan(): List<WifiScanResult>? {
        return suspendCancellableCoroutine { continuation ->
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
                // 系統拒絕這次掃描（節流），不是真的掃完，回傳 null 讓外層決定要不要重試。
                runCatching { appContext.unregisterReceiver(receiver) }
                if (continuation.isActive) {
                    continuation.resume(null) { _, _, _ -> }
                }
            }
        }
    }
}

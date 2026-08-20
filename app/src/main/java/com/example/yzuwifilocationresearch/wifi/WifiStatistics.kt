package com.example.yzuwifilocationresearch.wifi

import com.example.yzuwifilocationresearch.model.AccessPoint
import com.example.yzuwifilocationresearch.model.WifiScanResult
import kotlin.math.sqrt

/** 把 WifiScanProcessor 分組後的資料，算成每個 AP 的統計值（AccessPoint）。 */
object WifiStatistics {

    /** grouped：key 是 BSSID，value 是這個 AP 在所有輪次出現過的原始記錄。 */
    fun computeAccessPoints(grouped: Map<String, List<WifiScanResult>>): List<AccessPoint> {
        return grouped.map { (bssid, records) ->
            val rssiValues = records.map { it.rssi }
            val meanRssi = rssiValues.average()
            // 母體標準差：sqrt(Σ(RSSI - meanRSSI)² / N)。
            val variance = rssiValues.sumOf { rssi -> (rssi - meanRssi) * (rssi - meanRssi) } / rssiValues.size

            AccessPoint(
                ssid = records.first().ssid,
                bssid = bssid,
                frequency = records.first().frequency,
                appearanceCount = records.size,
                meanRssi = meanRssi,
                minRssi = rssiValues.min(),
                maxRssi = rssiValues.max(),
                standardDeviation = sqrt(variance)
            )
        }
    }
}

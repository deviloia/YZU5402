package com.example.yzuwifilocationresearch.wifi

import com.example.yzuwifilocationresearch.model.AccessPoint
import com.example.yzuwifilocationresearch.model.WifiScanResult
import kotlin.math.sqrt

object WifiStatistics {
    private const val MIN_APPEARANCE_COUNT = 3

    fun computeAccessPoints(grouped: Map<String, List<WifiScanResult>>): List<AccessPoint> {
        return grouped.mapNotNull { (bssid, records) ->
            if (records.size < MIN_APPEARANCE_COUNT) return@mapNotNull null

            val rssiValues = records.map { it.rssi }
            val meanRssi = rssiValues.average()
            val variance = rssiValues.sumOf { rssi ->
                (rssi - meanRssi) * (rssi - meanRssi)
            } / rssiValues.size

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

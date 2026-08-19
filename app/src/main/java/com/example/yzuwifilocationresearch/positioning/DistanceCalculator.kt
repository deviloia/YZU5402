package com.example.yzuwifilocationresearch.positioning

import com.example.yzuwifilocationresearch.model.AccessPoint
import kotlin.math.sqrt

/**
 * 算兩組 WiFi 指紋（AccessPoint 清單）之間的歐氏距離。
 * 只比較兩邊都有出現的 BSSID，用 meanRssi 當每個維度的值。
 */
object DistanceCalculator {

    /** 兩邊完全沒有共同 BSSID 時回傳的距離，代表「無法比較」，排序時會被排到最後。 */
    val NO_COMMON_AP_DISTANCE = Double.MAX_VALUE

    /** 歐氏距離：sqrt(Σ(RSSI差)^2)，只累加兩邊共同出現的 BSSID。 */
    fun euclideanDistance(a: List<AccessPoint>, b: List<AccessPoint>): Double {
        val aByBssid = a.associateBy { it.bssid }
        val bByBssid = b.associateBy { it.bssid }
        val commonBssids = aByBssid.keys.intersect(bByBssid.keys)

        if (commonBssids.isEmpty()) return NO_COMMON_AP_DISTANCE

        val sumOfSquares = commonBssids.sumOf { bssid ->
            val diff = aByBssid.getValue(bssid).meanRssi - bByBssid.getValue(bssid).meanRssi
            diff * diff
        }
        return sqrt(sumOfSquares)
    }
}

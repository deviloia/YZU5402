package com.example.yzuwifilocationresearch.positioning

import com.example.yzuwifilocationresearch.model.AccessPoint
import kotlin.math.abs
import kotlin.math.pow

/**
 * 算兩組 WiFi 指紋（AccessPoint 清單）之間的訊號空間距離。
 * 只比較兩邊都有出現的 BSSID，用 meanRssi 當每個維度的值。
 */
object DistanceCalculator {

    /** 兩邊完全沒有共同 BSSID 時回傳的距離，代表「無法比較」，排序時會被排到最後。 */
    val NO_COMMON_AP_DISTANCE = Double.MAX_VALUE

    /**
     * 明氏距離（Minkowski distance），p 可調：
     * p = 1 是曼哈頓距離，p = 2 是歐氏距離（預設）。
     */
    fun minkowskiDistance(
        a: List<AccessPoint>,
        b: List<AccessPoint>,
        p: Double = 2.0
    ): Double {
        val aByBssid = a.associateBy { it.bssid }
        val bByBssid = b.associateBy { it.bssid }
        val commonBssids = aByBssid.keys.intersect(bByBssid.keys)

        if (commonBssids.isEmpty()) return NO_COMMON_AP_DISTANCE

        val sum = commonBssids.sumOf { bssid ->
            val diff = abs(aByBssid.getValue(bssid).meanRssi - bByBssid.getValue(bssid).meanRssi)
            diff.pow(p)
        }
        return sum.pow(1.0 / p)
    }

    /** 歐氏距離：minkowskiDistance 的 p = 2 特例，KNN 預設使用這個。 */
    fun euclideanDistance(a: List<AccessPoint>, b: List<AccessPoint>): Double =
        minkowskiDistance(a, b, p = 2.0)
}

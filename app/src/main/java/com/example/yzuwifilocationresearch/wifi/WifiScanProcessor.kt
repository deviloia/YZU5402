package com.example.yzuwifilocationresearch.wifi

import com.example.yzuwifilocationresearch.model.WifiScanResult

/** 把多輪掃描結果依 BSSID 分組，準備交給 WifiStatistics 算統計值。 */
object WifiScanProcessor {

    /** rounds：多次呼叫 WifiScanner.scanOnce() 收集到的結果，一輪一個 List。 */
    fun groupByBssid(rounds: List<List<WifiScanResult>>): Map<String, List<WifiScanResult>> {
        return rounds
            .flatten()
            .groupBy { it.bssid }
    }
}

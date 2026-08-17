package com.example.yzuwifilocationresearch.model

data class AccessPoint(
    val ssid: String = "",
    val bssid: String = "",
    val frequency: Int = 0,
    val appearanceCount: Int = 0,
    val meanRssi: Double = 0.0,
    val minRssi: Int = 0,
    val maxRssi: Int = 0,
    val standardDeviation: Double = 0.0
)

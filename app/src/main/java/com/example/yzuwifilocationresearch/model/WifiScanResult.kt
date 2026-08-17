package com.example.yzuwifilocationresearch.model

data class WifiScanResult(
    val ssid: String = "",
    val bssid: String = "",
    val rssi: Int = 0,
    val frequency: Int = 0
)

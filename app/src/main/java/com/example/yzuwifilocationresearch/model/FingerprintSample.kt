package com.example.yzuwifilocationresearch.model

data class FingerprintSample(
    val documentId: String = "",
    val buildingId: String = "",
    val floorId: String = "",
    val positionName: String = "",
    val subPosition: String = "",
    val locationId: String = "",
    val note: String = "",
    val deviceBrand: String = "",
    val deviceModel: String = "",
    val androidVersion: String = "",
    val gpsLatitude: Double? = null,
    val gpsLongitude: Double? = null,
    val gpsAccuracy: Double? = null,
    val scanCount: Int = 0,
    val accessPoints: List<AccessPoint> = emptyList(),
    val createdAt: Long? = null
)

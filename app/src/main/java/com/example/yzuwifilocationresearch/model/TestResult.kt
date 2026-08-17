package com.example.yzuwifilocationresearch.model

data class TestResult(
    val documentId: String = "",
    val trueLocationId: String? = null,
    val deviceBrand: String = "",
    val deviceModel: String = "",
    val androidVersion: String = "",
    val gpsLatitude: Double? = null,
    val gpsLongitude: Double? = null,
    val gpsAccuracy: Double? = null,
    val gpsTimestamp: Long? = null,
    val predictedLocationId: String? = null,
    val predictedBuildingId: String? = null,
    val predictedFloorId: String? = null,
    val predictedPositionName: String? = null,
    val predictedSubPosition: String? = null,
    val knnK: Int = 0,
    val confidence: Double? = null,
    val apCount: Int = 0,
    val accessPoints: List<AccessPoint> = emptyList(),
    val gpsErrorMeters: Double? = null,
    val wifiErrorMeters: Double? = null,
    val createdAt: Long? = null
)

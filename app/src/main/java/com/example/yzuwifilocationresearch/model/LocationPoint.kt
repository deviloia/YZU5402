package com.example.yzuwifilocationresearch.model

data class LocationPoint(
    val locationId: String = "",
    val buildingId: String = "",
    val floorId: String = "",
    val positionName: String = "",
    val subPosition: String = "",
    val manualLatitude: Double? = null,
    val manualLongitude: Double? = null,
    val note: String = "",
    val updatedAt: Long? = null
)

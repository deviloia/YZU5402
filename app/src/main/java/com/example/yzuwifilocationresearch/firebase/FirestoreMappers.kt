package com.example.yzuwifilocationresearch.firebase

import com.example.yzuwifilocationresearch.model.AccessPoint
import com.example.yzuwifilocationresearch.model.FingerprintSample
import com.example.yzuwifilocationresearch.model.LocationPoint
import com.example.yzuwifilocationresearch.model.TestResult

object FirestoreMappers {
    fun locationToMap(location: LocationPoint): Map<String, Any?> = mapOf(
        "locationId" to location.locationId,
        "buildingId" to location.buildingId,
        "floorId" to location.floorId,
        "positionName" to location.positionName,
        "subPosition" to location.subPosition,
        "manualLatitude" to location.manualLatitude,
        "manualLongitude" to location.manualLongitude,
        "note" to location.note,
        "updatedAt" to location.updatedAt
    )

    fun mapToLocation(data: Map<String, Any?>): LocationPoint = LocationPoint(
        locationId = data.stringValue("locationId"),
        buildingId = data.stringValue("buildingId"),
        floorId = data.stringValue("floorId"),
        positionName = data.stringValue("positionName"),
        subPosition = data.stringValue("subPosition"),
        manualLatitude = data.doubleOrNull("manualLatitude"),
        manualLongitude = data.doubleOrNull("manualLongitude"),
        note = data.stringValue("note"),
        updatedAt = data.longOrNull("updatedAt")
    )

    fun fingerprintToMap(sample: FingerprintSample): Map<String, Any?> = mapOf(
        "documentId" to sample.documentId,
        "buildingId" to sample.buildingId,
        "floorId" to sample.floorId,
        "positionName" to sample.positionName,
        "subPosition" to sample.subPosition,
        "locationId" to sample.locationId,
        "note" to sample.note,
        "deviceBrand" to sample.deviceBrand,
        "deviceModel" to sample.deviceModel,
        "androidVersion" to sample.androidVersion,
        "gpsLatitude" to sample.gpsLatitude,
        "gpsLongitude" to sample.gpsLongitude,
        "gpsAccuracy" to sample.gpsAccuracy,
        "scanCount" to sample.scanCount,
        "accessPoints" to sample.accessPoints.map(::accessPointToMap),
        "createdAt" to sample.createdAt
    )

    fun mapToFingerprint(data: Map<String, Any?>): FingerprintSample = FingerprintSample(
        documentId = data.stringValue("documentId"),
        buildingId = data.stringValue("buildingId"),
        floorId = data.stringValue("floorId"),
        positionName = data.stringValue("positionName"),
        subPosition = data.stringValue("subPosition"),
        locationId = data.stringValue("locationId"),
        note = data.stringValue("note"),
        deviceBrand = data.stringValue("deviceBrand"),
        deviceModel = data.stringValue("deviceModel"),
        androidVersion = data.stringValue("androidVersion"),
        gpsLatitude = data.doubleOrNull("gpsLatitude"),
        gpsLongitude = data.doubleOrNull("gpsLongitude"),
        gpsAccuracy = data.doubleOrNull("gpsAccuracy"),
        scanCount = data.intValue("scanCount"),
        accessPoints = data.accessPointList("accessPoints"),
        createdAt = data.longOrNull("createdAt")
    )

    fun testResultToMap(result: TestResult): Map<String, Any?> = mapOf(
        "documentId" to result.documentId,
        "trueLocationId" to result.trueLocationId,
        "deviceBrand" to result.deviceBrand,
        "deviceModel" to result.deviceModel,
        "androidVersion" to result.androidVersion,
        "gpsLatitude" to result.gpsLatitude,
        "gpsLongitude" to result.gpsLongitude,
        "gpsAccuracy" to result.gpsAccuracy,
        "gpsTimestamp" to result.gpsTimestamp,
        "predictedLocationId" to result.predictedLocationId,
        "predictedBuildingId" to result.predictedBuildingId,
        "predictedFloorId" to result.predictedFloorId,
        "predictedPositionName" to result.predictedPositionName,
        "predictedSubPosition" to result.predictedSubPosition,
        "knnK" to result.knnK,
        "confidence" to result.confidence,
        "apCount" to result.apCount,
        "accessPoints" to result.accessPoints.map(::accessPointToMap),
        "gpsErrorMeters" to result.gpsErrorMeters,
        "wifiErrorMeters" to result.wifiErrorMeters,
        "createdAt" to result.createdAt
    )

    fun mapToTestResult(data: Map<String, Any?>): TestResult = TestResult(
        documentId = data.stringValue("documentId"),
        trueLocationId = data.stringOrNull("trueLocationId"),
        deviceBrand = data.stringValue("deviceBrand"),
        deviceModel = data.stringValue("deviceModel"),
        androidVersion = data.stringValue("androidVersion"),
        gpsLatitude = data.doubleOrNull("gpsLatitude"),
        gpsLongitude = data.doubleOrNull("gpsLongitude"),
        gpsAccuracy = data.doubleOrNull("gpsAccuracy"),
        gpsTimestamp = data.longOrNull("gpsTimestamp"),
        predictedLocationId = data.stringOrNull("predictedLocationId"),
        predictedBuildingId = data.stringOrNull("predictedBuildingId"),
        predictedFloorId = data.stringOrNull("predictedFloorId"),
        predictedPositionName = data.stringOrNull("predictedPositionName"),
        predictedSubPosition = data.stringOrNull("predictedSubPosition"),
        knnK = data.intValue("knnK"),
        confidence = data.doubleOrNull("confidence"),
        apCount = data.intValue("apCount"),
        accessPoints = data.accessPointList("accessPoints"),
        gpsErrorMeters = data.doubleOrNull("gpsErrorMeters"),
        wifiErrorMeters = data.doubleOrNull("wifiErrorMeters"),
        createdAt = data.longOrNull("createdAt")
    )

    private fun accessPointToMap(accessPoint: AccessPoint): Map<String, Any?> = mapOf(
        "ssid" to accessPoint.ssid,
        "bssid" to accessPoint.bssid,
        "frequency" to accessPoint.frequency,
        "appearanceCount" to accessPoint.appearanceCount,
        "meanRssi" to accessPoint.meanRssi,
        "minRssi" to accessPoint.minRssi,
        "maxRssi" to accessPoint.maxRssi,
        "standardDeviation" to accessPoint.standardDeviation
    )

    private fun mapToAccessPoint(data: Map<String, Any?>): AccessPoint = AccessPoint(
        ssid = data.stringValue("ssid"),
        bssid = data.stringValue("bssid"),
        frequency = data.intValue("frequency"),
        appearanceCount = data.intValue("appearanceCount"),
        meanRssi = data.doubleValue("meanRssi"),
        minRssi = data.intValue("minRssi"),
        maxRssi = data.intValue("maxRssi"),
        standardDeviation = data.doubleValue("standardDeviation")
    )

    private fun Map<String, Any?>.accessPointList(key: String): List<AccessPoint> {
        val rawList = this[key] as? List<*> ?: return emptyList()
        return rawList.mapNotNull { item ->
            @Suppress("UNCHECKED_CAST")
            (item as? Map<String, Any?>)?.let(::mapToAccessPoint)
        }
    }

    private fun Map<String, Any?>.stringValue(key: String): String = this[key] as? String ?: ""
    private fun Map<String, Any?>.stringOrNull(key: String): String? = this[key] as? String
    private fun Map<String, Any?>.intValue(key: String): Int = (this[key] as? Number)?.toInt() ?: 0
    private fun Map<String, Any?>.longOrNull(key: String): Long? = (this[key] as? Number)?.toLong()
    private fun Map<String, Any?>.doubleValue(key: String): Double = (this[key] as? Number)?.toDouble() ?: 0.0
    private fun Map<String, Any?>.doubleOrNull(key: String): Double? = (this[key] as? Number)?.toDouble()
}

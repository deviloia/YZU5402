package com.example.yzuwifilocationresearch.firebase

import com.example.yzuwifilocationresearch.model.AccessPoint
import com.example.yzuwifilocationresearch.model.FingerprintSample
import com.example.yzuwifilocationresearch.model.LocationPoint
import com.example.yzuwifilocationresearch.model.TestResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FirestoreMappersTest {
    @Test
    fun locationMapper_preservesUnsetGroundTruthAsNull() {
        val location = LocationPoint(
            locationId = "B5_4F_5402_WINDOW",
            manualLatitude = null,
            manualLongitude = null
        )

        val map = FirestoreMappers.locationToMap(location)
        val restored = FirestoreMappers.mapToLocation(map)

        assertTrue(map.containsKey("manualLatitude"))
        assertTrue(map.containsKey("manualLongitude"))
        assertNull(map["manualLatitude"])
        assertNull(map["manualLongitude"])
        assertNull(restored.manualLatitude)
        assertNull(restored.manualLongitude)
    }

    @Test
    fun fingerprintMapper_keepsNestedAccessPoints() {
        val sample = FingerprintSample(
            documentId = "fingerprint-001",
            locationId = "B5_4F_5402_WINDOW",
            accessPoints = listOf(
                AccessPoint(
                    ssid = "YZU-WLAN",
                    bssid = "00:11:22:33:44:55",
                    frequency = 2412,
                    appearanceCount = 10,
                    meanRssi = -45.5,
                    minRssi = -52,
                    maxRssi = -42,
                    standardDeviation = 2.4
                )
            )
        )

        val restored = FirestoreMappers.mapToFingerprint(FirestoreMappers.fingerprintToMap(sample))

        assertEquals(1, restored.accessPoints.size)
        assertEquals("00:11:22:33:44:55", restored.accessPoints.first().bssid)
        assertEquals(-45.5, restored.accessPoints.first().meanRssi, 0.001)
    }

    @Test
    fun testResultMapper_preservesFailedPredictionNulls() {
        val result = TestResult(
            documentId = "test-001",
            trueLocationId = null,
            predictedLocationId = null,
            predictedBuildingId = null,
            predictedFloorId = null,
            predictedPositionName = null,
            predictedSubPosition = null,
            confidence = null,
            gpsErrorMeters = null,
            wifiErrorMeters = null
        )

        val map = FirestoreMappers.testResultToMap(result)
        val restored = FirestoreMappers.mapToTestResult(map)

        assertNull(map["trueLocationId"])
        assertNull(map["predictedLocationId"])
        assertNull(map["predictedBuildingId"])
        assertNull(map["predictedFloorId"])
        assertNull(map["predictedPositionName"])
        assertNull(map["predictedSubPosition"])
        assertNull(map["confidence"])
        assertNull(map["gpsErrorMeters"])
        assertNull(map["wifiErrorMeters"])
        assertNull(restored.predictedLocationId)
        assertNull(restored.wifiErrorMeters)
    }
}

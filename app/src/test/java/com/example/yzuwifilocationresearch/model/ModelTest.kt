package com.example.yzuwifilocationresearch.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ModelTest {
    @Test
    fun locationPoint_canRepresentGroundTruthNotSetYet() {
        val location = LocationPoint(
            locationId = "B5_4F_5402_WINDOW",
            buildingId = "B5",
            floorId = "4F",
            positionName = "5402",
            subPosition = "WINDOW"
        )

        assertNull(location.manualLatitude)
        assertNull(location.manualLongitude)
    }

    @Test
    fun fingerprintSample_canContainMultipleAccessPoints() {
        val accessPoints = listOf(
            AccessPoint(
                ssid = "YZU-WLAN",
                bssid = "00:11:22:33:44:55",
                frequency = 2412,
                appearanceCount = 10,
                meanRssi = -45.5,
                minRssi = -51,
                maxRssi = -42,
                standardDeviation = 2.4
            ),
            AccessPoint(
                ssid = "YZU-Staff",
                bssid = "00:11:22:33:44:66",
                frequency = 5180,
                appearanceCount = 8,
                meanRssi = -62.0,
                minRssi = -70,
                maxRssi = -58,
                standardDeviation = 3.1
            )
        )

        val sample = FingerprintSample(
            documentId = "fingerprint-001",
            locationId = "B5_4F_5402_WINDOW",
            scanCount = 10,
            accessPoints = accessPoints
        )

        assertEquals(2, sample.accessPoints.size)
        assertEquals("00:11:22:33:44:55", sample.accessPoints.first().bssid)
    }

    @Test
    fun testResult_canBeCreatedBeforeGroundTruthErrorsAreCalculated() {
        val result = TestResult(
            documentId = "test-001",
            trueLocationId = null,
            predictedLocationId = "B5_4F_5402_WINDOW",
            gpsLatitude = 24.970123,
            gpsLongitude = 121.235678,
            gpsAccuracy = 18.6,
            gpsTimestamp = 1_787_019_000_000L,
            gpsErrorMeters = null,
            wifiErrorMeters = null
        )

        assertNull(result.trueLocationId)
        assertNull(result.gpsErrorMeters)
        assertNull(result.wifiErrorMeters)
    }

    @Test
    fun testResult_canRepresentFailedPredictionWithoutFakeStrings() {
        val result = TestResult(
            documentId = "test-failed-prediction",
            trueLocationId = null,
            apCount = 0,
            predictedLocationId = null,
            predictedBuildingId = null,
            predictedFloorId = null,
            predictedPositionName = null,
            predictedSubPosition = null,
            confidence = null,
            gpsErrorMeters = null,
            wifiErrorMeters = null
        )

        assertNull(result.predictedLocationId)
        assertNull(result.predictedBuildingId)
        assertNull(result.predictedFloorId)
        assertNull(result.predictedPositionName)
        assertNull(result.predictedSubPosition)
        assertNull(result.confidence)
    }

    @Test
    fun wifiScanResultAndAccessPointRepresentDifferentStages() {
        val rawScan = WifiScanResult(
            ssid = "YZU-WLAN",
            bssid = "00:11:22:33:44:55",
            rssi = -45,
            frequency = 2412
        )
        val fingerprintAp = AccessPoint(
            ssid = rawScan.ssid,
            bssid = rawScan.bssid,
            frequency = rawScan.frequency,
            appearanceCount = 10,
            meanRssi = -48.2,
            minRssi = -55,
            maxRssi = -43,
            standardDeviation = 3.5
        )

        assertEquals(-45, rawScan.rssi)
        assertEquals(10, fingerprintAp.appearanceCount)
        assertTrue(fingerprintAp.standardDeviation > 0.0)
    }
}

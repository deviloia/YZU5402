package com.example.yzuwifilocationresearch.positioning

import com.example.yzuwifilocationresearch.model.AccessPoint
import com.example.yzuwifilocationresearch.model.FingerprintSample
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class KnnLocatorTest {
    @Test
    fun rejectsSamplesWithTooFewSharedAccessPoints() {
        val current = aps("ap-a" to -40, "ap-b" to -50, "ap-c" to -60)
        val sample = sample(
            locationId = "五館_3F_5310_門口",
            floorId = "3F",
            positionName = "5310",
            subPosition = "門口",
            accessPoints = aps("ap-a" to -40, "ap-x" to -50, "ap-y" to -60)
        )

        val result = KnnLocator.locate(current, listOf(sample), k = 3)

        assertNull(result)
    }

    @Test
    fun missingAccessPointPenaltyPrefersFullerFingerprint() {
        val current = aps("ap-a" to -40, "ap-b" to -50, "ap-c" to -60)
        val partialButExact = sample(
            locationId = "五館_3F_5310_門口",
            floorId = "3F",
            positionName = "5310",
            subPosition = "門口",
            accessPoints = aps("ap-a" to -40, "ap-b" to -50, "ap-c" to -60)
        )
        val fullerRoom = sample(
            locationId = "五館_4F_5402_室內中",
            floorId = "4F",
            positionName = "5402",
            subPosition = "室內中",
            accessPoints = aps("ap-a" to -50, "ap-b" to -60, "ap-c" to -70)
        )
        val currentWithExtra = current + aps("ap-d" to -55, "ap-e" to -65)
        val fullerRoomWithExtras = fullerRoom.copy(
            accessPoints = fullerRoom.accessPoints + aps("ap-d" to -56, "ap-e" to -66)
        )

        val result = KnnLocator.locate(
            testAccessPoints = currentWithExtra,
            fingerprintSamples = listOf(partialButExact, fullerRoomWithExtras),
            k = 2
        )

        assertNotNull(result)
        assertEquals("5402", result?.predictedPositionName)
    }

    @Test
    fun weightedVoteCombinesSubPositionsInSameRoom() {
        val current = aps("ap-a" to -40, "ap-b" to -50, "ap-c" to -60)
        val room5402Door = sample("五館_4F_5402_門口", "4F", "5402", "門口", aps("ap-a" to -41, "ap-b" to -51, "ap-c" to -61))
        val room5402Inside = sample("五館_4F_5402_室內中", "4F", "5402", "室內中", aps("ap-a" to -42, "ap-b" to -52, "ap-c" to -62))
        val room5310 = sample("五館_3F_5310_門口", "3F", "5310", "門口", aps("ap-a" to -41, "ap-b" to -51, "ap-c" to -62))

        val result = KnnLocator.locate(current, listOf(room5310, room5402Door, room5402Inside), k = 3)
        val confidence = result?.let {
            ConfidenceCalculator.calculateConfidence(it.predictedLocationId, it.neighbors)
        }

        assertNotNull(result)
        assertEquals("5402", result?.predictedPositionName)
        assertTrue((confidence ?: 0.0) > 0.5)
    }

    private fun sample(
        locationId: String,
        floorId: String,
        positionName: String,
        subPosition: String,
        accessPoints: List<AccessPoint>
    ): FingerprintSample {
        return FingerprintSample(
            locationId = locationId,
            buildingId = "五館",
            floorId = floorId,
            positionName = positionName,
            subPosition = subPosition,
            accessPoints = accessPoints
        )
    }

    private fun aps(vararg values: Pair<String, Int>): List<AccessPoint> {
        return values.map { (bssid, rssi) ->
            AccessPoint(
                bssid = bssid,
                meanRssi = rssi.toDouble(),
                appearanceCount = 3
            )
        }
    }
}

package com.example.yzuwifilocationresearch.positioning

import com.example.yzuwifilocationresearch.model.AccessPoint

object DistanceCalculator {
    const val DEFAULT_MINIMUM_OVERLAP = 3
    const val DEFAULT_MISSING_AP_PENALTY = 35

    data class DistanceResult(
        val score: Double,
        val overlapCount: Int
    )

    /**
     * Uses the legacy score from the previous app.
     *
     * Samples with too few shared BSSIDs are rejected. Missing APs are penalized
     * so a fingerprint that matches only one strong AP does not beat a fuller
     * room fingerprint.
     */
    fun legacyScore(
        current: List<AccessPoint>,
        sample: List<AccessPoint>,
        minimumOverlap: Int = DEFAULT_MINIMUM_OVERLAP,
        missingApPenalty: Int = DEFAULT_MISSING_AP_PENALTY
    ): DistanceResult? {
        val currentByBssid = current.associateBy { it.bssid.normalizedBssid() }
        val sampleByBssid = sample.associateBy { it.bssid.normalizedBssid() }
        val commonBssids = currentByBssid.keys.intersect(sampleByBssid.keys)

        if (commonBssids.size < minimumOverlap) return null

        val missingPenaltyScore = (missingApPenalty * missingApPenalty).toDouble()
        val score = (currentByBssid.keys + sampleByBssid.keys).sumOf { bssid ->
            val currentAp = currentByBssid[bssid]
            val sampleAp = sampleByBssid[bssid]
            if (currentAp == null || sampleAp == null) {
                missingPenaltyScore
            } else {
                val diff = currentAp.meanRssi - sampleAp.meanRssi
                diff * diff
            }
        }

        return DistanceResult(score = score, overlapCount = commonBssids.size)
    }

    fun weightForScore(score: Double): Double = 1.0 / (score + 1.0)

    private fun String.normalizedBssid(): String = trim().lowercase()
}

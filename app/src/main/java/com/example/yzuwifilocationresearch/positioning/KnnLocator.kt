package com.example.yzuwifilocationresearch.positioning

import com.example.yzuwifilocationresearch.model.FingerprintSample

object KnnLocator {
    const val DEFAULT_K = 7

    data class Neighbor(
        val sample: FingerprintSample,
        val distance: Double,
        val overlapCount: Int,
        val weight: Double
    )

    data class LocateResult(
        val predictedLocationId: String,
        val predictedBuildingId: String,
        val predictedFloorId: String,
        val predictedPositionName: String,
        val predictedSubPosition: String,
        val neighbors: List<Neighbor>
    )

    fun locate(
        testAccessPoints: List<com.example.yzuwifilocationresearch.model.AccessPoint>,
        fingerprintSamples: List<FingerprintSample>,
        k: Int = DEFAULT_K
    ): LocateResult? {
        require(k > 0) { "k must be greater than 0." }
        if (testAccessPoints.isEmpty() || fingerprintSamples.isEmpty()) return null

        val neighbors = fingerprintSamples
            .mapNotNull { sample ->
                val distance = DistanceCalculator.legacyScore(
                    current = testAccessPoints,
                    sample = sample.accessPoints
                ) ?: return@mapNotNull null

                Neighbor(
                    sample = sample,
                    distance = distance.score,
                    overlapCount = distance.overlapCount,
                    weight = DistanceCalculator.weightForScore(distance.score)
                )
            }
            .sortedWith(
                compareBy<Neighbor> { it.distance }
                    .thenByDescending { it.overlapCount }
                    .thenBy { it.sample.buildingId }
                    .thenBy { it.sample.floorId }
                    .thenBy { it.sample.positionName }
                    .thenBy { it.sample.subPosition }
                    .thenBy { it.sample.locationId }
            )
            .take(k)

        if (neighbors.isEmpty()) return null

        val winningGroup = neighbors
            .groupBy { neighbor ->
                listOf(
                    neighbor.sample.buildingId,
                    neighbor.sample.floorId,
                    neighbor.sample.positionName
                ).joinToString("|")
            }
            .map { (_, group) -> KnnVoteGroup(group) }
            .sortedWith(
                compareByDescending<KnnVoteGroup> { it.totalWeight }
                    .thenBy { it.bestDistance }
                    .thenByDescending { it.totalOverlap }
                    .thenBy { it.representative.sample.buildingId }
                    .thenBy { it.representative.sample.floorId }
                    .thenBy { it.representative.sample.positionName }
            )
            .firstOrNull() ?: return null

        return winningGroup.representative.sample.let { sample ->
            LocateResult(
                predictedLocationId = sample.locationId,
                predictedBuildingId = sample.buildingId,
                predictedFloorId = sample.floorId,
                predictedPositionName = sample.positionName,
                predictedSubPosition = sample.subPosition,
                neighbors = neighbors
            )
        }
    }
}

private data class KnnVoteGroup(
    val neighbors: List<KnnLocator.Neighbor>
) {
    val totalWeight: Double = neighbors.sumOf { it.weight }
    val bestDistance: Double = neighbors.minOf { it.distance }
    val totalOverlap: Int = neighbors.sumOf { it.overlapCount }
    val representative: KnnLocator.Neighbor = neighbors.minWith(
        compareBy<KnnLocator.Neighbor> { it.distance }
            .thenByDescending { it.overlapCount }
            .thenBy { it.sample.subPosition }
            .thenBy { it.sample.locationId }
    )
}

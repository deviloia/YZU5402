package com.example.yzuwifilocationresearch.positioning

object ConfidenceCalculator {
    fun calculateConfidence(
        winningLocationId: String,
        neighbors: List<KnnLocator.Neighbor>
    ): Double {
        if (neighbors.isEmpty()) return 0.0

        val winningSample = neighbors.firstOrNull { it.sample.locationId == winningLocationId }?.sample
            ?: return 0.0
        val totalWeight = neighbors.sumOf { it.weight }
        if (totalWeight <= 0.0) return 0.0

        val winningWeight = neighbors
            .filter { neighbor ->
                neighbor.sample.buildingId == winningSample.buildingId &&
                    neighbor.sample.floorId == winningSample.floorId &&
                    neighbor.sample.positionName == winningSample.positionName
            }
            .sumOf { it.weight }

        return winningWeight / totalWeight
    }
}

package com.example.yzuwifilocationresearch.positioning

/** 算 KNN 定位結果的信心值（相對信心，不是校準過的真機率）。 */
object ConfidenceCalculator {

    /**
     * winningLocationId：KnnLocator.locate(...) 選出的最終預測位置。
     * neighbors：同一次 locate(...) 回傳的鄰居清單（含各自距離）。
     * 沒有任何鄰居時回傳 0.0，代表完全沒有依據，不是「有信心但剛好算出0」。
     */
    fun calculateConfidence(
        winningLocationId: String,
        neighbors: List<KnnLocator.Neighbor>
    ): Double {
        if (neighbors.isEmpty()) return 0.0

        // 跟 KnnLocator 用同一套 WKNN 權重公式：Wi = 1 / (Di + 1)。
        val weightByNeighbor = neighbors.associateWith { 1.0 / (it.distance + 1.0) }

        // 全部 K 個鄰居的權重總和，當作分母。
        val totalWeight = weightByNeighbor.values.sum()
        if (totalWeight == 0.0) return 0.0

        // 只挑出屬於「勝出位置」的鄰居，把它們的權重加總，當作分子。
        val winningWeight = neighbors
            .filter { it.sample.locationId == winningLocationId }
            .sumOf { weightByNeighbor.getValue(it) }

        // Confidence = 勝出位置權重 / 全部鄰居權重，範圍是 0.0 ~ 1.0。
        return winningWeight / totalWeight
    }
}

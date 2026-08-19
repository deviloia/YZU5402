package com.example.yzuwifilocationresearch.positioning

import com.example.yzuwifilocationresearch.model.AccessPoint
import com.example.yzuwifilocationresearch.model.FingerprintSample

/** KNN/WKNN 定位：拿測試指紋跟資料庫每一筆算距離，取最近 K 個，加權投票決定位置。 */
object KnnLocator {

    /** 一個鄰居：資料庫裡的一筆指紋樣本，加上跟測試指紋的距離。 */
    data class Neighbor(
        val sample: FingerprintSample,
        val distance: Double
    )

    /** 定位結果：最終預測的位置欄位，加上用來算 Confidence 的鄰居清單。 */
    data class LocateResult(
        val predictedLocationId: String,
        val predictedBuildingId: String,
        val predictedFloorId: String,
        val predictedPositionName: String,
        val predictedSubPosition: String,
        val neighbors: List<Neighbor>
    )

    /**
     * testAccessPoints：使用者這次測試掃到的統計結果。
     * fingerprintSamples：資料庫裡全部的指紋樣本（FingerprintRepository.getAllFingerprints()）。
     * k：取最近幾個鄰居參與投票。
     * 資料庫是空的、或沒有任何鄰居時回傳 null，不假裝有預測結果。
     */
    fun locate(
        testAccessPoints: List<AccessPoint>,
        fingerprintSamples: List<FingerprintSample>,
        k: Int
    ): LocateResult? {
        if (fingerprintSamples.isEmpty()) return null

        // 對資料庫每一筆算一次歐氏距離，由近到遠排序，取前 K 個。
        val neighbors = fingerprintSamples
            .map { sample -> Neighbor(sample, DistanceCalculator.euclideanDistance(testAccessPoints, sample.accessPoints)) }
            .sortedBy { it.distance }
            .take(k)

        if (neighbors.isEmpty()) return null

        // WKNN 權重：Wi = 1 / (Di + 1)，距離越近權重越高。
        val weightByNeighbor = neighbors.associateWith { 1.0 / (it.distance + 1.0) }

        // 依 locationId 分組，同一個位置的權重加總，權重最高的位置勝出。
        val weightByLocationId = neighbors
            .groupBy { it.sample.locationId }
            .mapValues { (_, group) -> group.sumOf { weightByNeighbor.getValue(it) } }

        val winningLocationId = weightByLocationId.maxByOrNull { it.value }?.key ?: return null
        val winningSample = neighbors.first { it.sample.locationId == winningLocationId }.sample

        return LocateResult(
            predictedLocationId = winningLocationId,
            predictedBuildingId = winningSample.buildingId,
            predictedFloorId = winningSample.floorId,
            predictedPositionName = winningSample.positionName,
            predictedSubPosition = winningSample.subPosition,
            neighbors = neighbors
        )
    }
}

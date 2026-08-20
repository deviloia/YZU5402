package com.example.yzuwifilocationresearch.model

/**
 * 一棟建築物的靜態地理範圍（多邊形頂點座標，依順序連成一圈）。
 * 用於 GPS 座標飄移時，仍能判斷大概位於哪一棟建築物。
 */
data class BuildingBoundary(
    val buildingId: String,
    val buildingName: String,
    val polygon: List<Pair<Double, Double>>
)
//給予五館BuildingLookup單位
package com.example.yzuwifilocationresearch.map

import com.example.yzuwifilocationresearch.model.BuildingBoundary

/**
 * 靜態建築物範圍表 + Point-in-Polygon 判斷。
 * 座標目前是佔位資料（TODO），需要替換成每棟建築物實際測量的角落經緯度。
 */
object BuildingLookup {

    // 座標來自 Google Maps 手動標記各角落（元智大學五館）。
    val buildings: List<BuildingBoundary> = listOf(
        BuildingBoundary(
            buildingId = "B5",
            buildingName = "五館",
            polygon = listOf(
                24.970460 to 121.268323,
                24.970460 to 121.268462,
                24.970217 to 121.268466,
                24.970215 to 121.268626,
                24.969732 to 121.268631,
                24.969734 to 121.268519,
                24.969797 to 121.268521,
                24.969797 to 121.268126,
                24.970025 to 121.268120,
                24.970024 to 121.267972,
                24.970459 to 121.267964,
                24.970472 to 121.268245
            )
        )
    )

    /** 回傳座標落在哪一棟建築物範圍內，都不在裡面則回傳 null（不假裝一定有答案）。 */
    fun findBuildingContaining(latitude: Double, longitude: Double): BuildingBoundary? {
        return buildings.firstOrNull { building -> isPointInPolygon(latitude, longitude, building.polygon) }
    }

    /** Ray casting（射線法）：從這個點往右畫一條射線，算跟多邊形邊界交叉奇數次代表在裡面。 */
    private fun isPointInPolygon(latitude: Double, longitude: Double, polygon: List<Pair<Double, Double>>): Boolean {
        if (polygon.size < 3) return false

        var isInside = false
        var j = polygon.size - 1
        for (i in polygon.indices) {
            val (latI, lngI) = polygon[i]
            val (latJ, lngJ) = polygon[j]

            val crossesRay = (latI > latitude) != (latJ > latitude)
            if (crossesRay) {
                val intersectLongitude = lngI + (latitude - latI) / (latJ - latI) * (lngJ - lngI)
                if (longitude < intersectLongitude) {
                    isInside = !isInside
                }
            }
            j = i
        }
        return isInside
    }
}

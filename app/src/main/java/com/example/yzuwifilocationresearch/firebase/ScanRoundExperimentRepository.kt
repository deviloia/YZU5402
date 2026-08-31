package com.example.yzuwifilocationresearch.firebase

import com.example.yzuwifilocationresearch.model.WifiScanResult
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * 用於「掃描輪數N分析」實驗：保留每一輪的原始RSSI值（不做統計），
 * 與正式的fingerprintSamples分開存放，離線分析時比較用前N輪算出
 * 的平均值與全部輪數算出的基準值之誤差，藉此驗證N=10的選定依據。
 */
class ScanRoundExperimentRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val experiments = firestore.collection(FirestoreCollections.SCAN_ROUND_EXPERIMENTS)

    suspend fun addExperiment(
        locationId: String,
        deviceBrand: String,
        deviceModel: String,
        rounds: List<List<WifiScanResult>>
    ): String {
        val document = experiments.document()
        val payload = mapOf(
            "locationId" to locationId,
            "deviceBrand" to deviceBrand,
            "deviceModel" to deviceModel,
            "roundCount" to rounds.size,
            "rounds" to rounds.mapIndexed { index, scanResults ->
                mapOf(
                    "roundNumber" to index + 1,
                    "scanResults" to scanResults.map { result ->
                        mapOf(
                            "ssid" to result.ssid,
                            "bssid" to result.bssid,
                            "rssi" to result.rssi,
                            "frequency" to result.frequency
                        )
                    }
                )
            },
            "createdAt" to System.currentTimeMillis()
        )
        document.set(payload).await()
        return document.id
    }
}

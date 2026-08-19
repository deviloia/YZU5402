package com.example.yzuwifilocationresearch.ui.result

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.yzuwifilocationresearch.firebase.TestResultRepository
import com.example.yzuwifilocationresearch.model.TestResult
import com.example.yzuwifilocationresearch.navigation.AppDestination
import com.example.yzuwifilocationresearch.ui.components.AppScaffold
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun ResultScreen(
    onBackClick: () -> Unit,
    onHomeClick: () -> Unit,
    onCollectClick: () -> Unit,
    onScanClick: () -> Unit,
    onHistoryClick: () -> Unit
) {
    // 顯示 testResults 裡最新的一筆（依 createdAt 排序），還沒讀到之前是 null。
    var latestResult by remember { mutableStateOf<TestResult?>(null) }

    LaunchedEffect(Unit) {
        val results = TestResultRepository().getAllTestResults()
        latestResult = results.maxByOrNull { it.createdAt ?: 0L }
    }

    AppScaffold(
        title = "掃描 / 定位結果",
        selectedDestination = AppDestination.Result,
        onBackClick = onBackClick,
        onHomeClick = onHomeClick,
        onCollectClick = onCollectClick,
        onScanClick = onScanClick,
        onHistoryClick = onHistoryClick
    ) { modifier ->
        val result = latestResult
        ResultContentRedesign(
            predictedLocationName = result?.predictedPositionName,
            predictedLocationId = result?.predictedLocationId,
            // confidence 存的是 0.0~1.0，畫面顯示要轉成百分比整數。
            confidencePercent = result?.confidence?.let { (it * 100).roundToInt() },
            k = result?.knnK,
            apCount = result?.apCount,
            // BuildingLookup 還沒接進來，暫時沒有「GPS 所在建築」的顯示文字。
            gpsAreaName = null,
            gpsLatitude = result?.gpsLatitude,
            gpsLongitude = result?.gpsLongitude,
            gpsAccuracyMeters = result?.gpsAccuracy,
            gpsUpdatedAt = result?.gpsTimestamp?.let(::formatTimestamp),
            wifiUpdatedAt = result?.createdAt?.let(::formatTimestamp),
            gpsErrorMeters = result?.gpsErrorMeters,
            wifiErrorMeters = result?.wifiErrorMeters,
            // 有 trueLocationId 才代表這筆有經過人工 Ground Truth 校正。
            isCalibrated = result?.trueLocationId != null,
            apList = result?.accessPoints.orEmpty().map { ap ->
                ApRowData(ssid = ap.ssid, bssid = ap.bssid, rssi = ap.meanRssi.roundToInt())
            },
            modifier = modifier
        )
    }
}

private fun formatTimestamp(epochMillis: Long): String {
    val formatter = SimpleDateFormat("MM/dd HH:mm", Locale.TAIWAN)
    return formatter.format(epochMillis)
}

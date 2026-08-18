package com.example.yzuwifilocationresearch.ui.result

import androidx.compose.runtime.Composable
import com.example.yzuwifilocationresearch.navigation.AppDestination
import com.example.yzuwifilocationresearch.ui.components.AppScaffold

@Composable
fun ResultScreen(
    onBackClick: () -> Unit,
    onHomeClick: () -> Unit,
    onCollectClick: () -> Unit,
    onScanClick: () -> Unit,
    onHistoryClick: () -> Unit
) {
    val mockAps = listOf(
        ApRowData("YZU-WLAN", "00:11:22:33:44:55", -45),
        ApRowData("YZU-Staff", "00:11:22:33:44:66", -58),
        ApRowData("YZU-Guest", "00:11:22:33:44:77", -63),
        ApRowData("YZU-Lab", "00:11:22:33:44:88", -69)
    )

    AppScaffold(
        title = "掃描 / 定位結果",
        selectedDestination = AppDestination.Result,
        onBackClick = onBackClick,
        onHomeClick = onHomeClick,
        onCollectClick = onCollectClick,
        onScanClick = onScanClick,
        onHistoryClick = onHistoryClick
    ) { modifier ->
        ResultContentRedesign(
            predictedLocationName = "五館 4F 5402 窗戶旁",
            predictedLocationId = "B5-4F-5402",
            confidencePercent = 82,
            k = 3,
            apCount = 18,
            gpsAreaName = "五館 4F 附近",
            gpsLatitude = 24.970123,
            gpsLongitude = 121.235678,
            gpsAccuracyMeters = 18.6,
            gpsUpdatedAt = "剛剛",
            wifiUpdatedAt = "剛剛",
            gpsErrorMeters = 18.2,
            wifiErrorMeters = 6.4,
            isCalibrated = false,
            apList = mockAps,
            modifier = modifier
        )
    }
}

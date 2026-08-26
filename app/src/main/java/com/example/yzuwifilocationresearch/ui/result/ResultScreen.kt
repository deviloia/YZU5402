package com.example.yzuwifilocationresearch.ui.result

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.yzuwifilocationresearch.firebase.LocationRepository
import com.example.yzuwifilocationresearch.firebase.TestResultRepository
import com.example.yzuwifilocationresearch.map.BuildingLookup
import com.example.yzuwifilocationresearch.model.LocationPoint
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
    var latestResult by remember { mutableStateOf<TestResult?>(null) }
    var groundTruthLocation by remember { mutableStateOf<LocationPoint?>(null) }

    LaunchedEffect(Unit) {
        val results = TestResultRepository().getAllTestResults()
        val result = results.maxByOrNull { it.createdAt ?: 0L }
        latestResult = result

        val locationId = result?.trueLocationId ?: result?.predictedLocationId
        groundTruthLocation = locationId
            ?.takeIf { it.isNotBlank() }
            ?.let { LocationRepository().getLocation(it) }
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
        val gpsAreaName = if (result?.gpsLatitude != null && result.gpsLongitude != null) {
            BuildingLookup.findBuildingContaining(
                latitude = result.gpsLatitude,
                longitude = result.gpsLongitude
            )?.buildingName
        } else {
            null
        }

        ResultContentRedesign(
            predictedLocationName = result?.predictedPositionName,
            predictedLocationId = result?.predictedLocationId,
            confidencePercent = result?.confidence?.let { (it * 100).roundToInt() },
            k = result?.knnK,
            apCount = result?.apCount,
            gpsAreaName = gpsAreaName,
            gpsLatitude = result?.gpsLatitude,
            gpsLongitude = result?.gpsLongitude,
            groundTruthLatitude = groundTruthLocation?.manualLatitude,
            groundTruthLongitude = groundTruthLocation?.manualLongitude,
            gpsAccuracyMeters = result?.gpsAccuracy,
            gpsUpdatedAt = result?.gpsTimestamp?.let(::formatTimestamp),
            wifiUpdatedAt = result?.createdAt?.let(::formatTimestamp),
            gpsErrorMeters = result?.gpsErrorMeters,
            wifiErrorMeters = result?.wifiErrorMeters,
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

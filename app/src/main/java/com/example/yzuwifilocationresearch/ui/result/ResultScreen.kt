package com.example.yzuwifilocationresearch.ui.result

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.example.yzuwifilocationresearch.firebase.LocationRepository
import com.example.yzuwifilocationresearch.firebase.TestResultRepository
import com.example.yzuwifilocationresearch.gps.GpsLocationManager
import com.example.yzuwifilocationresearch.gps.GpsReading
import com.example.yzuwifilocationresearch.map.BuildingLookup
import com.example.yzuwifilocationresearch.model.LocationPoint
import com.example.yzuwifilocationresearch.model.TestResult
import com.example.yzuwifilocationresearch.navigation.AppDestination
import com.example.yzuwifilocationresearch.ui.components.AppScaffold
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
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
    val context = LocalContext.current
    val gpsLocationManager = remember { GpsLocationManager(context) }
    var latestResult by remember { mutableStateOf<TestResult?>(null) }
    var groundTruthLocation by remember { mutableStateOf<LocationPoint?>(null) }
    var liveGpsReading by remember { mutableStateOf<GpsReading?>(null) }

    fun updateLiveGps(reading: GpsReading?) {
        if (reading != null) {
            liveGpsReading = reading
        }
    }

    LaunchedEffect(Unit) {
        val results = TestResultRepository().getAllTestResults()
        val result = results.maxByOrNull { it.createdAt ?: 0L }
        latestResult = result

        if (result?.gpsLatitude != null && result.gpsLongitude != null) {
            liveGpsReading = GpsReading(
                latitude = result.gpsLatitude,
                longitude = result.gpsLongitude,
                accuracy = result.gpsAccuracy?.toFloat() ?: 0f,
                timestamp = result.gpsTimestamp ?: result.createdAt ?: System.currentTimeMillis()
            )
        }

        val locationId = result?.trueLocationId ?: result?.predictedLocationId
        groundTruthLocation = locationId
            ?.takeIf { it.isNotBlank() }
            ?.let { LocationRepository().getLocation(it) }
    }

    DisposableEffect(Unit) {
        val stopUpdates = gpsLocationManager.startLocationUpdates(::updateLiveGps)
        onDispose {
            stopUpdates()
        }
    }

    LaunchedEffect(Unit) {
        while (isActive) {
            updateLiveGps(gpsLocationManager.getCurrentLocation())
            delay(1_000L)
        }
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
        val gpsReading = liveGpsReading
        val gpsAreaName = if (gpsReading != null) {
            BuildingLookup.findBuildingContaining(
                latitude = gpsReading.latitude,
                longitude = gpsReading.longitude
            )?.buildingName ?: "人未在五館內"
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
            gpsLatitude = gpsReading?.latitude,
            gpsLongitude = gpsReading?.longitude,
            groundTruthLatitude = groundTruthLocation?.manualLatitude,
            groundTruthLongitude = groundTruthLocation?.manualLongitude,
            gpsAccuracyMeters = gpsReading?.accuracy?.toDouble(),
            gpsUpdatedAt = gpsReading?.timestamp?.let(::formatTimestamp),
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

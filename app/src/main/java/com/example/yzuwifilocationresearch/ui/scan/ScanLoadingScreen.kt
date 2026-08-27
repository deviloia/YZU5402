package com.example.yzuwifilocationresearch.ui.scan

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.yzuwifilocationresearch.device.DeviceInfoProvider
import com.example.yzuwifilocationresearch.firebase.FingerprintRepository
import com.example.yzuwifilocationresearch.firebase.TestResultRepository
import com.example.yzuwifilocationresearch.gps.GpsLocationManager
import com.example.yzuwifilocationresearch.gps.GpsReading
import com.example.yzuwifilocationresearch.model.TestResult
import com.example.yzuwifilocationresearch.model.WifiScanResult
import com.example.yzuwifilocationresearch.navigation.AppDestination
import com.example.yzuwifilocationresearch.positioning.ConfidenceCalculator
import com.example.yzuwifilocationresearch.positioning.KnnLocator
import com.example.yzuwifilocationresearch.ui.components.ActionBlue
import com.example.yzuwifilocationresearch.ui.components.AppCard
import com.example.yzuwifilocationresearch.ui.components.AppScaffold
import com.example.yzuwifilocationresearch.ui.components.MapPlaceholderCard
import com.example.yzuwifilocationresearch.ui.components.TextMuted
import com.example.yzuwifilocationresearch.ui.components.TextStrong
import com.example.yzuwifilocationresearch.ui.components.WebMapCard
import com.example.yzuwifilocationresearch.wifi.WifiScanProcessor
import com.example.yzuwifilocationresearch.wifi.WifiScanner
import com.example.yzuwifilocationresearch.wifi.WifiStatistics
import kotlinx.coroutines.launch

private const val TEST_SCAN_ROUNDS = 5
private const val KNN_K = 3

@Composable
fun ScanLoadingScreen(
    onBackClick: () -> Unit,
    onFinished: () -> Unit,
    onHomeClick: () -> Unit,
    onCollectClick: () -> Unit,
    onScanClick: () -> Unit,
    onHistoryClick: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val deviceInfo = remember { DeviceInfoProvider.getDeviceInfo() }
    val gpsLocationManager = remember { GpsLocationManager(context) }

    var stepText by remember { mutableStateOf("準備掃描...") }
    var currentLatitude by remember { mutableStateOf<Double?>(null) }
    var currentLongitude by remember { mutableStateOf<Double?>(null) }
    var latestGpsReading by remember { mutableStateOf<GpsReading?>(null) }
    var hasLocationPermission by remember { mutableStateOf(false) }

    fun runTestFlow() {
        coroutineScope.launch {
            stepText = "取得 GPS 位置..."
            val firstGpsReading = gpsLocationManager.getCurrentLocation()
            if (firstGpsReading != null) {
                latestGpsReading = firstGpsReading
                currentLatitude = firstGpsReading.latitude
                currentLongitude = firstGpsReading.longitude
            }

            val scanner = WifiScanner(context)
            val rounds = mutableListOf<List<WifiScanResult>>()
            repeat(TEST_SCAN_ROUNDS) { index ->
                stepText = "Wi-Fi 掃描中... ${index + 1}/$TEST_SCAN_ROUNDS"
                rounds += scanner.scanOnce(
                    onThrottled = {
                        stepText = "Wi-Fi 掃描節流中，等待第 ${index + 1} 次掃描..."
                    }
                )
            }

            stepText = "計算 Wi-Fi 指紋定位..."
            val grouped = WifiScanProcessor.groupByBssid(rounds)
            val testAccessPoints = WifiStatistics.computeAccessPoints(grouped)
            val allSamples = FingerprintRepository().getAllFingerprints()
            val locateResult = KnnLocator.locate(testAccessPoints, allSamples, k = KNN_K)
            val confidence = locateResult?.let {
                ConfidenceCalculator.calculateConfidence(it.predictedLocationId, it.neighbors)
            }

            stepText = "儲存定位測試結果..."
            val resultGpsReading = latestGpsReading ?: firstGpsReading
            val testResult = TestResult(
                trueLocationId = null,
                deviceBrand = deviceInfo.deviceBrand,
                deviceModel = deviceInfo.deviceModel,
                androidVersion = deviceInfo.androidVersion,
                gpsLatitude = resultGpsReading?.latitude,
                gpsLongitude = resultGpsReading?.longitude,
                gpsAccuracy = resultGpsReading?.accuracy?.toDouble(),
                gpsTimestamp = resultGpsReading?.timestamp,
                predictedLocationId = locateResult?.predictedLocationId,
                predictedBuildingId = locateResult?.predictedBuildingId,
                predictedFloorId = locateResult?.predictedFloorId,
                predictedPositionName = locateResult?.predictedPositionName,
                predictedSubPosition = locateResult?.predictedSubPosition,
                knnK = KNN_K,
                confidence = confidence,
                apCount = testAccessPoints.size,
                accessPoints = testAccessPoints,
                gpsErrorMeters = null,
                wifiErrorMeters = null,
                createdAt = System.currentTimeMillis()
            )
            TestResultRepository().addTestResult(testResult)

            onFinished()
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasLocationPermission = isGranted
        runTestFlow()
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    DisposableEffect(hasLocationPermission) {
        if (!hasLocationPermission) {
            onDispose {}
        } else {
            val stopUpdates = gpsLocationManager.startLocationUpdates { reading ->
                latestGpsReading = reading
                currentLatitude = reading.latitude
                currentLongitude = reading.longitude
            }
            onDispose {
                stopUpdates()
            }
        }
    }

    AppScaffold(
        title = "Wi-Fi 掃描中",
        selectedDestination = AppDestination.ScanLoading,
        onBackClick = onBackClick,
        onHomeClick = onHomeClick,
        onCollectClick = onCollectClick,
        onScanClick = onScanClick,
        onHistoryClick = onHistoryClick
    ) { modifier ->
        LazyColumn(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp),
            contentPadding = PaddingValues(start = 20.dp, top = 24.dp, end = 20.dp, bottom = 32.dp),
            modifier = modifier.fillMaxSize()
        ) {
            item {
                CircularProgressIndicator(color = ActionBlue)
            }
            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("正在定位...", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = TextStrong)
                    Text(
                        "掃描期間 GPS 地圖會持續更新目前位置",
                        color = TextMuted,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
            item {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = ActionBlue)
            }
            item {
                AppCard {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("目前狀態", fontWeight = FontWeight.SemiBold, color = TextStrong)
                        Text(stepText, fontSize = 13.sp, color = TextMuted)
                    }
                }
            }
            item {
                val latitude = currentLatitude
                val longitude = currentLongitude
                if (latitude != null && longitude != null) {
                    WebMapCard(latitude = latitude, longitude = longitude)
                } else {
                    MapPlaceholderCard(
                        title = "等待 GPS 位置",
                        subtitle = "取得定位後會在 App 內顯示即時地圖"
                    )
                }
            }
        }
    }
}

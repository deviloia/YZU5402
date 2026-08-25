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

// 依先前討論：測試模式抓房間級精度即可，掃描次數比採集模式（10次）少，換取速度。
private const val TEST_SCAN_ROUNDS = 4
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

    // 顯示目前跑到哪一步，取代原本寫死的 Mock 文字。
    var stepText by remember { mutableStateOf("準備中…") }
    // 拿到 GPS 座標後才顯示地圖，用來即時顯示目前定位到的位置。
    var currentLatitude by remember { mutableStateOf<Double?>(null) }
    var currentLongitude by remember { mutableStateOf<Double?>(null) }

    // 整條測試流程：GPS → WiFi 掃描 N 次 → 讀指紋資料庫 → KNN → Confidence → 寫入 testResults。
    fun runTestFlow() {
        coroutineScope.launch {
            stepText = "取得 GPS 位置…"
            val gpsReading = GpsLocationManager(context).getCurrentLocation()
            currentLatitude = gpsReading?.latitude
            currentLongitude = gpsReading?.longitude

            val scanner = WifiScanner(context)
            val rounds = mutableListOf<List<WifiScanResult>>()
            repeat(TEST_SCAN_ROUNDS) { index ->
                stepText = "Wi-Fi 掃描中… ${index + 1}/$TEST_SCAN_ROUNDS"
                rounds += scanner.scanOnce(
                    onThrottled = { stepText = "系統限制掃描頻率，等待中…（第${index + 1}輪）" }
                )
            }

            stepText = "比對指紋資料庫…"
            val grouped = WifiScanProcessor.groupByBssid(rounds)
            val testAccessPoints = WifiStatistics.computeAccessPoints(grouped)

            // Test Mode 只能讀 fingerprintSamples，絕不能寫入它。
            val allSamples = FingerprintRepository().getAllFingerprints()
            val locateResult = KnnLocator.locate(testAccessPoints, allSamples, k = KNN_K)
            val confidence = locateResult?.let {
                ConfidenceCalculator.calculateConfidence(it.predictedLocationId, it.neighbors)
            }

            stepText = "寫入測試結果…"
            val testResult = TestResult(
                // 還沒有「選擇既有位置」/Ground Truth 確認功能，先誠實留 null，不假裝知道正確答案。
                trueLocationId = null,
                deviceBrand = deviceInfo.deviceBrand,
                deviceModel = deviceInfo.deviceModel,
                androidVersion = deviceInfo.androidVersion,
                gpsLatitude = gpsReading?.latitude,
                gpsLongitude = gpsReading?.longitude,
                gpsAccuracy = gpsReading?.accuracy?.toDouble(),
                gpsTimestamp = gpsReading?.timestamp,
                predictedLocationId = locateResult?.predictedLocationId,
                predictedBuildingId = locateResult?.predictedBuildingId,
                predictedFloorId = locateResult?.predictedFloorId,
                predictedPositionName = locateResult?.predictedPositionName,
                predictedSubPosition = locateResult?.predictedSubPosition,
                knnK = KNN_K,
                confidence = confidence,
                apCount = testAccessPoints.size,
                accessPoints = testAccessPoints,
                // 沒有 Ground Truth 可比對，誤差先留 null，不能塞 0.0。
                gpsErrorMeters = null,
                wifiErrorMeters = null,
                createdAt = System.currentTimeMillis()
            )
            // 透過 Repository 寫入，畫面不直接碰 Firestore API。
            TestResultRepository().addTestResult(testResult)

            onFinished()
        }
    }

    // 進畫面時先跳權限請求（若已同意過不會再跳窗），拿到回應後立刻開始整條流程，
    // 不論同意與否都會繼續：GpsLocationManager/WifiScanner 沒權限時各自回傳 null/空清單。
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) {
        runTestFlow()
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    AppScaffold(
        title = "Wi-Fi 指紋掃描中",
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
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("正在定位...", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = TextStrong)
                    Text(
                        "請稍候，完成後將顯示定位結果",
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
                        title = "即時位置",
                        subtitle = "定位完成後會在這裡顯示地圖"
                    )
                }
            }
        }
    }
}

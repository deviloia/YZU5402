package com.example.yzuwifilocationresearch.ui.collect

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.yzuwifilocationresearch.device.DeviceInfoProvider
import com.example.yzuwifilocationresearch.firebase.FingerprintRepository
import com.example.yzuwifilocationresearch.gps.GpsLocationManager
import com.example.yzuwifilocationresearch.gps.GpsReading
import com.example.yzuwifilocationresearch.model.FingerprintSample
import com.example.yzuwifilocationresearch.model.WifiScanResult
import com.example.yzuwifilocationresearch.navigation.AppDestination
import com.example.yzuwifilocationresearch.wifi.WifiScanProcessor
import com.example.yzuwifilocationresearch.wifi.WifiScanner
import com.example.yzuwifilocationresearch.wifi.WifiStatistics
import com.example.yzuwifilocationresearch.ui.components.ActionBlue
import com.example.yzuwifilocationresearch.ui.components.AppCard
import com.example.yzuwifilocationresearch.ui.components.AppScaffold
import com.example.yzuwifilocationresearch.ui.components.CollectGreen
import com.example.yzuwifilocationresearch.ui.components.GreenTint
import com.example.yzuwifilocationresearch.ui.components.MapPlaceholderCard
import com.example.yzuwifilocationresearch.ui.components.SectionLabel
import com.example.yzuwifilocationresearch.ui.components.StatusPill
import com.example.yzuwifilocationresearch.ui.components.TextMuted
import com.example.yzuwifilocationresearch.ui.components.TextStrong
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CollectScreen(
    onBackClick: () -> Unit,
    onHomeClick: () -> Unit,
    onCollectClick: () -> Unit,
    onScanClick: () -> Unit,
    onHistoryClick: () -> Unit
) {
    var collected by remember { mutableStateOf(false) }
    val deviceInfo = remember { DeviceInfoProvider.getDeviceInfo() }
    // 採集進度文字（第 N/10 次掃描），採集期間顯示，完成或未開始時是 null。
    var scanProgress by remember { mutableStateOf<String?>(null) }

    // 依規則預設值，採集模式連續掃描 10 次。
    val collectScanRounds = 10

    // GpsLocationManager 需要 Context 才能建立。
    val context = LocalContext.current
    // 讓按鈕點擊（非 suspend fun）也能啟動協程呼叫 getCurrentLocation()。
    val coroutineScope = rememberCoroutineScope()
    // 定位結果，拿到座標前是 null。
    var gpsReading by remember { mutableStateOf<GpsReading?>(null) }
    // 顯示給使用者看的狀態文字（定位中／失敗原因）。
    var gpsStatus by remember { mutableStateOf<String?>(null) }

    // 實際呼叫 GpsLocationManager 抓一次座標，成功/失敗都更新狀態。
    fun fetchGpsLocation() {
        gpsStatus = "定位中…"
        coroutineScope.launch {
            val reading = GpsLocationManager(context).getCurrentLocation()
            gpsReading = reading
            gpsStatus = if (reading == null) "拿不到定位（權限未開啟或訊號不足）" else null
        }
    }
    
    // 註冊「請求單一權限」的啟動器，之後在按鈕 onClick 裡呼叫 .launch(...) 才會真的跳出對話框。
    // 若使用者已經同意過權限，.launch(...) 不會再跳窗，直接回呼 granted = true。
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            fetchGpsLocation()
        } else {
            gpsReading = null
            gpsStatus = "定位權限被拒絕"
        }
    }

    // 組成 FingerprintSample 並寫入 Firestore。位置欄位目前沿用畫面上的 Mock 文字，
    // 等「選擇既有位置」功能做好後，要改成從選中的 LocationPoint 帶入 locationId。
    fun saveFingerprintSample() {
        // 跟抓 GPS 一樣，寫入 Firestore 也是 suspend fun，要在協程裡呼叫。
        coroutineScope.launch {
            // 連續掃描 collectScanRounds 次，每次掃完更新一次進度文字。
            val scanner = WifiScanner(context)
            val rounds = mutableListOf<List<WifiScanResult>>()
            repeat(collectScanRounds) { index ->
                scanProgress = "Wi-Fi 掃描中… ${index + 1}/$collectScanRounds"
                rounds += scanner.scanOnce()
            }
            scanProgress = null

            // 依 BSSID 分組，再算出每個 AP 的統計值。
            val grouped = WifiScanProcessor.groupByBssid(rounds)
            val accessPoints = WifiStatistics.computeAccessPoints(grouped)

            // 組一筆完整的指紋樣本資料，準備寫入 fingerprintSamples。
            val sample = FingerprintSample(
                // 位置欄位：目前是沿用畫面上寫死的 Mock 文字（TODO：改成真正選中的位置）。
                buildingId = "五館",
                floorId = "4F",
                positionName = "5402",
                subPosition = "窗戶旁",
                locationId = "B5_4F_5402_WINDOW",
                note = "靠窗右側",
                // 裝置資訊：真實資料，來自 DeviceInfoProvider。
                deviceBrand = deviceInfo.deviceBrand,
                deviceModel = deviceInfo.deviceModel,
                androidVersion = deviceInfo.androidVersion,
                // GPS 欄位：用 ?. 安全呼叫，沒定位過就是 null，不塞假數字。
                gpsLatitude = gpsReading?.latitude,
                gpsLongitude = gpsReading?.longitude,
                gpsAccuracy = gpsReading?.accuracy?.toDouble(),
                // WiFi 掃描結果：真實資料，來自 WifiScanner → WifiScanProcessor → WifiStatistics。
                scanCount = collectScanRounds,
                accessPoints = accessPoints,
                createdAt = System.currentTimeMillis()
            )
            // 透過 Repository 寫入 Firestore，畫面不直接碰 Firestore API。
            FingerprintRepository().addFingerprint(sample)
            // 寫入成功後才顯示「已完成」，不是按下按鈕就顯示。
            collected = true
        }
    }
//UI
    AppScaffold(
        title = "Wi-Fi 指紋採集",
        selectedDestination = AppDestination.Collect,
        onBackClick = onBackClick,
        onHomeClick = onHomeClick,
        onCollectClick = onCollectClick,
        onScanClick = onScanClick,
        onHistoryClick = onHistoryClick
    ) { modifier ->
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 32.dp),
            modifier = modifier.fillMaxSize()
        ) {
            item {
                AppCard {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        StatusPill(text = "採集模式", color = CollectGreen, background = GreenTint)
                        Text("建立位置指紋樣本", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextStrong)
                        Text("此模式會建立新的 Wi-Fi 指紋資料。", fontSize = 12.5.sp, color = TextMuted)
                    }
                }
            }
            item { SectionLabel("位置資訊") }
            item { MockField("館別", "五館") }
            item { MockField("樓層", "4F") }
            item { MockField("位置名稱", "5402") }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("區域類型", fontWeight = FontWeight.SemiBold, color = TextStrong)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("門口", "室內中", "窗戶旁", "走廊", "廁所", "其他").forEach { area ->
                            AssistChip(
                                onClick = {},
                                label = { Text(area) },
                                enabled = area == "窗戶旁"
                            )
                        }
                    }
                }
            }
            item { MockField("備註", "靠窗右側") }
            item { SectionLabel("裝置資訊") }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    DeviceInfoCard("手機品牌", deviceInfo.deviceBrand, Modifier.weight(1f))
                    DeviceInfoCard("手機型號", deviceInfo.deviceModel, Modifier.weight(1f))
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    DeviceInfoCard("Android 版本", deviceInfo.androidVersion, Modifier.weight(1f))
                    DeviceInfoCard("API Level", deviceInfo.apiLevel.toString(), Modifier.weight(1f))
                }
            }
            item { SectionLabel("GPS 定位測試") }
            item {
                AppCard {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        // 三選一顯示：有座標 > 有狀態文字（定位中／失敗）> 尚未定位。
                        if (gpsReading != null) {
                            val reading = gpsReading!!
                            Text("緯度：${reading.latitude}", fontSize = 13.sp, color = TextStrong)
                            Text("經度：${reading.longitude}", fontSize = 13.sp, color = TextStrong)
                            Text("精確度：±${reading.accuracy} m", fontSize = 12.sp, color = TextMuted)
                        } else if (gpsStatus != null) {
                            Text(gpsStatus!!, fontSize = 13.sp, color = TextMuted)
                        } else {
                            Text("尚未定位", fontSize = 13.sp, color = TextMuted)
                        }
                        // 按下去才真的跳出系統權限對話框（或已同意過就直接抓定位）。
                        OutlinedButton(
                            onClick = { permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("測試 GPS 定位", color = ActionBlue)
                        }
                    }
                }
            }
            item {
                MapPlaceholderCard(
                    title = "標記實際位置",
                    subtitle = "五館 4F 5402 窗戶旁"
                )
            }
            if (scanProgress != null) {
                item {
                    AppCard {
                        Row(Modifier.padding(14.dp)) {
                            StatusPill(text = scanProgress!!, color = ActionBlue, background = GreenTint)
                        }
                    }
                }
            }
            item {
                // 按下去呼叫真正的寫入函式，不再只是切換一個假的 UI 狀態。
                // 掃描期間（scanProgress != null）停用按鈕，避免重複觸發。
                Button(
                    onClick = { saveFingerprintSample() },
                    enabled = scanProgress == null,
                    colors = ButtonDefaults.buttonColors(containerColor = CollectGreen),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (scanProgress != null) "採集中…" else "開始採集")
                }
            }
            // collected 只有在 Firestore 真的寫入成功後才會變 true。
            if (collected) {
                item {
                    AppCard {
                        Row(Modifier.padding(14.dp)) {
                            StatusPill(text = "已寫入 Firestore（fingerprintSamples）。", color = CollectGreen, background = GreenTint)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MockField(label: String, value: String) {
    OutlinedTextField(
        value = value,
        onValueChange = {},
        readOnly = true,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun DeviceInfoCard(title: String, value: String, modifier: Modifier = Modifier) {
    AppCard(modifier = modifier) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, fontSize = 11.5.sp, color = TextMuted)
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextStrong)
        }
    }
}

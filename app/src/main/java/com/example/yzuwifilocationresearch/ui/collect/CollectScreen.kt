package com.example.yzuwifilocationresearch.ui.collect

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.yzuwifilocationresearch.device.DeviceInfoProvider
import com.example.yzuwifilocationresearch.firebase.FingerprintRepository
import com.example.yzuwifilocationresearch.firebase.ScanRoundExperimentRepository
import com.example.yzuwifilocationresearch.gps.GpsLocationManager
import com.example.yzuwifilocationresearch.gps.GpsReading
import com.example.yzuwifilocationresearch.model.FingerprintSample
import com.example.yzuwifilocationresearch.model.WifiScanResult
import com.example.yzuwifilocationresearch.navigation.AppDestination
import com.example.yzuwifilocationresearch.ui.components.ActionBlue
import com.example.yzuwifilocationresearch.ui.components.AppCard
import com.example.yzuwifilocationresearch.ui.components.AppScaffold
import com.example.yzuwifilocationresearch.ui.components.CollectGreen
import com.example.yzuwifilocationresearch.ui.components.GreenTint
import com.example.yzuwifilocationresearch.ui.components.MapPlaceholderCard
import com.example.yzuwifilocationresearch.ui.components.RedTint
import com.example.yzuwifilocationresearch.ui.components.SectionLabel
import com.example.yzuwifilocationresearch.ui.components.StatusPill
import com.example.yzuwifilocationresearch.ui.components.TextMuted
import com.example.yzuwifilocationresearch.ui.components.TextStrong
import com.example.yzuwifilocationresearch.wifi.WifiScanProcessor
import com.example.yzuwifilocationresearch.wifi.WifiScanner
import com.example.yzuwifilocationresearch.wifi.WifiStatistics
import kotlinx.coroutines.launch

private const val DEFAULT_NOTE = "靠窗右側"
private const val DEFAULT_FLOOR = "4F"
private const val DEFAULT_POSITION_NAME = "5402"
private val buildingOptions = listOf("一館", "二館", "三館", "五館", "六館", "七館")
private val areaTypeOptions = listOf("門口", "室內中", "窗戶旁", "走廊", "廁所", "其他")

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
    var scanProgress by remember { mutableStateOf<String?>(null) }
    var formError by remember { mutableStateOf<String?>(null) }

    var buildingId by remember { mutableStateOf("五館") }
    var floorId by remember { mutableStateOf(DEFAULT_FLOOR) }
    var positionName by remember { mutableStateOf(DEFAULT_POSITION_NAME) }
    var areaType by remember { mutableStateOf("窗戶旁") }
    var note by remember { mutableStateOf(DEFAULT_NOTE) }
    var defaultFloorCleared by remember { mutableStateOf(false) }
    var defaultPositionNameCleared by remember { mutableStateOf(false) }
    var defaultNoteCleared by remember { mutableStateOf(false) }

    val collectScanRounds = 10
    val experimentScanRounds = 30
    var experimentProgress by remember { mutableStateOf<String?>(null) }
    var experimentDone by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val deviceInfo = remember { DeviceInfoProvider.getDeviceInfo() }
    var gpsReading by remember { mutableStateOf<GpsReading?>(null) }
    var gpsStatus by remember { mutableStateOf<String?>(null) }

    fun fetchGpsLocation() {
        gpsStatus = "定位中..."
        coroutineScope.launch {
            val reading = GpsLocationManager(context).getCurrentLocation()
            gpsReading = reading
            gpsStatus = if (reading == null) "無法取得定位，請確認權限或 GPS 訊號" else null
        }
    }

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

    fun validateForm(): Boolean {
        formError = when {
            buildingId.isBlank() -> "館別不能空"
            floorId.isBlank() -> "樓層不能空"
            positionName.isBlank() -> "位置名稱不能空"
            else -> null
        }
        return formError == null
    }

    fun saveFingerprintSample() {
        if (!validateForm()) return

        val currentBuildingId = buildingId.trim()
        val currentFloorId = floorId.trim()
        val currentPositionName = positionName.trim()
        val currentAreaType = areaType.trim()
        val currentNote = note.trim()

        coroutineScope.launch {
            collected = false
            val scanner = WifiScanner(context)
            val rounds = mutableListOf<List<WifiScanResult>>()
            repeat(collectScanRounds) { index ->
                scanProgress = "Wi-Fi 掃描中… ${index + 1}/$collectScanRounds"
                rounds += scanner.scanOnce(
                    onThrottled = { scanProgress = "系統限制掃描頻率，等待中…（第${index + 1}輪）" }
                )
            }
            scanProgress = null

            val grouped = WifiScanProcessor.groupByBssid(rounds)
            val accessPoints = WifiStatistics.computeAccessPoints(grouped)

            val sample = FingerprintSample(
                buildingId = currentBuildingId,
                floorId = currentFloorId,
                positionName = currentPositionName,
                subPosition = currentAreaType,
                locationId = buildLocationId(
                    buildingId = currentBuildingId,
                    floorId = currentFloorId,
                    positionName = currentPositionName,
                    subPosition = currentAreaType
                ),
                note = currentNote,
                deviceBrand = deviceInfo.deviceBrand,
                deviceModel = deviceInfo.deviceModel,
                androidVersion = deviceInfo.androidVersion,
                gpsLatitude = gpsReading?.latitude,
                gpsLongitude = gpsReading?.longitude,
                gpsAccuracy = gpsReading?.accuracy?.toDouble(),
                scanCount = collectScanRounds,
                accessPoints = accessPoints,
                createdAt = System.currentTimeMillis()
            )
            FingerprintRepository().addFingerprint(sample)
            collected = true
        }
    }

    fun runScanRoundExperiment() {
        if (!validateForm()) return

        val currentLocationId = buildLocationId(
            buildingId = buildingId.trim(),
            floorId = floorId.trim(),
            positionName = positionName.trim(),
            subPosition = areaType.trim()
        )

        coroutineScope.launch {
            experimentDone = false
            val scanner = WifiScanner(context)
            val rounds = mutableListOf<List<WifiScanResult>>()
            repeat(experimentScanRounds) { index ->
                experimentProgress = "實驗掃描中… ${index + 1}/$experimentScanRounds"
                rounds += scanner.scanOnce(
                    onThrottled = { experimentProgress = "系統限制掃描頻率，等待中…（第${index + 1}輪）" }
                )
            }
            experimentProgress = null

            ScanRoundExperimentRepository().addExperiment(
                locationId = currentLocationId,
                deviceBrand = deviceInfo.deviceBrand,
                deviceModel = deviceInfo.deviceModel,
                rounds = rounds
            )
            experimentDone = true
        }
    }

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
                        Text("填寫位置後會使用目前表單內容建立 fingerprintSamples。", fontSize = 12.5.sp, color = TextMuted)
                    }
                }
            }

            formError?.let { error ->
                item {
                    AppCard {
                        Row(Modifier.padding(14.dp)) {
                            StatusPill(text = error, color = TextStrong, background = RedTint)
                        }
                    }
                }
            }

            item { SectionLabel("位置資訊") }
            item {
                BuildingDropdownField(
                    value = buildingId,
                    enabled = scanProgress == null,
                    onValueChange = { buildingId = it }
                )
            }
            item {
                EditableField(
                    label = "樓層",
                    value = floorId,
                    enabled = scanProgress == null,
                    onValueChange = { floorId = it },
                    modifier = Modifier.onFocusChanged { focusState ->
                        if (focusState.isFocused && !defaultFloorCleared && floorId == DEFAULT_FLOOR) {
                            floorId = ""
                            defaultFloorCleared = true
                        }
                    }
                )
            }
            item {
                EditableField(
                    label = "位置名稱",
                    value = positionName,
                    enabled = scanProgress == null,
                    onValueChange = { positionName = it },
                    modifier = Modifier.onFocusChanged { focusState ->
                        if (
                            focusState.isFocused &&
                            !defaultPositionNameCleared &&
                            positionName == DEFAULT_POSITION_NAME
                        ) {
                            positionName = ""
                            defaultPositionNameCleared = true
                        }
                    }
                )
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("區域類型", fontWeight = FontWeight.SemiBold, color = TextStrong)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        areaTypeOptions.forEach { option ->
                            FilterChip(
                                selected = areaType == option,
                                onClick = { if (scanProgress == null) areaType = option },
                                enabled = scanProgress == null,
                                label = { Text(option) }
                            )
                        }
                    }
                }
            }
            item {
                EditableField(
                    label = "備註",
                    value = note,
                    enabled = scanProgress == null,
                    onValueChange = { note = it },
                    modifier = Modifier.onFocusChanged { focusState ->
                        if (focusState.isFocused && !defaultNoteCleared && note == DEFAULT_NOTE) {
                            note = ""
                            defaultNoteCleared = true
                        }
                    }
                )
            }

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
                        OutlinedButton(
                            onClick = { permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION) },
                            enabled = scanProgress == null,
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
                    subtitle = listOf(buildingId, floorId, positionName, areaType)
                        .filter { it.isNotBlank() }
                        .joinToString(" ")
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
                Button(
                    onClick = { saveFingerprintSample() },
                    enabled = scanProgress == null,
                    colors = ButtonDefaults.buttonColors(containerColor = CollectGreen),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (scanProgress != null) "採集中..." else "開始採集")
                }
            }

            if (collected) {
                item {
                    AppCard {
                        Row(Modifier.padding(14.dp)) {
                            StatusPill(text = "已寫入 Firestore（fingerprintSamples）。", color = CollectGreen, background = GreenTint)
                        }
                    }
                }
            }

            item { SectionLabel("實驗：掃描輪數N分析") }
            item {
                AppCard {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "在目前表單標記的位置連續掃描30輪，保留每輪原始RSSI值，" +
                                "寫入 scanRoundExperiments 供離線分析N=1~10時的估計誤差。" +
                                "不影響正式的 fingerprintSamples 資料。",
                            fontSize = 12.5.sp,
                            color = TextMuted
                        )
                        if (experimentProgress != null) {
                            StatusPill(text = experimentProgress!!, color = ActionBlue, background = GreenTint)
                        }
                        OutlinedButton(
                            onClick = { runScanRoundExperiment() },
                            enabled = experimentProgress == null,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (experimentProgress != null) "實驗掃描中..." else "開始實驗掃描（30輪）")
                        }
                        if (experimentDone) {
                            StatusPill(text = "已寫入 Firestore（scanRoundExperiments）。", color = CollectGreen, background = GreenTint)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BuildingDropdownField(
    value: String,
    enabled: Boolean,
    onValueChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        OutlinedButton(
            onClick = { if (enabled) expanded = true },
            enabled = enabled,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("館別：$value", color = MaterialTheme.colorScheme.onSurface)
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            buildingOptions.forEach { building ->
                DropdownMenuItem(
                    text = { Text(building, color = MaterialTheme.colorScheme.onSurface) },
                    onClick = {
                        onValueChange(building)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun EditableField(
    label: String,
    value: String,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
        readOnly = false,
        label = { Text(label) },
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
            disabledTextColor = MaterialTheme.colorScheme.onSurface
        ),
        modifier = modifier.fillMaxWidth()
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

private fun buildLocationId(
    buildingId: String,
    floorId: String,
    positionName: String,
    subPosition: String
): String {
    return listOf(buildingId, floorId, positionName, subPosition)
        .joinToString("_")
        .uppercase()
        .replace(Regex("[^A-Z0-9_\\u4E00-\\u9FFF]"), "_")
        .replace(Regex("_+"), "_")
        .trim('_')
}

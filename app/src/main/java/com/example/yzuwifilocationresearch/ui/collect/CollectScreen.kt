package com.example.yzuwifilocationresearch.ui.collect

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.yzuwifilocationresearch.device.DeviceInfoProvider
import com.example.yzuwifilocationresearch.navigation.AppDestination
import com.example.yzuwifilocationresearch.ui.components.AppScaffold
import com.example.yzuwifilocationresearch.ui.components.CollectGreen
import com.example.yzuwifilocationresearch.ui.components.MockMapCard
import com.example.yzuwifilocationresearch.ui.components.NeutralBlueGray

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
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEAF7EF)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("採集模式：建立位置指紋樣本", fontWeight = FontWeight.SemiBold, color = CollectGreen)
                        Text("此模式會建立新的 Wi-Fi 指紋資料。", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            item { MockField("館別", "五館") }
            item { MockField("樓層", "4F") }
            item { MockField("位置名稱", "5402") }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("區域類型", fontWeight = FontWeight.SemiBold)
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
            item {
                MockMapCard(
                    title = "標記實際位置",
                    subtitle = "五館 4F 5402 窗戶旁"
                )
            }
            item {
                Button(
                    onClick = { collected = true },
                    colors = ButtonDefaults.buttonColors(containerColor = CollectGreen),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("開始採集")
                }
            }
            if (collected) {
                item {
                    Text(
                        text = "Mock 成功：已完成 UI 採集流程，未上傳 Firebase。",
                        color = CollectGreen,
                        style = MaterialTheme.typography.bodyMedium
                    )
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
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, style = MaterialTheme.typography.labelMedium, color = NeutralBlueGray)
            Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
        }
    }
}

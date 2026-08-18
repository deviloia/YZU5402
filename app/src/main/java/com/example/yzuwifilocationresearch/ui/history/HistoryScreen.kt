package com.example.yzuwifilocationresearch.ui.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.yzuwifilocationresearch.navigation.AppDestination
import com.example.yzuwifilocationresearch.ui.components.ActionBlue
import com.example.yzuwifilocationresearch.ui.components.AppCard
import com.example.yzuwifilocationresearch.ui.components.AppScaffold
import com.example.yzuwifilocationresearch.ui.components.BlueTint
import com.example.yzuwifilocationresearch.ui.components.CollectGreen
import com.example.yzuwifilocationresearch.ui.components.GpsRed
import com.example.yzuwifilocationresearch.ui.components.GrayTint
import com.example.yzuwifilocationresearch.ui.components.GreenTint
import com.example.yzuwifilocationresearch.ui.components.RedTint
import com.example.yzuwifilocationresearch.ui.components.SectionHeader
import com.example.yzuwifilocationresearch.ui.components.StatusPill
import com.example.yzuwifilocationresearch.ui.components.TextMuted
import com.example.yzuwifilocationresearch.ui.components.TextStrong

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun HistoryScreen(
    onBackClick: () -> Unit,
    onEditLocationClick: () -> Unit,
    onHomeClick: () -> Unit,
    onCollectClick: () -> Unit,
    onScanClick: () -> Unit,
    onHistoryClick: () -> Unit
) {
    val records = listOf(
        MockRecord("2026-08-17 13:30", "五館 4F 5402 窗戶旁", "Samsung S23", "Android 14", "18.2m", "82%", "已校正"),
        MockRecord("2026-08-17 13:12", "五館 4F 5402 室內中", "Samsung S23", "Android 14", "21.6m", "76%", "已校正"),
        MockRecord("2026-08-17 12:45", "五館 4F 5401 門口", "Pixel 8", "Android 15", "15.7m", "71%", "未校正"),
        MockRecord("2026-08-16 16:20", "五館 3F 5303 走廊", "Samsung S23", "Android 14", "20.1m", "68%", "未校正"),
        MockRecord("2026-08-16 15:58", "五館 4F 5402 窗戶旁", "Pixel 8", "Android 15", "18.9m", "80%", "已校正")
    )

    AppScaffold(
        title = "歷史紀錄",
        selectedDestination = AppDestination.History,
        onBackClick = onBackClick,
        onHomeClick = onHomeClick,
        onCollectClick = onCollectClick,
        onScanClick = onScanClick,
        onHistoryClick = onHistoryClick
    ) { modifier ->
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 32.dp),
            modifier = modifier.fillMaxSize()
        ) {
            item {
                AppCard {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        SectionHeader("篩選", "館別 / 樓層 / 位置")
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            AssistChip(onClick = {}, label = { Text("館別：五館") })
                            AssistChip(onClick = {}, label = { Text("樓層：4F") })
                            AssistChip(onClick = {}, label = { Text("位置：5402") })
                        }
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(selected = true, onClick = {}, label = { Text("單選") })
                            FilterChip(selected = false, onClick = {}, label = { Text("多選") })
                            FilterChip(selected = false, onClick = {}, label = { Text("全選") })
                        }
                    }
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = onEditLocationClick,
                        colors = ButtonDefaults.buttonColors(containerColor = ActionBlue),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("批次修改位置")
                    }
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(onClick = {}, modifier = Modifier.weight(1f)) {
                        Text("下載選取 CSV")
                    }
                    OutlinedButton(onClick = {}, modifier = Modifier.weight(1f)) {
                        Text("下載全部 CSV")
                    }
                }
            }
            items(records) { record ->
                HistoryRecordCard(record)
            }
        }
    }
}

@Composable
private fun HistoryRecordCard(record: MockRecord) {
    val isCalibrated = record.status == "已校正"
    AppCard {
        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(12.dp)
        ) {
            Checkbox(checked = isCalibrated, onCheckedChange = {})
            Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.weight(1f)) {
                Text(record.dateTime, fontSize = 11.5.sp, color = TextMuted)
                Text(record.location, fontWeight = FontWeight.SemiBold, color = TextStrong)
                Text("${record.deviceModel} / ${record.android}", fontSize = 12.sp, color = TextMuted)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatusPill(text = "GPS 誤差 ${record.gpsError}", color = GpsRed, background = RedTint)
                    StatusPill(text = "Confidence ${record.confidence}", color = ActionBlue, background = BlueTint)
                    StatusPill(
                        text = record.status,
                        color = if (isCalibrated) CollectGreen else TextMuted,
                        background = if (isCalibrated) GreenTint else GrayTint
                    )
                }
            }
        }
    }
}

private data class MockRecord(
    val dateTime: String,
    val location: String,
    val deviceModel: String,
    val android: String,
    val gpsError: String,
    val confidence: String,
    val status: String
)

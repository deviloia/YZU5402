package com.example.yzuwifilocationresearch.ui.result

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.yzuwifilocationresearch.navigation.AppDestination
import com.example.yzuwifilocationresearch.ui.components.ActionBlue
import com.example.yzuwifilocationresearch.ui.components.AppScaffold
import com.example.yzuwifilocationresearch.ui.components.CollectGreen
import com.example.yzuwifilocationresearch.ui.components.GpsRed
import com.example.yzuwifilocationresearch.ui.components.LegendRow
import com.example.yzuwifilocationresearch.ui.components.MockMapCard
import com.example.yzuwifilocationresearch.ui.components.NeutralBlueGray
import com.example.yzuwifilocationresearch.ui.components.SectionHeader

@Composable
fun ResultScreen(
    onBackClick: () -> Unit,
    onHomeClick: () -> Unit,
    onCollectClick: () -> Unit,
    onScanClick: () -> Unit,
    onHistoryClick: () -> Unit
) {
    val mockAps = listOf(
        MockAp("YZU-WLAN", "00:11:22:33:44:55", "-45"),
        MockAp("YZU-Staff", "00:11:22:33:44:66", "-58"),
        MockAp("YZU-Guest", "00:11:22:33:44:77", "-63"),
        MockAp("YZU-Lab", "00:11:22:33:44:88", "-69")
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
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 32.dp),
            modifier = modifier.fillMaxSize()
        ) {
            item {
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFEAF7EF)), shape = RoundedCornerShape(8.dp)) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Wi-Fi 預測位置", color = CollectGreen, fontWeight = FontWeight.SemiBold)
                        Text("五館 4F 5402 窗戶旁", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Text("Confidence：82%")
                            Text("KNN：K = 3")
                            Text("AP：18")
                        }
                    }
                }
            }
            item {
                SectionHeader(title = "位置比較")
                PositionCard("Wi-Fi Prediction", "五館 4F 5402 窗戶旁", ActionBlue)
                PositionCard("GPS Position", "24.970123\n121.235678\nAccuracy 18.6 m", GpsRed)
                PositionCard("Ground Truth", "24.970198\n121.235732", CollectGreen)
            }
            item {
                Card(shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("GPS Error Mock", fontWeight = FontWeight.SemiBold)
                        Text("18.2 m", color = GpsRed, fontWeight = FontWeight.Bold)
                    }
                }
            }
            item {
                MockMapCard(
                    title = "Mock Map",
                    subtitle = "綠色 = Ground Truth，紅色 = GPS，藍色 = Wi-Fi Prediction"
                )
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    LegendRow(CollectGreen, "綠色 = Ground Truth")
                    LegendRow(GpsRed, "紅色 = GPS")
                    LegendRow(ActionBlue, "藍色 = Wi-Fi Prediction")
                }
            }
            item { SectionHeader(title = "AP List", subtitle = "Mock Wi-Fi scan results") }
            items(mockAps) { ap ->
                Card(shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(ap.ssid, fontWeight = FontWeight.SemiBold)
                        Text(ap.bssid, color = NeutralBlueGray, style = MaterialTheme.typography.bodySmall)
                        Text("RSSI ${ap.rssi}", color = ActionBlue, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    Button(onClick = {}, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = ActionBlue)) {
                        Text("儲存紀錄")
                    }
                    OutlinedButton(onClick = {}, modifier = Modifier.weight(1f)) {
                        Text("下載本次 CSV")
                    }
                }
            }
        }
    }
}

@Composable
private fun PositionCard(title: String, value: String, color: Color) {
    Card(shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, color = color, fontWeight = FontWeight.SemiBold)
            Text(value)
        }
    }
}

private data class MockAp(
    val ssid: String,
    val bssid: String,
    val rssi: String
)

package com.example.yzuwifilocationresearch.ui.locationedit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.yzuwifilocationresearch.navigation.AppDestination
import com.example.yzuwifilocationresearch.ui.components.AppScaffold
import com.example.yzuwifilocationresearch.ui.components.CollectGreen
import com.example.yzuwifilocationresearch.ui.components.MockMapCard
import com.example.yzuwifilocationresearch.ui.components.NeutralBlueGray

@Composable
fun LocationEditScreen(
    onBackClick: () -> Unit,
    onHomeClick: () -> Unit,
    onCollectClick: () -> Unit,
    onScanClick: () -> Unit,
    onHistoryClick: () -> Unit
) {
    var saved by remember { mutableStateOf(false) }

    AppScaffold(
        title = "修改實際位置",
        selectedDestination = AppDestination.LocationEdit,
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
                Card(shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("目前修改目標", color = NeutralBlueGray, style = MaterialTheme.typography.bodySmall)
                        Text("五館 / 4F / 5402 / 窗戶旁", fontWeight = FontWeight.SemiBold)
                        Text("locationId", color = NeutralBlueGray, style = MaterialTheme.typography.bodySmall)
                        Text("B5_4F_5402_WINDOW", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
            item {
                MockMapCard(
                    title = "點擊地圖設定 Ground Truth",
                    subtitle = "Mock Map Card：此階段不使用 Google Maps SDK"
                )
            }
            item {
                Button(
                    onClick = { saved = true },
                    colors = ButtonDefaults.buttonColors(containerColor = CollectGreen),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("儲存位置")
                }
            }
            if (saved) {
                item {
                    Text(
                        "Mock 成功：位置 UI 已儲存，未寫入 Firebase。",
                        color = CollectGreen,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

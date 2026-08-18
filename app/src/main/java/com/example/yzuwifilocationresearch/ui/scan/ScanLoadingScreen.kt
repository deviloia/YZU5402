package com.example.yzuwifilocationresearch.ui.scan

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.yzuwifilocationresearch.navigation.AppDestination
import com.example.yzuwifilocationresearch.ui.components.ActionBlue
import com.example.yzuwifilocationresearch.ui.components.AppCard
import com.example.yzuwifilocationresearch.ui.components.AppScaffold
import com.example.yzuwifilocationresearch.ui.components.TextMuted
import com.example.yzuwifilocationresearch.ui.components.TextStrong
import kotlinx.coroutines.delay

@Composable
fun ScanLoadingScreen(
    onBackClick: () -> Unit,
    onFinished: () -> Unit,
    onHomeClick: () -> Unit,
    onCollectClick: () -> Unit,
    onScanClick: () -> Unit,
    onHistoryClick: () -> Unit
) {
    LaunchedEffect(Unit) {
        // TODO: Phase Test Mode 時改為真實 Wi-Fi / KNN 流程
        delay(2_000)
        onFinished()
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
                CircularProgressIndicator(progress = { 0.68f }, color = ActionBlue)
            }
            item {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("正在掃描附近 Wi-Fi...", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = TextStrong)
                    Text(
                        "請稍候，完成後將顯示定位結果",
                        color = TextMuted,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
            item {
                LinearProgressIndicator(progress = { 0.68f }, modifier = Modifier.fillMaxWidth(), color = ActionBlue)
            }
            item {
                AppCard {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        ScanStep("1", "掃描 Wi-Fi", "已偵測 AP 數量：18")
                        ScanStep("2", "比對指紋", "Mock 指紋資料比對中")
                        ScanStep("3", "顯示結果", "準備進入定位結果")
                    }
                }
            }
        }
    }
}

@Composable
private fun ScanStep(number: String, title: String, subtitle: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(number, color = ActionBlue, fontWeight = FontWeight.Bold)
        Column {
            Text(title, fontWeight = FontWeight.SemiBold, color = TextStrong)
            Text(subtitle, fontSize = 12.sp, color = TextMuted)
        }
    }
}

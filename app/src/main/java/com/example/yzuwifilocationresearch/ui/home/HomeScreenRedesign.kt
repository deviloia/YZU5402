package com.example.yzuwifilocationresearch.ui.home

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.yzuwifilocationresearch.firebase.LocationRepository
import com.example.yzuwifilocationresearch.ui.components.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 改版首頁。獨立檔案，不覆寫原本的 HomeScreen。
 * 要切換時，只把 NavHost 裡的 HomeScreen(...) 換成 HomeScreenRedesign(...)。
 *
 * 統計數字傳 null 就顯示「—」，不做假資料。
 */
@Composable
fun HomeScreenRedesign(
    sampleCount: Int? = null,
    testCount: Int? = null,
    calibratedCount: Int? = null,
    lastResultTitle: String? = null,
    lastResultTime: String? = null,
    lastConfidencePercent: Int? = null,
    lastGpsErrorMeters: Double? = null,
    onCollectClick: () -> Unit,
    onScanClick: () -> Unit,
    onHistoryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var firestoreStatus by remember { mutableStateOf(FirestoreStatus.Checking) }

    LaunchedEffect(Unit) {
        firestoreStatus = FirestoreStatus.Checking
        firestoreStatus = try {
            withContext(Dispatchers.IO) {
                LocationRepository().getAllLocations()
            }
            FirestoreStatus.Connected
        } catch (error: Exception) {
            Log.e("HomeScreenRedesign", "Firestore status check failed", error)
            FirestoreStatus.Failed
        }
    }

    Column(modifier.fillMaxSize().background(ScreenBackground)) {
        HomeHero(
            sampleCount = sampleCount,
            testCount = testCount,
            calibratedCount = calibratedCount,
            firestoreStatus = firestoreStatus
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SectionLabel("開始作業")

            PrimaryActionRow(
                icon = Icons.Default.LocationOn,
                iconBackground = CollectGreen,
                title = "Wi-Fi 指紋採集",
                subtitle = "建立位置指紋樣本資料庫",
                onClick = onCollectClick
            )
            PrimaryActionRow(
                icon = Icons.Default.Search,
                iconBackground = ActionBlue,
                title = "Wi-Fi 指紋掃描",
                subtitle = "測試目前位置並比對 KNN",
                onClick = onScanClick
            )
            SecondaryActionRow(
                icon = Icons.Default.DateRange,
                title = "歷史紀錄",
                subtitle = "查看、篩選與匯出 CSV",
                onClick = onHistoryClick
            )

            if (lastResultTitle != null) {
                LastResultCard(
                    title = lastResultTitle,
                    time = lastResultTime,
                    confidencePercent = lastConfidencePercent,
                    gpsErrorMeters = lastGpsErrorMeters
                )
            }
        }
    }
}

@Composable
private fun HomeHero(
    sampleCount: Int?,
    testCount: Int?,
    calibratedCount: Int?,
    firestoreStatus: FirestoreStatus
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(PrimaryBlue)
            .padding(start = 20.dp, end = 20.dp, top = 22.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                "YZU CAMPUS Wi-Fi",
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp,
                color = Color.White.copy(alpha = 0.6f)
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(Color.White.copy(alpha = 0.14f))
                    .padding(horizontal = 9.dp, vertical = 4.dp)
            ) {
                Box(
                    Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(firestoreStatus.dotColor)
                )
                Text(
                    firestoreStatus.label,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(
                "Wi-Fi 指紋測試系統",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                "精準定位・可靠數據・智慧分析",
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.68f)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Color.White.copy(alpha = 0.1f))
                .padding(vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HeroStat("指紋樣本", sampleCount, Modifier.weight(1f))
            HeroDivider()
            HeroStat("測試紀錄", testCount, Modifier.weight(1f))
            HeroDivider()
            HeroStat("已校正", calibratedCount, Modifier.weight(1f))
        }
    }
}

private enum class FirestoreStatus(
    val label: String,
    val dotColor: Color
) {
    Checking("Firestore 檢查中", Color(0xFFFBBF24)),
    Connected("Firestore 已連線", Color(0xFF4ADE80)),
    Failed("Firestore 連線失敗", Color(0xFFF87171))
}

@Composable
private fun HeroStat(label: String, value: Int?, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Text(
            value?.toString() ?: "—",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(label, fontSize = 11.sp, color = Color.White.copy(alpha = 0.62f))
    }
}

@Composable
private fun HeroDivider() {
    Box(
        Modifier
            .width(1.dp)
            .height(34.dp)
            .background(Color.White.copy(alpha = 0.16f))
    )
}

@Composable
private fun PrimaryActionRow(
    icon: ImageVector,
    iconBackground: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    AppCard(onClick = onClick) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            Box(
                Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconBackground),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = Color.White)
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextStrong)
                Text(subtitle, fontSize = 12.5.sp, color = NeutralBlueGray)
            }
            Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = Color(0xFFB4BECC))
        }
    }
}

@Composable
private fun SecondaryActionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    AppCard(onClick = onClick) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Box(
                Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .background(Color(0xFFEEF1F6)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = NeutralBlueGray)
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(title, fontSize = 14.5.sp, fontWeight = FontWeight.SemiBold, color = TextStrong)
                Text(subtitle, fontSize = 12.sp, color = NeutralBlueGray)
            }
            Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = Color(0xFFB4BECC))
        }
    }
}

@Composable
private fun LastResultCard(
    title: String,
    time: String?,
    confidencePercent: Int?,
    gpsErrorMeters: Double?
) {
    Column(
        modifier = Modifier
            .padding(top = 6.dp)
            .fillMaxWidth()
            .clip(CardShape)
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                "最近一筆測試",
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp,
                color = TextMuted
            )
            Text(time ?: "—", fontSize = 11.sp, color = TextMuted)
        }
        Text(title, fontSize = 14.5.sp, fontWeight = FontWeight.SemiBold, color = TextStrong)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatusPill(
                text = confidencePercent?.let { "Confidence $it%" } ?: "Confidence —",
                color = ActionBlue,
                background = BlueTint
            )
            StatusPill(
                text = gpsErrorMeters?.let { "GPS 誤差 ${"%.1f".format(it)} m" } ?: "GPS 誤差 —",
                color = GpsRed,
                background = RedTint
            )
        }
    }
}

package com.example.yzuwifilocationresearch.ui.result

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.yzuwifilocationresearch.ui.components.*

/** 一列 AP 掃描結果。 */
data class ApRowData(
    val ssid: String,
    val bssid: String,
    val rssi: Int
)

/**
 * 改版定位結果頁的內容區（獨立檔案，不覆寫原本的 ResultScreen）。
 * 版面順序：Wi-Fi 預測卡 → 誤差雙卡 → 詳細定位資料表格 → 地圖占位 → AP List。
 * 任何 null 值一律顯示「—」。
 */
@Composable
fun ResultContentRedesign(
    predictedLocationName: String?,
    predictedLocationId: String?,
    confidencePercent: Int?,
    k: Int?,
    apCount: Int?,
    gpsAreaName: String?,
    gpsLatitude: Double?,
    gpsLongitude: Double?,
    gpsAccuracyMeters: Double?,
    gpsUpdatedAt: String?,
    wifiUpdatedAt: String?,
    gpsErrorMeters: Double?,
    wifiErrorMeters: Double?,
    isCalibrated: Boolean,
    apList: List<ApRowData> = emptyList(),
    onCopyDetails: () -> Unit = {},
    onOpenInMaps: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ScreenBackground)
            .verticalScroll(rememberScrollState())
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        PredictionCard(
            predictedLocationName = predictedLocationName,
            predictedLocationId = predictedLocationId,
            confidencePercent = confidencePercent,
            k = k,
            apCount = apCount,
            isCalibrated = isCalibrated
        )

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ErrorStatCard("GPS Error", gpsErrorMeters, GpsRed, Modifier.weight(1f))
            ErrorStatCard("Wi-Fi Error", wifiErrorMeters, GpsRed, Modifier.weight(1f))
        }

        DetailComparisonCard(
            gpsAreaName = gpsAreaName,
            predictedLocationName = predictedLocationName,
            gpsLatitude = gpsLatitude,
            gpsLongitude = gpsLongitude,
            gpsAccuracyMeters = gpsAccuracyMeters,
            confidencePercent = confidencePercent,
            gpsUpdatedAt = gpsUpdatedAt,
            wifiUpdatedAt = wifiUpdatedAt,
            onCopyDetails = onCopyDetails,
            onOpenInMaps = onOpenInMaps
        )

        if (gpsLatitude != null && gpsLongitude != null) {
            WebMapCard(
                latitude = gpsLatitude,
                longitude = gpsLongitude,
                height = 180
            )
        } else {
            MapPlaceholderCard(
                title = "GPS 地圖",
                subtitle = "沒有 GPS 座標時無法顯示地圖",
                height = 150
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 2.dp, top = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            SectionLabel("AP LIST")
            Text(
                if (apList.isEmpty()) "—" else "${apList.size} 筆・依 RSSI 排序",
                fontSize = 11.5.sp,
                color = TextMuted
            )
        }

        AppCard {
            apList.sortedByDescending { it.rssi }.forEachIndexed { index, ap ->
                if (index > 0) RowDivider()
                ApRow(ap)
            }
        }
    }
}

@Composable
private fun PredictionCard(
    predictedLocationName: String?,
    predictedLocationId: String?,
    confidencePercent: Int?,
    k: Int?,
    apCount: Int?,
    isCalibrated: Boolean
) {
    AppCard {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                Box(
                    Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(CollectGreen)
                )
                Text(
                    "Wi-Fi 預測位置",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.4.sp,
                    color = CollectGreen
                )
                Spacer(Modifier.weight(1f))
                if (isCalibrated) {
                    StatusPill("已校正", CollectGreen, GreenTint)
                }
            }

            Text(
                predictedLocationName ?: "—",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = TextStrong
            )
            Text(predictedLocationId ?: "—", fontSize = 11.5.sp, color = TextMuted)

            Box(Modifier.fillMaxWidth().height(1.dp).background(HairLine))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        confidencePercent?.let { "Confidence $it%" } ?: "Confidence —",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextStrong
                    )
                    Text(
                        "WKNN 相對信心值，非校準機率",
                        fontSize = 11.5.sp,
                        color = TextMuted
                    )
                }
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("K = ${k?.toString() ?: "—"}", fontSize = 11.5.sp, color = NeutralBlueGray)
                    Text("AP = ${apCount?.toString() ?: "—"}", fontSize = 11.5.sp, color = NeutralBlueGray)
                }
            }
        }
    }
}

@Composable
private fun ErrorStatCard(
    label: String,
    meters: Double?,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .padding(horizontal = 14.dp, vertical = 13.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(label, fontSize = 11.5.sp, color = TextMuted)
        Text(
            meters?.let { "${"%.1f".format(it)} m" } ?: "—",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = if (meters == null) Color(0xFFB4BECC) else valueColor
        )
    }
}

/** 「詳細定位資料」：GPS 與 Wi-Fi 室內兩欄對照表。 */
@Composable
private fun DetailComparisonCard(
    gpsAreaName: String?,
    predictedLocationName: String?,
    gpsLatitude: Double?,
    gpsLongitude: Double?,
    gpsAccuracyMeters: Double?,
    confidencePercent: Int?,
    gpsUpdatedAt: String?,
    wifiUpdatedAt: String?,
    onCopyDetails: () -> Unit,
    onOpenInMaps: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CardShape)
            .background(Color.White)
            .border(1.dp, OutlineGray, CardShape)
            .padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("詳細定位資料", fontSize = 14.5.sp, fontWeight = FontWeight.Bold, color = TextStrong)
            Text(
                "複製",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = NeutralBlueGray,
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .border(1.dp, OutlineGray, RoundedCornerShape(999.dp))
                    .padding(horizontal = 12.dp, vertical = 5.dp)
            )
        }

        Column {
            DetailRow(
                label = "",
                gps = "GPS",
                wifi = "Wi-Fi 室內",
                isHeader = true
            )
            RowDivider(inset = 0)
            DetailRow(
                label = "推斷位置",
                gps = gpsAreaName ?: "—",
                wifi = predictedLocationName ?: "—",
                wifiStrong = true
            )
            RowDivider(inset = 0)
            DetailRow(
                label = "緯 / 經度",
                gps = if (gpsLatitude != null && gpsLongitude != null)
                    "${"%.6f".format(gpsLatitude)}\n${"%.6f".format(gpsLongitude)}" else "—",
                wifi = "—"
            )
            RowDivider(inset = 0)
            DetailRow(
                label = "精度 / 信心",
                gps = gpsAccuracyMeters?.let { "±${"%.1f".format(it)} 公尺" } ?: "—",
                gpsColor = GpsRed,
                wifi = confidencePercent?.let { "約 $it%" } ?: "—",
                wifiColor = ActionBlue
            )
            RowDivider(inset = 0)
            DetailRow(
                label = "更新時間",
                gps = gpsUpdatedAt ?: "—",
                wifi = wifiUpdatedAt ?: "—"
            )
        }

        Text(
            "在 Google Maps 查看",
            fontSize = 12.5.sp,
            color = ActionBlue,
            textDecoration = TextDecoration.Underline
        )
    }
}

@Composable
private fun DetailRow(
    label: String,
    gps: String,
    wifi: String,
    isHeader: Boolean = false,
    gpsColor: Color = TextStrong,
    wifiColor: Color = TextStrong,
    wifiStrong: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = if (isHeader) 0.dp else 12.dp)
            .padding(bottom = if (isHeader) 10.dp else 0.dp)
    ) {
        Text(
            label,
            modifier = Modifier.width(74.dp),
            fontSize = 13.sp,
            color = TextMuted
        )
        Text(
            gps,
            modifier = Modifier.weight(1f),
            fontSize = if (isHeader) 14.5.sp else 13.5.sp,
            fontWeight = if (isHeader) FontWeight.SemiBold else FontWeight.Normal,
            lineHeight = 19.sp,
            color = if (isHeader) TextStrong else gpsColor
        )
        Text(
            wifi,
            modifier = Modifier.weight(1f),
            fontSize = if (isHeader) 14.5.sp else 13.5.sp,
            fontWeight = if (isHeader || wifiStrong) FontWeight.SemiBold else FontWeight.Normal,
            lineHeight = 19.sp,
            color = if (isHeader) ActionBlue else wifiColor
        )
    }
}

@Composable
private fun ApRow(ap: ApRowData) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(Modifier.weight(1f)) {
            Text(ap.ssid, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextStrong)
            Text(ap.bssid, fontSize = 11.sp, color = Color(0xFF9AA7B8))
        }
        // RSSI −30 dBm ≈ 滿格，−90 dBm ≈ 空格
        val ratio = ((ap.rssi + 90).coerceIn(0, 60)) / 60f
        val strong = ap.rssi >= -60
        Column(
            modifier = Modifier.width(74.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                "${ap.rssi} dBm",
                fontSize = 12.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (strong) ActionBlue else NeutralBlueGray
            )
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFFEEF1F6))
            ) {
                Box(
                    Modifier
                        .fillMaxWidth(ratio)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(if (strong) ActionBlue else Color(0xFF8FA9CB))
                )
            }
        }
    }
}

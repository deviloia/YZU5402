package com.example.yzuwifilocationresearch.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.yzuwifilocationresearch.navigation.AppDestination

// ── 原有色票（不變，其他畫面仍可使用）
val PrimaryBlue = Color(0xFF0D47A1)
val ActionBlue = Color(0xFF0B63CE)
val CollectGreen = Color(0xFF198754)
val GpsRed = Color(0xFFE53935)
val NeutralBlueGray = Color(0xFF526173)
val ScreenBackground = Color(0xFFF5F7FB)

// ── 版面改版新增的中性色 / 淺底色
val TextStrong = Color(0xFF0F1B2D)
val TextMuted = Color(0xFF7C8AA0)
val HairLine = Color(0xFFEEF1F6)
val BorderLine = Color(0xFFE6EBF2)
val OutlineGray = Color(0xFFDCE3EC)
val BlueTint = Color(0xFFE9F1FC)
val GreenTint = Color(0xFFE7F5EC)
val RedTint = Color(0xFFFDECEC)
val GrayTint = Color(0xFFF1F4F8)

val CardShape = RoundedCornerShape(14.dp)

/** 改版：白底頂欄 + 深色標題，右側可放狀態標籤。 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScaffold(
    title: String,
    selectedDestination: AppDestination,
    onBackClick: (() -> Unit)? = null,
    onHomeClick: () -> Unit,
    onCollectClick: () -> Unit,
    onScanClick: () -> Unit,
    onHistoryClick: () -> Unit,
    trailing: @Composable (() -> Unit)? = null,
    bottomBar: @Composable (() -> Unit)? = null,
    content: @Composable (Modifier) -> Unit
) {
    Scaffold(
        containerColor = ScreenBackground,
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            text = title,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextStrong
                        )
                    },
                    navigationIcon = {
                        if (onBackClick != null) {
                            IconButton(onClick = onBackClick) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "返回", tint = TextStrong)
                            }
                        }
                    },
                    actions = { trailing?.invoke() },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
                )
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(BorderLine)
                )
            }
        },
        bottomBar = {
            Column {
                bottomBar?.invoke()
                AppBottomNavigation(
                    selectedDestination = selectedDestination,
                    onHomeClick = onHomeClick,
                    onCollectClick = onCollectClick,
                    onScanClick = onScanClick,
                    onHistoryClick = onHistoryClick
                )
            }
        }
    ) { innerPadding ->
        content(Modifier.padding(innerPadding))
    }
}

@Composable
fun AppBottomNavigation(
    selectedDestination: AppDestination,
    onHomeClick: () -> Unit,
    onCollectClick: () -> Unit,
    onScanClick: () -> Unit,
    onHistoryClick: () -> Unit
) {
    val itemColors = NavigationBarItemDefaults.colors(
        selectedIconColor = ActionBlue,
        selectedTextColor = ActionBlue,
        indicatorColor = BlueTint,
        unselectedIconColor = TextMuted,
        unselectedTextColor = TextMuted
    )

    NavigationBar(containerColor = Color.White, tonalElevation = 0.dp) {
        NavigationBarItem(
            selected = selectedDestination == AppDestination.Home,
            onClick = onHomeClick,
            colors = itemColors,
            icon = { Icon(Icons.Default.Home, contentDescription = null) },
            label = { Text("首頁", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
        )
        NavigationBarItem(
            selected = selectedDestination == AppDestination.Collect,
            onClick = onCollectClick,
            colors = itemColors,
            icon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
            label = { Text("採集", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
        )
        NavigationBarItem(
            selected = selectedDestination == AppDestination.ScanLoading ||
                selectedDestination == AppDestination.Result,
            onClick = onScanClick,
            colors = itemColors,
            icon = { Icon(Icons.Default.Search, contentDescription = null) },
            label = { Text("掃描", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
        )
        NavigationBarItem(
            selected = selectedDestination == AppDestination.History ||
                selectedDestination == AppDestination.LocationEdit,
            onClick = onHistoryClick,
            colors = itemColors,
            icon = { Icon(Icons.Default.DateRange, contentDescription = null) },
            label = { Text("歷史", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
        )
    }
}

/** 小節標題：全大寫式的灰色 label，取代原本的粗體標題。 */
@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier.padding(start = 2.dp),
        fontSize = 11.5.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.6.sp,
        color = TextMuted
    )
}

@Composable
fun SectionHeader(title: String, subtitle: String? = null) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = TextStrong)
        if (subtitle != null) {
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = NeutralBlueGray)
        }
    }
}

/** 白底圓角卡（改版統一樣式）。 */
@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val base = if (onClick != null) modifier.clickable(onClick = onClick) else modifier
    Card(
        modifier = base.fillMaxWidth(),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column { content() }
    }
}

/** 表格式的一條分隔線（左右留白）。 */
@Composable
fun RowDivider(inset: Int = 16) {
    Box(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = inset.dp)
            .height(1.dp)
            .background(HairLine)
    )
}

/** 有色圓點 + 文字，用於圖例與比較列。 */
@Composable
fun LegendRow(color: Color, label: String, modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = modifier
    ) {
        Box(
            Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(label, fontSize = 11.5.sp, color = NeutralBlueGray)
    }
}

/** 小色塊標籤（Confidence / GPS 誤差 / 已校正…）。 */
@Composable
fun StatusPill(text: String, color: Color, background: Color, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier
            .clip(RoundedCornerShape(5.dp))
            .background(background)
            .padding(horizontal = 7.dp, vertical = 3.dp),
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        color = color
    )
}

/** 地圖占位卡（虛線框），高度可調，不會被捲動區壓縮。 */
@Composable
fun MapPlaceholderCard(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    height: Int = 150,
    accent: Color = CollectGreen,
    icon: ImageVector = Icons.Default.LocationOn
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .fillMaxWidth()
            .height(height.dp)
            .clip(CardShape)
            .background(Color(0xFFEDF3F1))
            .padding(16.dp)
    ) {
        Icon(icon, contentDescription = null, tint = accent)
        Text(title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextStrong)
        Text(subtitle, fontSize = 11.5.sp, color = TextMuted)
    }
}

@Deprecated("改用 MapPlaceholderCard", ReplaceWith("MapPlaceholderCard(title, subtitle, modifier)"))
@Composable
fun MockMapCard(title: String, subtitle: String, modifier: Modifier = Modifier) =
    MapPlaceholderCard(title, subtitle, modifier)

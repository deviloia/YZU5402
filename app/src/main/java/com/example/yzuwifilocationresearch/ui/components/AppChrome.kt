package com.example.yzuwifilocationresearch.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.yzuwifilocationresearch.navigation.AppDestination

val PrimaryBlue = Color(0xFF0D47A1)
val ActionBlue = Color(0xFF0B63CE)
val CollectGreen = Color(0xFF198754)
val GpsRed = Color(0xFFE53935)
val NeutralBlueGray = Color(0xFF526173)
val ScreenBackground = Color(0xFFF5F7FB)

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
    content: @Composable (Modifier) -> Unit
) {
    Scaffold(
        containerColor = ScreenBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = title,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    if (onBackClick != null) {
                        IconButton(onClick = onBackClick) {
                            Text(text = "<", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PrimaryBlue)
            )
        },
        bottomBar = {
            AppBottomNavigation(
                selectedDestination = selectedDestination,
                onHomeClick = onHomeClick,
                onCollectClick = onCollectClick,
                onScanClick = onScanClick,
                onHistoryClick = onHistoryClick
            )
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
    NavigationBar(containerColor = Color.White) {
        NavigationBarItem(
            selected = selectedDestination == AppDestination.Home,
            onClick = onHomeClick,
            icon = { Text(text = "H") },
            label = { Text(text = "首頁") }
        )
        NavigationBarItem(
            selected = selectedDestination == AppDestination.Collect,
            onClick = onCollectClick,
            icon = { Text(text = "C") },
            label = { Text(text = "採集") }
        )
        NavigationBarItem(
            selected = selectedDestination == AppDestination.ScanLoading || selectedDestination == AppDestination.Result,
            onClick = onScanClick,
            icon = { Text(text = "S") },
            label = { Text(text = "掃描") }
        )
        NavigationBarItem(
            selected = selectedDestination == AppDestination.History || selectedDestination == AppDestination.LocationEdit,
            onClick = onHistoryClick,
            icon = { Text(text = "R") },
            label = { Text(text = "歷史") }
        )
    }
}

@Composable
fun SectionHeader(
    title: String,
    subtitle: String? = null
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        if (subtitle != null) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = NeutralBlueGray
            )
        }
    }
}

@Composable
fun LegendRow(
    color: Color,
    label: String,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
    ) {
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(text = label, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
fun MockMapCard(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    androidx.compose.material3.Card(
        modifier = modifier
            .fillMaxWidth()
            .height(160.dp),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .background(Color(0xFFEAF2EF))
                .padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = PrimaryBlue
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = NeutralBlueGray
            )
        }
    }
}

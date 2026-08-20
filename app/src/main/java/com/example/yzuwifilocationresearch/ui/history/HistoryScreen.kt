package com.example.yzuwifilocationresearch.ui.history

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.yzuwifilocationresearch.export.CsvExporter
import com.example.yzuwifilocationresearch.firebase.TestResultRepository
import com.example.yzuwifilocationresearch.model.TestResult
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
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun HistoryScreen(
    onBackClick: () -> Unit,
    onEditLocationClick: () -> Unit,
    onHomeClick: () -> Unit,
    onCollectClick: () -> Unit,
    onScanClick: () -> Unit,
    onHistoryClick: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repository = remember { TestResultRepository() }
    var uiState by remember { mutableStateOf(HistoryUiState(isLoading = true)) }
    var pendingCsv by remember { mutableStateOf<String?>(null) }

    fun loadHistory() {
        scope.launch {
            uiState = uiState.copy(isLoading = true, errorMessage = null, exportMessage = null)
            uiState = try {
                val results = withContext(Dispatchers.IO) {
                    repository.getAllTestResults()
                        .sortedByDescending { it.createdAt ?: Long.MIN_VALUE }
                }
                HistoryUiState(results = results)
            } catch (error: Exception) {
                HistoryUiState(errorMessage = error.message ?: "Failed to load history")
            }
        }
    }

    fun writeCsv(uri: Uri, csv: String) {
        scope.launch {
            val result = runCatching {
                withContext(Dispatchers.IO) {
                    context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                        OutputStreamWriter(outputStream, Charsets.UTF_8).use { writer ->
                            writer.write(csv)
                        }
                    } ?: error("Cannot open export destination")
                }
            }
            uiState = uiState.copy(
                exportMessage = if (result.isSuccess) {
                    "CSV exported"
                } else {
                    "CSV export failed: ${result.exceptionOrNull()?.message.orEmpty()}"
                }
            )
        }
    }

    val createCsvLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        val csv = pendingCsv
        pendingCsv = null
        if (uri != null && csv != null) {
            writeCsv(uri, csv)
        }
    }

    LaunchedEffect(Unit) {
        loadHistory()
    }

    AppScaffold(
        title = "History",
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
                        SectionHeader("testResults", "Live Firestore test result records")
                        Text(
                            text = if (uiState.results.isEmpty()) {
                                "No test results loaded"
                            } else {
                                "${uiState.results.size} records"
                            },
                            fontSize = 12.5.sp,
                            color = TextMuted
                        )
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
                        Text("Edit Ground Truth")
                    }
                    OutlinedButton(
                        onClick = {
                            pendingCsv = CsvExporter.testResultsToCsv(uiState.results)
                            createCsvLauncher.launch(CsvExporter.defaultTestResultsFileName())
                        },
                        enabled = uiState.results.isNotEmpty(),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Export CSV")
                    }
                }
            }
            uiState.exportMessage?.let { message ->
                item {
                    AppCard {
                        Text(
                            text = message,
                            modifier = Modifier.padding(14.dp),
                            fontSize = 12.5.sp,
                            color = TextMuted
                        )
                    }
                }
            }
            when {
                uiState.isLoading -> {
                    item {
                        LoadingCard()
                    }
                }

                uiState.errorMessage != null -> {
                    item {
                        ErrorCard(
                            message = uiState.errorMessage.orEmpty(),
                            onRetryClick = { loadHistory() }
                        )
                    }
                }

                uiState.results.isEmpty() -> {
                    item {
                        EmptyHistoryCard()
                    }
                }

                else -> {
                    itemsIndexed(uiState.results) { index, result ->
                        HistoryRecordCard(result = result, fallbackIndex = index)
                    }
                }
            }
        }
    }
}

private data class HistoryUiState(
    val isLoading: Boolean = false,
    val results: List<TestResult> = emptyList(),
    val errorMessage: String? = null,
    val exportMessage: String? = null
)

@Composable
private fun LoadingCard() {
    AppCard {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            CircularProgressIndicator()
            Text("Loading Firestore history", color = TextMuted)
        }
    }
}

@Composable
private fun ErrorCard(message: String, onRetryClick: () -> Unit) {
    AppCard {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Load failed", fontWeight = FontWeight.SemiBold, color = TextStrong)
            Text(message, fontSize = 12.5.sp, color = TextMuted)
            OutlinedButton(onClick = onRetryClick) {
                Text("Reload")
            }
        }
    }
}

@Composable
private fun EmptyHistoryCard() {
    AppCard {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("No testResults", fontWeight = FontWeight.SemiBold, color = TextStrong)
            Text("Firestore has no test result records yet. No mock data is shown and CSV export is disabled.", fontSize = 12.5.sp, color = TextMuted)
        }
    }
}

@Composable
private fun HistoryRecordCard(result: TestResult, fallbackIndex: Int) {
    val hasGroundTruth = result.trueLocationId != null
    AppCard {
        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(12.dp)
        ) {
            Checkbox(checked = hasGroundTruth, onCheckedChange = null)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.weight(1f)) {
                Text(formatDateTime(result.createdAt), fontSize = 11.5.sp, color = TextMuted)
                Text(formatPredictedLocation(result, fallbackIndex), fontWeight = FontWeight.SemiBold, color = TextStrong)
                Text(formatDevice(result), fontSize = 12.sp, color = TextMuted)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatusPill(text = "GPS Error ${formatMeters(result.gpsErrorMeters)}", color = GpsRed, background = RedTint)
                    StatusPill(text = "Wi-Fi Error ${formatMeters(result.wifiErrorMeters)}", color = GpsRed, background = RedTint)
                    StatusPill(text = "Confidence ${formatConfidence(result.confidence)}", color = ActionBlue, background = BlueTint)
                    StatusPill(
                        text = if (hasGroundTruth) "Ground Truth set" else "No Ground Truth",
                        color = if (hasGroundTruth) CollectGreen else TextMuted,
                        background = if (hasGroundTruth) GreenTint else GrayTint
                    )
                }
            }
        }
    }
}

private fun formatPredictedLocation(result: TestResult, fallbackIndex: Int): String {
    val parts = listOfNotNull(
        result.predictedBuildingId,
        result.predictedFloorId,
        result.predictedPositionName,
        result.predictedSubPosition
    ).filter { it.isNotBlank() }

    return parts.joinToString(" / ").ifBlank {
        result.predictedLocationId ?: result.documentId.ifBlank { "Test result #${fallbackIndex + 1}" }
    }
}

private fun formatDevice(result: TestResult): String {
    val device = listOf(result.deviceBrand, result.deviceModel)
        .filter { it.isNotBlank() }
        .joinToString(" ")
        .ifBlank { "Unknown device" }
    val android = result.androidVersion.ifBlank { "Unknown Android" }
    return "$device / Android $android / AP ${result.apCount} / K ${result.knnK}"
}

private fun formatMeters(value: Double?): String {
    return value?.let { "${String.format(Locale.US, "%.1f", it)} m" } ?: "--"
}

private fun formatConfidence(value: Double?): String {
    return value?.let {
        val percent = if (it <= 1.0) it * 100.0 else it
        "${String.format(Locale.US, "%.0f", percent)}%"
    } ?: "--"
}

private fun formatDateTime(epochMillis: Long?): String {
    if (epochMillis == null) return "No timestamp"
    return SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.TAIWAN).format(Date(epochMillis))
}

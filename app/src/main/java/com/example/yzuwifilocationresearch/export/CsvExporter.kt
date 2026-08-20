package com.example.yzuwifilocationresearch.export

import com.example.yzuwifilocationresearch.model.AccessPoint
import com.example.yzuwifilocationresearch.model.TestResult
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CsvExporter {
    private const val UTF8_BOM = "\uFEFF"

    fun defaultTestResultsFileName(): String {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        return "testResults_$timestamp.csv"
    }

    fun testResultsToCsv(results: List<TestResult>): String {
        val rows = buildList {
            add(testResultHeaders)
            results.forEach { result ->
                add(result.toCsvRow())
            }
        }

        return UTF8_BOM + rows.joinToString(separator = "\n") { row ->
            row.joinToString(separator = ",", transform = ::escapeCsv)
        }
    }

    private val testResultHeaders = listOf(
        "documentId",
        "createdAtText",
        "createdAtEpochMs",
        "trueLocationId",
        "deviceBrand",
        "deviceModel",
        "androidVersion",
        "gpsLatitude",
        "gpsLongitude",
        "gpsAccuracyMeters",
        "gpsTimestampEpochMs",
        "predictedLocationId",
        "predictedBuildingId",
        "predictedFloorId",
        "predictedPositionName",
        "predictedSubPosition",
        "knnK",
        "confidence",
        "apCount",
        "gpsErrorMeters",
        "wifiErrorMeters",
        "accessPoints"
    )

    private fun TestResult.toCsvRow(): List<String> = listOf(
        documentId,
        formatDateTime(createdAt),
        createdAt?.toString().orEmpty(),
        trueLocationId.orEmpty(),
        deviceBrand,
        deviceModel,
        androidVersion,
        gpsLatitude?.toString().orEmpty(),
        gpsLongitude?.toString().orEmpty(),
        gpsAccuracy?.toString().orEmpty(),
        gpsTimestamp?.toString().orEmpty(),
        predictedLocationId.orEmpty(),
        predictedBuildingId.orEmpty(),
        predictedFloorId.orEmpty(),
        predictedPositionName.orEmpty(),
        predictedSubPosition.orEmpty(),
        knnK.toString(),
        confidence?.toString().orEmpty(),
        apCount.toString(),
        gpsErrorMeters?.toString().orEmpty(),
        wifiErrorMeters?.toString().orEmpty(),
        accessPoints.joinToString(separator = " | ") { accessPoint ->
            accessPoint.toCsvSummary()
        }
    )

    private fun AccessPoint.toCsvSummary(): String {
        return listOf(
            ssid,
            bssid,
            "freq=$frequency",
            "count=$appearanceCount",
            "meanRssi=$meanRssi",
            "minRssi=$minRssi",
            "maxRssi=$maxRssi",
            "sd=$standardDeviation"
        ).joinToString(separator = ";")
    }

    private fun escapeCsv(value: String): String {
        val mustQuote = value.any { it == ',' || it == '"' || it == '\n' || it == '\r' }
        val escaped = value.replace("\"", "\"\"")
        return if (mustQuote) "\"$escaped\"" else escaped
    }

    private fun formatDateTime(epochMillis: Long?): String {
        if (epochMillis == null) return ""
        return SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.TAIWAN).format(Date(epochMillis))
    }
}

package com.example.yzuwifilocationresearch.navigation

sealed class AppDestination(
    val route: String,
    val label: String
) {
    data object Home : AppDestination("home", "首頁")
    data object Collect : AppDestination("collect", "採集")
    data object ScanLoading : AppDestination("scan_loading", "掃描")
    data object Result : AppDestination("result", "結果")
    data object History : AppDestination("history", "歷史")
    data object LocationEdit : AppDestination("location_edit", "位置")
}

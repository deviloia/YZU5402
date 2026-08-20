package com.example.yzuwifilocationresearch.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.yzuwifilocationresearch.ui.collect.CollectScreen
import com.example.yzuwifilocationresearch.ui.history.HistoryScreen
import com.example.yzuwifilocationresearch.ui.home.HomeScreenRedesign
import com.example.yzuwifilocationresearch.ui.locationedit.LocationEditScreen
import com.example.yzuwifilocationresearch.ui.result.ResultScreen
import com.example.yzuwifilocationresearch.ui.scan.ScanLoadingScreen

@Composable
fun AppNavGraph(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = AppDestination.Home.route
    ) {
        composable(AppDestination.Home.route) {
            HomeScreenRedesign(
                onCollectClick = { navController.navigateSingleTop(AppDestination.Collect.route) },
                onScanClick = { navController.navigateSingleTop(AppDestination.ScanLoading.route) },
                onHistoryClick = { navController.navigateSingleTop(AppDestination.History.route) }
            )
        }
        composable(AppDestination.Collect.route) {
            CollectScreen(
                onBackClick = { navController.popBackStack() },
                onHomeClick = { navController.navigateSingleTop(AppDestination.Home.route) },
                onCollectClick = { navController.navigateSingleTop(AppDestination.Collect.route) },
                onScanClick = { navController.navigateSingleTop(AppDestination.ScanLoading.route) },
                onHistoryClick = { navController.navigateSingleTop(AppDestination.History.route) }
            )
        }
        composable(AppDestination.ScanLoading.route) {
            ScanLoadingScreen(
                onBackClick = { navController.popBackStack() },
                onFinished = {
                    navController.navigate(AppDestination.Result.route) {
                        popUpTo(AppDestination.ScanLoading.route) {
                            inclusive = true
                        }
                    }
                },
                onHomeClick = { navController.navigateSingleTop(AppDestination.Home.route) },
                onCollectClick = { navController.navigateSingleTop(AppDestination.Collect.route) },
                onScanClick = { navController.navigateSingleTop(AppDestination.ScanLoading.route) },
                onHistoryClick = { navController.navigateSingleTop(AppDestination.History.route) }
            )
        }
        composable(AppDestination.Result.route) {
            ResultScreen(
                onBackClick = { navController.popBackStack() },
                onHomeClick = { navController.navigateSingleTop(AppDestination.Home.route) },
                onCollectClick = { navController.navigateSingleTop(AppDestination.Collect.route) },
                onScanClick = { navController.navigateSingleTop(AppDestination.ScanLoading.route) },
                onHistoryClick = { navController.navigateSingleTop(AppDestination.History.route) }
            )
        }
        composable(AppDestination.History.route) {
            HistoryScreen(
                onBackClick = { navController.popBackStack() },
                onEditLocationClick = { navController.navigate(AppDestination.LocationEdit.route) },
                onHomeClick = { navController.navigateSingleTop(AppDestination.Home.route) },
                onCollectClick = { navController.navigateSingleTop(AppDestination.Collect.route) },
                onScanClick = { navController.navigateSingleTop(AppDestination.ScanLoading.route) },
                onHistoryClick = { navController.navigateSingleTop(AppDestination.History.route) }
            )
        }
        composable(AppDestination.LocationEdit.route) {
            LocationEditScreen(
                onBackClick = { navController.popBackStack() },
                onHomeClick = { navController.navigateSingleTop(AppDestination.Home.route) },
                onCollectClick = { navController.navigateSingleTop(AppDestination.Collect.route) },
                onScanClick = { navController.navigateSingleTop(AppDestination.ScanLoading.route) },
                onHistoryClick = { navController.navigateSingleTop(AppDestination.History.route) }
            )
        }
    }
}

private fun NavHostController.navigateSingleTop(route: String) {
    if (currentDestination?.route == route) return

    navigate(route) {
        launchSingleTop = true
        popUpTo(AppDestination.Home.route) {
            saveState = true
        }
        restoreState = true
    }
}

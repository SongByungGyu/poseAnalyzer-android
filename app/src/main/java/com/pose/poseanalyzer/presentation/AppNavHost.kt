package com.pose.poseanalyzer.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.pose.poseanalyzer.presentation.history.HistoryListScreen
import com.pose.poseanalyzer.presentation.history.TrendScreen
import com.pose.poseanalyzer.presentation.home.HomeScreen
import com.pose.poseanalyzer.presentation.measurement.MeasurementWizardScreen
import com.pose.poseanalyzer.presentation.result.AnalysisResultScreen
import com.pose.poseanalyzer.presentation.result.HistoryDetailScreen
import com.pose.poseanalyzer.presentation.settings.SettingsScreen
import com.pose.poseanalyzer.presentation.theme.AppColors
import com.pose.poseanalyzer.presentation.theme.AppTypography

/**
 * 앱 라우팅 정의.
 *
 * iOS `AppTabView` + `NavigationStack` 통합 대응.
 * Home / History는 하단 탭. Wizard/Result/Settings/Trend/HistoryDetail은 push.
 */
object Routes {
    const val HOME = "home"
    const val HISTORY = "history"
    const val SETTINGS = "settings"
    const val WIZARD = "wizard"
    const val RESULT = "result"
    const val TREND = "trend"
    const val HISTORY_DETAIL = "history/detail/{sessionId}"

    fun historyDetail(sessionId: String) = "history/detail/$sessionId"
}

private data class BottomTab(val route: String, val icon: androidx.compose.ui.graphics.vector.ImageVector, val label: String)

private val BOTTOM_TABS = listOf(
    BottomTab(Routes.HOME, Icons.Filled.Home, "홈"),
    BottomTab(Routes.HISTORY, Icons.Filled.History, "기록")
)

@Composable
fun AppNavHost(navController: NavHostController = rememberNavController()) {
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route

    val isBottomTabRoute = currentRoute == Routes.HOME || currentRoute == Routes.HISTORY

    Scaffold(
        bottomBar = {
            if (isBottomTabRoute) {
                NavigationBar(containerColor = AppColors.SurfaceElevated) {
                    BOTTOM_TABS.forEach { tab ->
                        val selected = backStack?.destination?.hierarchy?.any { it.route == tab.route } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(tab.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(tab.icon, contentDescription = tab.label) },
                            label = { Text(tab.label, style = AppTypography.micro) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            composable(Routes.HOME) {
                HomeScreen(
                    onStartMeasurement = { navController.navigate(Routes.WIZARD) },
                    onOpenSettings = { navController.navigate(Routes.SETTINGS) },
                    onOpenLatestResult = { sessionId ->
                        navController.navigate(Routes.historyDetail(sessionId))
                    }
                )
            }
            composable(Routes.HISTORY) {
                HistoryListScreen(
                    onBack = { navController.navigate(Routes.HOME) {
                        popUpTo(Routes.HOME) { inclusive = false }
                        launchSingleTop = true
                    }},
                    onItemClick = { sessionId ->
                        navController.navigate(Routes.historyDetail(sessionId))
                    },
                    onOpenTrend = { navController.navigate(Routes.TREND) }
                )
            }
            composable(Routes.SETTINGS) {
                SettingsScreen(onBack = { navController.popBackStack() })
            }
            composable(Routes.WIZARD) {
                MeasurementWizardScreen(
                    onCompleted = {
                        navController.navigate(Routes.RESULT) {
                            popUpTo(Routes.WIZARD) { inclusive = true }
                        }
                    },
                    onCancel = { navController.popBackStack() }
                )
            }
            composable(Routes.RESULT) {
                AnalysisResultScreen(
                    onBack = {
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.HOME) { inclusive = false }
                            launchSingleTop = true
                        }
                    }
                )
            }
            composable(Routes.TREND) {
                TrendScreen(onBack = { navController.popBackStack() })
            }
            composable(Routes.HISTORY_DETAIL) { backStackEntry ->
                val sessionId = backStackEntry.arguments?.getString("sessionId") ?: ""
                HistoryDetailScreen(
                    sessionId = sessionId,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}

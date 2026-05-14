package com.pose.poseanalyzer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.pose.poseanalyzer.presentation.history.HistoryListScreen
import com.pose.poseanalyzer.presentation.history.TrendScreen
import com.pose.poseanalyzer.presentation.home.HomeScreen
import com.pose.poseanalyzer.presentation.measurement.MeasurementWizardScreen
import com.pose.poseanalyzer.presentation.result.AnalysisResultScreen
import com.pose.poseanalyzer.presentation.theme.AppColors
import com.pose.poseanalyzer.presentation.theme.PoseTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * 진입점 — TopRoute 기반 단순 라우팅.
 *
 * Plan A2d에서 Compose Navigation + bottom tab으로 본격 분리 예정.
 *   - Home: HomeScreen
 *   - Wizard: MeasurementWizardScreen
 *   - Result: AnalysisResultScreen (저장 가능)
 *   - HistoryList: 기록 리스트
 *   - HistoryDetail: 기록에서 진입한 결과 (readOnly)
 *   - Trend: 추이 그래프
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PoseTheme { AppRoot() }
        }
    }
}

private enum class TopRoute { Home, Wizard, Result, History, HistoryDetail, Trend }

@Composable
private fun AppRoot() {
    var route by remember { mutableStateOf(TopRoute.Home) }

    Surface(modifier = Modifier.fillMaxSize(), color = AppColors.Surface) {
        when (route) {
            TopRoute.Home -> HomeScreen(
                onStartMeasurement = { route = TopRoute.Wizard },
                onOpenSettings = { route = TopRoute.History }, // 임시 — Plan A2d에서 Settings 분리
                onOpenLatestResult = { route = TopRoute.History }
            )
            TopRoute.Wizard -> MeasurementWizardScreen(
                onCompleted = { route = TopRoute.Result },
                onCancel = { route = TopRoute.Home }
            )
            TopRoute.Result -> AnalysisResultScreen(
                onBack = { route = TopRoute.Home }
            )
            TopRoute.History -> HistoryListScreen(
                onBack = { route = TopRoute.Home },
                onItemClick = { route = TopRoute.HistoryDetail },
                onOpenTrend = { route = TopRoute.Trend }
            )
            TopRoute.HistoryDetail -> {
                // Plan A2c 후속: HistoryDetail (readOnly) 별도 ViewModel/Screen.
                // 지금은 일단 History로 되돌리는 placeholder — Plan A2d에서 본격 구현.
                AnalysisResultScreen(onBack = { route = TopRoute.History })
            }
            TopRoute.Trend -> TrendScreen(onBack = { route = TopRoute.History })
        }
    }
}

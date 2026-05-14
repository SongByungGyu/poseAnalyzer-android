package com.pose.poseanalyzer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.pose.poseanalyzer.presentation.common.AppButton
import com.pose.poseanalyzer.presentation.common.AppNavBar
import com.pose.poseanalyzer.presentation.home.HomeScreen
import com.pose.poseanalyzer.presentation.measurement.MeasurementWizardScreen
import com.pose.poseanalyzer.presentation.theme.AppColors
import com.pose.poseanalyzer.presentation.theme.AppSpacing
import com.pose.poseanalyzer.presentation.theme.AppTypography
import com.pose.poseanalyzer.presentation.theme.PoseTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * 임시 진입점.
 *
 * Plan A2d에서 Compose Navigation + bottom tab으로 본격 분리 예정.
 * 지금은 state 기반 화면 전환:
 *   - HOME: HomeScreen
 *   - WIZARD: MeasurementWizardScreen (분석 완료 시 RESULT_PLACEHOLDER)
 *   - RESULT_PLACEHOLDER: 임시 결과 화면 (Plan A2c에서 AnalysisResultScreen 작성)
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

private enum class TopRoute { Home, Wizard, ResultPlaceholder }

@Composable
private fun AppRoot() {
    var route by remember { mutableStateOf(TopRoute.Home) }
    var resultSessionId by remember { mutableStateOf<String?>(null) }

    Surface(modifier = Modifier.fillMaxSize(), color = AppColors.Surface) {
        when (route) {
            TopRoute.Home -> HomeScreen(
                onStartMeasurement = { route = TopRoute.Wizard },
                onOpenSettings = { /* Plan A2d */ },
                onOpenLatestResult = { sessionId ->
                    resultSessionId = sessionId
                    route = TopRoute.ResultPlaceholder
                }
            )
            TopRoute.Wizard -> MeasurementWizardScreen(
                onCompleted = { sessionId ->
                    resultSessionId = sessionId
                    route = TopRoute.ResultPlaceholder
                },
                onCancel = { route = TopRoute.Home }
            )
            TopRoute.ResultPlaceholder -> ResultPlaceholder(
                sessionId = resultSessionId,
                onBack = { route = TopRoute.Home }
            )
        }
    }
}

/** Plan A2c에서 AnalysisResultScreen으로 교체 예정. */
@Composable
private fun ResultPlaceholder(sessionId: String?, onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        AppNavBar(title = "측정 결과", onBack = onBack)
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(AppSpacing.s4),
                modifier = Modifier.padding(AppSpacing.s5)
            ) {
                Text("분석이 완료되었습니다", style = AppTypography.title, color = AppColors.OnSurface)
                Text(
                    "Session ID: ${sessionId ?: "(없음)"}",
                    style = AppTypography.callout,
                    color = AppColors.OnSurfaceSecondary
                )
                Text(
                    "Plan A2c에서 8가지 자세별 결과 카드 + 비대칭 분석 + 사진 위 관절 오버레이를 표시합니다.",
                    style = AppTypography.body,
                    color = AppColors.OnSurfaceSecondary
                )
                AppButton("홈으로", onClick = onBack)
            }
        }
    }
}

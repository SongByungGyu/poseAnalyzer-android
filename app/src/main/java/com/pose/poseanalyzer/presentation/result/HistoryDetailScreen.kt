package com.pose.poseanalyzer.presentation.result

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pose.poseanalyzer.domain.model.AsymmetryResult
import com.pose.poseanalyzer.domain.model.PoseFrame
import com.pose.poseanalyzer.domain.model.PostureResult
import com.pose.poseanalyzer.presentation.common.AppCard
import com.pose.poseanalyzer.presentation.common.AppEmptyState
import com.pose.poseanalyzer.presentation.common.AppNavBar
import com.pose.poseanalyzer.presentation.common.SectionHeader
import com.pose.poseanalyzer.presentation.theme.AppColors
import com.pose.poseanalyzer.presentation.theme.AppShapes
import com.pose.poseanalyzer.presentation.theme.AppSpacing
import com.pose.poseanalyzer.presentation.theme.AppTypography

/**
 * 기록 탭에서 진입한 결과 — 저장된 세션을 SessionRepository에서 fetch.
 *
 * 사진 위 관절 오버레이는 빈 frame이므로 사진만 표시.
 * 직전 비교 섹션은 표시하지 않음 (readOnly).
 *
 * iOS `AnalysisResultDetailView` 대응.
 */
@Composable
fun HistoryDetailScreen(
    sessionId: String,
    onBack: () -> Unit,
    viewModel: HistoryDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    LaunchedEffect(sessionId) { viewModel.load(sessionId) }

    Surface(modifier = Modifier.fillMaxSize(), color = AppColors.Surface) {
        Column(modifier = Modifier.fillMaxSize()) {
            AppNavBar(title = "측정 기록", onBack = onBack)

            if (state.notFound) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    AppEmptyState(
                        icon = Icons.Filled.ErrorOutline,
                        title = "기록을 불러올 수 없습니다",
                        description = "사진 파일이 누락되었거나 데이터가 손상되었습니다."
                    )
                }
                return@Column
            }

            val report = state.report
            if (report == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("불러오는 중…", style = AppTypography.body, color = AppColors.OnSurfaceSecondary)
                }
                return@Column
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = AppSpacing.s4)
                    .padding(bottom = AppSpacing.s9),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.s5)
            ) {
                Row(
                    modifier = Modifier.padding(top = AppSpacing.s2),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.s2)
                ) {
                    PhotoOnlyCard(
                        bitmap = report.frontImage, frame = report.frontFrame, label = "정면",
                        modifier = Modifier.weight(1f)
                    )
                    PhotoOnlyCard(
                        bitmap = report.sideImage, frame = report.sideFrame, label = "측면",
                        modifier = Modifier.weight(1f)
                    )
                }

                SectionHeader("자세 판정 (8가지)")
                Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.s2)) {
                    report.postures.forEach { PostureResultCard(result = it) }
                }

                SectionHeader("좌우 비대칭")
                AppCard {
                    AsymmetryRow(label = "어깨", diff = report.asymmetry.shoulder)
                    HorizontalDivider(modifier = Modifier.padding(vertical = AppSpacing.s2), color = AppColors.Divider)
                    AsymmetryRow(label = "골반", diff = report.asymmetry.hip)
                }
            }
        }
    }
}

@Composable
private fun PhotoOnlyCard(
    bitmap: android.graphics.Bitmap,
    frame: PoseFrame,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(AppSpacing.s1)) {
        Text(label, style = AppTypography.micro, color = AppColors.OnSurfaceTertiary)
        Surface(
            shape = AppShapes.medium,
            border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.Divider),
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.75f)
        ) {
            // frame이 비어 있어도 PoseOverlayCanvas는 사진만 표시
            PoseOverlayCanvas(bitmap = bitmap, frame = frame, modifier = Modifier.fillMaxSize())
        }
    }
}

@Composable
private fun AsymmetryRow(label: String, diff: AsymmetryResult.Difference) {
    val isBalanced = diff.direction == AsymmetryResult.Direction.BALANCED
    val dirColor = if (isBalanced) AppColors.StatusNormal else AppColors.StatusCaution
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = AppTypography.body, color = AppColors.OnSurfaceSecondary, modifier = Modifier.width(40.dp))
        Text(diff.direction.koreanName, style = AppTypography.body, color = dirColor)
        Spacer(modifier = Modifier.weight(1f))
        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
            diff.cm?.let { cm ->
                Text(String.format("%.1fcm 차이", cm), style = AppTypography.caption, color = AppColors.OnSurface)
            }
            Text(String.format("기울기 %.1f°", diff.angleDegrees), style = AppTypography.micro, color = AppColors.OnSurfaceTertiary)
        }
    }
}

package com.pose.poseanalyzer.presentation.result

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pose.poseanalyzer.domain.model.PostureResult
import com.pose.poseanalyzer.domain.model.PostureStatus
import com.pose.poseanalyzer.presentation.common.StatusBadge
import com.pose.poseanalyzer.presentation.theme.AppColors
import com.pose.poseanalyzer.presentation.theme.AppShapes
import com.pose.poseanalyzer.presentation.theme.AppSpacing
import com.pose.poseanalyzer.presentation.theme.AppTypography

/**
 * 개별 자세 결과 카드.
 *
 * iOS `PostureResultCard.swift` 1:1 대응.
 * - 좌측 status indicator strip
 * - 자세명 + StatusBadge
 * - 큰 수치 (°/ratio)
 * - 게이지 (status 위치 표시)
 * - 조언 (있을 때만)
 */
@Composable
fun PostureResultCard(result: PostureResult, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = AppShapes.large,
        color = AppColors.SurfaceElevated,
        border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.Divider),
        shadowElevation = 1.dp
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            // 좌측 status indicator strip
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(80.dp)  // strip 최소 높이 — 내용에 따라 카드가 더 커짐
                    .padding(vertical = AppSpacing.s3)
                    .background(statusColor(result.status))
            )
            Column(
                modifier = Modifier
                    .padding(horizontal = AppSpacing.s4, vertical = AppSpacing.s3)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.s2)
            ) {
                // 헤더 (자세명 + 뱃지)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        result.type.koreanName,
                        style = AppTypography.headline,
                        color = AppColors.OnSurface,
                        modifier = Modifier.weight(1f)
                    )
                    StatusBadge(result.status)
                }
                // 수치
                MetricRow(result)
                // 게이지
                if (result.status != PostureStatus.UNMEASURABLE) {
                    StatusGauge(result.status)
                }
                // 조언
                result.advice?.let { advice ->
                    Text(advice, style = AppTypography.caption, color = AppColors.OnSurfaceSecondary)
                }
            }
        }
    }
}

@Composable
private fun MetricRow(result: PostureResult) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            if (result.status == PostureStatus.UNMEASURABLE) {
                Text(
                    "—",
                    style = AppTypography.display.copy(fontSize = 26.sp, fontWeight = FontWeight.Bold),
                    color = AppColors.OnSurfaceTertiary
                )
            } else {
                val text = if (result.type == com.pose.poseanalyzer.domain.model.PostureType.ROUND_SHOULDER) {
                    String.format("%.0f", result.deviationValue)
                } else {
                    String.format("%.1f", result.deviationValue)
                }
                Text(
                    text,
                    style = AppTypography.display.copy(fontSize = 26.sp, fontWeight = FontWeight.Bold),
                    color = AppColors.OnSurface
                )
                Text(
                    result.deviationUnitSymbol,
                    style = AppTypography.callout,
                    color = AppColors.OnSurfaceTertiary,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
        }
        if (result.status != PostureStatus.UNMEASURABLE) {
            Text(
                result.deviationLabel,
                style = AppTypography.caption.copy(fontSize = 12.sp),
                color = AppColors.OnSurfaceTertiary
            )
        }
    }
}

@Composable
private fun StatusGauge(status: PostureStatus) {
    val markerFraction = when (status) {
        PostureStatus.NORMAL -> 0.18f
        PostureStatus.CAUTION -> 0.52f
        PostureStatus.SUSPECT -> 0.84f
        PostureStatus.UNMEASURABLE -> 0.50f
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(10.dp)
    ) {
        // gradient bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .align(Alignment.CenterStart)
                .clip(androidx.compose.foundation.shape.RoundedCornerShape(2.dp))
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            AppColors.StatusNormal.copy(alpha = 0.22f),
                            AppColors.StatusCaution.copy(alpha = 0.22f),
                            AppColors.StatusSuspect.copy(alpha = 0.22f)
                        )
                    )
                )
        )
        // marker
        Box(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(10.dp)
                    .align(Alignment.CenterStart)
                    .padding(start = with(androidx.compose.ui.platform.LocalDensity.current) {
                        // marker는 fraction 위치로 옮김 — Compose에선 Spacer + fraction이 깔끔
                        0.dp
                    })
                    .background(AppColors.OnSurface)
            )
            // 위 marker는 위치 0이라 안 보임 — 대신 Row + Spacer fraction으로 정확히 배치
            Row(modifier = Modifier.fillMaxWidth()) {
                Spacer(modifier = Modifier.weight(markerFraction))
                Box(modifier = Modifier
                    .width(3.dp)
                    .height(10.dp)
                    .background(AppColors.OnSurface))
                Spacer(modifier = Modifier.weight(1f - markerFraction))
            }
        }
    }
}

@Composable
@ReadOnlyComposable
private fun statusColor(status: PostureStatus) = when (status) {
    PostureStatus.NORMAL -> AppColors.StatusNormal
    PostureStatus.CAUTION -> AppColors.StatusCaution
    PostureStatus.SUSPECT -> AppColors.StatusSuspect
    PostureStatus.UNMEASURABLE -> AppColors.StatusUnmeasurable
}

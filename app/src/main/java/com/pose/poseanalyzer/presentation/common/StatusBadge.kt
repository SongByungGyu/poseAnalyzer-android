package com.pose.poseanalyzer.presentation.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pose.poseanalyzer.domain.model.PostureStatus
import com.pose.poseanalyzer.presentation.theme.AppColors
import com.pose.poseanalyzer.presentation.theme.AppSpacing
import com.pose.poseanalyzer.presentation.theme.AppTypography
import com.pose.poseanalyzer.presentation.theme.PoseTheme

/**
 * 4단계 상태 라벨 (정상 / 주의 / 의심 / 측정 불가).
 *
 * iOS `StatusBadge.swift` 1:1 대응.
 */
@Composable
fun StatusBadge(
    status: PostureStatus,
    modifier: Modifier = Modifier
) {
    val color = when (status) {
        PostureStatus.NORMAL -> AppColors.StatusNormal
        PostureStatus.CAUTION -> AppColors.StatusCaution
        PostureStatus.SUSPECT -> AppColors.StatusSuspect
        PostureStatus.UNMEASURABLE -> AppColors.StatusUnmeasurable
    }
    Text(
        text = status.koreanName,
        style = AppTypography.micro,
        color = Color.White,
        modifier = modifier
            .background(color, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    )
}

@Preview
@Composable
private fun PreviewStatusBadge() {
    PoseTheme {
        Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.s2), modifier = Modifier.padding(AppSpacing.s4)) {
            StatusBadge(PostureStatus.NORMAL)
            StatusBadge(PostureStatus.CAUTION)
            StatusBadge(PostureStatus.SUSPECT)
            StatusBadge(PostureStatus.UNMEASURABLE)
        }
    }
}

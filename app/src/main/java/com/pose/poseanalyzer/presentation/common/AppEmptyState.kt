package com.pose.poseanalyzer.presentation.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.pose.poseanalyzer.presentation.theme.AppColors
import com.pose.poseanalyzer.presentation.theme.AppSpacing
import com.pose.poseanalyzer.presentation.theme.AppTypography

/**
 * 데이터 없음 등 비어 있는 상태 표시 (아이콘 + 제목 + 설명).
 *
 * iOS `AppEmptyState.swift` 1:1 대응.
 */
@Composable
fun AppEmptyState(
    icon: ImageVector,
    title: String,
    description: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(AppSpacing.s6),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppSpacing.s3)
    ) {
        Icon(
            icon, contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = AppColors.OnSurfaceTertiary
        )
        Text(title, style = AppTypography.headline, color = AppColors.OnSurface)
        Text(
            description, style = AppTypography.body, color = AppColors.OnSurfaceSecondary,
            textAlign = TextAlign.Center
        )
    }
}

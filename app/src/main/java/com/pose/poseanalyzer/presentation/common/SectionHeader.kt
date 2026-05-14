package com.pose.poseanalyzer.presentation.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.pose.poseanalyzer.presentation.theme.AppColors
import com.pose.poseanalyzer.presentation.theme.AppSpacing
import com.pose.poseanalyzer.presentation.theme.AppTypography

/**
 * 리스트/카드 섹션 상단 — 제목 + 옵션 trailing 액션.
 *
 * iOS `SectionHeader.swift` 1:1 대응.
 */
@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    trailingText: String? = null,
    onTrailingClick: (() -> Unit)? = null
) {
    Row(
        modifier = modifier.padding(vertical = AppSpacing.s2),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(title, style = AppTypography.title, color = AppColors.OnSurface)
        if (trailingText != null && onTrailingClick != null) {
            TextButton(onClick = onTrailingClick) {
                Text(trailingText, style = AppTypography.callout, color = AppColors.BrandPrimary)
            }
        }
    }
}

package com.pose.poseanalyzer.presentation.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.pose.poseanalyzer.presentation.theme.AppColors
import com.pose.poseanalyzer.presentation.theme.AppShapes
import com.pose.poseanalyzer.presentation.theme.AppSpacing

/**
 * 콘텐츠를 감싸는 표준 카드 — 흰 배경, 12dp radius, 1dp elevation.
 *
 * iOS `AppCard.swift` 1:1 대응.
 */
@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    contentPadding: Dp = AppSpacing.s4,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier,
        shape = AppShapes.medium,
        colors = CardDefaults.cardColors(containerColor = AppColors.SurfaceElevated),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(contentPadding), content = content)
    }
}

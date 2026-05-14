package com.pose.poseanalyzer.presentation.measurement

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pose.poseanalyzer.presentation.theme.AppColors
import com.pose.poseanalyzer.presentation.theme.AppSpacing
import com.pose.poseanalyzer.presentation.theme.AppTypography

/**
 * 분석 중 풀스크린.
 *
 * iOS `AnalyzingView.swift` 1:1 대응.
 */
@Composable
fun AnalyzingScreen(phase: String) {
    Surface(modifier = Modifier.fillMaxSize(), color = AppColors.Surface) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(
                color = AppColors.BrandPrimary,
                strokeWidth = 4.dp,
                modifier = Modifier.size(56.dp)
            )
            Text(
                phase,
                style = AppTypography.headline,
                color = AppColors.OnSurface,
                modifier = Modifier.padding(top = AppSpacing.s5, bottom = AppSpacing.s2)
            )
            Text(
                "잠시만 기다려주세요…",
                style = AppTypography.callout,
                color = AppColors.OnSurfaceSecondary
            )
        }
    }
}

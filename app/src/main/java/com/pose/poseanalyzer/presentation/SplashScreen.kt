package com.pose.poseanalyzer.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.RadialGradientShader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pose.poseanalyzer.presentation.theme.AppColors
import com.pose.poseanalyzer.presentation.theme.AppSpacing

/**
 * Splash 화면 — 인디고 그라디언트 + 브랜드 마크 + 워드마크.
 *
 * iOS `LaunchView.swift` 1:1 대응.
 */
@Composable
fun SplashScreen(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        AppColors.BrandPrimaryLight,
                        AppColors.BrandPrimary,
                        AppColors.BrandPrimaryDark
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.size(280.dp)
        ) {
            Spacer(modifier = Modifier.size(AppSpacing.s5))
            LaunchMark()
            Spacer(modifier = Modifier.size(AppSpacing.s4))
            Text(
                "PoseAnalyzer",
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                letterSpacing = (-0.6).sp
            )
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 48.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Text(
                "POSTURE ANALYSIS · 자세 분석",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.65f),
                letterSpacing = 2.sp
            )
        }
    }
}

@Composable
private fun LaunchMark() {
    Canvas(modifier = Modifier.size(120.dp)) {
        val s = size.minDimension / 200f
        val mint = Color(0xFF56BAB0)
        // 머리
        drawCircle(
            color = Color.White,
            radius = 14f * s,
            center = Offset(100f * s, 56f * s)
        )
        // 어깨 바
        drawLine(
            color = Color.White,
            start = Offset(60f * s, 80f * s),
            end = Offset(140f * s, 80f * s),
            strokeWidth = 8f * s,
            cap = StrokeCap.Round
        )
        // 척추 컬럼
        drawLine(
            color = Color.White,
            start = Offset(100f * s, 74f * s),
            end = Offset(100f * s, 140f * s),
            strokeWidth = 10f * s,
            cap = StrokeCap.Round
        )
        // 골반 바
        drawLine(
            color = Color.White,
            start = Offset(74f * s, 136f * s),
            end = Offset(126f * s, 136f * s),
            strokeWidth = 8f * s,
            cap = StrokeCap.Round
        )
        // mint 정렬 도트
        drawCircle(
            color = mint,
            radius = 4.5f * s,
            center = Offset(100f * s, 107f * s)
        )
    }
}

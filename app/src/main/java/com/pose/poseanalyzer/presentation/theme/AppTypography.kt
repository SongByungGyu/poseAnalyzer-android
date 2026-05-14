package com.pose.poseanalyzer.presentation.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * PoseAnalyzer 타이포 토큰 (합니다체 톤 — "중립 관찰자").
 *
 * 시스템 폰트(SDK 기본)를 그대로 사용. iOS `AppFont.swift` 1:1 대응.
 */
object AppTypography {
    val display = TextStyle(fontSize = 34.sp, fontWeight = FontWeight.Black, letterSpacing = (-0.5).sp)
    val title = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.3).sp)
    val headline = TextStyle(fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
    val body = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Normal, lineHeight = 22.sp)
    val callout = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium)
    val caption = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Normal)
    val micro = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.5.sp)
}

/** Material 3 Typography로도 노출 */
val PoseTypography = Typography(
    displayMedium = AppTypography.display,
    titleLarge = AppTypography.title,
    titleMedium = AppTypography.headline,
    bodyLarge = AppTypography.body,
    bodyMedium = AppTypography.callout,
    bodySmall = AppTypography.caption,
    labelSmall = AppTypography.micro
)

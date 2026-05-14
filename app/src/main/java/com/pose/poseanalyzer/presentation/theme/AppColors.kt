package com.pose.poseanalyzer.presentation.theme

import androidx.compose.ui.graphics.Color

/**
 * PoseAnalyzer 브랜드 컬러 토큰.
 *
 * Pose Indigo (#3B5BDB)를 중심으로 한 4단계 상태 컬러.
 * iOS `AppColor.swift` 1:1 대응.
 */
object AppColors {
    // Brand
    val BrandPrimary = Color(0xFF3B5BDB)
    val BrandPrimaryDark = Color(0xFF2A47C3)
    val BrandPrimaryLight = Color(0xFF5B6EE8)

    // Status (PostureStatus)
    val StatusNormal = Color(0xFF56BAB0)       // mint
    val StatusCaution = Color(0xFFE3B341)      // amber
    val StatusSuspect = Color(0xFFE07A5F)      // orange-coral
    val StatusUnmeasurable = Color(0xFF9BA1A6) // neutral gray

    // Neutrals (Light Mode)
    val Surface = Color(0xFFF7F8FA)
    val SurfaceElevated = Color(0xFFFFFFFF)
    val SurfaceMuted = Color(0xFFEEF0F3)
    val OnSurface = Color(0xFF1B1F26)
    val OnSurfaceSecondary = Color(0xFF5A6068)
    val OnSurfaceTertiary = Color(0xFF8B9099)
    val Divider = Color(0xFFE2E5EA)
}

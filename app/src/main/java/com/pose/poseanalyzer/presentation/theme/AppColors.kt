package com.pose.poseanalyzer.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

/**
 * PoseAnalyzer 브랜드 컬러 토큰.
 *
 * 각 토큰은 @Composable getter — 사용 시점에 isSystemInDarkTheme() 분기.
 * 사용 패턴은 기존과 동일: `AppColors.BrandPrimary` (단, @Composable scope 안에서만).
 *
 * 라이트 hex는 iOS `AppColor.swift`와 일부 일치 / 일부 Android 고유.
 * 다크 hex: Brand·Surface·OnSurface·Divider는 iOS 차용, Status는 라이트 hex 명도 +10% (Android mint 톤 유지).
 */
object AppColors {
    // Brand
    val BrandPrimary: Color
        @Composable @ReadOnlyComposable
        get() = if (isSystemInDarkTheme()) Color(0xFF5B6EE8) else Color(0xFF3B5BDB)

    val BrandPrimaryDark: Color
        @Composable @ReadOnlyComposable
        get() = if (isSystemInDarkTheme()) Color(0xFF4054BB) else Color(0xFF2A47C3)

    val BrandPrimaryLight: Color
        @Composable @ReadOnlyComposable
        get() = if (isSystemInDarkTheme()) Color(0xFF7A8BEC) else Color(0xFF5B6EE8)

    // Status (PostureStatus) — 4단계, 라이트 hex 명도 +10% 로 다크 hex 도출
    val StatusNormal: Color
        @Composable @ReadOnlyComposable
        get() = if (isSystemInDarkTheme()) Color(0xFF6BC8BF) else Color(0xFF56BAB0)

    val StatusCaution: Color
        @Composable @ReadOnlyComposable
        get() = if (isSystemInDarkTheme()) Color(0xFFEAC471) else Color(0xFFE3B341)

    val StatusSuspect: Color
        @Composable @ReadOnlyComposable
        get() = if (isSystemInDarkTheme()) Color(0xFFE69680) else Color(0xFFE07A5F)

    val StatusUnmeasurable: Color
        @Composable @ReadOnlyComposable
        get() = if (isSystemInDarkTheme()) Color(0xFFB2B7BB) else Color(0xFF9BA1A6)

    // Surfaces — iOS dark variant 차용
    val Surface: Color
        @Composable @ReadOnlyComposable
        get() = if (isSystemInDarkTheme()) Color(0xFF1F2532) else Color(0xFFF7F8FA)

    val SurfaceElevated: Color
        @Composable @ReadOnlyComposable
        get() = if (isSystemInDarkTheme()) Color(0xFF161B26) else Color(0xFFFFFFFF)

    val SurfaceMuted: Color
        @Composable @ReadOnlyComposable
        get() = if (isSystemInDarkTheme()) Color(0xFF1F2532) else Color(0xFFEEF0F3)

    // Foregrounds — iOS dark variant 차용
    val OnSurface: Color
        @Composable @ReadOnlyComposable
        get() = if (isSystemInDarkTheme()) Color(0xFFF2F4F8) else Color(0xFF1B1F26)

    val OnSurfaceSecondary: Color
        @Composable @ReadOnlyComposable
        get() = if (isSystemInDarkTheme()) Color(0xFFB4BCCB) else Color(0xFF5A6068)

    val OnSurfaceTertiary: Color
        @Composable @ReadOnlyComposable
        get() = if (isSystemInDarkTheme()) Color(0xFF8A94A6) else Color(0xFF8B9099)

    // Border — iOS dark variant 차용
    val Divider: Color
        @Composable @ReadOnlyComposable
        get() = if (isSystemInDarkTheme()) Color(0xFF232A38) else Color(0xFFE2E5EA)
}

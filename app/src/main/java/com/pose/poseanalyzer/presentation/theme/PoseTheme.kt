package com.pose.poseanalyzer.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

/**
 * 앱 전역 테마 — 모든 화면을 이 컴포지블로 감싸야 토큰이 적용됨.
 *
 * Material 3 ColorScheme를 [AppColors]로 채움. 다크 모드는 1차 MVP 미지원
 * (iOS와 동일 — Plan B에서 확장 검토).
 */
@Composable
fun PoseTheme(content: @Composable () -> Unit) {
    val colorScheme = lightColorScheme(
        primary = AppColors.BrandPrimary,
        onPrimary = AppColors.SurfaceElevated,
        primaryContainer = AppColors.BrandPrimaryLight,
        background = AppColors.Surface,
        surface = AppColors.SurfaceElevated,
        surfaceVariant = AppColors.SurfaceMuted,
        onBackground = AppColors.OnSurface,
        onSurface = AppColors.OnSurface,
        onSurfaceVariant = AppColors.OnSurfaceSecondary,
        outline = AppColors.Divider
    )
    MaterialTheme(
        colorScheme = colorScheme,
        typography = PoseTypography,
        shapes = PoseShapes,
        content = content
    )
}

package com.pose.poseanalyzer.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

/**
 * 앱 전역 테마 — 모든 화면을 이 컴포지블로 감싸야 토큰이 적용됨.
 *
 * Material 3 ColorScheme를 [AppColors]로 채움. iOS와 동일하게 시스템 다크 모드 지원.
 * 화면이 직접 사용하는 [AppColors] 토큰들은 @Composable getter 패턴으로 자동 분기되며,
 * 여기선 추가로 Material 컴포넌트(Button·Card 등)가 사용하는 ColorScheme도 다크용으로 분기.
 */
@Composable
fun PoseTheme(content: @Composable () -> Unit) {
    val isDark = isSystemInDarkTheme()
    val colorScheme = if (isDark) {
        darkColorScheme(
            primary = AppColors.BrandPrimary,
            onPrimary = AppColors.OnSurface,
            primaryContainer = AppColors.BrandPrimaryDark,
            background = AppColors.Surface,
            surface = AppColors.SurfaceElevated,
            surfaceVariant = AppColors.SurfaceMuted,
            onBackground = AppColors.OnSurface,
            onSurface = AppColors.OnSurface,
            onSurfaceVariant = AppColors.OnSurfaceSecondary,
            outline = AppColors.Divider
        )
    } else {
        lightColorScheme(
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
    }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = PoseTypography,
        shapes = PoseShapes,
        content = content
    )
}

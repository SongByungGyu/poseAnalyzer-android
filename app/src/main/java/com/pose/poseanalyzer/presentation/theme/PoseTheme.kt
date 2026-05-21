package com.pose.poseanalyzer.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

/**
 * 앱 전역 테마 — 모든 화면을 이 컴포지블로 감싸야 토큰이 적용됨.
 *
 * Material 3 ColorScheme를 [AppColors]로 채움. **Android는 라이트 only.**
 * iOS는 모든 토큰을 light/dark variant로 정의해 다크 지원 — 양 플랫폼 divergence.
 * 다크 토큰·Theme 분기 추가는 별도 백로그 항목 참고 (`docs/backlog.md`).
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

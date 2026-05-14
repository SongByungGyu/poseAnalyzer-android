package com.pose.poseanalyzer.presentation.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pose.poseanalyzer.presentation.theme.AppColors
import com.pose.poseanalyzer.presentation.theme.AppSpacing
import com.pose.poseanalyzer.presentation.theme.AppTypography
import com.pose.poseanalyzer.presentation.theme.PoseTheme

enum class AppButtonVariant { Primary, Secondary, Text }

/**
 * 앱 표준 버튼.
 *
 * - Primary: 채움 (브랜드 색)
 * - Secondary: 테두리만
 * - Text: 텍스트만
 *
 * iOS `AppButton.swift` 1:1 대응.
 */
@Composable
fun AppButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: AppButtonVariant = AppButtonVariant.Primary,
    enabled: Boolean = true,
    loading: Boolean = false
) {
    val content: @Composable () -> Unit = {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            if (loading) {
                CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(16.dp))
            } else {
                Text(text, style = AppTypography.headline)
            }
        }
    }
    val btnModifier = modifier.height(48.dp)
    when (variant) {
        AppButtonVariant.Primary -> Button(
            onClick = onClick,
            enabled = enabled && !loading,
            modifier = btnModifier,
            colors = ButtonDefaults.buttonColors(
                containerColor = AppColors.BrandPrimary,
                contentColor = AppColors.SurfaceElevated
            )
        ) { content() }
        AppButtonVariant.Secondary -> OutlinedButton(
            onClick = onClick,
            enabled = enabled && !loading,
            modifier = btnModifier
        ) { content() }
        AppButtonVariant.Text -> TextButton(
            onClick = onClick,
            enabled = enabled && !loading,
            modifier = btnModifier.wrapContentWidth()
        ) { content() }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewButtons() {
    PoseTheme {
        Row(
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.s2),
            modifier = Modifier.padding(AppSpacing.s4)
        ) {
            AppButton("측정 시작", {})
            AppButton("취소", {}, variant = AppButtonVariant.Secondary)
            AppButton("자세히", {}, variant = AppButtonVariant.Text)
        }
    }
}

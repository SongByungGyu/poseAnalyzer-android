package com.pose.poseanalyzer.presentation.measurement

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.pose.poseanalyzer.presentation.theme.AppColors
import com.pose.poseanalyzer.presentation.theme.AppSpacing
import com.pose.poseanalyzer.presentation.theme.AppTypography

/**
 * "카메라" / "갤러리" 선택 BottomSheet.
 *
 * iOS `PhotoInputSheet.swift` 1:1 대응.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoInputBottomSheet(
    onCamera: () -> Unit,
    onGallery: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = AppColors.SurfaceElevated
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppSpacing.s4, vertical = AppSpacing.s3)
                .padding(bottom = AppSpacing.s5)
        ) {
            Text(
                "사진을 어떻게 추가할까요?",
                style = AppTypography.headline,
                color = AppColors.OnSurface,
                modifier = Modifier.padding(bottom = AppSpacing.s3)
            )
            OptionRow(
                icon = Icons.Filled.PhotoCamera,
                title = "카메라로 촬영",
                description = "가이드 오버레이를 따라 찍습니다",
                onClick = { onCamera(); onDismiss() }
            )
            Spacer(modifier = Modifier.size(AppSpacing.s2))
            OptionRow(
                icon = Icons.Filled.PhotoLibrary,
                title = "갤러리에서 선택",
                description = "이미 촬영한 사진을 사용합니다",
                onClick = { onGallery(); onDismiss() }
            )
        }
    }
}

@Composable
private fun OptionRow(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = AppColors.SurfaceMuted,
        shape = com.pose.poseanalyzer.presentation.theme.AppShapes.medium,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(AppSpacing.s4),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.s3)
        ) {
            Surface(
                color = AppColors.BrandPrimary,
                shape = CircleShape,
                modifier = Modifier.size(44.dp)
            ) {
                Icon(
                    icon, contentDescription = null,
                    modifier = Modifier
                        .size(44.dp)
                        .padding(10.dp),
                    tint = androidx.compose.ui.graphics.Color.White
                )
            }
            Column(modifier = Modifier.padding(end = AppSpacing.s2)) {
                Text(title, style = AppTypography.headline, color = AppColors.OnSurface)
                Text(description, style = AppTypography.caption, color = AppColors.OnSurfaceSecondary)
            }
        }
    }
}

@Composable
fun PhotoInputClickClickable(
    label: String = "사진 추가",
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        color = AppColors.BrandPrimary,
        shape = com.pose.poseanalyzer.presentation.theme.AppShapes.medium
    ) {
        Text(
            label,
            style = AppTypography.headline,
            color = androidx.compose.ui.graphics.Color.White,
            modifier = Modifier.padding(horizontal = AppSpacing.s5, vertical = AppSpacing.s3)
        )
    }
}

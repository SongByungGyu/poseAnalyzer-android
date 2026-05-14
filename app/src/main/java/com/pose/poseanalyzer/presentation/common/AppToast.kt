package com.pose.poseanalyzer.presentation.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.pose.poseanalyzer.presentation.theme.AppShapes
import com.pose.poseanalyzer.presentation.theme.AppSpacing
import com.pose.poseanalyzer.presentation.theme.AppTypography
import kotlinx.coroutines.delay

/**
 * 하단에서 올라오는 짧은 알림 (SnackbarHost 대체).
 *
 * iOS `AppToast.swift` 1:1 대응.
 */
@Composable
fun AppToast(
    message: String,
    visible: Boolean,
    modifier: Modifier = Modifier,
    durationMs: Long = 2000L,
    onDismiss: () -> Unit
) {
    LaunchedEffect(visible, message) {
        if (visible) {
            delay(durationMs)
            onDismiss()
        }
    }
    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.BottomCenter) {
        AnimatedVisibility(
            visible = visible,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut()
        ) {
            Text(
                text = message,
                style = AppTypography.callout,
                color = Color.White,
                modifier = Modifier
                    .padding(AppSpacing.s4)
                    .background(Color.Black.copy(alpha = 0.85f), AppShapes.medium)
                    .padding(horizontal = AppSpacing.s4, vertical = AppSpacing.s3)
            )
        }
    }
}

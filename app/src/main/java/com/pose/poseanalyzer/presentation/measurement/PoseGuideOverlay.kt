package com.pose.poseanalyzer.presentation.measurement

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.pose.poseanalyzer.domain.model.SessionView
import com.pose.poseanalyzer.presentation.theme.AppColors
import com.pose.poseanalyzer.presentation.theme.AppShapes
import com.pose.poseanalyzer.presentation.theme.AppSpacing
import com.pose.poseanalyzer.presentation.theme.AppTypography

/**
 * 카메라 프리뷰 위에 표시되는 자세 가이드 오버레이.
 *
 * iOS `PoseGuideOverlay.swift` 1:1 대응:
 * - 외부 dim 배경
 * - 점선 실루엣 (BodyShape viewBox 200×470)
 * - 상단 STEP 배지 + 안내 텍스트
 */
@Composable
fun PoseGuideOverlay(
    view: SessionView,
    step: Int? = null,
    totalSteps: Int = 3,
    modifier: Modifier = Modifier
) {
    val title = if (view == SessionView.FRONT) "정면 사진" else "측면 사진"
    val hint = if (view == SessionView.FRONT) {
        "어깨와 골반이 보이도록\n정면을 향해 서주세요"
    } else {
        "한쪽 옆모습 전체가\n보이도록 서주세요"
    }
    Box(modifier = modifier.fillMaxSize()) {
        // 1) 실루엣 외부만 dim
        // BlendMode.Clear가 작동하려면 오프스크린 레이어가 필요 — graphicsLayer로 강제.
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
        ) {
            val width = size.width
            val height = size.height
            val silhouetteWidth = width * 0.55f
            val silhouetteHeight = height * 0.62f
            val left = (width - silhouetteWidth) / 2f
            val top = (height - silhouetteHeight) / 2f
            drawRect(color = Color.Black.copy(alpha = 0.38f))
            // 실루엣 영역을 투명하게 (마스킹)
            clipRect(
                left = left, top = top,
                right = left + silhouetteWidth, bottom = top + silhouetteHeight
            ) {
                drawRect(color = Color.Black, blendMode = BlendMode.Clear)
            }
        }

        // 2) 점선 실루엣
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(AppSpacing.s5)
        ) {
            val silhouetteWidth = size.width * 0.55f
            val silhouetteHeight = size.height * 0.62f
            val left = (size.width - silhouetteWidth) / 2f
            val top = (size.height - silhouetteHeight) / 2f
            translate(left, top) {
                val path = bodyPathPublic(view, silhouetteWidth, silhouetteHeight)
                drawPath(
                    path = path,
                    color = Color.White,
                    style = Stroke(
                        width = 2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f))
                    )
                )
            }
        }

        // 3) 상단 STEP 배지 + 안내
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = AppSpacing.s10, start = AppSpacing.s4, end = AppSpacing.s4),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(AppSpacing.s3)
        ) {
            if (step != null) {
                StepBadge(step = step, total = totalSteps, title = title)
            } else {
                Text(
                    title,
                    style = AppTypography.micro,
                    color = Color.White,
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.55f), AppShapes.medium)
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                )
            }
            Text(
                hint,
                style = AppTypography.callout,
                color = Color.White.copy(alpha = 0.92f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
private fun StepBadge(step: Int, total: Int, title: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.s2),
        modifier = Modifier
            .background(Color.Black.copy(alpha = 0.55f), AppShapes.xlarge)
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .background(AppColors.BrandPrimary, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text("$step", style = AppTypography.micro, color = Color.White)
        }
        Text("STEP", style = AppTypography.micro, color = Color.White.copy(alpha = 0.55f))
        Text(title, style = AppTypography.callout, color = Color.White)
    }
}

// bodyPath는 BodyPath.kt의 bodyPathPublic으로 분리 — WizardStepScreen에서도 사용.

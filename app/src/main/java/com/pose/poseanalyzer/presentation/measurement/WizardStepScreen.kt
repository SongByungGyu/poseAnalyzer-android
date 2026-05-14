package com.pose.poseanalyzer.presentation.measurement

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.pose.poseanalyzer.domain.model.SessionView
import com.pose.poseanalyzer.presentation.common.AppButton
import com.pose.poseanalyzer.presentation.common.AppButtonVariant
import com.pose.poseanalyzer.presentation.theme.AppColors
import com.pose.poseanalyzer.presentation.theme.AppShapes
import com.pose.poseanalyzer.presentation.theme.AppSpacing
import com.pose.poseanalyzer.presentation.theme.AppTypography

/**
 * 마법사 사진 step (정면/측면 공용).
 *
 * iOS `WizardStepView.swift` 1:1 대응.
 */
@Composable
fun WizardStepScreen(
    view: SessionView,
    step: Int,
    totalSteps: Int = 3,
    onAddPhoto: () -> Unit,
    onBack: () -> Unit
) {
    val title = if (view == SessionView.FRONT) "정면 사진" else "측면 사진"
    val description = if (view == SessionView.FRONT) {
        "어깨와 골반이 보이도록 정면을 향해 서주세요. 한 명만 보이는 사진을 사용해주세요."
    } else {
        "한쪽 옆모습 전체가 보이도록 서주세요. 머리부터 발까지 다 포함되어야 합니다."
    }
    Surface(modifier = Modifier.fillMaxSize(), color = AppColors.Surface) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 상단 nav
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(AppSpacing.s3),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로")
                }
                Text(
                    "STEP $step / $totalSteps",
                    style = AppTypography.callout,
                    color = AppColors.OnSurfaceSecondary,
                    modifier = Modifier.padding(start = AppSpacing.s1)
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = AppSpacing.s5),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(AppSpacing.s4)
            ) {
                Text(
                    title,
                    style = AppTypography.display,
                    color = AppColors.OnSurface,
                    textAlign = TextAlign.Center
                )
                Text(
                    description,
                    style = AppTypography.body,
                    color = AppColors.OnSurfaceSecondary,
                    textAlign = TextAlign.Center
                )

                // 점선 실루엣 일러스트 (BodyShape 동일)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = AppSpacing.s2)
                        .aspectRatio(0.5f)
                ) {
                    SilhouetteIllustration(view = view, modifier = Modifier.fillMaxSize())
                }

                AppButton(
                    text = "사진 추가",
                    onClick = onAddPhoto,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun SilhouetteIllustration(view: SessionView, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val silhouetteWidth = w * 0.55f
        val silhouetteHeight = h * 0.95f
        val left = (w - silhouetteWidth) / 2f
        val top = (h - silhouetteHeight) / 2f
        translate(left, top) {
            val path = com.pose.poseanalyzer.presentation.measurement.bodyPathPublic(view, silhouetteWidth, silhouetteHeight)
            drawPath(
                path = path,
                color = AppColors.OnSurfaceTertiary,
                style = Stroke(
                    width = 2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f))
                )
            )
        }
    }
}

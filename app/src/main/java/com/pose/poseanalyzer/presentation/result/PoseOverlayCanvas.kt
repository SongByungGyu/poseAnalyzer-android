package com.pose.poseanalyzer.presentation.result

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.pose.poseanalyzer.domain.model.JointName
import com.pose.poseanalyzer.domain.model.Point2D
import com.pose.poseanalyzer.domain.model.PoseFrame
import com.pose.poseanalyzer.presentation.theme.AppColors

/**
 * 사진 위에 PoseFrame 관절 본 + 노드를 오버레이.
 *
 * iOS `PoseOverlayView.swift` 1:1 대응.
 * ML Kit은 좌상단 원점이라 Y 뒤집기 없음.
 */
@Composable
fun PoseOverlayCanvas(
    bitmap: Bitmap,
    frame: PoseFrame,
    modifier: Modifier = Modifier,
    nodeColor: Color = AppColors.BrandPrimary,
    lineColor: Color = AppColors.StatusNormal,
    lineWidthDp: androidx.compose.ui.unit.Dp = 2.dp,
    nodeRadiusDp: androidx.compose.ui.unit.Dp = 4.dp
) {
    val imageBitmap: ImageBitmap = remember(bitmap) { bitmap.asImageBitmap() }
    Box(modifier = modifier) {
        androidx.compose.foundation.Image(
            bitmap = imageBitmap,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )
        Canvas(modifier = Modifier.fillMaxSize()) {
            // 이미지가 ContentScale.Fit이므로 실제 그려진 이미지 영역 계산
            val imgAspect = bitmap.width.toFloat() / bitmap.height.toFloat()
            val canvasAspect = size.width / size.height
            val drawW: Float
            val drawH: Float
            val offsetX: Float
            val offsetY: Float
            if (imgAspect > canvasAspect) {
                drawW = size.width
                drawH = size.width / imgAspect
                offsetX = 0f
                offsetY = (size.height - drawH) / 2f
            } else {
                drawH = size.height
                drawW = size.height * imgAspect
                offsetX = (size.width - drawW) / 2f
                offsetY = 0f
            }
            fun pt(p: Point2D) = Offset(offsetX + p.x * drawW, offsetY + p.y * drawH)

            // 본 (관절 짝 연결)
            for ((a, b) in BONES) {
                val pa = frame.joints[a] ?: continue
                val pb = frame.joints[b] ?: continue
                val opacity = if (minOf(pa.confidence, pb.confidence) < 0.3f) 0.3f else 1f
                drawLine(
                    color = lineColor.copy(alpha = opacity),
                    start = pt(pa.location),
                    end = pt(pb.location),
                    strokeWidth = lineWidthDp.toPx(),
                    cap = StrokeCap.Round
                )
            }
            // neck 라인 (양 어깨 중점 → nose)
            val neck = frame.neck
            val nose = frame.point(JointName.NOSE)
            if (neck != null && nose != null) {
                drawLine(
                    color = lineColor.copy(alpha = 0.8f),
                    start = pt(neck),
                    end = pt(nose),
                    strokeWidth = lineWidthDp.toPx(),
                    cap = StrokeCap.Round
                )
            }
            // 노드
            for ((_, joint) in frame.joints) {
                val opacity = if (joint.confidence < 0.3f) 0.3f else 1f
                drawCircle(
                    color = nodeColor.copy(alpha = opacity),
                    center = pt(joint.location),
                    radius = nodeRadiusDp.toPx()
                )
                drawCircle(
                    color = Color.White.copy(alpha = opacity),
                    center = pt(joint.location),
                    radius = nodeRadiusDp.toPx(),
                    style = Stroke(width = 1.dp.toPx())
                )
            }
        }
    }
}

private val BONES: List<Pair<JointName, JointName>> = listOf(
    JointName.LEFT_SHOULDER to JointName.RIGHT_SHOULDER,
    JointName.LEFT_SHOULDER to JointName.LEFT_ELBOW,
    JointName.LEFT_ELBOW to JointName.LEFT_WRIST,
    JointName.RIGHT_SHOULDER to JointName.RIGHT_ELBOW,
    JointName.RIGHT_ELBOW to JointName.RIGHT_WRIST,
    JointName.LEFT_SHOULDER to JointName.LEFT_HIP,
    JointName.RIGHT_SHOULDER to JointName.RIGHT_HIP,
    JointName.LEFT_HIP to JointName.RIGHT_HIP,
    JointName.LEFT_HIP to JointName.LEFT_KNEE,
    JointName.LEFT_KNEE to JointName.LEFT_ANKLE,
    JointName.RIGHT_HIP to JointName.RIGHT_KNEE,
    JointName.RIGHT_KNEE to JointName.RIGHT_ANKLE
)

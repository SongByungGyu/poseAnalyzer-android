package com.pose.poseanalyzer.presentation.measurement

import androidx.compose.ui.graphics.Path
import com.pose.poseanalyzer.domain.model.SessionView

/**
 * 사람 실루엣 path — viewBox 200×470을 [width]×[height]로 스케일.
 *
 * iOS `BodyShape` (PoseGuideOverlay.swift 내부) 1:1 대응.
 * PoseGuideOverlay + WizardStepScreen 둘 다 사용.
 */
internal fun bodyPathPublic(view: SessionView, width: Float, height: Float): Path {
    val viewBoxW = 200f
    val viewBoxH = 470f
    val sx = width / viewBoxW
    val sy = height / viewBoxH
    fun x(v: Float) = v * sx
    fun y(v: Float) = v * sy

    val p = Path()
    when (view) {
        SessionView.FRONT -> {
            p.moveTo(x(100f), y(28f))
            p.cubicTo(x(130f), y(28f), x(142f), y(56f), x(132f), y(84f))
            p.lineTo(x(162f), y(116f))
            p.lineTo(x(168f), y(196f))
            p.lineTo(x(144f), y(216f))
            p.lineTo(x(142f), y(268f))
            p.lineTo(x(156f), y(460f))
            p.lineTo(x(130f), y(460f))
            p.lineTo(x(116f), y(304f))
            p.lineTo(x(100f), y(280f))
            p.lineTo(x(84f), y(304f))
            p.lineTo(x(70f), y(460f))
            p.lineTo(x(44f), y(460f))
            p.lineTo(x(58f), y(268f))
            p.lineTo(x(56f), y(216f))
            p.lineTo(x(32f), y(196f))
            p.lineTo(x(38f), y(116f))
            p.lineTo(x(68f), y(84f))
            p.cubicTo(x(58f), y(56f), x(70f), y(28f), x(100f), y(28f))
            p.close()
        }
        SessionView.SIDE -> {
            p.moveTo(x(116f), y(28f))
            p.cubicTo(x(138f), y(28f), x(148f), y(50f), x(144f), y(76f))
            p.lineTo(x(150f), y(88f))
            p.lineTo(x(142f), y(96f))
            p.lineTo(x(138f), y(104f))
            p.lineTo(x(122f), y(110f))
            p.lineTo(x(124f), y(124f))
            p.lineTo(x(142f), y(138f))
            p.cubicTo(x(140f), y(168f), x(134f), y(200f), x(128f), y(230f))
            p.lineTo(x(134f), y(260f))
            p.cubicTo(x(138f), y(280f), x(138f), y(300f), x(132f), y(320f))
            p.lineTo(x(138f), y(360f))
            p.cubicTo(x(138f), y(400f), x(134f), y(430f), x(128f), y(460f))
            p.lineTo(x(104f), y(460f))
            p.lineTo(x(102f), y(432f))
            p.lineTo(x(108f), y(380f))
            p.lineTo(x(102f), y(320f))
            p.cubicTo(x(96f), y(296f), x(96f), y(270f), x(102f), y(246f))
            p.lineTo(x(96f), y(226f))
            p.lineTo(x(92f), y(196f))
            p.cubicTo(x(86f), y(168f), x(84f), y(138f), x(86f), y(124f))
            p.cubicTo(x(80f), y(116f), x(78f), y(104f), x(82f), y(92f))
            p.lineTo(x(80f), y(80f))
            p.cubicTo(x(78f), y(66f), x(84f), y(50f), x(96f), y(38f))
            p.cubicTo(x(102f), y(32f), x(108f), y(28f), x(116f), y(28f))
            p.close()
        }
    }
    return p
}

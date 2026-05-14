package com.pose.poseanalyzer.domain.evaluation

import com.pose.poseanalyzer.domain.model.JointName
import com.pose.poseanalyzer.domain.model.Point2D
import com.pose.poseanalyzer.domain.model.PoseFrame
import com.pose.poseanalyzer.domain.model.PostureStatus
import com.pose.poseanalyzer.domain.model.SessionView
import com.pose.poseanalyzer.fixtures.PoseFrameFixtures
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.math.cos
import kotlin.math.sin

class AnteriorPelvicTiltEvaluatorTest {

    private val evaluator = AnteriorPelvicTiltEvaluator()

    private fun setupFrame(angleDeg: Double): PoseFrame {
        val hip = Point2D(0.5f, 0.5f)
        // shoulder 위쪽 (0.5, 0.3), 즉 shoulder-hip 벡터 (0, -0.2) → 각도 -90° (위로)
        val shoulder = Point2D(0.5f, 0.3f)
        // knee 위치: shoulder-hip-knee 각도 = angleDeg.
        // shoulder->hip 벡터 = (0, 0.2). 각도 90° (atan2).
        // knee 벡터가 (90° - (180-angleDeg)) = (angleDeg - 90)°이면 두 벡터 사이 = (180-angleDeg).
        // hip 기준 두 점 사이 각도 = angleBetween(shoulder, hip, knee)
        // shoulder-hip 벡터 방향: 90° (downward in atan2 terms, dy>0)
        // 우리는 angleDeg = 두 벡터 사이 각도. shoulder 벡터는 hip→shoulder = -90°(상향).
        // knee 벡터: hip→knee. 두 벡터 사이 각도 = angleDeg.
        val armLength = 0.15f
        // hip→shoulder 방향 각도 = atan2(-0.2, 0) = -90° (or 270°)
        // hip→knee 방향이 angleDeg 만큼 떨어져야 함.
        // 회전: hip→knee = hip→shoulder 회전 angleDeg
        // shoulder 벡터: (0, -1). 시계방향 회전 angleDeg → (sin(θ), -cos(θ))
        val rad = Math.toRadians(angleDeg)
        val knee = Point2D(
            hip.x + (armLength * sin(rad)).toFloat(),
            hip.y - (armLength * cos(rad)).toFloat()
        )

        return PoseFrameFixtures.frame(
            SessionView.SIDE,
            mapOf(
                JointName.RIGHT_SHOULDER to shoulder,
                JointName.RIGHT_HIP to hip,
                JointName.RIGHT_KNEE to knee
            )
        )
    }

    @Test
    fun `각도 180 normal`() {
        val result = evaluator.evaluate(setupFrame(180.0))
        assertEquals(PostureStatus.NORMAL, result.status)
    }

    @Test
    fun `각도 172 caution 전방`() {
        val result = evaluator.evaluate(setupFrame(172.0))
        assertEquals(PostureStatus.CAUTION, result.status)
    }

    @Test
    fun `각도 188 caution 후방`() {
        val result = evaluator.evaluate(setupFrame(188.0))
        assertEquals(PostureStatus.CAUTION, result.status)
    }

    @Test
    fun `각도 160 suspect`() {
        val result = evaluator.evaluate(setupFrame(160.0))
        assertEquals(PostureStatus.SUSPECT, result.status)
    }
}

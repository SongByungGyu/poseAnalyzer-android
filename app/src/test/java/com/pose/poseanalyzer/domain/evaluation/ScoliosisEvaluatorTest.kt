package com.pose.poseanalyzer.domain.evaluation

import com.pose.poseanalyzer.domain.model.JointName
import com.pose.poseanalyzer.domain.model.Point2D
import com.pose.poseanalyzer.domain.model.PoseFrame
import com.pose.poseanalyzer.domain.model.PostureStatus
import com.pose.poseanalyzer.domain.model.SessionView
import com.pose.poseanalyzer.fixtures.PoseFrameFixtures
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.math.tan

class ScoliosisEvaluatorTest {

    private val evaluator = ScoliosisEvaluator()

    /** 어깨 기울기만 [tiltDeg]°, 골반은 수평 */
    private fun setupFrame(shoulderTiltDeg: Double, hipTiltDeg: Double): PoseFrame {
        val shoulderRise = (tan(Math.toRadians(shoulderTiltDeg)) * 0.2).toFloat()
        val hipRise = (tan(Math.toRadians(hipTiltDeg)) * 0.2).toFloat()

        return PoseFrameFixtures.frame(
            SessionView.FRONT,
            mapOf(
                JointName.LEFT_SHOULDER to Point2D(0.4f, 0.3f),
                JointName.RIGHT_SHOULDER to Point2D(0.6f, 0.3f + shoulderRise),
                JointName.LEFT_HIP to Point2D(0.42f, 0.6f),
                JointName.RIGHT_HIP to Point2D(0.58f, 0.6f + hipRise)
            )
        )
    }

    @Test
    fun `둘 다 1도 normal`() {
        val result = evaluator.evaluate(setupFrame(1.0, 1.0))
        assertEquals(PostureStatus.NORMAL, result.status)
    }

    @Test
    fun `어깨 4_5도 caution`() {
        // 정상 0~4° / 주의 4~5° → 4.5°는 caution
        val result = evaluator.evaluate(setupFrame(4.5, 1.0))
        assertEquals(PostureStatus.CAUTION, result.status)
    }

    @Test
    fun `골반 6도 suspect`() {
        val result = evaluator.evaluate(setupFrame(1.0, 6.0))
        assertEquals(PostureStatus.SUSPECT, result.status)
    }

    @Test
    fun `관절 누락 unmeasurable`() {
        val frame = PoseFrameFixtures.frame(
            SessionView.FRONT,
            mapOf(JointName.LEFT_SHOULDER to Point2D(0.4f, 0.3f))
        )
        val result = evaluator.evaluate(frame)
        assertEquals(PostureStatus.UNMEASURABLE, result.status)
    }
}

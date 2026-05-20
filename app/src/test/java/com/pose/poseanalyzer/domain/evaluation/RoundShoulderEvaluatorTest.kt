package com.pose.poseanalyzer.domain.evaluation

import com.pose.poseanalyzer.domain.model.JointName
import com.pose.poseanalyzer.domain.model.Point2D
import com.pose.poseanalyzer.domain.model.PostureStatus
import com.pose.poseanalyzer.domain.model.SessionView
import com.pose.poseanalyzer.fixtures.PoseFrameFixtures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RoundShoulderEvaluatorTest {

    private val evaluator = RoundShoulderEvaluator()

    /**
     * 측면 사진 기본 프레임:
     * - leftShoulder (0.5, 0.5)
     * - leftHip (0.5, 0.8) → torsoHeight = 0.3
     * - leftEar (earX, 0.3)
     * 비율 = |earX - 0.5| / 0.3
     * 임계값: <0.05 정상 / 0.05~0.10 주의 / >0.10 의심
     */
    private fun setupFrame(ratio: Double): com.pose.poseanalyzer.domain.model.PoseFrame {
        val torsoHeight = 0.3f
        val gap = (ratio * torsoHeight).toFloat()
        val shoulderX = 0.5f
        return PoseFrameFixtures.frame(
            SessionView.SIDE,
            mapOf(
                JointName.LEFT_SHOULDER to Point2D(shoulderX, 0.5f),
                JointName.LEFT_HIP to Point2D(shoulderX, 0.5f + torsoHeight),
                JointName.LEFT_EAR to Point2D(shoulderX - gap, 0.3f)
            )
        )
    }

    @Test
    fun `ratio 0_03 normal`() {
        val result = evaluator.evaluate(setupFrame(0.03))
        assertEquals(PostureStatus.NORMAL, result.status)
    }

    @Test
    fun `ratio 0_07 caution`() {
        val result = evaluator.evaluate(setupFrame(0.07))
        assertEquals(PostureStatus.CAUTION, result.status)
    }

    @Test
    fun `ratio 0_15 suspect`() {
        val result = evaluator.evaluate(setupFrame(0.15))
        assertEquals(PostureStatus.SUSPECT, result.status)
    }

    @Test
    fun `어깨 누락 unmeasurable`() {
        val frame = PoseFrameFixtures.frame(
            SessionView.SIDE,
            mapOf(JointName.RIGHT_EAR to Point2D(0.5f, 0.3f))
        )
        val result = evaluator.evaluate(frame)
        assertEquals(PostureStatus.UNMEASURABLE, result.status)
    }

    @Test
    fun `귀가 없어도 코눈으로 추정`() {
        // 코(0.46, 0.3), 눈(0.49, 0.3) → 추정 귀 = (0.532, 0.3)
        // 어깨(0.5, 0.5), 엉덩이(0.5, 0.8) → 비율 = 0.107 → suspect 영역
        val frame = PoseFrameFixtures.frame(
            SessionView.SIDE,
            mapOf(
                JointName.NOSE to Point2D(0.46f, 0.3f),
                JointName.LEFT_EYE to Point2D(0.49f, 0.3f),
                JointName.LEFT_SHOULDER to Point2D(0.5f, 0.5f),
                JointName.LEFT_HIP to Point2D(0.5f, 0.8f)
            )
        )
        val result = evaluator.evaluate(frame)
        assertNotEquals(PostureStatus.UNMEASURABLE, result.status)
        assertTrue(result.usedJointNames.any { it.contains("→EAR") })
    }
}

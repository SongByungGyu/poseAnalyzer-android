package com.pose.poseanalyzer.domain.evaluation

import com.pose.poseanalyzer.domain.model.JointName
import com.pose.poseanalyzer.domain.model.Point2D
import com.pose.poseanalyzer.domain.model.PoseFrame
import com.pose.poseanalyzer.domain.model.PostureStatus
import com.pose.poseanalyzer.domain.model.SessionView
import com.pose.poseanalyzer.fixtures.PoseFrameFixtures
import org.junit.Assert.assertEquals
import org.junit.Test

class KneeHyperextensionEvaluatorTest {

    private val evaluator = KneeHyperextensionEvaluator()

    /**
     * 무릎 정상 (180°) — hip-knee-ankle 직선.
     */
    private fun straightLeg(): PoseFrame = PoseFrameFixtures.frame(
        SessionView.SIDE,
        mapOf(
            JointName.RIGHT_HIP to Point2D(0.5f, 0.3f),
            JointName.RIGHT_KNEE to Point2D(0.5f, 0.5f),
            JointName.RIGHT_ANKLE to Point2D(0.5f, 0.7f)
        )
    )

    /**
     * 과신전 (>180°) — hip이 약간 뒤로, knee가 앞으로 나옴.
     * hip (0.45, 0.3), knee (0.5, 0.5), ankle (0.45, 0.7) → 무릎이 앞으로
     */
    private fun hyperextended(kneeOffset: Float): PoseFrame = PoseFrameFixtures.frame(
        SessionView.SIDE,
        mapOf(
            JointName.RIGHT_HIP to Point2D(0.5f - kneeOffset, 0.3f),
            JointName.RIGHT_KNEE to Point2D(0.5f, 0.5f),
            JointName.RIGHT_ANKLE to Point2D(0.5f - kneeOffset, 0.7f)
        )
    )

    @Test
    fun `직선 다리 normal`() {
        val result = evaluator.evaluate(straightLeg())
        assertEquals(PostureStatus.NORMAL, result.status)
    }

    @Test
    fun `약한 과신전 normal or caution`() {
        // 0.005 offset → 매우 약한 과신전
        val result = evaluator.evaluate(hyperextended(0.005f))
        assertEquals(
            "약한 과신전은 normal 또는 caution",
            true,
            result.status == PostureStatus.NORMAL || result.status == PostureStatus.CAUTION
        )
    }

    @Test
    fun `심한 과신전 suspect`() {
        // 큰 offset → 각도가 크게 벗어남
        val result = evaluator.evaluate(hyperextended(0.05f))
        assertEquals(
            "심한 과신전은 caution 또는 suspect",
            true,
            result.status == PostureStatus.CAUTION || result.status == PostureStatus.SUSPECT
        )
    }

    @Test
    fun `관절 누락 unmeasurable`() {
        val frame = PoseFrameFixtures.frame(SessionView.SIDE, emptyMap())
        val result = evaluator.evaluate(frame)
        assertEquals(PostureStatus.UNMEASURABLE, result.status)
    }
}

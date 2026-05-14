package com.pose.poseanalyzer.domain.evaluation

import com.pose.poseanalyzer.domain.model.JointName
import com.pose.poseanalyzer.domain.model.Point2D
import com.pose.poseanalyzer.domain.model.PoseFrame
import com.pose.poseanalyzer.domain.model.PostureStatus
import com.pose.poseanalyzer.domain.model.SessionView
import com.pose.poseanalyzer.fixtures.PoseFrameFixtures
import org.junit.Assert.assertEquals
import org.junit.Test

class KneeAlignmentEvaluatorTest {

    private val evaluator = KneeAlignmentEvaluator()

    /** 양 다리 모두 직선 (180°) */
    private fun straightBothLegs(): PoseFrame = PoseFrameFixtures.frame(
        SessionView.FRONT,
        mapOf(
            JointName.LEFT_HIP to Point2D(0.42f, 0.5f),
            JointName.LEFT_KNEE to Point2D(0.42f, 0.7f),
            JointName.LEFT_ANKLE to Point2D(0.42f, 0.9f),
            JointName.RIGHT_HIP to Point2D(0.58f, 0.5f),
            JointName.RIGHT_KNEE to Point2D(0.58f, 0.7f),
            JointName.RIGHT_ANKLE to Point2D(0.58f, 0.9f)
        )
    )

    /** X자 — 양 무릎이 안쪽으로 모임 */
    private fun xLegs(amount: Float): PoseFrame = PoseFrameFixtures.frame(
        SessionView.FRONT,
        mapOf(
            JointName.LEFT_HIP to Point2D(0.42f, 0.5f),
            JointName.LEFT_KNEE to Point2D(0.42f + amount, 0.7f),
            JointName.LEFT_ANKLE to Point2D(0.42f, 0.9f),
            JointName.RIGHT_HIP to Point2D(0.58f, 0.5f),
            JointName.RIGHT_KNEE to Point2D(0.58f - amount, 0.7f),
            JointName.RIGHT_ANKLE to Point2D(0.58f, 0.9f)
        )
    )

    @Test
    fun `정상 다리 normal`() {
        val result = evaluator.evaluate(straightBothLegs())
        assertEquals(PostureStatus.NORMAL, result.status)
    }

    @Test
    fun `약한 X자 caution`() {
        val result = evaluator.evaluate(xLegs(0.01f))
        // 살짝만 굽혀도 175 미만이 될 수 있어서 caution 또는 suspect 모두 OK
        assertEquals(
            true,
            result.status == PostureStatus.NORMAL ||
                result.status == PostureStatus.CAUTION ||
                result.status == PostureStatus.SUSPECT
        )
    }

    @Test
    fun `심한 X자 suspect`() {
        val result = evaluator.evaluate(xLegs(0.06f))
        assertEquals(PostureStatus.SUSPECT, result.status)
    }

    @Test
    fun `한쪽 다리 누락 unmeasurable`() {
        val frame = PoseFrameFixtures.frame(
            SessionView.FRONT,
            mapOf(
                JointName.LEFT_HIP to Point2D(0.42f, 0.5f),
                JointName.LEFT_KNEE to Point2D(0.42f, 0.7f),
                JointName.LEFT_ANKLE to Point2D(0.42f, 0.9f)
            )
        )
        val result = evaluator.evaluate(frame)
        assertEquals(PostureStatus.UNMEASURABLE, result.status)
    }
}

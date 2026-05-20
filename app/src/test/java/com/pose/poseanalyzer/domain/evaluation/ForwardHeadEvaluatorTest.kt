package com.pose.poseanalyzer.domain.evaluation

import com.pose.poseanalyzer.domain.model.JointName
import com.pose.poseanalyzer.domain.model.Point2D
import com.pose.poseanalyzer.domain.model.PostureStatus
import com.pose.poseanalyzer.domain.model.PostureType
import com.pose.poseanalyzer.domain.model.SessionView
import com.pose.poseanalyzer.fixtures.PoseFrameFixtures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ForwardHeadEvaluatorTest {

    private val evaluator = ForwardHeadEvaluator()

    @Test
    fun `각도 175 normal`() {
        val frame = PoseFrameFixtures.sideEarShoulderHipAngle(175.0)
        val result = evaluator.evaluate(frame)
        assertEquals(PostureType.FORWARD_HEAD, result.type)
        assertEquals(PostureStatus.NORMAL, result.status)
    }

    @Test
    fun `각도 165 caution`() {
        val frame = PoseFrameFixtures.sideEarShoulderHipAngle(165.0)
        val result = evaluator.evaluate(frame)
        assertEquals(PostureStatus.CAUTION, result.status)
    }

    @Test
    fun `각도 140 suspect`() {
        val frame = PoseFrameFixtures.sideEarShoulderHipAngle(140.0)
        val result = evaluator.evaluate(frame)
        assertEquals(PostureStatus.SUSPECT, result.status)
    }

    @Test
    fun `관절 누락 unmeasurable`() {
        val frame = PoseFrameFixtures.frame(
            SessionView.SIDE,
            mapOf(JointName.RIGHT_EAR to Point2D(0.5f, 0.5f))
        )
        val result = evaluator.evaluate(frame)
        assertEquals(PostureStatus.UNMEASURABLE, result.status)
    }

    @Test
    fun `귀가 없어도 코눈으로 추정`() {
        // 코(0.5, 0.3), 눈(0.5, 0.35) → 추정 귀 = (0.5, 0.42)
        // 어깨(0.5, 0.5), 엉덩이(0.5, 0.7) → 수직 정렬 → 각도 180°
        val frame = PoseFrameFixtures.frame(
            SessionView.SIDE,
            mapOf(
                JointName.NOSE to Point2D(0.5f, 0.3f),
                JointName.LEFT_EYE to Point2D(0.5f, 0.35f),
                JointName.LEFT_SHOULDER to Point2D(0.5f, 0.5f),
                JointName.LEFT_HIP to Point2D(0.5f, 0.7f)
            )
        )
        val result = evaluator.evaluate(frame)
        assertEquals(PostureStatus.NORMAL, result.status)
        assertTrue(result.usedJointNames.any { it.contains("→EAR") })
    }

    @Test
    fun `귀와 코눈 둘다 있으면 코눈추정 우선`() {
        // 귀·코·눈 모두 검출됨 → 코+눈 추정이 우선 사용돼야 함.
        val frame = PoseFrameFixtures.frame(
            SessionView.SIDE,
            mapOf(
                JointName.NOSE to Point2D(0.5f, 0.3f),
                JointName.LEFT_EYE to Point2D(0.5f, 0.35f),
                JointName.LEFT_EAR to Point2D(0.6f, 0.4f),
                JointName.LEFT_SHOULDER to Point2D(0.5f, 0.5f),
                JointName.LEFT_HIP to Point2D(0.5f, 0.7f)
            )
        )
        val result = evaluator.evaluate(frame)
        assertTrue(result.usedJointNames.any { it.contains("→EAR") })
    }
}

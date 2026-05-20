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

class HeadTiltEvaluatorTest {

    private val evaluator = HeadTiltEvaluator()

    private fun framEars(tiltDeg: Double): PoseFrame {
        val rise = (tan(Math.toRadians(tiltDeg)) * 0.2).toFloat()
        return PoseFrameFixtures.frame(
            SessionView.FRONT,
            mapOf(
                JointName.LEFT_EAR to Point2D(0.4f, 0.2f),
                JointName.RIGHT_EAR to Point2D(0.6f, 0.2f + rise)
            )
        )
    }

    @Test
    fun `귀 기울기 1도 normal`() {
        val result = evaluator.evaluate(framEars(1.0))
        assertEquals(PostureStatus.NORMAL, result.status)
    }

    @Test
    fun `귀 기울기 4_5도 caution`() {
        // 정상 0~4° / 주의 4~5° → 4.5°는 caution
        val result = evaluator.evaluate(framEars(4.5))
        assertEquals(PostureStatus.CAUTION, result.status)
    }

    @Test
    fun `귀 기울기 7도 suspect`() {
        val result = evaluator.evaluate(framEars(7.0))
        assertEquals(PostureStatus.SUSPECT, result.status)
    }

    @Test
    fun `눈만 있어도 측정`() {
        val rise = (tan(Math.toRadians(1.0)) * 0.1).toFloat()
        val frame = PoseFrameFixtures.frame(
            SessionView.FRONT,
            mapOf(
                JointName.LEFT_EYE to Point2D(0.45f, 0.2f),
                JointName.RIGHT_EYE to Point2D(0.55f, 0.2f + rise)
            )
        )
        val result = evaluator.evaluate(frame)
        assertEquals(PostureStatus.NORMAL, result.status)
        assertEquals(listOf("LEFT_EYE", "RIGHT_EYE"), result.usedJointNames)
    }

    @Test
    fun `눈 귀 둘다 있으면 눈 우선`() {
        val frame = PoseFrameFixtures.frame(
            SessionView.FRONT,
            mapOf(
                JointName.LEFT_EYE to Point2D(0.45f, 0.2f),
                JointName.RIGHT_EYE to Point2D(0.55f, 0.2f),
                JointName.LEFT_EAR to Point2D(0.4f, 0.2f),
                JointName.RIGHT_EAR to Point2D(0.6f, 0.22f)
            )
        )
        val result = evaluator.evaluate(frame)
        assertEquals(listOf("LEFT_EYE", "RIGHT_EYE"), result.usedJointNames)
    }

    @Test
    fun `눈 없으면 귀로 fallback`() {
        val frame = PoseFrameFixtures.frame(
            SessionView.FRONT,
            mapOf(
                JointName.LEFT_EAR to Point2D(0.4f, 0.2f),
                JointName.RIGHT_EAR to Point2D(0.6f, 0.2f)
            )
        )
        val result = evaluator.evaluate(frame)
        assertEquals(PostureStatus.NORMAL, result.status)
        assertEquals(listOf("LEFT_EAR", "RIGHT_EAR"), result.usedJointNames)
    }

    @Test
    fun `귀 눈 모두 없으면 unmeasurable`() {
        val frame = PoseFrameFixtures.frame(SessionView.FRONT, emptyMap())
        val result = evaluator.evaluate(frame)
        assertEquals(PostureStatus.UNMEASURABLE, result.status)
    }
}

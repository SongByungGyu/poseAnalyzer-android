package com.pose.poseanalyzer.domain.evaluation

import com.pose.poseanalyzer.domain.model.JointName
import com.pose.poseanalyzer.domain.model.Point2D
import com.pose.poseanalyzer.domain.model.PostureStatus
import com.pose.poseanalyzer.domain.model.PostureType
import com.pose.poseanalyzer.domain.model.SessionView
import com.pose.poseanalyzer.fixtures.PoseFrameFixtures
import org.junit.Assert.assertEquals
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
}

package com.pose.poseanalyzer.domain.evaluation

import com.pose.poseanalyzer.domain.model.JointName
import com.pose.poseanalyzer.domain.model.Point2D
import com.pose.poseanalyzer.domain.model.PostureStatus
import com.pose.poseanalyzer.domain.model.SessionView
import com.pose.poseanalyzer.fixtures.PoseFrameFixtures
import org.junit.Assert.assertEquals
import org.junit.Test

class RoundShoulderEvaluatorTest {

    private val evaluator = RoundShoulderEvaluator()

    /** 어깨폭 0.2, 귀-어깨 수평거리 = ratio * 어깨폭 */
    private fun setupFrame(ratio: Double): com.pose.poseanalyzer.domain.model.PoseFrame {
        val shoulderWidth = 0.2f
        val gap = (ratio * shoulderWidth).toFloat()
        return PoseFrameFixtures.frame(
            SessionView.SIDE,
            mapOf(
                JointName.LEFT_SHOULDER to Point2D(0.3f, 0.5f),
                JointName.RIGHT_SHOULDER to Point2D(0.3f + shoulderWidth, 0.5f),
                JointName.RIGHT_EAR to Point2D(0.3f + shoulderWidth - gap, 0.3f),
                JointName.LEFT_EAR to Point2D(0.3f - gap, 0.3f)
            )
        )
    }

    @Test
    fun `ratio 0_10 normal`() {
        val result = evaluator.evaluate(setupFrame(0.10))
        assertEquals(PostureStatus.NORMAL, result.status)
    }

    @Test
    fun `ratio 0_20 caution`() {
        val result = evaluator.evaluate(setupFrame(0.20))
        assertEquals(PostureStatus.CAUTION, result.status)
    }

    @Test
    fun `ratio 0_35 suspect`() {
        val result = evaluator.evaluate(setupFrame(0.35))
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
}

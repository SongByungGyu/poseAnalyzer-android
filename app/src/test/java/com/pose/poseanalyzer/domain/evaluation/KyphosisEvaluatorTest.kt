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

class KyphosisEvaluatorTest {

    private val evaluator = KyphosisEvaluator()

    /**
     * neck = (LS+RS)/2. 양 어깨가 (0.4, 0.4) (0.6, 0.4) → neck (0.5, 0.4)
     * Kyphosis는 neck-shoulder-hip 각도. shoulder를 vertex로 두고
     * neck은 위쪽 (0.5, 0.4), hip은 [angleDeg]에 따라 배치.
     *
     * 단순화: useRight = true 사용 → RIGHT_SHOULDER가 vertex
     */
    private fun setupFrame(angleDeg: Double): PoseFrame {
        val shoulder = Point2D(0.6f, 0.4f)
        // neck = (LS+RS)/2 → neck = (0.5, 0.4)
        val leftShoulder = Point2D(0.4f, 0.4f)
        val neck = Point2D(0.5f, 0.4f)

        // shoulder를 vertex로, neck 방향 = (neck - shoulder) = (-0.1, 0)
        // 이 벡터의 각도 0° (왼쪽으로 가는 방향이므로 atan2(0,-0.1) = 180°)
        // hip은 shoulder 기준 angleDeg 가 형성되도록 배치.
        // angleBetween(neck, shoulder, hip) = angleDeg
        // neck-shoulder 방향: 180°. hip 방향이 (180° - angleDeg)이면 두 벡터 사이 각도 = angleDeg.
        val hipAngleRad = Math.toRadians(180.0 - angleDeg)
        val armLength = 0.15f
        val hip = Point2D(
            shoulder.x + (armLength * cos(hipAngleRad)).toFloat(),
            shoulder.y + (armLength * sin(hipAngleRad)).toFloat()
        )

        return PoseFrameFixtures.frame(
            SessionView.SIDE,
            mapOf(
                JointName.LEFT_SHOULDER to leftShoulder,
                JointName.RIGHT_SHOULDER to shoulder,
                JointName.RIGHT_HIP to hip
            )
        )
    }

    @Test
    fun `각도 178 normal`() {
        val result = evaluator.evaluate(setupFrame(178.0))
        assertEquals(PostureStatus.NORMAL, result.status)
    }

    @Test
    fun `각도 170 caution`() {
        val result = evaluator.evaluate(setupFrame(170.0))
        assertEquals(PostureStatus.CAUTION, result.status)
    }

    @Test
    fun `각도 140 suspect`() {
        val result = evaluator.evaluate(setupFrame(140.0))
        assertEquals(PostureStatus.SUSPECT, result.status)
    }

    @Test
    fun `한쪽 어깨 누락 unmeasurable (neck 못 만들면)`() {
        val frame = PoseFrameFixtures.frame(
            SessionView.SIDE,
            mapOf(
                JointName.RIGHT_SHOULDER to Point2D(0.6f, 0.4f),
                JointName.RIGHT_HIP to Point2D(0.6f, 0.7f)
            )
        )
        val result = evaluator.evaluate(frame)
        assertEquals(PostureStatus.UNMEASURABLE, result.status)
    }
}

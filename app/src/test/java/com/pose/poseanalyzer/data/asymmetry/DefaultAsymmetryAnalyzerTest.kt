package com.pose.poseanalyzer.data.asymmetry

import com.pose.poseanalyzer.domain.model.AsymmetryResult
import com.pose.poseanalyzer.domain.model.JointName
import com.pose.poseanalyzer.domain.model.Point2D
import com.pose.poseanalyzer.domain.model.SessionView
import com.pose.poseanalyzer.fixtures.PoseFrameFixtures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DefaultAsymmetryAnalyzerTest {

    private val analyzer = DefaultAsymmetryAnalyzer()

    @Test
    fun `좌우 균형 BALANCED`() {
        val frame = PoseFrameFixtures.frame(
            SessionView.FRONT,
            mapOf(
                JointName.LEFT_SHOULDER to Point2D(0.4f, 0.3f),
                JointName.RIGHT_SHOULDER to Point2D(0.6f, 0.3f),
                JointName.LEFT_HIP to Point2D(0.42f, 0.6f),
                JointName.RIGHT_HIP to Point2D(0.58f, 0.6f)
            )
        )
        val result = analyzer.analyze(frame, null)
        assertEquals(AsymmetryResult.Direction.BALANCED, result.shoulder.direction)
        assertEquals(AsymmetryResult.Direction.BALANCED, result.hip.direction)
    }

    @Test
    fun `오른쪽 어깨가 아래로 = LEFT_HIGHER (ML Kit 좌상단 원점)`() {
        // 오른쪽 어깨 y가 더 큼 = 아래로 → 왼쪽이 높음
        val frame = PoseFrameFixtures.frame(
            SessionView.FRONT,
            mapOf(
                JointName.LEFT_SHOULDER to Point2D(0.4f, 0.3f),
                JointName.RIGHT_SHOULDER to Point2D(0.6f, 0.35f),
                JointName.LEFT_HIP to Point2D(0.42f, 0.6f),
                JointName.RIGHT_HIP to Point2D(0.58f, 0.6f)
            )
        )
        val result = analyzer.analyze(frame, null)
        assertEquals(AsymmetryResult.Direction.LEFT_HIGHER, result.shoulder.direction)
        assertTrue("어깨 각도 > 0", result.shoulder.angleDegrees > 0)
        assertNull("키 미입력 시 cm 없음", result.shoulder.cm)
    }

    @Test
    fun `왼쪽 엉덩이가 아래로 = RIGHT_HIGHER`() {
        val frame = PoseFrameFixtures.frame(
            SessionView.FRONT,
            mapOf(
                JointName.LEFT_SHOULDER to Point2D(0.4f, 0.3f),
                JointName.RIGHT_SHOULDER to Point2D(0.6f, 0.3f),
                JointName.LEFT_HIP to Point2D(0.42f, 0.65f),
                JointName.RIGHT_HIP to Point2D(0.58f, 0.6f)
            )
        )
        val result = analyzer.analyze(frame, null)
        assertEquals(AsymmetryResult.Direction.RIGHT_HIGHER, result.hip.direction)
    }

    @Test
    fun `키 입력 시 cm 환산`() {
        val frame = PoseFrameFixtures.frame(
            SessionView.FRONT,
            mapOf(
                JointName.NOSE to Point2D(0.5f, 0.1f),
                JointName.LEFT_SHOULDER to Point2D(0.4f, 0.3f),
                JointName.RIGHT_SHOULDER to Point2D(0.6f, 0.35f),
                JointName.LEFT_HIP to Point2D(0.42f, 0.6f),
                JointName.RIGHT_HIP to Point2D(0.58f, 0.6f),
                JointName.LEFT_ANKLE to Point2D(0.42f, 0.9f),
                JointName.RIGHT_ANKLE to Point2D(0.58f, 0.9f)
            )
        )
        val result = analyzer.analyze(frame, heightCm = 170.0)
        assertNotNull("키 있으면 cm 환산", result.shoulder.cm)
        assertTrue("cm > 0", result.shoulder.cm!! > 0)
    }

    @Test
    fun `관절 누락 시 0 균형 반환`() {
        val frame = PoseFrameFixtures.frame(SessionView.FRONT, emptyMap())
        val result = analyzer.analyze(frame, null)
        assertEquals(AsymmetryResult.Direction.BALANCED, result.shoulder.direction)
        assertEquals(0.0, result.shoulder.ratio, 0.001)
    }
}

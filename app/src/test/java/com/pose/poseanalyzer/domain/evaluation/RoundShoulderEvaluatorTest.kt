package com.pose.poseanalyzer.domain.evaluation

import com.pose.poseanalyzer.domain.model.JointName
import com.pose.poseanalyzer.domain.model.Point2D
import com.pose.poseanalyzer.domain.model.PoseFrame
import com.pose.poseanalyzer.domain.model.PostureStatus
import com.pose.poseanalyzer.domain.model.SessionView
import com.pose.poseanalyzer.fixtures.PoseFrameFixtures
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.math.PI
import kotlin.math.tan

/**
 * RoundShoulderEvaluator v2 — FSA (Forward Shoulder Angle).
 *
 * 임계값: 정상 <47° / 주의 47~52° / 의심 ≥52°.
 * 알고리즘: FSA = atan2(|acromion.x - C7.x|, |acromion.y - C7.y|) × 180/π.
 * C7 = 양 어깨 중점, acromion = 카메라 가까운 쪽(신뢰도 높은) 어깨.
 *
 * 좌표계: atan2 절대값 사용으로 양 플랫폼 동일 결과.
 */
class RoundShoulderEvaluatorTest {

    private val evaluator = RoundShoulderEvaluator()

    /**
     * 양 어깨를 대칭 배치해 C7=(0.5,0.5)·acromion(우)=C7+(dx,dy)가 되도록 한다.
     * acromion 신뢰도가 left와 같으면 pickBest는 right 선택 (코드 규약).
     */
    private fun frameWithAcromionOffset(dx: Double, dy: Double): PoseFrame =
        PoseFrameFixtures.frame(
            SessionView.SIDE,
            mapOf(
                JointName.LEFT_SHOULDER to Point2D((0.5 - dx).toFloat(), (0.5 - dy).toFloat()),
                JointName.RIGHT_SHOULDER to Point2D((0.5 + dx).toFloat(), (0.5 + dy).toFloat())
            )
        )

    private fun frameForFsa(fsaDeg: Double, dy: Double = 0.2): PoseFrame {
        val dx = dy * tan(fsaDeg * PI / 180.0)
        return frameWithAcromionOffset(dx, dy)
    }

    @Test
    fun `정상 FSA 25도면 NORMAL`() {
        val result = evaluator.evaluate(frameForFsa(25.0))
        assertEquals(PostureStatus.NORMAL, result.status)
        assertEquals("v2", result.algorithmVersion)
    }

    @Test
    fun `caution 영역 FSA 47_5도면 CAUTION`() {
        // spec § 2.2의 cutoff 47° 근처. 닫힌 경계(47.0 자체)는 다른 evaluator와의
        // 일관성을 위해 NORMAL(normalRange `0..47` 포함). 47.5°부터 명확한 CAUTION.
        // MCID 1.34° 보다 작은 ±0.5° 차이는 임상적으로 noise (spec § 2.2 인용).
        val result = evaluator.evaluate(frameForFsa(47.5))
        assertEquals(PostureStatus.CAUTION, result.status)
    }

    @Test
    fun `FSA 50도면 CAUTION`() {
        val result = evaluator.evaluate(frameForFsa(50.0))
        assertEquals(PostureStatus.CAUTION, result.status)
    }

    @Test
    fun `FSA 55도면 SUSPECT`() {
        val result = evaluator.evaluate(frameForFsa(55.0))
        assertEquals(PostureStatus.SUSPECT, result.status)
    }

    @Test
    fun `양 어깨 신뢰도 부족하면 UNMEASURABLE`() {
        val frame = PoseFrameFixtures.frame(
            SessionView.SIDE,
            mapOf(
                JointName.LEFT_SHOULDER to Point2D(0.45f, 0.5f),
                JointName.RIGHT_SHOULDER to Point2D(0.55f, 0.5f)
            ),
            confidence = 0.1f // < DEFAULT_CONFIDENCE 0.3
        )
        val result = evaluator.evaluate(frame)
        assertEquals(PostureStatus.UNMEASURABLE, result.status)
    }

    @Test
    fun `C7과 acromion 너무 가까우면 UNMEASURABLE`() {
        // dx=dy ≈ 0 → 어깨 둘이 정확히 같은 위치 → 거리 0 → 측정 불가
        val frame = PoseFrameFixtures.frame(
            SessionView.SIDE,
            mapOf(
                JointName.LEFT_SHOULDER to Point2D(0.5f, 0.5f),
                JointName.RIGHT_SHOULDER to Point2D(0.5f, 0.5f)
            )
        )
        val result = evaluator.evaluate(frame)
        assertEquals(PostureStatus.UNMEASURABLE, result.status)
    }
}

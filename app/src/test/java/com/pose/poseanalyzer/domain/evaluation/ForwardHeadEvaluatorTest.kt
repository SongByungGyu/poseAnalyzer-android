package com.pose.poseanalyzer.domain.evaluation

import com.pose.poseanalyzer.domain.model.JointName
import com.pose.poseanalyzer.domain.model.Point2D
import com.pose.poseanalyzer.domain.model.PoseFrame
import com.pose.poseanalyzer.domain.model.PostureStatus
import com.pose.poseanalyzer.domain.model.PostureType
import com.pose.poseanalyzer.domain.model.SessionView
import com.pose.poseanalyzer.fixtures.PoseFrameFixtures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import kotlin.math.PI
import kotlin.math.tan

/**
 * ForwardHeadEvaluator v2 — CVA (Craniovertebral Angle).
 *
 * 임계값: 정상 ≥53° / 주의 48~53° / 의심 <48°.
 * 알고리즘: CVA = atan2(|tragus.y - C7.y|, |tragus.x - C7.x|) × 180/π.
 * C7 = 양 어깨 중점 (ML Kit `.neck` 미제공).
 * tragus = 코+눈 추정 1순위 (검출 귀 fallback).
 *
 * 좌표계: ML Kit은 좌상단 원점이지만 atan2 절대값 사용으로 영향 제거.
 */
class ForwardHeadEvaluatorTest {

    private val evaluator = ForwardHeadEvaluator()

    /**
     * tragus 위치를 코+눈 동일점으로 주입해 추정 귀가 그 점이 되도록 한다
     * (코 = 눈일 때 추정 귀 = 눈 + (눈-코)×1.4 = 눈). 어깨는 좌·우 대칭으로 C7=(0.5,0.5).
     */
    private fun frameWithTragusAndC7(tragus: Point2D): PoseFrame =
        PoseFrameFixtures.frame(
            SessionView.SIDE,
            mapOf(
                JointName.NOSE to tragus,
                JointName.LEFT_EYE to tragus,
                JointName.RIGHT_EYE to tragus,
                JointName.LEFT_SHOULDER to Point2D(0.45f, 0.5f),
                JointName.RIGHT_SHOULDER to Point2D(0.55f, 0.5f)
            )
        )

    private fun tragusForCva(cvaDeg: Double, dx: Double = 0.2): Point2D {
        val dyAbs = dx * tan(cvaDeg * PI / 180.0)
        // 좌상단 원점: 위쪽으로 가려면 y가 작아짐. C7.y = 0.5, tragus.y = 0.5 - dyAbs.
        // Double로 계산 후 Float 캐스팅 — Float 누적 정밀도 손실 최소화.
        return Point2D((0.5 + dx).toFloat(), (0.5 - dyAbs).toFloat())
    }

    @Test
    fun `정상 자세 CVA 60도면 NORMAL`() {
        val frame = frameWithTragusAndC7(tragusForCva(60.0))
        val result = evaluator.evaluate(frame)
        assertEquals(PostureType.FORWARD_HEAD, result.type)
        assertEquals(PostureStatus.NORMAL, result.status)
        assertEquals("v2", result.algorithmVersion)
    }

    @Test
    fun `경계 CVA 53도면 NORMAL`() {
        val frame = frameWithTragusAndC7(tragusForCva(53.0))
        val result = evaluator.evaluate(frame)
        assertEquals(PostureStatus.NORMAL, result.status)
    }

    @Test
    fun `CVA 50도면 CAUTION`() {
        val frame = frameWithTragusAndC7(tragusForCva(50.0))
        val result = evaluator.evaluate(frame)
        assertEquals(PostureStatus.CAUTION, result.status)
    }

    @Test
    fun `CVA 40도면 SUSPECT`() {
        val frame = frameWithTragusAndC7(tragusForCva(40.0))
        val result = evaluator.evaluate(frame)
        assertEquals(PostureStatus.SUSPECT, result.status)
    }

    @Test
    fun `귀 미검출 코눈 추정으로 측정 가능`() {
        // 코=(0.5,0.6) 눈=(0.5,0.5) → 추정 귀 = (0.5, 0.5 + (0.5-0.6)×1.4) = (0.5, 0.36)
        // C7 = (0.5, 0.5) → dx=0, dy=0.14 → CVA = 90° → NORMAL
        val frame = PoseFrameFixtures.frame(
            SessionView.SIDE,
            mapOf(
                JointName.NOSE to Point2D(0.5f, 0.6f),
                JointName.LEFT_EYE to Point2D(0.5f, 0.5f),
                JointName.LEFT_SHOULDER to Point2D(0.45f, 0.5f),
                JointName.RIGHT_SHOULDER to Point2D(0.55f, 0.5f)
            )
        )
        val result = evaluator.evaluate(frame)
        assertNotEquals(PostureStatus.UNMEASURABLE, result.status)
        assertEquals(PostureStatus.NORMAL, result.status)
    }

    @Test
    fun `모든 landmark 신뢰도 부족하면 UNMEASURABLE`() {
        val frame = PoseFrameFixtures.frame(
            SessionView.SIDE,
            mapOf(
                JointName.NOSE to Point2D(0.5f, 0.4f),
                JointName.LEFT_SHOULDER to Point2D(0.45f, 0.5f),
                JointName.RIGHT_SHOULDER to Point2D(0.55f, 0.5f)
            ),
            confidence = 0.1f // < DEFAULT_CONFIDENCE 0.3
        )
        val result = evaluator.evaluate(frame)
        assertEquals(PostureStatus.UNMEASURABLE, result.status)
    }
}

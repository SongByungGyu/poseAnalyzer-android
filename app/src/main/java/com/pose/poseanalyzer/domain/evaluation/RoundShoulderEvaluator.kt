package com.pose.poseanalyzer.domain.evaluation

import com.pose.poseanalyzer.domain.model.JointName
import com.pose.poseanalyzer.domain.model.Point2D
import com.pose.poseanalyzer.domain.model.PoseFrame
import com.pose.poseanalyzer.domain.model.PostureResult
import com.pose.poseanalyzer.domain.model.PostureStatus
import com.pose.poseanalyzer.domain.model.PostureType
import com.pose.poseanalyzer.domain.model.SessionView
import com.pose.poseanalyzer.domain.model.Thresholds
import javax.inject.Inject
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.hypot

/**
 * 라운드숄더 (Round Shoulder) 판정 — v2: FSA (Forward Shoulder Angle).
 *
 * 임상 표준. C7-acromion 선이 수직선과 이루는 각도 = FSA.
 * - C7 = 양 어깨 중점 (ML Kit `.neck` 미제공)
 * - acromion = 카메라 가까운 쪽(신뢰도 높은) 어깨
 * - FSA = atan2(|acromion.x - C7.x|, |acromion.y - C7.y|) × 180/π
 *   atan2 절대값 사용으로 좌표계 영향 제거.
 *
 * 임계값: 정상 <47° / 주의 47~52° / 의심 ≥52° (cutoff 52° MDPI 2022).
 * 엉덩이 좌표 불필요.
 */
class RoundShoulderEvaluator @Inject constructor() : PostureEvaluator {

    override val type = PostureType.ROUND_SHOULDER
    override val requiredView = SessionView.SIDE

    private val thresholds = Thresholds(
        normalRange = 0.0..47.0,
        cautionRange = 47.0..52.0,
        direction = Thresholds.Direction.LOWER_IS_NORMAL
    )

    override fun evaluate(frame: PoseFrame): PostureResult {
        val c7 = frame.neck
            ?: return PostureResult.unmeasurable(type, "측면 어깨 인식 부족 (C7 산출 불가)")

        val acromion = pickBest(frame, JointName.LEFT_SHOULDER, JointName.RIGHT_SHOULDER)
            ?: return PostureResult.unmeasurable(type, "어깨 인식 부족")

        val dx = abs(acromion.first.x - c7.x).toDouble()
        val dy = abs(acromion.first.y - c7.y).toDouble()
        if (hypot(dx, dy) < 0.005) {
            return PostureResult.unmeasurable(type, "C7와 어깨가 너무 가까움 (양 어깨 위치 동일)")
        }

        val fsa = atan2(dx, dy) * 180.0 / PI
        val status = thresholds.evaluate(fsa)

        return PostureResult(
            type = type,
            status = status,
            primaryMetric = fsa,
            primaryMetricUnit = PostureResult.MetricUnit.DEGREE,
            thresholds = thresholds,
            usedJointNames = listOf("C7(neck)", acromion.second.name),
            confidence = frame.averageConfidence(
                listOf(JointName.LEFT_SHOULDER, JointName.RIGHT_SHOULDER)
            ),
            advice = if (status == PostureStatus.NORMAL) null
                else "어깨를 뒤로 펴는 스트레칭을 정기적으로 해주세요.",
            algorithmVersion = "v2"
        )
    }

    private fun pickBest(
        frame: PoseFrame,
        left: JointName,
        right: JointName
    ): Pair<Point2D, JointName>? {
        val l = frame.point(left)?.takeIf { frame.isReliable(left) }
        val r = frame.point(right)?.takeIf { frame.isReliable(right) }
        return when {
            l != null && r != null -> {
                val lc = frame.averageConfidence(listOf(left))
                val rc = frame.averageConfidence(listOf(right))
                if (rc >= lc) r to right else l to left
            }
            l != null -> l to left
            r != null -> r to right
            else -> null
        }
    }
}

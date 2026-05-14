package com.pose.poseanalyzer.domain.evaluation

import com.pose.poseanalyzer.domain.model.JointName
import com.pose.poseanalyzer.domain.model.PoseFrame
import com.pose.poseanalyzer.domain.model.PostureResult
import com.pose.poseanalyzer.domain.model.PostureStatus
import com.pose.poseanalyzer.domain.model.PostureType
import com.pose.poseanalyzer.domain.model.SessionView
import com.pose.poseanalyzer.domain.model.Thresholds
import com.pose.poseanalyzer.util.GeometryMath
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.min

/**
 * 무릎 X자(Genu Valgum) / O자(Genu Varum) 정렬 판정.
 *
 * 측정: 양 다리 각각 엉덩이-무릎-발목 각도.
 * 임계값: 175~180 정상, 170~175 / 180~185 주의, 그 바깥 의심.
 */
class KneeAlignmentEvaluator @Inject constructor() : PostureEvaluator {

    override val type = PostureType.KNEE_ALIGNMENT
    override val requiredView = SessionView.FRONT

    private val thresholds = Thresholds(
        normalRange = 175.0..180.0,
        cautionRange = 170.0..185.0,
        direction = Thresholds.Direction.CENTERED_ON_RANGE
    )

    override fun evaluate(frame: PoseFrame): PostureResult {
        val leftJoints = listOf(JointName.LEFT_HIP, JointName.LEFT_KNEE, JointName.LEFT_ANKLE)
        val rightJoints = listOf(JointName.RIGHT_HIP, JointName.RIGHT_KNEE, JointName.RIGHT_ANKLE)

        if (!frame.areReliable(leftJoints) || !frame.areReliable(rightJoints)) {
            return PostureResult.unmeasurable(type, "양 다리 관절 신뢰도 부족")
        }

        val lh = frame.point(JointName.LEFT_HIP)
        val lk = frame.point(JointName.LEFT_KNEE)
        val la = frame.point(JointName.LEFT_ANKLE)
        val rh = frame.point(JointName.RIGHT_HIP)
        val rk = frame.point(JointName.RIGHT_KNEE)
        val ra = frame.point(JointName.RIGHT_ANKLE)
        if (lh == null || lk == null || la == null || rh == null || rk == null || ra == null) {
            return PostureResult.unmeasurable(type, "관절 좌표 누락")
        }

        val leftAngle = GeometryMath.angleBetween(lh, lk, la)
        val rightAngle = GeometryMath.angleBetween(rh, rk, ra)

        val leftDeviation = min(abs(leftAngle - 175), abs(leftAngle - 180))
        val rightDeviation = min(abs(rightAngle - 175), abs(rightAngle - 180))
        val primary = if (leftDeviation > rightDeviation) leftAngle else rightAngle
        val status = thresholds.evaluate(primary)

        val pattern = when {
            leftAngle < 175 && rightAngle < 175 -> "X자(내반슬) 경향"
            leftAngle > 180 && rightAngle > 180 -> "O자(외반슬) 경향"
            else -> "한쪽 다리 정렬 이상"
        }

        return PostureResult(
            type = type,
            status = status,
            primaryMetric = primary,
            primaryMetricUnit = PostureResult.MetricUnit.DEGREE,
            thresholds = thresholds,
            usedJointNames = (leftJoints + rightJoints).map { it.name },
            confidence = frame.averageConfidence(leftJoints + rightJoints),
            advice = if (status == PostureStatus.NORMAL) null
                else String.format("%s. 좌측 %.0f° / 우측 %.0f°", pattern, leftAngle, rightAngle)
        )
    }
}

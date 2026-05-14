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

/**
 * 무릎 과신전 (Knee Hyperextension) 판정.
 *
 * 측정: 엉덩이-무릎-발목 각도.
 * 임계값: ≤185 정상, 185~190 주의, >190 의심 (한 방향 — 과신전만 평가).
 *
 * `acos`는 0~180만 반환하므로 cross-product 부호로 과신전 방향(>180°) 보정.
 */
class KneeHyperextensionEvaluator @Inject constructor() : PostureEvaluator {

    override val type = PostureType.KNEE_HYPEREXTENSION
    override val requiredView = SessionView.SIDE

    private val thresholds = Thresholds(
        normalRange = 0.0..185.0,
        cautionRange = 185.0..190.0,
        direction = Thresholds.Direction.HIGHER_IS_NORMAL
    )

    override fun evaluate(frame: PoseFrame): PostureResult {
        val leftJoints = listOf(JointName.LEFT_HIP, JointName.LEFT_KNEE, JointName.LEFT_ANKLE)
        val rightJoints = listOf(JointName.RIGHT_HIP, JointName.RIGHT_KNEE, JointName.RIGHT_ANKLE)

        val leftReliable = frame.areReliable(leftJoints)
        val rightReliable = frame.areReliable(rightJoints)
        if (!leftReliable && !rightReliable) {
            return PostureResult.unmeasurable(type, "엉덩이·무릎·발목 관절 인식 부족")
        }

        val leftConf = if (leftReliable) frame.averageConfidence(leftJoints) else 0.0
        val rightConf = if (rightReliable) frame.averageConfidence(rightJoints) else 0.0
        val useRight = rightConf > leftConf
        val joints = if (useRight) rightJoints else leftJoints

        val hip = frame.point(joints[0])
        val knee = frame.point(joints[1])
        val ankle = frame.point(joints[2])
        if (hip == null || knee == null || ankle == null) {
            return PostureResult.unmeasurable(type, "관절 좌표 누락")
        }

        val rawAngle = GeometryMath.angleBetween(hip, knee, ankle)
        // 무릎이 엉덩이-발목 라인의 어느 쪽에 있는지 cross-product 부호로 판별.
        // iOS Vision은 y축이 좌하단 원점이라 cross < 0이 과신전이지만,
        // ML Kit은 좌상단 원점이라 같은 자세에서 cross 부호가 반대. → cross > 0 → 과신전.
        val cross =
            (knee.x - hip.x).toDouble() * (ankle.y - knee.y).toDouble() -
                (knee.y - hip.y).toDouble() * (ankle.x - knee.x).toDouble()
        val angle = if (cross > 0) 360.0 - rawAngle else rawAngle
        val status = thresholds.evaluate(angle)

        return PostureResult(
            type = type,
            status = status,
            primaryMetric = angle,
            primaryMetricUnit = PostureResult.MetricUnit.DEGREE,
            thresholds = thresholds,
            usedJointNames = joints.map { it.name },
            confidence = if (useRight) rightConf else leftConf,
            advice = if (status == PostureStatus.NORMAL) null
                else "서 있을 때 무릎을 살짝 굽혀 정렬을 유지해보세요."
        )
    }
}

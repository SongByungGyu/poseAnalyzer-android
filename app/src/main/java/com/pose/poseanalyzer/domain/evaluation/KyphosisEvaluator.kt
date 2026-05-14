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
 * 흉추 후만증 (Kyphosis) 판정 — 등 위쪽 굽음.
 *
 * 측정: 목(양 어깨 중점)-어깨-엉덩이 각도.
 * 임계값: ≥175° 정상, 165~175° 주의, <165° 의심.
 *
 * iOS는 Vision의 `.neck`을 직접 사용하지만 ML Kit엔 neck이 없으므로
 * [PoseFrame.neck]에서 양 어깨 중점으로 계산.
 */
class KyphosisEvaluator @Inject constructor() : PostureEvaluator {

    override val type = PostureType.KYPHOSIS
    override val requiredView = SessionView.SIDE

    private val thresholds = Thresholds(
        normalRange = 175.0..360.0,
        cautionRange = 165.0..175.0,
        direction = Thresholds.Direction.HIGHER_IS_NORMAL
    )

    override fun evaluate(frame: PoseFrame): PostureResult {
        val neck = frame.neck
            ?: return PostureResult.unmeasurable(type, "양 어깨(목 계산용) 관절 인식 부족")

        val leftJoints = listOf(JointName.LEFT_SHOULDER, JointName.LEFT_HIP)
        val rightJoints = listOf(JointName.RIGHT_SHOULDER, JointName.RIGHT_HIP)

        val leftReliable = frame.areReliable(leftJoints)
        val rightReliable = frame.areReliable(rightJoints)

        if (!leftReliable && !rightReliable) {
            return PostureResult.unmeasurable(type, "어깨·엉덩이 관절 인식 부족")
        }

        val leftConf = if (leftReliable) frame.averageConfidence(leftJoints) else 0.0
        val rightConf = if (rightReliable) frame.averageConfidence(rightJoints) else 0.0
        val useRight = rightConf > leftConf
        val joints = if (useRight) rightJoints else leftJoints

        val shoulder = frame.point(joints[0])
        val hip = frame.point(joints[1])
        if (shoulder == null || hip == null) {
            return PostureResult.unmeasurable(type, "관절 좌표 누락")
        }

        val angle = GeometryMath.angleBetween(neck, shoulder, hip)
        val status = thresholds.evaluate(angle)

        return PostureResult(
            type = type,
            status = status,
            primaryMetric = angle,
            primaryMetricUnit = PostureResult.MetricUnit.DEGREE,
            thresholds = thresholds,
            usedJointNames = listOf("NECK") + joints.map { it.name },
            confidence = if (useRight) rightConf else leftConf,
            advice = if (status == PostureStatus.NORMAL) null
                else "흉추 신전 스트레칭(폼롤러)을 권장합니다."
        )
    }
}

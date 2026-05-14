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
 * 골반 전방/후방경사 판정.
 *
 * 측정: 어깨-엉덩이-무릎 각도.
 * 임계값: 175~185 정상, 170~175 / 185~190 주의, 그 바깥 의심.
 */
class AnteriorPelvicTiltEvaluator @Inject constructor() : PostureEvaluator {

    override val type = PostureType.ANTERIOR_PELVIC_TILT
    override val requiredView = SessionView.SIDE

    private val thresholds = Thresholds(
        normalRange = 175.0..185.0,
        cautionRange = 170.0..190.0,
        direction = Thresholds.Direction.CENTERED_ON_RANGE
    )

    override fun evaluate(frame: PoseFrame): PostureResult {
        val leftJoints = listOf(JointName.LEFT_SHOULDER, JointName.LEFT_HIP, JointName.LEFT_KNEE)
        val rightJoints = listOf(JointName.RIGHT_SHOULDER, JointName.RIGHT_HIP, JointName.RIGHT_KNEE)

        val leftReliable = frame.areReliable(leftJoints)
        val rightReliable = frame.areReliable(rightJoints)
        if (!leftReliable && !rightReliable) {
            return PostureResult.unmeasurable(type, "어깨·엉덩이·무릎 관절 인식 부족")
        }

        val leftConf = if (leftReliable) frame.averageConfidence(leftJoints) else 0.0
        val rightConf = if (rightReliable) frame.averageConfidence(rightJoints) else 0.0
        val useRight = rightConf > leftConf
        val joints = if (useRight) rightJoints else leftJoints

        val shoulder = frame.point(joints[0])
        val hip = frame.point(joints[1])
        val knee = frame.point(joints[2])
        if (shoulder == null || hip == null || knee == null) {
            return PostureResult.unmeasurable(type, "관절 좌표 누락")
        }

        val angle = GeometryMath.angleBetween(shoulder, hip, knee)
        val status = thresholds.evaluate(angle)

        val direction = when {
            angle < 175 -> "전방경사 경향"
            angle > 185 -> "후방경사 경향"
            else -> ""
        }

        return PostureResult(
            type = type,
            status = status,
            primaryMetric = angle,
            primaryMetricUnit = PostureResult.MetricUnit.DEGREE,
            thresholds = thresholds,
            usedJointNames = joints.map { it.name },
            confidence = if (useRight) rightConf else leftConf,
            advice = if (status == PostureStatus.NORMAL) null
                else "$direction. 코어 강화와 골반 정렬 운동을 권장합니다."
        )
    }
}

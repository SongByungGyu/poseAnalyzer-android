package com.pose.poseanalyzer.domain.evaluation

import com.pose.poseanalyzer.domain.model.JointName
import com.pose.poseanalyzer.domain.model.PoseFrame
import com.pose.poseanalyzer.domain.model.PostureResult
import com.pose.poseanalyzer.domain.model.PostureType
import com.pose.poseanalyzer.domain.model.SessionView
import com.pose.poseanalyzer.domain.model.Thresholds
import com.pose.poseanalyzer.util.GeometryMath
import javax.inject.Inject

/**
 * 거북목 (Forward Head Posture) 판정.
 *
 * 측정: 귀-어깨-엉덩이 각도.
 * 임계값: 정상 ≥170°, 주의 160~170°, 의심 <160°.
 */
class ForwardHeadEvaluator @Inject constructor() : PostureEvaluator {

    override val type = PostureType.FORWARD_HEAD
    override val requiredView = SessionView.SIDE

    private val thresholds = Thresholds(
        normalRange = 170.0..360.0,
        cautionRange = 160.0..170.0,
        direction = Thresholds.Direction.HIGHER_IS_NORMAL
    )

    override fun evaluate(frame: PoseFrame): PostureResult {
        val leftJoints = listOf(JointName.LEFT_EAR, JointName.LEFT_SHOULDER, JointName.LEFT_HIP)
        val rightJoints = listOf(JointName.RIGHT_EAR, JointName.RIGHT_SHOULDER, JointName.RIGHT_HIP)

        val leftReliable = frame.areReliable(leftJoints)
        val rightReliable = frame.areReliable(rightJoints)

        if (!leftReliable && !rightReliable) {
            return PostureResult.unmeasurable(type, "측면 귀·어깨·엉덩이 관절 인식 부족")
        }

        val leftConf = if (leftReliable) frame.averageConfidence(leftJoints) else 0.0
        val rightConf = if (rightReliable) frame.averageConfidence(rightJoints) else 0.0
        val useRight = rightConf > leftConf
        val joints = if (useRight) rightJoints else leftJoints

        val ear = frame.point(joints[0])
        val shoulder = frame.point(joints[1])
        val hip = frame.point(joints[2])
        if (ear == null || shoulder == null || hip == null) {
            return PostureResult.unmeasurable(type, "관절 좌표 누락")
        }

        val angle = GeometryMath.angleBetween(ear, shoulder, hip)
        val status = thresholds.evaluate(angle)

        return PostureResult(
            type = type,
            status = status,
            primaryMetric = angle,
            primaryMetricUnit = PostureResult.MetricUnit.DEGREE,
            thresholds = thresholds,
            usedJointNames = joints.map { it.name },
            confidence = if (useRight) rightConf else leftConf,
            advice = if (status == com.pose.poseanalyzer.domain.model.PostureStatus.NORMAL) null
                else "장시간 고개를 숙이지 마시고, 모니터 높이를 눈높이로 맞춰주세요."
        )
    }
}

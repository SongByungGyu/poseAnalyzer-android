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
 * 라운드숄더 (Round Shoulder) 판정.
 *
 * 측정: 측면 사진에서 어깨가 귀보다 얼마나 앞에 있는지 (수평 거리 / 어깨 폭 비율).
 * 임계값: <0.15 정상, 0.15~0.25 주의, >0.25 의심.
 */
class RoundShoulderEvaluator @Inject constructor() : PostureEvaluator {

    override val type = PostureType.ROUND_SHOULDER
    override val requiredView = SessionView.SIDE

    private val thresholds = Thresholds(
        normalRange = 0.0..0.15,
        cautionRange = 0.15..0.25,
        direction = Thresholds.Direction.LOWER_IS_NORMAL
    )

    override fun evaluate(frame: PoseFrame): PostureResult {
        val leftShoulder = frame.point(JointName.LEFT_SHOULDER)
        val rightShoulder = frame.point(JointName.RIGHT_SHOULDER)
        if (leftShoulder == null || rightShoulder == null) {
            return PostureResult.unmeasurable(type, "어깨 관절 인식 부족")
        }
        val shoulderWidth = GeometryMath.distance(leftShoulder, rightShoulder)
        if (shoulderWidth <= 0.01) {
            return PostureResult.unmeasurable(type, "어깨 폭 측정 실패")
        }

        val leftReliable = frame.areReliable(listOf(JointName.LEFT_EAR, JointName.LEFT_SHOULDER))
        val rightReliable = frame.areReliable(listOf(JointName.RIGHT_EAR, JointName.RIGHT_SHOULDER))

        if (!leftReliable && !rightReliable) {
            return PostureResult.unmeasurable(type, "귀·어깨 관절 신뢰도 부족")
        }

        val leftConf = if (leftReliable) frame.averageConfidence(listOf(JointName.LEFT_EAR, JointName.LEFT_SHOULDER)) else 0.0
        val rightConf = if (rightReliable) frame.averageConfidence(listOf(JointName.RIGHT_EAR, JointName.RIGHT_SHOULDER)) else 0.0
        val useRight = rightConf > leftConf
        val earName = if (useRight) JointName.RIGHT_EAR else JointName.LEFT_EAR
        val shoulderName = if (useRight) JointName.RIGHT_SHOULDER else JointName.LEFT_SHOULDER

        val ear = frame.point(earName)
        val shoulder = frame.point(shoulderName)
        if (ear == null || shoulder == null) {
            return PostureResult.unmeasurable(type, "관절 좌표 누락")
        }

        val ratio = GeometryMath.horizontalGapRatio(ear, shoulder, referenceWidth = shoulderWidth)
        val status = thresholds.evaluate(ratio)

        return PostureResult(
            type = type,
            status = status,
            primaryMetric = ratio,
            primaryMetricUnit = PostureResult.MetricUnit.RATIO,
            thresholds = thresholds,
            usedJointNames = listOf(earName.name, shoulderName.name),
            confidence = if (useRight) rightConf else leftConf,
            advice = if (status == PostureStatus.NORMAL) null
                else "어깨를 뒤로 펴는 스트레칭을 정기적으로 해주세요."
        )
    }
}

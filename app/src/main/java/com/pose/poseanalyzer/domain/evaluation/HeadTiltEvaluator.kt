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
 * 머리 좌우 기울기 (Head Tilt) — 정면 사진.
 *
 * 양 귀를 우선 사용. 신뢰도 부족 시 양 눈으로 fallback.
 */
class HeadTiltEvaluator @Inject constructor() : PostureEvaluator {

    override val type = PostureType.HEAD_TILT
    override val requiredView = SessionView.FRONT

    private val thresholds = Thresholds(
        normalRange = 0.0..2.0,
        cautionRange = 2.0..5.0,
        direction = Thresholds.Direction.LOWER_IS_NORMAL
    )

    override fun evaluate(frame: PoseFrame): PostureResult {
        val earsReliable = frame.areReliable(listOf(JointName.LEFT_EAR, JointName.RIGHT_EAR))
        val eyesReliable = frame.areReliable(listOf(JointName.LEFT_EYE, JointName.RIGHT_EYE))

        val leftName: JointName
        val rightName: JointName
        val usedConfidence: Double
        when {
            earsReliable -> {
                leftName = JointName.LEFT_EAR
                rightName = JointName.RIGHT_EAR
                usedConfidence = frame.averageConfidence(listOf(JointName.LEFT_EAR, JointName.RIGHT_EAR))
            }
            eyesReliable -> {
                leftName = JointName.LEFT_EYE
                rightName = JointName.RIGHT_EYE
                usedConfidence = frame.averageConfidence(listOf(JointName.LEFT_EYE, JointName.RIGHT_EYE))
            }
            else -> {
                return PostureResult.unmeasurable(type, "양 귀·양 눈 모두 인식 부족")
            }
        }

        val left = frame.point(leftName)
        val right = frame.point(rightName)
        if (left == null || right == null) {
            return PostureResult.unmeasurable(type, "관절 좌표 누락")
        }

        val tilt = GeometryMath.absLineAngleFromHorizontal(left, right)
        val status = thresholds.evaluate(tilt)

        return PostureResult(
            type = type,
            status = status,
            primaryMetric = tilt,
            primaryMetricUnit = PostureResult.MetricUnit.DEGREE,
            thresholds = thresholds,
            usedJointNames = listOf(leftName.name, rightName.name),
            confidence = usedConfidence,
            advice = if (status == PostureStatus.NORMAL) null
                else "한쪽으로 머리를 기우는 습관이 있는지 확인해보세요."
        )
    }
}

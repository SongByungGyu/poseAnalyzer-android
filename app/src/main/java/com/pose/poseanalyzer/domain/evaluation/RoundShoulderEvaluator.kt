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
 * 측정: 귀(또는 코·눈으로 추정한 귀)와 어깨의 수평 거리를
 *       **어깨-엉덩이 세로 거리** (몸통 높이) 로 나눈 비율.
 *       양 어깨 거리는 측면에서 거의 0이라 불안정해 분모로 부적합.
 * 임계값: <0.05 정상, 0.05~0.10 주의, >0.10 의심.
 */
class RoundShoulderEvaluator @Inject constructor() : PostureEvaluator {

    override val type = PostureType.ROUND_SHOULDER
    override val requiredView = SessionView.SIDE

    private val thresholds = Thresholds(
        normalRange = 0.0..0.05,
        cautionRange = 0.05..0.10,
        direction = Thresholds.Direction.LOWER_IS_NORMAL
    )

    override fun evaluate(frame: PoseFrame): PostureResult {
        // 어깨·엉덩이 (몸통 높이 기준) 필수. 좌/우 중 둘 다 신뢰 가능한 쪽 선택.
        val leftCore = listOf(JointName.LEFT_SHOULDER, JointName.LEFT_HIP)
        val rightCore = listOf(JointName.RIGHT_SHOULDER, JointName.RIGHT_HIP)
        val leftCoreOK = frame.areReliable(leftCore)
        val rightCoreOK = frame.areReliable(rightCore)

        if (!leftCoreOK && !rightCoreOK) {
            return PostureResult.unmeasurable(type, "측면 어깨·엉덩이 인식 부족")
        }

        val useRight = when {
            leftCoreOK && rightCoreOK ->
                frame.averageConfidence(rightCore) > frame.averageConfidence(leftCore)
            else -> rightCoreOK
        }

        val earName = if (useRight) JointName.RIGHT_EAR else JointName.LEFT_EAR
        val eyeName = if (useRight) JointName.RIGHT_EYE else JointName.LEFT_EYE
        val shoulderName = if (useRight) JointName.RIGHT_SHOULDER else JointName.LEFT_SHOULDER
        val hipName = if (useRight) JointName.RIGHT_HIP else JointName.LEFT_HIP

        val shoulder = frame.point(shoulderName)
        val hip = frame.point(hipName)
        if (shoulder == null || hip == null) {
            return PostureResult.unmeasurable(type, "관절 좌표 누락")
        }

        // 분모: 몸통 세로 길이 (어깨 ↔ 엉덩이)
        val torsoHeight = kotlin.math.abs((shoulder.y - hip.y).toDouble())
        if (torsoHeight <= 0.01) {
            return PostureResult.unmeasurable(type, "몸통 길이 측정 실패")
        }

        // 머리 기준점(귀) 결정 — 코+눈 추정을 1순위로 사용.
        // 코가 가려진 경우(마스크 등)에만 검출된 귀로 fallback.
        val nose = frame.point(JointName.NOSE)
        val eye = frame.point(eyeName)
        val noseEyeOK = frame.areReliable(listOf(JointName.NOSE, eyeName))
        val realEar = if (frame.isReliable(earName)) frame.point(earName) else null

        val earPoint: com.pose.poseanalyzer.domain.model.Point2D
        val usedJoints: List<String>
        when {
            noseEyeOK && nose != null && eye != null -> {
                earPoint = GeometryMath.estimateEarFromNoseEye(nose, eye)
                usedJoints = listOf("NOSE+${eyeName.name}→EAR", shoulderName.name, hipName.name)
            }
            realEar != null -> {
                earPoint = realEar
                usedJoints = listOf(earName.name, shoulderName.name, hipName.name)
            }
            else -> return PostureResult.unmeasurable(
                type, "옆모습에서 얼굴(코·눈·귀)이 잘 보이도록 다시 촬영해 주세요."
            )
        }

        val ratio = GeometryMath.horizontalGapRatio(earPoint, shoulder, referenceWidth = torsoHeight)
        val status = thresholds.evaluate(ratio)

        val advice = if (status == PostureStatus.NORMAL) null
            else "어깨를 뒤로 펴는 스트레칭을 정기적으로 해주세요."

        return PostureResult(
            type = type,
            status = status,
            primaryMetric = ratio,
            primaryMetricUnit = PostureResult.MetricUnit.RATIO,
            thresholds = thresholds,
            usedJointNames = usedJoints,
            confidence = frame.averageConfidence(if (useRight) rightCore else leftCore),
            advice = advice
        )
    }
}

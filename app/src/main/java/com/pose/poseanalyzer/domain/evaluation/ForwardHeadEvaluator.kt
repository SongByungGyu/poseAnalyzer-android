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
        // 어깨·엉덩이는 필수. 좌/우 중 둘 다 신뢰 가능한 쪽 선택.
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

        // 머리 기준점(귀) 결정 — 코+눈 추정을 1순위로 사용.
        // 검출된 귀는 머리카락/포니테일에 흔들려 ML 엔진 간 편차가 큼.
        // 코·눈은 머리카락 영향이 없고 추정이 결정론적 기하라 양 플랫폼이 수렴함.
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

        val angle = GeometryMath.angleBetween(earPoint, shoulder, hip)
        val status = thresholds.evaluate(angle)

        return PostureResult(
            type = type,
            status = status,
            primaryMetric = angle,
            primaryMetricUnit = PostureResult.MetricUnit.DEGREE,
            thresholds = thresholds,
            usedJointNames = usedJoints,
            confidence = frame.averageConfidence(if (useRight) rightCore else leftCore),
            advice = if (status == com.pose.poseanalyzer.domain.model.PostureStatus.NORMAL) null
                else "장시간 고개를 숙이지 마시고, 모니터 높이를 눈높이로 맞춰주세요."
        )
    }
}

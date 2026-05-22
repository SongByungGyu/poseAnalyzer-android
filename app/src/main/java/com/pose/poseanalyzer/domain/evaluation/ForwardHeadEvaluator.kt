package com.pose.poseanalyzer.domain.evaluation

import com.pose.poseanalyzer.domain.model.JointName
import com.pose.poseanalyzer.domain.model.Point2D
import com.pose.poseanalyzer.domain.model.PoseFrame
import com.pose.poseanalyzer.domain.model.PostureResult
import com.pose.poseanalyzer.domain.model.PostureStatus
import com.pose.poseanalyzer.domain.model.PostureType
import com.pose.poseanalyzer.domain.model.SessionView
import com.pose.poseanalyzer.domain.model.Thresholds
import com.pose.poseanalyzer.util.GeometryMath
import javax.inject.Inject
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2

/**
 * 거북목 (Forward Head Posture) 판정 — v2: CVA (Craniovertebral Angle).
 *
 * 임상 표준. C7과 tragus를 잇는 선이 수평선과 이루는 각도 = CVA.
 * - C7 = 양 어깨 중점 (ML Kit은 `.neck` 미제공 — iOS Vision의 `.neck` 대응)
 * - tragus = 코+눈 추정 1순위 / 검출 귀 2순위
 * - CVA = atan2(|tragus.y - C7.y|, |tragus.x - C7.x|) × 180/π
 *   atan2 절대값 사용으로 ML Kit(좌상단)·Vision(좌하단) 좌표계 영향 제거.
 *
 * 임계값: 정상 ≥53° / 주의 48~53° / 의심 <48°.
 * 엉덩이 좌표 불필요 — 상반신 모드 확장 발판 (별도 spec).
 */
class ForwardHeadEvaluator @Inject constructor() : PostureEvaluator {

    override val type = PostureType.FORWARD_HEAD
    override val requiredView = SessionView.SIDE

    private val thresholds = Thresholds(
        normalRange = 53.0..180.0,
        cautionRange = 48.0..53.0,
        direction = Thresholds.Direction.HIGHER_IS_NORMAL
    )

    override fun evaluate(frame: PoseFrame): PostureResult {
        val c7 = frame.neck
            ?: return PostureResult.unmeasurable(type, "측면 어깨 인식 부족 (C7 산출 불가)")

        val nose = frame.point(JointName.NOSE)
        val noseReliable = frame.isReliable(JointName.NOSE)
        val bestEye = pickBest(frame, JointName.LEFT_EYE, JointName.RIGHT_EYE)
        val bestEar = pickBest(frame, JointName.LEFT_EAR, JointName.RIGHT_EAR)

        val tragus: Point2D
        val usedLabel: String
        when {
            noseReliable && nose != null && bestEye != null -> {
                tragus = GeometryMath.estimateEarFromNoseEye(nose, bestEye.first)
                usedLabel = "NOSE+${bestEye.second.name}→EAR"
            }
            bestEar != null -> {
                tragus = bestEar.first
                usedLabel = bestEar.second.name
            }
            else -> return PostureResult.unmeasurable(
                type, "옆모습에서 얼굴(코·눈·귀)이 잘 보이도록 다시 촬영해 주세요."
            )
        }

        val dy = abs(tragus.y - c7.y).toDouble()
        val dx = abs(tragus.x - c7.x).toDouble()
        val cva = atan2(dy, dx) * 180.0 / PI
        val status = thresholds.evaluate(cva)

        return PostureResult(
            type = type,
            status = status,
            primaryMetric = cva,
            primaryMetricUnit = PostureResult.MetricUnit.DEGREE,
            thresholds = thresholds,
            usedJointNames = listOf("C7(neck)", usedLabel),
            confidence = frame.averageConfidence(
                listOf(
                    JointName.LEFT_SHOULDER, JointName.RIGHT_SHOULDER,
                    JointName.NOSE, JointName.LEFT_EYE, JointName.RIGHT_EYE
                )
            ),
            advice = if (status == PostureStatus.NORMAL) null
                else "장시간 고개를 숙이지 마시고, 모니터 높이를 눈높이로 맞춰주세요.",
            algorithmVersion = "v2"
        )
    }

    /** 좌·우 중 신뢰 가능한 쪽 반환. 둘 다면 신뢰도 높은 쪽. */
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

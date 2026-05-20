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
import kotlin.math.max

/**
 * 척추측만 추정 — 어깨/엉덩이 좌우 기울기 합산.
 *
 * 측정: 양 어깨 직선 + 양 엉덩이 직선의 수평선 대비 기울기 (절댓값).
 * 임계값: 둘 다 <2° 정상, 둘 중 하나 2~5° 주의, 5° 초과 의심.
 */
class ScoliosisEvaluator @Inject constructor() : PostureEvaluator {

    override val type = PostureType.SCOLIOSIS
    override val requiredView = SessionView.FRONT

    // 정상 0~4° — ML 모델 간 좌표 차이로 인한 2~3° 노이즈를 흡수.
    // 의심 >5°는 임상 기준(Cobb 10°+ 상당)이라 유지.
    private val thresholds = Thresholds(
        normalRange = 0.0..4.0,
        cautionRange = 4.0..5.0,
        direction = Thresholds.Direction.LOWER_IS_NORMAL
    )

    override fun evaluate(frame: PoseFrame): PostureResult {
        val needed = listOf(
            JointName.LEFT_SHOULDER, JointName.RIGHT_SHOULDER,
            JointName.LEFT_HIP, JointName.RIGHT_HIP
        )
        if (!frame.areReliable(needed)) {
            return PostureResult.unmeasurable(type, "양 어깨·엉덩이 관절 신뢰도 부족")
        }

        val ls = frame.point(JointName.LEFT_SHOULDER)
        val rs = frame.point(JointName.RIGHT_SHOULDER)
        val lh = frame.point(JointName.LEFT_HIP)
        val rh = frame.point(JointName.RIGHT_HIP)
        if (ls == null || rs == null || lh == null || rh == null) {
            return PostureResult.unmeasurable(type, "관절 좌표 누락")
        }

        val shoulderTilt = GeometryMath.absLineAngleFromHorizontal(ls, rs)
        val hipTilt = GeometryMath.absLineAngleFromHorizontal(lh, rh)
        val primary = max(shoulderTilt, hipTilt)
        val status = thresholds.evaluate(primary)

        return PostureResult(
            type = type,
            status = status,
            primaryMetric = primary,
            primaryMetricUnit = PostureResult.MetricUnit.DEGREE,
            thresholds = thresholds,
            usedJointNames = needed.map { it.name },
            confidence = frame.averageConfidence(needed),
            advice = if (status == PostureStatus.NORMAL) null
                else String.format(
                    "어깨 기울기 %.1f° / 골반 기울기 %.1f°. 전문가 상담을 권장합니다.",
                    shoulderTilt, hipTilt
                )
        )
    }
}

package com.pose.poseanalyzer.domain.model

/**
 * 단일 자세 판정 결과.
 */
data class PostureResult(
    val type: PostureType,
    val status: PostureStatus,
    val primaryMetric: Double,
    val primaryMetricUnit: MetricUnit,
    val thresholds: Thresholds,
    val usedJointNames: List<String>,
    val confidence: Double,
    val advice: String?
) {
    enum class MetricUnit(val symbol: String) {
        DEGREE("°"),
        RATIO(""),
        CENTIMETER("cm")
    }

    // region Deviation Display

    /** 이상값 대비 편차 (항상 양수, 클수록 나쁨) */
    val deviationValue: Double get() = when (type) {
        PostureType.FORWARD_HEAD         -> maxOf(0.0, 180.0 - primaryMetric)
        PostureType.ROUND_SHOULDER       -> primaryMetric * 100.0   // 비율 → %
        PostureType.KYPHOSIS             -> maxOf(0.0, 180.0 - primaryMetric)
        PostureType.ANTERIOR_PELVIC_TILT -> kotlin.math.abs(180.0 - primaryMetric)
        PostureType.KNEE_HYPEREXTENSION  -> maxOf(0.0, primaryMetric - 180.0)
        PostureType.SCOLIOSIS            -> primaryMetric
        PostureType.HEAD_TILT            -> primaryMetric
        PostureType.KNEE_ALIGNMENT       -> kotlin.math.abs(primaryMetric - 180.0)
    }

    /** 편차 단위 기호 */
    val deviationUnitSymbol: String get() =
        if (type == PostureType.ROUND_SHOULDER) "%" else "°"

    /** 편차 방향/의미 레이블 */
    val deviationLabel: String get() = when (type) {
        PostureType.FORWARD_HEAD         -> "앞으로 기울어짐"
        PostureType.ROUND_SHOULDER       -> "전방 이동"
        PostureType.KYPHOSIS             -> "굽은 정도"
        PostureType.ANTERIOR_PELVIC_TILT -> if (primaryMetric < 180.0) "전방경사" else "후방경사"
        PostureType.KNEE_HYPEREXTENSION  -> "과신전"
        PostureType.SCOLIOSIS            -> "기울어짐"
        PostureType.HEAD_TILT            -> "기울어짐"
        PostureType.KNEE_ALIGNMENT       -> if (primaryMetric < 180.0) "X자 경향" else "O자 경향"
    }

    // endregion

    companion object {
        /**
         * 신뢰도 부족 등 측정 불가 결과 생성용 헬퍼.
         */
        fun unmeasurable(type: PostureType, reason: String): PostureResult =
            PostureResult(
                type = type,
                status = PostureStatus.UNMEASURABLE,
                primaryMetric = 0.0,
                primaryMetricUnit = MetricUnit.DEGREE,
                thresholds = Thresholds(
                    normalRange = 0.0..0.0,
                    cautionRange = null,
                    direction = Thresholds.Direction.HIGHER_IS_NORMAL
                ),
                usedJointNames = emptyList(),
                confidence = 0.0,
                advice = reason
            )
    }
}

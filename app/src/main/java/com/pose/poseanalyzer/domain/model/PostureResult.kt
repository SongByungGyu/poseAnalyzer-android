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

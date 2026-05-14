package com.pose.poseanalyzer.domain.model

/**
 * 자세 판정 임계값.
 *
 * 측정값이 [normalRange]에 들어가면 정상, [cautionRange]에 들어가면 주의,
 * 그 외는 의심으로 판정.
 *
 * [direction]은 정상이 어느 쪽에 있는지 표시 (UI 도식용).
 */
data class Thresholds(
    val normalRange: ClosedRange<Double>,
    val cautionRange: ClosedRange<Double>?,
    val direction: Direction
) {
    enum class Direction {
        HIGHER_IS_NORMAL,
        LOWER_IS_NORMAL,
        CENTERED_ON_RANGE
    }

    /** 측정값 평가 → [PostureStatus] */
    fun evaluate(value: Double): PostureStatus {
        if (normalRange.contains(value)) return PostureStatus.NORMAL
        cautionRange?.let { if (it.contains(value)) return PostureStatus.CAUTION }
        return PostureStatus.SUSPECT
    }
}

package com.pose.poseanalyzer.domain.model

/**
 * 좌우 비대칭 분석 결과 (정면 사진).
 */
data class AsymmetryResult(
    val shoulder: Difference,
    val hip: Difference
) {
    data class Difference(
        /** cm 환산값 (키 입력 시에만). 없으면 null */
        val cm: Double?,
        /** 신장 대비 비율 (0~1) */
        val ratio: Double,
        /** 수평선 대비 기울기 (도). 절댓값. */
        val angleDegrees: Double,
        val direction: Direction
    )

    enum class Direction(val koreanName: String) {
        LEFT_HIGHER("왼쪽이 높음"),
        RIGHT_HIGHER("오른쪽이 높음"),
        BALANCED("균형")
    }
}

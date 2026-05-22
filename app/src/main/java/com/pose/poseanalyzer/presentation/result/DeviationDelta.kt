package com.pose.poseanalyzer.presentation.result

/**
 * 직전 측정 대비 편차 변화 결과.
 *
 * 거북목·라운드숄더는 v1→v2 알고리즘 마이그레이션이 있어 다른 버전끼리는 비교 불가.
 */
sealed class DeviationDelta {
    /** 같은 알고리즘 버전끼리 비교 가능. delta > 0 악화, < 0 개선. */
    data class Compare(val delta: Double) : DeviationDelta()

    /** 거북목·라운드숄더에서 algorithmVersion 다를 때. */
    data class DifferentAlgorithm(val message: String) : DeviationDelta()

    /** 비교 대상 없음 (이전 측정 없음 / UNMEASURABLE / 항목 누락). */
    data object NoPrevious : DeviationDelta()
}

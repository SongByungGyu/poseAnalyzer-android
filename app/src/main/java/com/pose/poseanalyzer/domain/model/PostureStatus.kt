package com.pose.poseanalyzer.domain.model

/**
 * 판정 결과 상태 (4단계).
 *
 * 디자인 토큰 매핑은 Plan A2a에서 정의:
 * - [NORMAL] = mint green
 * - [CAUTION] = amber
 * - [SUSPECT] = orange
 * - [UNMEASURABLE] = neutral gray
 */
enum class PostureStatus(val koreanName: String) {
    NORMAL("정상"),
    CAUTION("주의"),
    SUSPECT("의심"),
    UNMEASURABLE("측정 불가");
}

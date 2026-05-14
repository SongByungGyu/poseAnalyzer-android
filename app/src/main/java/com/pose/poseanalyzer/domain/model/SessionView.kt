package com.pose.poseanalyzer.domain.model

/**
 * 사진의 촬영 시점 (정면 / 측면).
 */
enum class SessionView(val koreanName: String) {
    FRONT("정면"),
    SIDE("측면");
}

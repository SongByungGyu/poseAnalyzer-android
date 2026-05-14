package com.pose.poseanalyzer.domain.model

/**
 * 판정 가능한 자세 종류 (1차 MVP 8개).
 *
 * 각 항목은 어떤 [SessionView]가 필요한지 명시합니다.
 */
enum class PostureType(val koreanName: String, val requiredView: SessionView) {
    FORWARD_HEAD("거북목", SessionView.SIDE),
    ROUND_SHOULDER("라운드숄더", SessionView.SIDE),
    KYPHOSIS("흉추 후만증", SessionView.SIDE),
    ANTERIOR_PELVIC_TILT("골반 전방경사", SessionView.SIDE),
    KNEE_HYPEREXTENSION("무릎 과신전", SessionView.SIDE),
    SCOLIOSIS("척추측만", SessionView.FRONT),
    HEAD_TILT("머리 좌우 기울기", SessionView.FRONT),
    KNEE_ALIGNMENT("무릎 X/O자", SessionView.FRONT);
}

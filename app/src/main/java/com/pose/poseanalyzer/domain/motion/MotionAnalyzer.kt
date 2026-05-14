package com.pose.poseanalyzer.domain.motion

import com.pose.poseanalyzer.domain.model.PoseFrame
import kotlinx.coroutines.flow.Flow

/**
 * 동작 분석 (2차 영상 분석용 placeholder).
 *
 * iOS의 [MotionAnalyzer] 1:1 대응. 1차 정지 사진 MVP에선 구현 없음.
 * Plan B 또는 향후 영상 분석 단계에서 [com.pose.poseanalyzer.data.motion]에
 * 구현체 추가 예정.
 */
interface MotionAnalyzer {
    /** 분석할 동작 이름 (예: "squat", "push_up") */
    val name: String

    /**
     * 연속된 [PoseFrame] 스트림을 분석하여 [MotionResult] 스트림 반환.
     */
    fun analyze(stream: Flow<PoseFrame>): Flow<MotionResult>
}

/**
 * 동작 분석 한 시점의 결과.
 */
data class MotionResult(
    val timestampMs: Long,
    val phase: Phase,
    val repCount: Int,
    val notes: String?
) {
    enum class Phase {
        IDLE,
        IN_PROGRESS,
        COMPLETE
    }
}

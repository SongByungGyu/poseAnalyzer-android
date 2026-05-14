package com.pose.poseanalyzer.domain.model

import android.graphics.Bitmap
import java.util.UUID

/**
 * 한 세션의 모든 분석 결과 (저장 전 메모리 객체).
 *
 * Room 저장 시에는 이미지는 파일 경로로 변환되어 별도 저장됩니다 ([SessionEntity]).
 */
data class SessionReport(
    val id: UUID = UUID.randomUUID(),
    val measuredAt: Long = System.currentTimeMillis(),
    val frontImage: Bitmap,
    val sideImage: Bitmap,
    val frontFrame: PoseFrame,
    val sideFrame: PoseFrame,
    val postures: List<PostureResult>,
    val asymmetry: AsymmetryResult,
    val heightCmAtMeasure: Double?
) {
    fun posture(type: PostureType): PostureResult? = postures.firstOrNull { it.type == type }

    /** Bitmap/PoseFrame 비교는 무거우므로 id 기반 equality */
    override fun equals(other: Any?): Boolean = other is SessionReport && other.id == id
    override fun hashCode(): Int = id.hashCode()
}

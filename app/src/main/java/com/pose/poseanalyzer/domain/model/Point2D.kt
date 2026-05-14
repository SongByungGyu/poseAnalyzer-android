package com.pose.poseanalyzer.domain.model

/**
 * 도메인 레이어의 2D 좌표 (Android 의존성 없음).
 *
 * iOS의 [CGPoint] 대응. Pose 검출 경계에서 ML Kit의 [android.graphics.PointF]로부터
 * 변환되어 도메인 안으로 들어옵니다. UI 레이어에서 Compose의 `Offset`이나 `PointF`로
 * 재변환합니다.
 *
 * 좌표계: 정규화 0~1, **좌상단 원점** (Android/ML Kit 기본).
 */
data class Point2D(val x: Float, val y: Float) {
    companion object {
        val ZERO = Point2D(0f, 0f)
    }
}

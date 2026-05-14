package com.pose.poseanalyzer.domain.detection

import android.graphics.Bitmap
import com.pose.poseanalyzer.domain.model.PoseFrame
import com.pose.poseanalyzer.domain.model.SessionView

/**
 * 사진 또는 영상 프레임에서 사람 관절을 검출.
 *
 * iOS의 `PoseDetector` 프로토콜과 1:1 대응.
 * 1차 MVP는 정지 사진만 지원 — 영상은 Plan A2/B+에서 MotionAnalyzer로 확장.
 */
interface PoseDetector {
    suspend fun detect(image: Bitmap, view: SessionView): PoseFrame
}

/**
 * PoseDetector가 던질 수 있는 에러.
 */
sealed class PoseDetectionException(message: String) : Exception(message) {
    /** 사람이 없거나 관절 추출 실패 */
    object NoPersonDetected : PoseDetectionException("사람을 인식할 수 없습니다.")

    /** 여러 명 감지 (현재 ML Kit는 단일 인물만 반환하므로 거의 발생하지 않음 — 미래 대비) */
    class MultiplePersonsDetected(count: Int) :
        PoseDetectionException("여러 명($count 명)이 감지되었습니다. 한 명만 보이는 사진을 사용해주세요.")

    /** ML Kit 내부 실패 (모델 로드 / IO / GPU 등) */
    class VisionFailed(detail: String) :
        PoseDetectionException("분석 중 오류가 발생했습니다: $detail")

    /** Bitmap 형식 불량 */
    object InvalidImage : PoseDetectionException("사진 형식이 올바르지 않습니다.")
}

package com.pose.poseanalyzer.presentation.result

import com.pose.poseanalyzer.domain.model.SessionReport
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 직전 분석 결과를 메모리에 임시 보관하는 싱글톤.
 *
 * Android ViewModel은 SavedStateHandle만 받을 수 있어 Bitmap 포함 SessionReport를
 * navigation argument로 넘기기 어렵다. 다음 두 경로를 지원:
 *
 * 1) 측정 직후 — MeasurementViewModel이 [hold]로 저장 후 AnalysisResultScreen 진입.
 *    저장되지 않은 상태이므로 사용자가 "저장"을 누르면 SessionRepository로 들어감.
 * 2) 기록 탭에서 진입 — 저장된 Session을 [SessionRepository]에서 fetch → [SessionReport] 복원.
 *    (Bitmap은 ImageStore에서 load. frontFrame/sideFrame은 빈 PoseFrame.)
 *
 * Process death 시 1)은 휘발됨 — 사용자가 측정 직후 앱이 종료되면 직전 결과 잃지만
 * 어차피 사진 + 분석은 다시 해야 하므로 허용. 1차 MVP 정책.
 */
@Singleton
class ResultHolder @Inject constructor() {
    private var current: SessionReport? = null

    fun hold(report: SessionReport) {
        current = report
    }

    fun consume(): SessionReport? = current

    fun clear() {
        current = null
    }
}

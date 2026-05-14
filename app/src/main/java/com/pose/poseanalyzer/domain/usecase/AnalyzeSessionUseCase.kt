package com.pose.poseanalyzer.domain.usecase

import android.graphics.Bitmap
import com.pose.poseanalyzer.domain.asymmetry.AsymmetryAnalyzer
import com.pose.poseanalyzer.domain.detection.PoseDetector
import com.pose.poseanalyzer.domain.evaluation.PostureEvaluator
import com.pose.poseanalyzer.domain.model.PostureResult
import com.pose.poseanalyzer.domain.model.SessionReport
import com.pose.poseanalyzer.domain.model.SessionView
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

/**
 * View가 호출하는 단일 진입점: 정면+측면 사진 → [SessionReport].
 *
 * iOS `AnalyzeSessionUseCase`와 1:1 대응. `async let` → `async/await`.
 */
class AnalyzeSessionUseCase @Inject constructor(
    private val detector: PoseDetector,
    private val evaluators: List<PostureEvaluator>,
    private val asymmetryAnalyzer: AsymmetryAnalyzer
) {
    suspend fun analyze(
        front: Bitmap,
        side: Bitmap,
        heightCm: Double?
    ): SessionReport = coroutineScope {
        // 1) 두 사진 병렬 분석
        val frontDeferred = async { detector.detect(front, SessionView.FRONT) }
        val sideDeferred = async { detector.detect(side, SessionView.SIDE) }
        val frontFrame = frontDeferred.await()
        val sideFrame = sideDeferred.await()

        // 2) 각 Evaluator 실행 (해당 view에 맞춰 분배)
        val results: List<PostureResult> = evaluators.map { evaluator ->
            val frame = if (evaluator.requiredView == SessionView.FRONT) frontFrame else sideFrame
            evaluator.evaluate(frame)
        }

        // 3) 비대칭 분석 (정면 사진)
        val asymmetry = asymmetryAnalyzer.analyze(frontFrame, heightCm)

        SessionReport(
            measuredAt = System.currentTimeMillis(),
            frontImage = front,
            sideImage = side,
            frontFrame = frontFrame,
            sideFrame = sideFrame,
            postures = results,
            asymmetry = asymmetry,
            heightCmAtMeasure = heightCm
        )
    }
}

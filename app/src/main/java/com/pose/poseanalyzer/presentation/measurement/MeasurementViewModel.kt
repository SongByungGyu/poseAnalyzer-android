package com.pose.poseanalyzer.presentation.measurement

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pose.poseanalyzer.data.SessionRepository
import com.pose.poseanalyzer.data.UserProfileRepository
import com.pose.poseanalyzer.domain.detection.PoseDetectionException
import com.pose.poseanalyzer.domain.model.SessionReport
import com.pose.poseanalyzer.domain.usecase.AnalyzeSessionUseCase
import com.pose.poseanalyzer.presentation.result.ResultHolder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 측정 마법사 상태 관리: 정면 사진 → 측면 사진 → 키 입력 → 분석.
 *
 * iOS `MeasurementViewModel.swift` 1:1 대응.
 */
@HiltViewModel
class MeasurementViewModel @Inject constructor(
    private val analyzeUseCase: AnalyzeSessionUseCase,
    private val userProfileRepository: UserProfileRepository,
    private val sessionRepository: SessionRepository,
    private val resultHolder: ResultHolder
) : ViewModel() {

    enum class Step { FRONT, SIDE, HEIGHT, ANALYZING, DONE }

    data class State(
        val step: Step = Step.FRONT,
        val frontImage: Bitmap? = null,
        val sideImage: Bitmap? = null,
        val heightInput: String = "",
        val analyzingPhase: String = "관절 인식 중…",
        val report: SessionReport? = null,
        val errorMessage: String? = null,
        val storedHeightLoaded: Boolean = false
    )

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val saved = userProfileRepository.getHeightCm()
            _state.update { it.copy(heightInput = saved?.let { v -> v.toInt().toString() } ?: "", storedHeightLoaded = true) }
        }
    }

    fun setFrontImage(bitmap: Bitmap) {
        _state.update { it.copy(frontImage = bitmap, step = Step.SIDE) }
    }

    fun setSideImage(bitmap: Bitmap) {
        viewModelScope.launch {
            val saved = userProfileRepository.getHeightCm()
            if (saved != null) {
                _state.update { it.copy(sideImage = bitmap, step = Step.ANALYZING) }
                startAnalysis()
            } else {
                _state.update { it.copy(sideImage = bitmap, step = Step.HEIGHT) }
            }
        }
    }

    fun updateHeightInput(value: String) {
        _state.update { it.copy(heightInput = value) }
    }

    fun submitHeight() {
        val parsed = parsedHeight()
        viewModelScope.launch {
            if (parsed != null) {
                userProfileRepository.updateHeightCm(parsed)
            }
            _state.update { it.copy(step = Step.ANALYZING, errorMessage = null) }
            startAnalysis()
        }
    }

    fun skipHeight() {
        _state.update { it.copy(step = Step.ANALYZING, errorMessage = null) }
        startAnalysis()
    }

    fun retryFromBeginning() {
        _state.update {
            State(heightInput = it.heightInput, storedHeightLoaded = it.storedHeightLoaded)
        }
    }

    fun dismissError() {
        _state.update { it.copy(errorMessage = null) }
    }

    /**
     * 분석 완료 후 [ResultHolder]에 결과 보관 → AnalysisResultScreen 진입.
     *
     * Plan A2c부터는 자동 저장 X — 결과 화면에서 사용자가 저장 버튼을 눌러야 SessionRepository에 들어감.
     */
    fun handOffReport(onReady: () -> Unit) {
        val report = _state.value.report ?: return
        resultHolder.hold(report)
        onReady()
    }

    /** 키 입력 유효성 (50~250 cm) */
    fun parsedHeight(): Double? {
        val v = _state.value.heightInput.toDoubleOrNull() ?: return null
        return if (v in 50.0..250.0) v else null
    }

    fun isHeightValid(): Boolean = parsedHeight() != null

    private fun startAnalysis() {
        val s = _state.value
        val front = s.frontImage
        val side = s.sideImage
        if (front == null || side == null) {
            _state.update { it.copy(errorMessage = "사진이 누락되었습니다.", step = Step.HEIGHT) }
            return
        }
        val heightForAnalysis = parsedHeight()
        viewModelScope.launch {
            _state.update { it.copy(analyzingPhase = "관절 인식 중…") }
            runCatching {
                analyzeUseCase.analyze(front, side, heightForAnalysis)
            }.onSuccess { report ->
                _state.update { it.copy(analyzingPhase = "자세 분석 중…") }
                kotlinx.coroutines.delay(300)
                _state.update { it.copy(report = report, step = Step.DONE) }
            }.onFailure { e ->
                val msg = when (e) {
                    is PoseDetectionException -> e.message ?: "분석 실패"
                    else -> "예상치 못한 오류: ${e.message}"
                }
                _state.update { it.copy(errorMessage = msg, step = Step.HEIGHT) }
            }
        }
    }
}

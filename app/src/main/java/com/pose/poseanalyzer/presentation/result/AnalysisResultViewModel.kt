package com.pose.poseanalyzer.presentation.result

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pose.poseanalyzer.data.SessionRepository
import com.pose.poseanalyzer.data.room.SessionWithPostures
import com.pose.poseanalyzer.domain.model.PostureStatus
import com.pose.poseanalyzer.domain.model.PostureType
import com.pose.poseanalyzer.domain.model.SessionReport
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

/**
 * 분석 결과 화면 ViewModel.
 *
 * iOS `AnalysisResultViewModel.swift` 1:1 대응.
 *
 * 두 진입 모드:
 *   - 측정 직후: [ResultHolder]에서 SessionReport pop. 저장 가능.
 *   - 기록에서 진입: [setReadOnlySession]으로 외부에서 sessionId 주입. 저장 안 됨.
 */
@HiltViewModel
class AnalysisResultViewModel @Inject constructor(
    private val resultHolder: ResultHolder,
    private val sessionRepository: SessionRepository
) : ViewModel() {

    data class State(
        val report: SessionReport? = null,
        val previousSession: SessionWithPostures? = null,
        val isSaved: Boolean = false,
        val isSaving: Boolean = false,
        val errorMessage: String? = null,
        val isReadOnly: Boolean = false
    )

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()

    /** 측정 직후 진입 — ResultHolder에서 결과 가져오기 */
    fun loadFromHolder() {
        val report = resultHolder.consume() ?: return
        _state.update { it.copy(report = report, isReadOnly = false, isSaved = false) }
        loadPreviousSession(report.id)
    }

    private fun loadPreviousSession(excludingId: UUID) {
        viewModelScope.launch {
            val prev = runCatching { sessionRepository.fetchLatest(excludingId = excludingId) }.getOrNull()
            _state.update { it.copy(previousSession = prev) }
        }
    }

    /** 저장 액션 */
    fun save(onSaved: () -> Unit) {
        val s = _state.value
        if (s.isSaving || s.isSaved) return
        val report = s.report ?: return
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            runCatching { sessionRepository.save(report) }
                .onSuccess {
                    _state.update { it.copy(isSaved = true, isSaving = false) }
                    onSaved()
                }
                .onFailure { e ->
                    _state.update {
                        it.copy(
                            isSaving = false,
                            errorMessage = "저장에 실패했습니다: ${e.message}"
                        )
                    }
                }
        }
    }

    fun dismissError() {
        _state.update { it.copy(errorMessage = null) }
    }

    /**
     * 직전 측정 대비 편차 변화량 (양수 = 악화, 음수 = 개선). null이면 비교 불가.
     */
    fun deviationDelta(type: PostureType): Double? {
        val s = _state.value
        val current = s.report?.postures?.firstOrNull { it.type == type }
            ?: return null
        if (current.status == PostureStatus.UNMEASURABLE) return null
        val prev = s.previousSession ?: return null
        val prevPosture = prev.postures.firstOrNull { it.typeRaw == type.name }
            ?: return null
        if (prevPosture.statusRaw == PostureStatus.UNMEASURABLE.name) return null
        return current.deviationValue - prevPosture.primaryMetric.let { raw ->
            when (type) {
                PostureType.FORWARD_HEAD         -> maxOf(0.0, 180.0 - raw)
                PostureType.ROUND_SHOULDER       -> raw * 100.0
                PostureType.KYPHOSIS             -> maxOf(0.0, 180.0 - raw)
                PostureType.ANTERIOR_PELVIC_TILT -> kotlin.math.abs(180.0 - raw)
                PostureType.KNEE_HYPEREXTENSION  -> maxOf(0.0, raw - 180.0)
                PostureType.SCOLIOSIS            -> raw
                PostureType.HEAD_TILT            -> raw
                PostureType.KNEE_ALIGNMENT       -> kotlin.math.abs(raw - 180.0)
            }
        }
    }
}

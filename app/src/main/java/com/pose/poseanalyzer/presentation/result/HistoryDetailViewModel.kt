package com.pose.poseanalyzer.presentation.result

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pose.poseanalyzer.data.ImageStore
import com.pose.poseanalyzer.data.SessionRepository
import com.pose.poseanalyzer.domain.model.AsymmetryResult
import com.pose.poseanalyzer.domain.model.JointName
import com.pose.poseanalyzer.domain.model.PoseFrame
import com.pose.poseanalyzer.domain.model.PostureResult
import com.pose.poseanalyzer.domain.model.PostureStatus
import com.pose.poseanalyzer.domain.model.PostureType
import com.pose.poseanalyzer.domain.model.SessionReport
import com.pose.poseanalyzer.domain.model.SessionView
import com.pose.poseanalyzer.domain.model.Thresholds
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

/**
 * 저장된 세션을 SessionReport로 복원해서 결과 화면(readOnly) 표시용.
 *
 * iOS `AnalysisResultDetailView` 1:1 대응.
 */
@HiltViewModel
class HistoryDetailViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val imageStore: ImageStore
) : ViewModel() {

    data class State(
        val report: SessionReport? = null,
        val notFound: Boolean = false
    )

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()

    fun load(sessionId: String) {
        viewModelScope.launch {
            val sw = runCatching { sessionRepository.fetch(UUID.fromString(sessionId)) }
                .getOrNull()
            if (sw == null) {
                _state.update { it.copy(notFound = true) }
                return@launch
            }
            val frontBitmap = imageStore.load(sw.session.frontImagePath)
            val sideBitmap = imageStore.load(sw.session.sideImagePath)
            if (frontBitmap == null || sideBitmap == null) {
                _state.update { it.copy(notFound = true) }
                return@launch
            }

            val postures = sw.postures.map { rec ->
                val type = PostureType.valueOf(rec.typeRaw)
                val status = runCatching { PostureStatus.valueOf(rec.statusRaw) }
                    .getOrDefault(PostureStatus.UNMEASURABLE)
                val unit = runCatching { PostureResult.MetricUnit.valueOf(rec.primaryMetricUnitRaw) }
                    .getOrDefault(PostureResult.MetricUnit.DEGREE)
                PostureResult(
                    type = type,
                    status = status,
                    primaryMetric = rec.primaryMetric,
                    primaryMetricUnit = unit,
                    thresholds = Thresholds(0.0..0.0, null, Thresholds.Direction.HIGHER_IS_NORMAL),
                    usedJointNames = emptyList(),
                    confidence = rec.confidence,
                    advice = rec.advice
                )
            }
            val asymmetry = AsymmetryResult(
                shoulder = AsymmetryResult.Difference(
                    cm = sw.session.asymmetryShoulderCm,
                    ratio = sw.session.asymmetryShoulderRatio,
                    angleDegrees = sw.session.asymmetryShoulderAngle,
                    direction = runCatching {
                        AsymmetryResult.Direction.valueOf(sw.session.asymmetryShoulderDirectionRaw)
                    }.getOrDefault(AsymmetryResult.Direction.BALANCED)
                ),
                hip = AsymmetryResult.Difference(
                    cm = sw.session.asymmetryHipCm,
                    ratio = sw.session.asymmetryHipRatio,
                    angleDegrees = sw.session.asymmetryHipAngle,
                    direction = runCatching {
                        AsymmetryResult.Direction.valueOf(sw.session.asymmetryHipDirectionRaw)
                    }.getOrDefault(AsymmetryResult.Direction.BALANCED)
                )
            )
            val emptyFrame = PoseFrame(
                joints = emptyMap<JointName, PoseFrame.Joint>(),
                view = SessionView.FRONT,
                imageWidth = frontBitmap.width,
                imageHeight = frontBitmap.height
            )
            val emptySideFrame = emptyFrame.copy(
                view = SessionView.SIDE,
                imageWidth = sideBitmap.width,
                imageHeight = sideBitmap.height
            )
            _state.update {
                it.copy(
                    report = SessionReport(
                        id = UUID.fromString(sw.session.id),
                        measuredAt = sw.session.measuredAtMs,
                        frontImage = frontBitmap,
                        sideImage = sideBitmap,
                        frontFrame = emptyFrame,
                        sideFrame = emptySideFrame,
                        postures = postures,
                        asymmetry = asymmetry,
                        heightCmAtMeasure = sw.session.heightCmAtMeasure
                    )
                )
            }
        }
    }
}

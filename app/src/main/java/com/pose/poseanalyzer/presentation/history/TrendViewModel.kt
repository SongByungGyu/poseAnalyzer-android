package com.pose.poseanalyzer.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pose.poseanalyzer.data.SessionRepository
import com.pose.poseanalyzer.domain.model.PostureStatus
import com.pose.poseanalyzer.domain.model.PostureType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TrendViewModel @Inject constructor(
    private val sessionRepository: SessionRepository
) : ViewModel() {

    enum class Range(val days: Int?, val label: String) {
        LAST_7(7, "7일"),
        LAST_30(30, "30일"),
        ALL(null, "전체")
    }

    data class Point(
        val measuredAtMs: Long,
        val value: Double,
        val status: PostureStatus,
        val algorithmVersion: String
    )

    data class State(
        val selectedType: PostureType = PostureType.FORWARD_HEAD,
        val range: Range = Range.LAST_30,
        val points: List<Point> = emptyList()
    ) {
        /**
         * 거북목·라운드숄더 그래프에서 v1→v2 전환 시 첫 v2 포인트의 인덱스.
         * 다른 자세이거나 전환이 없거나 모두 같은 버전이면 null.
         */
        val firstV2Index: Int?
            get() {
                if (selectedType != PostureType.FORWARD_HEAD &&
                    selectedType != PostureType.ROUND_SHOULDER
                ) return null
                val hasV1 = points.any { it.algorithmVersion != "v2" }
                val firstV2 = points.indexOfFirst { it.algorithmVersion == "v2" }
                return if (hasV1 && firstV2 > 0) firstV2 else null
            }
    }

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()

    fun refresh() {
        viewModelScope.launch {
            recompute()
        }
    }

    fun selectType(type: PostureType) {
        _state.update { it.copy(selectedType = type) }
        viewModelScope.launch { recompute() }
    }

    fun selectRange(range: Range) {
        _state.update { it.copy(range = range) }
        viewModelScope.launch { recompute() }
    }

    private suspend fun recompute() {
        val s = _state.value
        val all = runCatching { sessionRepository.fetchAll() }.getOrDefault(emptyList())
        val cutoff: Long? = s.range.days?.let { System.currentTimeMillis() - it * 24L * 3600L * 1000L }
        val typeName = s.selectedType.name
        val points = all
            .filter { cutoff == null || it.session.measuredAtMs >= cutoff }
            .mapNotNull { sw ->
                val p = sw.postures.firstOrNull { it.typeRaw == typeName } ?: return@mapNotNull null
                val status = runCatching { PostureStatus.valueOf(p.statusRaw) }.getOrDefault(PostureStatus.UNMEASURABLE)
                if (status == PostureStatus.UNMEASURABLE) return@mapNotNull null
                Point(sw.session.measuredAtMs, p.primaryMetric, status, p.algorithmVersion)
            }
            .sortedBy { it.measuredAtMs }
        _state.update { it.copy(points = points) }
    }
}

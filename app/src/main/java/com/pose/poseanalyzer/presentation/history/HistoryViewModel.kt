package com.pose.poseanalyzer.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pose.poseanalyzer.data.SessionRepository
import com.pose.poseanalyzer.data.room.SessionWithPostures
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val sessionRepository: SessionRepository
) : ViewModel() {

    data class State(
        val sessions: List<SessionWithPostures> = emptyList(),
        val errorMessage: String? = null,
        val deletionTargetId: String? = null
    )

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()

    fun refresh() {
        viewModelScope.launch {
            runCatching { sessionRepository.fetchAll() }
                .onSuccess { list -> _state.update { it.copy(sessions = list, errorMessage = null) } }
                .onFailure { e ->
                    _state.update { it.copy(sessions = emptyList(), errorMessage = "기록 조회 실패: ${e.message}") }
                }
        }
    }

    fun requestDeletion(id: String) {
        _state.update { it.copy(deletionTargetId = id) }
    }

    fun cancelDeletion() {
        _state.update { it.copy(deletionTargetId = null) }
    }

    fun confirmDeletion() {
        val id = _state.value.deletionTargetId ?: return
        viewModelScope.launch {
            runCatching { sessionRepository.delete(UUID.fromString(id)) }
                .onFailure { e ->
                    _state.update { it.copy(errorMessage = "삭제 실패: ${e.message}") }
                }
            _state.update { it.copy(deletionTargetId = null) }
            refresh()
        }
    }

    fun dismissError() {
        _state.update { it.copy(errorMessage = null) }
    }
}

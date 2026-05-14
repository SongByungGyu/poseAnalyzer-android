package com.pose.poseanalyzer.presentation.home

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
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val sessionRepository: SessionRepository
) : ViewModel() {

    data class State(
        val latestSession: SessionWithPostures? = null,
        val isWizardPresented: Boolean = false
    )

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()

    /** 화면 진입 시 / 측정 후 새로고침 */
    fun refresh() {
        viewModelScope.launch {
            val latest = runCatching { sessionRepository.fetchLatest() }.getOrNull()
            _state.update { it.copy(latestSession = latest) }
        }
    }

    fun startMeasurement() {
        _state.update { it.copy(isWizardPresented = true) }
    }

    fun dismissWizard() {
        _state.update { it.copy(isWizardPresented = false) }
    }
}

package com.pose.poseanalyzer.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pose.poseanalyzer.data.UserProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userProfileRepository: UserProfileRepository
) : ViewModel() {

    data class State(
        val heightInput: String = "",
        val isLoaded: Boolean = false,
        val saveSuccess: Boolean = false
    )

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()

    fun load() {
        viewModelScope.launch {
            val saved = userProfileRepository.getHeightCm()
            _state.update {
                it.copy(
                    heightInput = saved?.let { v -> v.toInt().toString() } ?: "",
                    isLoaded = true
                )
            }
        }
    }

    fun updateInput(value: String) {
        _state.update { it.copy(heightInput = value.filter { c -> c.isDigit() || c == '.' }.take(5)) }
    }

    /** 비어 있으면 OK (키 미입력 허용). 값 있으면 50~250 범위. */
    fun isValid(): Boolean {
        val input = _state.value.heightInput
        if (input.isEmpty()) return true
        val v = input.toDoubleOrNull() ?: return false
        return v in 50.0..250.0
    }

    fun save() {
        val input = _state.value.heightInput
        val value = if (input.isEmpty()) null else input.toDoubleOrNull()
        viewModelScope.launch {
            userProfileRepository.updateHeightCm(value)
            _state.update { it.copy(saveSuccess = true) }
        }
    }

    fun dismissSuccess() {
        _state.update { it.copy(saveSuccess = false) }
    }
}

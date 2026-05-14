package com.pose.poseanalyzer.presentation.measurement

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.pose.poseanalyzer.domain.model.SessionView

/**
 * 측정 마법사 orchestrator.
 *
 * ViewModel state에 따라 적절한 step Composable 호출.
 * iOS `MeasurementWizardView.swift` 1:1 대응.
 */
@Composable
fun MeasurementWizardScreen(
    onCompleted: (sessionId: String) -> Unit,
    onCancel: () -> Unit,
    viewModel: MeasurementViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var sourcePickerOpen by remember { mutableStateOf(false) }
    var cameraOpen by remember { mutableStateOf(false) }

    // 결과 도착 시 자동 저장 후 onCompleted
    LaunchedEffect(state.step) {
        if (state.step == MeasurementViewModel.Step.DONE) {
            viewModel.persistReport(onCompleted)
        }
    }

    // PhotoPicker launcher
    val pickFromGallery = rememberPhotoPicker { bitmap ->
        when (state.step) {
            MeasurementViewModel.Step.FRONT -> viewModel.setFrontImage(bitmap)
            MeasurementViewModel.Step.SIDE -> viewModel.setSideImage(bitmap)
            else -> {}
        }
    }

    when {
        cameraOpen -> {
            val view = if (state.step == MeasurementViewModel.Step.FRONT) SessionView.FRONT else SessionView.SIDE
            val step = if (state.step == MeasurementViewModel.Step.FRONT) 1 else 2
            CustomCameraScreen(
                view = view,
                step = step,
                onCaptured = { bitmap ->
                    cameraOpen = false
                    if (view == SessionView.FRONT) viewModel.setFrontImage(bitmap)
                    else viewModel.setSideImage(bitmap)
                },
                onClose = { cameraOpen = false }
            )
        }
        state.step == MeasurementViewModel.Step.FRONT -> {
            WizardStepScreen(
                view = SessionView.FRONT,
                step = 1,
                onAddPhoto = { sourcePickerOpen = true },
                onBack = onCancel
            )
        }
        state.step == MeasurementViewModel.Step.SIDE -> {
            WizardStepScreen(
                view = SessionView.SIDE,
                step = 2,
                onAddPhoto = { sourcePickerOpen = true },
                onBack = onCancel
            )
        }
        state.step == MeasurementViewModel.Step.HEIGHT -> {
            WizardHeightStepScreen(
                heightInput = state.heightInput,
                onHeightChange = viewModel::updateHeightInput,
                onSubmit = viewModel::submitHeight,
                onSkip = viewModel::skipHeight,
                isValid = viewModel.isHeightValid()
            )
        }
        state.step == MeasurementViewModel.Step.ANALYZING -> {
            AnalyzingScreen(phase = state.analyzingPhase)
        }
        state.step == MeasurementViewModel.Step.DONE -> {
            // onCompleted 콜백이 이미 실행됨 — 잠깐 분석 화면 유지
            AnalyzingScreen(phase = "결과 화면으로 이동 중…")
        }
    }

    if (sourcePickerOpen) {
        PhotoInputBottomSheet(
            onCamera = { cameraOpen = true },
            onGallery = pickFromGallery,
            onDismiss = { sourcePickerOpen = false }
        )
    }

    state.errorMessage?.let { msg ->
        AlertDialog(
            onDismissRequest = viewModel::dismissError,
            title = { Text("분석에 실패했습니다") },
            text = { Text(msg) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.dismissError()
                    viewModel.retryFromBeginning()
                }) {
                    Text("다시 측정")
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissError) {
                    Text("닫기")
                }
            }
        )
    }
}

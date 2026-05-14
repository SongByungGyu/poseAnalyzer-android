package com.pose.poseanalyzer.presentation.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.hilt.navigation.compose.hiltViewModel
import com.pose.poseanalyzer.presentation.common.AppButton
import com.pose.poseanalyzer.presentation.common.AppCard
import com.pose.poseanalyzer.presentation.common.AppNavBar
import com.pose.poseanalyzer.presentation.common.SectionHeader
import com.pose.poseanalyzer.presentation.theme.AppColors
import com.pose.poseanalyzer.presentation.theme.AppSpacing
import com.pose.poseanalyzer.presentation.theme.AppTypography

/**
 * 설정 — 키 입력/수정.
 *
 * iOS `SettingsView.swift` 1:1 대응.
 */
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    LaunchedEffect(Unit) { viewModel.load() }

    Surface(modifier = Modifier.fillMaxSize(), color = AppColors.Surface) {
        Column(modifier = Modifier.fillMaxSize()) {
            AppNavBar(title = "설정", onBack = onBack)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(AppSpacing.s4),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.s4)
            ) {
                SectionHeader("프로필")
                AppCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "키 (cm)",
                            style = AppTypography.body,
                            color = AppColors.OnSurface,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = state.heightInput,
                            onValueChange = viewModel::updateInput,
                            placeholder = { Text("미입력") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1.2f)
                        )
                    }
                }
                AppButton(
                    text = "저장",
                    onClick = viewModel::save,
                    enabled = viewModel.isValid(),
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    "키는 사진 속 신장 픽셀과 비교하여 어깨/골반 비대칭을 cm 단위로 환산하는 데 사용됩니다. 입력하지 않으면 어깨너비 비율로 표시됩니다.",
                    style = AppTypography.caption,
                    color = AppColors.OnSurfaceTertiary
                )
            }
        }
    }

    if (state.saveSuccess) {
        AlertDialog(
            onDismissRequest = viewModel::dismissSuccess,
            title = { Text("저장 완료") },
            text = { Text("설정이 저장되었습니다.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.dismissSuccess()
                    onBack()
                }) {
                    Text("확인")
                }
            }
        )
    }
}

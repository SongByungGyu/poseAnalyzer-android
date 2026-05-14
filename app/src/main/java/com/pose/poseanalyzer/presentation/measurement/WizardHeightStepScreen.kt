package com.pose.poseanalyzer.presentation.measurement

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pose.poseanalyzer.presentation.common.AppButton
import com.pose.poseanalyzer.presentation.common.AppButtonVariant
import com.pose.poseanalyzer.presentation.theme.AppColors
import com.pose.poseanalyzer.presentation.theme.AppSpacing
import com.pose.poseanalyzer.presentation.theme.AppTypography

/**
 * 키 입력 step. 비대칭 cm 환산용 (옵션 — 건너뛰기 가능).
 *
 * iOS `WizardHeightStepView.swift` 1:1 대응.
 */
@Composable
fun WizardHeightStepScreen(
    heightInput: String,
    onHeightChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onSkip: () -> Unit,
    isValid: Boolean
) {
    Surface(modifier = Modifier.fillMaxSize(), color = AppColors.Surface) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = AppSpacing.s5)
                .padding(top = AppSpacing.s11),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.s4)
        ) {
            Text(
                "키를 입력하시면\n비대칭 정도를 cm로 환산해드립니다.",
                style = AppTypography.display,
                color = AppColors.OnSurface
            )
            Text(
                "선택사항입니다. 건너뛸 수 있습니다.",
                style = AppTypography.body,
                color = AppColors.OnSurfaceSecondary
            )
            Spacer(modifier = Modifier.size(AppSpacing.s4))
            OutlinedTextField(
                value = heightInput,
                onValueChange = { input ->
                    // 숫자 + 소수점 1개만 허용 (4자리)
                    val filtered = input.filter { it.isDigit() || it == '.' }.take(5)
                    onHeightChange(filtered)
                },
                label = { Text("키 (cm)") },
                placeholder = { Text("예) 170") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                textStyle = TextStyle(fontSize = 24.sp, textAlign = TextAlign.Center),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.size(AppSpacing.s4))
            AppButton(
                text = if (isValid) "분석 시작" else "유효한 키를 입력해주세요",
                onClick = onSubmit,
                enabled = isValid,
                modifier = Modifier.fillMaxWidth()
            )
            AppButton(
                text = "건너뛰기",
                onClick = onSkip,
                variant = AppButtonVariant.Text,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}


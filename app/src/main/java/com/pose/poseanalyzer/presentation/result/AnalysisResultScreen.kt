package com.pose.poseanalyzer.presentation.result

import android.graphics.Bitmap
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.North
import androidx.compose.material.icons.filled.South
import androidx.compose.material.icons.filled.East
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pose.poseanalyzer.domain.model.AsymmetryResult
import com.pose.poseanalyzer.domain.model.PoseFrame
import com.pose.poseanalyzer.domain.model.PostureResult
import com.pose.poseanalyzer.domain.model.PostureType
import com.pose.poseanalyzer.presentation.common.AppButton
import com.pose.poseanalyzer.presentation.common.AppButtonVariant
import com.pose.poseanalyzer.presentation.common.AppCard
import com.pose.poseanalyzer.presentation.common.AppNavBar
import com.pose.poseanalyzer.presentation.common.AppToast
import com.pose.poseanalyzer.presentation.common.SectionHeader
import com.pose.poseanalyzer.presentation.theme.AppColors
import com.pose.poseanalyzer.presentation.theme.AppShapes
import com.pose.poseanalyzer.presentation.theme.AppSpacing
import com.pose.poseanalyzer.presentation.theme.AppTypography

/**
 * 분석 결과 화면 — 사진 + 관절 오버레이 + 8 자세 카드 + 비대칭 + 직전 비교.
 *
 * iOS `AnalysisResultView.swift` 1:1 대응.
 *
 * @param onBack 결과 닫고 홈 복귀
 */
@Composable
fun AnalysisResultScreen(
    onBack: () -> Unit,
    viewModel: AnalysisResultViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var savedToastVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadFromHolder()
    }

    val report = state.report
    if (report == null) {
        // 빈 상태 — 데이터 없으면 즉시 복귀
        LaunchedEffect(state) {
            if (state.report == null) {
                // 아직 로드 중일 수도 있음 — 조금만 더 기다림
            }
        }
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("결과를 불러오는 중…", style = AppTypography.body, color = AppColors.OnSurfaceSecondary)
        }
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Surface(modifier = Modifier.fillMaxSize(), color = AppColors.Surface) {
            Column(modifier = Modifier.fillMaxSize()) {
                AppNavBar(
                    title = "분석 결과",
                    onBack = onBack,
                    trailing = {
                        if (!state.isReadOnly && !state.isSaved) {
                            TextButton(
                                onClick = {
                                    viewModel.save {
                                        savedToastVisible = true
                                    }
                                },
                                enabled = !state.isSaving
                            ) {
                                Text(
                                    if (state.isSaving) "저장 중…" else "저장",
                                    color = AppColors.BrandPrimary,
                                    style = AppTypography.callout
                                )
                            }
                        }
                    }
                )
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = AppSpacing.s4)
                        .padding(bottom = AppSpacing.s9),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.s5)
                ) {
                    PhotosSection(
                        frontImage = report.frontImage,
                        frontFrame = report.frontFrame,
                        sideImage = report.sideImage,
                        sideFrame = report.sideFrame
                    )
                    PosturesSection(report.postures)
                    AsymmetrySection(report.asymmetry)
                    if (!state.isReadOnly && state.previousSession != null) {
                        PreviousComparisonSection(
                            postures = report.postures,
                            getDeviationDelta = viewModel::deviationDelta
                        )
                    }
                }
            }
        }
        AppToast(
            message = "저장되었습니다",
            visible = savedToastVisible,
            modifier = Modifier.align(Alignment.BottomCenter),
            onDismiss = {
                savedToastVisible = false
                onBack()
            }
        )
        state.errorMessage?.let { msg ->
            AlertDialog(
                onDismissRequest = viewModel::dismissError,
                title = { Text("저장 실패") },
                text = { Text(msg) },
                confirmButton = {
                    TextButton(onClick = viewModel::dismissError) { Text("확인") }
                }
            )
        }
    }
}

@Composable
private fun PhotosSection(
    frontImage: Bitmap,
    frontFrame: PoseFrame,
    sideImage: Bitmap,
    sideFrame: PoseFrame
) {
    Row(
        modifier = Modifier.padding(top = AppSpacing.s2),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.s2)
    ) {
        PhotoCard(image = frontImage, frame = frontFrame, label = "정면", modifier = Modifier.weight(1f))
        PhotoCard(image = sideImage, frame = sideFrame, label = "측면", modifier = Modifier.weight(1f))
    }
}

@Composable
private fun PhotoCard(
    image: Bitmap,
    frame: PoseFrame,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(AppSpacing.s1)) {
        Text(label, style = AppTypography.micro, color = AppColors.OnSurfaceTertiary)
        Surface(
            shape = AppShapes.medium,
            border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.Divider),
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.75f)
        ) {
            PoseOverlayCanvas(bitmap = image, frame = frame, modifier = Modifier.fillMaxSize())
        }
    }
}

@Composable
private fun PosturesSection(postures: List<PostureResult>) {
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.s2)) {
        SectionHeader("자세 판정 (8가지)")
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.s2)) {
            postures.forEach { result ->
                PostureResultCard(result = result)
            }
        }
    }
}

@Composable
private fun AsymmetrySection(asymmetry: AsymmetryResult) {
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.s2)) {
        SectionHeader("좌우 비대칭")
        AppCard {
            AsymmetryRow("어깨", asymmetry.shoulder)
            HorizontalDivider(modifier = Modifier.padding(vertical = AppSpacing.s2), color = AppColors.Divider)
            AsymmetryRow("골반", asymmetry.hip)
        }
    }
}

@Composable
private fun AsymmetryRow(label: String, diff: AsymmetryResult.Difference) {
    val isBalanced = diff.direction == AsymmetryResult.Direction.BALANCED
    val dirColor = if (isBalanced) AppColors.StatusNormal else AppColors.StatusCaution
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            style = AppTypography.body,
            color = AppColors.OnSurfaceSecondary,
            modifier = Modifier.width(40.dp)
        )
        Text(
            diff.direction.koreanName,
            style = AppTypography.body,
            color = dirColor
        )
        Spacer(modifier = Modifier.weight(1f))
        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
            diff.cm?.let { cm ->
                Text(
                    String.format("%.1fcm 차이", cm),
                    style = AppTypography.caption,
                    color = AppColors.OnSurface
                )
            }
            Text(
                String.format("기울기 %.1f°", diff.angleDegrees),
                style = AppTypography.micro,
                color = AppColors.OnSurfaceTertiary
            )
        }
    }
}

@Composable
private fun PreviousComparisonSection(
    postures: List<PostureResult>,
    getDeviationDelta: (PostureType) -> Double?
) {
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.s2)) {
        SectionHeader("직전 측정 대비")
        AppCard {
            postures.forEach { p ->
                getDeviationDelta(p.type)?.let { d ->
                    ComparisonRow(result = p, deviationDelta = d)
                }
            }
        }
    }
}

@Composable
private fun ComparisonRow(
    result: PostureResult,
    deviationDelta: Double
) {
    // 편차 기준: 음수 = 개선(deviation 감소), 양수 = 악화(deviation 증가)
    val isStable = kotlin.math.abs(deviationDelta) < 1.0
    val improved  = deviationDelta < -1.0
    val color: Color = when {
        isStable -> AppColors.OnSurfaceTertiary
        improved -> AppColors.StatusNormal
        else     -> AppColors.StatusCaution
    }
    val icon = when {
        isStable -> Icons.Filled.East
        improved -> Icons.Filled.South
        else     -> Icons.Filled.North
    }
    val valueStr = if (result.type == com.pose.poseanalyzer.domain.model.PostureType.ROUND_SHOULDER) {
        String.format("%.0f", deviationDelta)
    } else {
        String.format("%.1f", deviationDelta)
    }
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            result.type.koreanName,
            style = AppTypography.callout,
            color = AppColors.OnSurface,
            modifier = Modifier.weight(1f)
        )
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.padding(end = 4.dp))
        Text(
            String.format(
                "%s%s%s",
                if (deviationDelta > 0) "+" else "",
                valueStr,
                result.deviationUnitSymbol
            ),
            style = AppTypography.caption,
            color = color
        )
    }
}

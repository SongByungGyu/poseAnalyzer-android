package com.pose.poseanalyzer.presentation.history

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.pose.poseanalyzer.domain.model.PostureType
import com.pose.poseanalyzer.presentation.common.AppCard
import com.pose.poseanalyzer.presentation.common.AppEmptyState
import com.pose.poseanalyzer.presentation.common.AppNavBar
import com.pose.poseanalyzer.presentation.theme.AppColors
import com.pose.poseanalyzer.presentation.theme.AppSpacing
import com.pose.poseanalyzer.presentation.theme.AppTypography
import kotlinx.coroutines.launch

/**
 * 추이 그래프 화면 — 자세별 시간축 LineChart (Vico).
 *
 * iOS `TrendView.swift` 1:1 대응.
 */
@Composable
fun TrendScreen(
    onBack: () -> Unit,
    viewModel: TrendViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val modelProducer = remember { CartesianChartModelProducer() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) { viewModel.refresh() }
    LaunchedEffect(state.points) {
        if (state.points.isNotEmpty()) {
            scope.launch {
                modelProducer.runTransaction {
                    lineSeries {
                        series(
                            x = state.points.indices.map { it.toFloat() },
                            y = state.points.map { it.value.toFloat() }
                        )
                    }
                }
            }
        }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = AppColors.Surface) {
        Column(modifier = Modifier.fillMaxSize()) {
            AppNavBar(title = "추이", onBack = onBack)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(AppSpacing.s4),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.s4)
            ) {
                TypePicker(state.selectedType, viewModel::selectType)
                RangePicker(state.range, viewModel::selectRange)
                ChartArea(
                    pointsEmpty = state.points.isEmpty(),
                    selectedType = state.selectedType,
                    modelProducer = modelProducer,
                    pointsCount = state.points.size,
                    firstV2Index = state.firstV2Index
                )
                if (state.points.size <= 1 && state.points.isNotEmpty()) {
                    AppCard {
                        Text(
                            "비교를 위해 측정을 더 진행해주세요",
                            style = AppTypography.caption,
                            color = AppColors.OnSurfaceTertiary,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TypePicker(selected: PostureType, onSelect: (PostureType) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.s2)
    ) {
        PostureType.entries.forEach { t ->
            val isSelected = (t == selected)
            val bg = if (isSelected) AppColors.BrandPrimary else AppColors.SurfaceElevated
            val fg = if (isSelected) Color.White else AppColors.OnSurfaceSecondary
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(bg)
                    .border(if (isSelected) 0.dp else 1.dp, AppColors.Divider, CircleShape)
                    .clickable { onSelect(t) }
                    .padding(horizontal = AppSpacing.s3, vertical = AppSpacing.s2)
            ) {
                Text(t.koreanName, style = AppTypography.caption, color = fg)
            }
        }
    }
}

@Composable
private fun RangePicker(range: TrendViewModel.Range, onSelect: (TrendViewModel.Range) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(AppColors.SurfaceMuted)
            .padding(2.dp)
    ) {
        TrendViewModel.Range.entries.forEach { r ->
            val isSelected = (r == range)
            val bg = if (isSelected) AppColors.SurfaceElevated else Color.Transparent
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(6.dp))
                    .background(bg)
                    .clickable { onSelect(r) }
                    .padding(vertical = AppSpacing.s2),
                contentAlignment = Alignment.Center
            ) {
                Text(r.label, style = AppTypography.callout, color = AppColors.OnSurface)
            }
        }
    }
}

@Composable
private fun ChartArea(
    pointsEmpty: Boolean,
    selectedType: PostureType,
    modelProducer: CartesianChartModelProducer,
    pointsCount: Int,
    firstV2Index: Int?
) {
    if (pointsEmpty) {
        AppCard(modifier = Modifier.fillMaxWidth()) {
            AppEmptyState(
                icon = Icons.AutoMirrored.Filled.ShowChart,
                title = "표시할 데이터가 없습니다",
                description = "선택한 기간에 ${selectedType.koreanName} 측정 결과가 없습니다."
            )
        }
    } else {
        Box(modifier = Modifier.fillMaxWidth().height(240.dp)) {
            CartesianChartHost(
                chart = rememberCartesianChart(
                    rememberLineCartesianLayer(),
                    startAxis = VerticalAxis.rememberStart(),
                    bottomAxis = HorizontalAxis.rememberBottom()
                ),
                modelProducer = modelProducer,
                modifier = Modifier.fillMaxSize()
            )
            // v1 → v2 알고리즘 변경 시점 마커 (거북목·라운드숄더만)
            if (firstV2Index != null && pointsCount >= 2) {
                AlgorithmChangeMarker(
                    fraction = firstV2Index.toFloat() / (pointsCount - 1).toFloat(),
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
private fun AlgorithmChangeMarker(fraction: Float, modifier: Modifier = Modifier) {
    // Vico 차트는 양쪽에 axis padding이 있어 정확한 데이터 영역과 약간 어긋날 수 있음.
    // 1차 MVP는 부모 너비 기준 단순 비율로 표시 — 시각 검증에서 조정.
    val strokeColor = AppColors.OnSurfaceTertiary
    Box(modifier = modifier) {
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            val x = size.width * fraction
            val dashLen = 6.dp.toPx()
            val gapLen = 4.dp.toPx()
            drawLine(
                color = strokeColor,
                start = androidx.compose.ui.geometry.Offset(x, 0f),
                end = androidx.compose.ui.geometry.Offset(x, size.height),
                strokeWidth = 1.5f.dp.toPx(),
                pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(
                    floatArrayOf(dashLen, gapLen)
                )
            )
        }
        // 라벨: fraction(0~1) → BiasAlignment(-1~1)로 매핑해서 가로 위치 결정.
        Text(
            "알고리즘 변경",
            style = AppTypography.micro,
            color = AppColors.OnSurfaceTertiary,
            modifier = Modifier
                .align(androidx.compose.ui.BiasAlignment(
                    horizontalBias = (fraction * 2f - 1f).coerceIn(-1f, 1f),
                    verticalBias = -1f // top
                ))
                .padding(top = 2.dp, start = 4.dp)
        )
    }
}

@Composable
private fun rememberCoroutineScope() = androidx.compose.runtime.rememberCoroutineScope()

package com.pose.poseanalyzer.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pose.poseanalyzer.presentation.common.AppCard
import com.pose.poseanalyzer.presentation.common.AppNavBar
import com.pose.poseanalyzer.presentation.common.SectionHeader
import com.pose.poseanalyzer.presentation.theme.AppColors
import com.pose.poseanalyzer.presentation.theme.AppShapes
import com.pose.poseanalyzer.presentation.theme.AppSpacing
import com.pose.poseanalyzer.presentation.theme.AppTypography
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 홈 화면 — 측정 진입점 + 최근 측정 요약.
 *
 * iOS `HomeView.swift` 1:1 대응.
 */
@Composable
fun HomeScreen(
    onStartMeasurement: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenLatestResult: (sessionId: String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) { viewModel.refresh() }

    Surface(modifier = Modifier.fillMaxSize(), color = AppColors.Surface) {
        Column(modifier = Modifier.fillMaxSize()) {
            AppNavBar(
                title = "PoseAnalyzer",
                trailing = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Filled.Settings, contentDescription = "설정", tint = AppColors.BrandPrimary)
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
                Hero(onStartMeasurement = {
                    viewModel.startMeasurement()
                    onStartMeasurement()
                })

                state.latestSession?.let { latest ->
                    RecentMeasurement(
                        measuredAtMs = latest.session.measuredAtMs,
                        onClick = { onOpenLatestResult(latest.session.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun Hero(onStartMeasurement: () -> Unit) {
    Column(
        modifier = Modifier.padding(top = AppSpacing.s2),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.s4)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.s1)) {
            Text(
                "오늘의 자세를\n측정해보세요",
                style = AppTypography.display.copy(fontWeight = FontWeight.Black),
                color = AppColors.OnSurface
            )
            Text(
                "정면·측면 사진 2장이면 충분합니다.",
                style = AppTypography.callout,
                color = AppColors.OnSurfaceSecondary
            )
        }
        CtaCard(onClick = onStartMeasurement)
    }
}

@Composable
private fun CtaCard(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(AppShapes.large)
            .background(
                Brush.linearGradient(
                    colors = listOf(AppColors.BrandPrimary, AppColors.BrandPrimaryLight)
                )
            )
            .clickable(onClick = onClick)
            .padding(horizontal = AppSpacing.s5, vertical = AppSpacing.s4)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    "3 STEPS · 약 30초",
                    style = AppTypography.micro,
                    color = Color.White.copy(alpha = 0.85f)
                )
                Text("측정 시작", style = AppTypography.title, color = Color.White)
            }
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(Color.White.copy(alpha = 0.18f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.PhotoCamera,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
private fun RecentMeasurement(measuredAtMs: Long, onClick: () -> Unit) {
    val date = SimpleDateFormat("yyyy년 M월 d일 EEEE", Locale.KOREAN).format(Date(measuredAtMs))
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.s2)) {
        SectionHeader("최근 측정")
        AppCard(modifier = Modifier.clickable(onClick = onClick)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(date, style = AppTypography.callout, color = AppColors.OnSurface)
                    Text("자세 8가지 분석 완료", style = AppTypography.caption, color = AppColors.OnSurfaceSecondary)
                }
                Spacer(modifier = Modifier.size(AppSpacing.s2))
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForwardIos,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = AppColors.OnSurfaceTertiary
                )
            }
        }
    }
}

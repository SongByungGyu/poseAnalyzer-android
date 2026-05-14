package com.pose.poseanalyzer.presentation.history

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pose.poseanalyzer.data.room.PostureEntity
import com.pose.poseanalyzer.data.room.SessionWithPostures
import com.pose.poseanalyzer.domain.model.PostureStatus
import com.pose.poseanalyzer.presentation.common.AppCard
import com.pose.poseanalyzer.presentation.common.AppEmptyState
import com.pose.poseanalyzer.presentation.common.AppNavBar
import com.pose.poseanalyzer.presentation.theme.AppColors
import com.pose.poseanalyzer.presentation.theme.AppSpacing
import com.pose.poseanalyzer.presentation.theme.AppTypography
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.foundation.gestures.detectTapGestures

/**
 * 기록 탭 — 시간 역순 세션 카드 리스트.
 *
 * iOS `HistoryListView.swift` 1:1 대응.
 *
 * @param onItemClick 세션 카드 탭 → AnalysisResultDetail (readOnly)
 * @param onOpenTrend 추이 화면
 */
@Composable
fun HistoryListScreen(
    onBack: () -> Unit,
    onItemClick: (sessionId: String) -> Unit,
    onOpenTrend: () -> Unit,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    LaunchedEffect(Unit) { viewModel.refresh() }

    Surface(modifier = Modifier.fillMaxSize(), color = AppColors.Surface) {
        Column(modifier = Modifier.fillMaxSize()) {
            AppNavBar(
                title = "기록",
                onBack = onBack,
                trailing = {
                    Row(
                        modifier = Modifier
                            .clickable(onClick = onOpenTrend)
                            .padding(horizontal = AppSpacing.s3, vertical = AppSpacing.s2),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ShowChart,
                            contentDescription = null,
                            tint = AppColors.BrandPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text("추이", style = AppTypography.callout, color = AppColors.BrandPrimary)
                    }
                }
            )
            if (state.sessions.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    AppEmptyState(
                        icon = Icons.Filled.Inbox,
                        title = "아직 기록이 없습니다",
                        description = "측정을 시작하면 여기에 표시됩니다."
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(AppSpacing.s4),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.s2)
                ) {
                    items(state.sessions, key = { it.session.id }) { session ->
                        HistoryRow(
                            session = session,
                            onClick = { onItemClick(session.session.id) },
                            onLongPress = { viewModel.requestDeletion(session.session.id) }
                        )
                    }
                }
            }
        }
    }

    state.deletionTargetId?.let {
        AlertDialog(
            onDismissRequest = viewModel::cancelDeletion,
            title = { Text("삭제 확인") },
            text = { Text("이 측정 기록을 삭제하시겠습니까? 사진도 함께 삭제됩니다.") },
            confirmButton = {
                TextButton(onClick = viewModel::confirmDeletion) {
                    Text("삭제", color = AppColors.StatusSuspect)
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::cancelDeletion) { Text("취소") }
            }
        )
    }

    state.errorMessage?.let { msg ->
        AlertDialog(
            onDismissRequest = viewModel::dismissError,
            title = { Text("오류") },
            text = { Text(msg) },
            confirmButton = {
                TextButton(onClick = viewModel::dismissError) { Text("확인") }
            }
        )
    }
}

@Composable
private fun HistoryRow(
    session: SessionWithPostures,
    onClick: () -> Unit,
    onLongPress: () -> Unit
) {
    val dateFmt = SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.KOREAN)
    AppCard(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(session.session.id) {
                detectTapGestures(
                    onTap = { onClick() },
                    onLongPress = { onLongPress() }
                )
            },
        contentPadding = AppSpacing.s3
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    dateFmt.format(Date(session.session.measuredAtMs)),
                    style = AppTypography.callout,
                    color = AppColors.OnSurface
                )
                StatusDots(postures = session.postures)
            }
            Icon(
                Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                modifier = Modifier.size(12.dp),
                tint = AppColors.OnSurfaceTertiary
            )
        }
    }
}

@Composable
private fun StatusDots(postures: List<PostureEntity>) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        // typeRaw 정렬해서 안정된 순서
        postures.sortedBy { it.typeRaw }.forEach { p ->
            val status = runCatching { PostureStatus.valueOf(p.statusRaw) }
                .getOrDefault(PostureStatus.UNMEASURABLE)
            val color = when (status) {
                PostureStatus.NORMAL -> AppColors.StatusNormal
                PostureStatus.CAUTION -> AppColors.StatusCaution
                PostureStatus.SUSPECT -> AppColors.StatusSuspect
                PostureStatus.UNMEASURABLE -> AppColors.StatusUnmeasurable
            }
            Box(modifier = Modifier.size(8.dp).background(color, CircleShape))
        }
    }
}

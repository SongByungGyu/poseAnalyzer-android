package com.pose.poseanalyzer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.pose.poseanalyzer.domain.model.PostureStatus
import com.pose.poseanalyzer.presentation.common.AppButton
import com.pose.poseanalyzer.presentation.common.AppButtonVariant
import com.pose.poseanalyzer.presentation.common.AppCard
import com.pose.poseanalyzer.presentation.common.AppEmptyState
import com.pose.poseanalyzer.presentation.common.AppNavBar
import com.pose.poseanalyzer.presentation.common.AppToast
import com.pose.poseanalyzer.presentation.common.SectionHeader
import com.pose.poseanalyzer.presentation.common.StatusBadge
import com.pose.poseanalyzer.presentation.theme.AppSpacing
import com.pose.poseanalyzer.presentation.theme.AppTypography
import com.pose.poseanalyzer.presentation.theme.PoseTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PoseTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    PreviewGallery()
                }
            }
        }
    }
}

/**
 * Plan A2a 시점의 통합 화면 — 디자인 토큰 + 7개 공통 컴포넌트를 한 번에 확인.
 *
 * Plan A2b에서 실제 HomeScreen + 측정 마법사로 교체 예정.
 */
@Composable
private fun PreviewGallery() {
    var toastVisible by remember { mutableStateOf(false) }
    Box {
        Column(modifier = Modifier.fillMaxSize()) {
            AppNavBar(title = "PoseAnalyzer")
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(AppSpacing.s4),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.s4)
            ) {
                SectionHeader("상태 뱃지")
                Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.s2)) {
                    StatusBadge(PostureStatus.NORMAL)
                    StatusBadge(PostureStatus.CAUTION)
                    StatusBadge(PostureStatus.SUSPECT)
                    StatusBadge(PostureStatus.UNMEASURABLE)
                }

                SectionHeader("버튼")
                Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.s2)) {
                    AppButton("측정 시작", { toastVisible = true })
                    AppButton("취소", {}, variant = AppButtonVariant.Secondary)
                }

                SectionHeader("카드")
                AppCard {
                    Text("거북목", style = AppTypography.headline)
                    Text("측면 기준 168°", style = AppTypography.body)
                }

                SectionHeader("빈 상태")
                AppEmptyState(
                    icon = Icons.Filled.Inbox,
                    title = "측정 기록이 없습니다",
                    description = "오른쪽 상단의 새 측정 버튼으로 시작해보세요."
                )
            }
        }
        AppToast(
            message = "토스트 알림입니다",
            visible = toastVisible,
            modifier = Modifier.align(Alignment.BottomCenter),
            onDismiss = { toastVisible = false }
        )
    }
}

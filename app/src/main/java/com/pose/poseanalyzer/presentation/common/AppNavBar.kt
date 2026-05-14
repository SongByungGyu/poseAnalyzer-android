package com.pose.poseanalyzer.presentation.common

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.pose.poseanalyzer.presentation.theme.AppColors
import com.pose.poseanalyzer.presentation.theme.AppTypography
import com.pose.poseanalyzer.presentation.theme.PoseTheme

/**
 * 화면 상단 navigation bar — Center title + 옵션 back/trailing.
 *
 * iOS `AppNavBar.swift` 1:1 대응.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavBar(
    title: String,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null
) {
    CenterAlignedTopAppBar(
        title = { Text(title, style = AppTypography.headline) },
        navigationIcon = {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로")
                }
            }
        },
        actions = { trailing?.invoke() ?: Box(Modifier) },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = AppColors.Surface,
            titleContentColor = AppColors.OnSurface
        ),
        modifier = modifier
    )
}

@Preview
@Composable
private fun PreviewNavBar() {
    PoseTheme { AppNavBar("측정", onBack = {}) }
}

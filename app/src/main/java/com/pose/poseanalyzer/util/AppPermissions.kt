package com.pose.poseanalyzer.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat

/**
 * 카메라 권한 상태 + 요청 launcher Composable helper.
 *
 * iOS `AppPermissions.swift` 1:1 대응.
 *
 * @return Triple of (granted, request, denied)
 *   - granted: 현재 허용 상태
 *   - request: 권한 요청 시작 launcher
 *   - denied: 마지막 요청이 거부됐는지 (지속적 거부 안내용)
 */
@Composable
fun rememberCameraPermissionState(context: Context): CameraPermissionState {
    var granted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    var denied by remember { mutableStateOf(false) }
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        granted = isGranted
        denied = !isGranted
    }
    return remember(granted, denied) {
        CameraPermissionState(
            granted = granted,
            denied = denied,
            request = { launcher.launch(Manifest.permission.CAMERA) }
        )
    }
}

data class CameraPermissionState(
    val granted: Boolean,
    val denied: Boolean,
    val request: () -> Unit
)

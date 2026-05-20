package com.pose.poseanalyzer.presentation.measurement

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.core.content.ContextCompat
import com.pose.poseanalyzer.domain.model.SessionView
import com.pose.poseanalyzer.util.rememberCameraPermissionState
import java.io.ByteArrayOutputStream
import java.util.concurrent.Executor

/**
 * 카메라 권한 + LifecycleCameraController + 셔터.
 *
 * iOS `CustomCameraView.swift` 1:1 대응.
 */
@Composable
fun CustomCameraScreen(
    view: SessionView,
    step: Int,
    totalSteps: Int = 3,
    onCaptured: (Bitmap) -> Unit,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val permission = rememberCameraPermissionState(context)

    if (!permission.granted) {
        PermissionRequest(
            onRequest = permission.request,
            denied = permission.denied,
            onClose = onClose
        )
        return
    }

    val cameraController = remember {
        LifecycleCameraController(context).apply {
            cameraSelector = androidx.camera.core.CameraSelector.DEFAULT_BACK_CAMERA
            setEnabledUseCases(LifecycleCameraController.IMAGE_CAPTURE)
        }
    }
    DisposableEffect(lifecycleOwner) {
        cameraController.bindToLifecycle(lifecycleOwner)
        onDispose { cameraController.unbind() }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        AndroidView(
            factory = { ctx: Context ->
                PreviewView(ctx).apply {
                    // COMPATIBLE 모드(TextureView)로 강제 — 기본 PERFORMANCE 모드(SurfaceView)는
                    // 그 위에 Compose Canvas가 그려질 때 z-order/마스킹 문제로 검은 화면이 될 수 있음.
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                    this.controller = cameraController
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        PoseGuideOverlay(view = view, step = step, totalSteps = totalSteps)

        // 닫기 버튼 (safeArea top inset)
        IconButton(
            onClick = onClose,
            modifier = Modifier
                .align(Alignment.TopStart)
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(Color.Black.copy(alpha = 0.55f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Close, contentDescription = "닫기", tint = Color.White)
            }
        }

        // 하단 셔터
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(bottom = 40.dp)
                .fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.Bottom
        ) {
            ShutterButton(
                onClick = {
                    capturePhoto(
                        controller = cameraController,
                        executor = ContextCompat.getMainExecutor(context),
                        onSuccess = onCaptured,
                        onError = { /* TODO Plan A2c에서 에러 UI */ }
                    )
                }
            )
        }
    }
}

@Composable
private fun ShutterButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(72.dp)
            .background(Color.White.copy(alpha = 0.2f), CircleShape)
            .border(width = 3.dp, color = Color.White, shape = CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(Color.White, CircleShape)
                .border(width = 1.dp, color = Color.White.copy(alpha = 0.5f), shape = CircleShape)
        ) {
            IconButton(
                onClick = onClick,
                modifier = Modifier.size(56.dp)
            ) {
                // 빈 — 박스 전체가 클릭
            }
        }
    }
}

@Composable
private fun PermissionRequest(
    onRequest: () -> Unit,
    denied: Boolean,
    onClose: () -> Unit
) {
    var requested by remember { mutableStateOf(false) }
    if (!requested && !denied) {
        // 자동 요청
        DisposableEffect(Unit) {
            requested = true
            onRequest()
            onDispose { }
        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.foundation.layout.Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            androidx.compose.material3.Text(
                if (denied) "카메라 권한이 필요합니다.\n설정에서 권한을 허용해주세요."
                else "카메라 권한을 요청 중입니다...",
                color = Color.White,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            IconButton(onClick = onClose) {
                Icon(Icons.Filled.Close, contentDescription = "닫기", tint = Color.White)
            }
        }
    }
}

private fun capturePhoto(
    controller: LifecycleCameraController,
    executor: Executor,
    onSuccess: (Bitmap) -> Unit,
    onError: (Exception) -> Unit
) {
    val outputOptions = ImageCapture.OutputFileOptions.Builder(
        ByteArrayOutputStream() // dummy — JPEG bytes는 callback에서 직접 받기 위해 다른 API 사용
    ).build()

    // 실제로는 takePicture(onCaptureCallback) 사용 — Bitmap 콜백 받음.
    controller.takePicture(
        executor,
        object : ImageCapture.OnImageCapturedCallback() {
            override fun onCaptureSuccess(image: androidx.camera.core.ImageProxy) {
                val bitmap = image.toBitmap()
                val rotation = image.imageInfo.rotationDegrees
                image.close()
                val rotated = if (rotation != 0) rotateBitmap(bitmap, rotation.toFloat()) else bitmap
                onSuccess(rotated)
            }

            override fun onError(exception: ImageCaptureException) {
                onError(exception)
            }
        }
    )
}

private fun rotateBitmap(bitmap: Bitmap, degrees: Float): Bitmap {
    val matrix = Matrix().apply { postRotate(degrees) }
    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
}

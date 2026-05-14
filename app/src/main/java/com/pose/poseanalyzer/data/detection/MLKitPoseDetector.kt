package com.pose.poseanalyzer.data.detection

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.accurate.AccuratePoseDetectorOptions
import com.pose.poseanalyzer.domain.detection.PoseDetectionException
import com.pose.poseanalyzer.domain.detection.PoseDetector
import com.pose.poseanalyzer.domain.model.JointName
import com.pose.poseanalyzer.domain.model.Point2D
import com.pose.poseanalyzer.domain.model.PoseFrame
import com.pose.poseanalyzer.domain.model.SessionView
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Google ML Kit Pose Detection 기반 [PoseDetector] 구현.
 *
 * iOS의 `VisionPoseDetector`와 1:1 대응.
 *
 * - `AccuratePoseDetectorOptions` + `SINGLE_IMAGE_MODE`: 1장 정확도 모드
 * - `suspendCancellableCoroutine`: ML Kit의 `Task<Pose>` → `suspend fun`
 * - ML Kit 좌표 (픽셀) → 정규화 좌표 (0~1) 변환
 * - `inFrameLikelihood` → confidence
 *
 * 사람 인식 부족 (신뢰도 0.3 이상 landmark 5개 미만) → [PoseDetectionException.NoPersonDetected]
 */
@Singleton
class MLKitPoseDetector @Inject constructor() : PoseDetector {

    private val detector by lazy {
        val options = AccuratePoseDetectorOptions.Builder()
            .setDetectorMode(AccuratePoseDetectorOptions.SINGLE_IMAGE_MODE)
            .build()
        PoseDetection.getClient(options)
    }

    override suspend fun detect(image: Bitmap, view: SessionView): PoseFrame =
        suspendCancellableCoroutine { cont ->
            val inputImage = try {
                InputImage.fromBitmap(image, 0)
            } catch (e: IllegalArgumentException) {
                cont.resumeWithException(PoseDetectionException.InvalidImage)
                return@suspendCancellableCoroutine
            }
            detector.process(inputImage)
                .addOnSuccessListener { pose ->
                    try {
                        val frame = makeFrame(pose, view, image.width, image.height)
                        cont.resume(frame)
                    } catch (e: PoseDetectionException) {
                        cont.resumeWithException(e)
                    } catch (e: Exception) {
                        cont.resumeWithException(
                            PoseDetectionException.VisionFailed(e.message ?: "관절 추출 실패")
                        )
                    }
                }
                .addOnFailureListener { e ->
                    cont.resumeWithException(
                        PoseDetectionException.VisionFailed(e.message ?: "ML Kit 분석 실패")
                    )
                }
        }

    private fun makeFrame(pose: Pose, view: SessionView, width: Int, height: Int): PoseFrame {
        val allLandmarks = pose.allPoseLandmarks
        if (allLandmarks.isEmpty()) {
            throw PoseDetectionException.NoPersonDetected
        }

        val reliableCount = allLandmarks.count { it.inFrameLikelihood > 0.3f }
        if (reliableCount < 5) {
            throw PoseDetectionException.NoPersonDetected
        }

        val joints = mutableMapOf<JointName, PoseFrame.Joint>()
        for (landmark in allLandmarks) {
            val jointName = JointName.fromMlKit(landmark.landmarkType) ?: continue
            val nx = landmark.position.x / width.toFloat()
            val ny = landmark.position.y / height.toFloat()
            joints[jointName] = PoseFrame.Joint(
                name = jointName,
                location = Point2D(nx, ny),
                confidence = landmark.inFrameLikelihood
            )
        }

        return PoseFrame(
            joints = joints,
            view = view,
            imageWidth = width,
            imageHeight = height
        )
    }
}

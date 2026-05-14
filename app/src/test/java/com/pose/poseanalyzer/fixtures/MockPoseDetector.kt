package com.pose.poseanalyzer.fixtures

import android.graphics.Bitmap
import com.pose.poseanalyzer.domain.detection.PoseDetector
import com.pose.poseanalyzer.domain.model.PoseFrame
import com.pose.poseanalyzer.domain.model.SessionView

/**
 * 단위테스트용 PoseDetector 페이크.
 *
 * 미리 정의된 [PoseFrame]을 [SessionView]에 따라 반환.
 */
class MockPoseDetector(
    private val frontFrame: PoseFrame,
    private val sideFrame: PoseFrame
) : PoseDetector {
    override suspend fun detect(image: Bitmap, view: SessionView): PoseFrame =
        when (view) {
            SessionView.FRONT -> frontFrame
            SessionView.SIDE -> sideFrame
        }
}

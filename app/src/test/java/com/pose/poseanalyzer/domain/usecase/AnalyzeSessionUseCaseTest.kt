package com.pose.poseanalyzer.domain.usecase

import android.graphics.Bitmap
import com.pose.poseanalyzer.data.asymmetry.DefaultAsymmetryAnalyzer
import com.pose.poseanalyzer.domain.evaluation.ForwardHeadEvaluator
import com.pose.poseanalyzer.domain.evaluation.HeadTiltEvaluator
import com.pose.poseanalyzer.domain.evaluation.ScoliosisEvaluator
import com.pose.poseanalyzer.domain.model.JointName
import com.pose.poseanalyzer.domain.model.Point2D
import com.pose.poseanalyzer.domain.model.PostureType
import com.pose.poseanalyzer.domain.model.SessionView
import com.pose.poseanalyzer.fixtures.MockPoseDetector
import com.pose.poseanalyzer.fixtures.PoseFrameFixtures
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class AnalyzeSessionUseCaseTest {

    @Test
    fun `evaluator 별 view 분배 + asymmetry 포함된 SessionReport 반환`() = runTest {
        val front = PoseFrameFixtures.frame(
            SessionView.FRONT,
            mapOf(
                JointName.LEFT_EAR to Point2D(0.45f, 0.2f),
                JointName.RIGHT_EAR to Point2D(0.55f, 0.2f),
                JointName.LEFT_SHOULDER to Point2D(0.4f, 0.3f),
                JointName.RIGHT_SHOULDER to Point2D(0.6f, 0.3f),
                JointName.LEFT_HIP to Point2D(0.42f, 0.6f),
                JointName.RIGHT_HIP to Point2D(0.58f, 0.6f)
            )
        )
        val side = PoseFrameFixtures.sideEarShoulderHipAngle(175.0)
        val detector = MockPoseDetector(front, side)
        val useCase = AnalyzeSessionUseCase(
            detector = detector,
            evaluators = listOf(
                ForwardHeadEvaluator(),
                HeadTiltEvaluator(),
                ScoliosisEvaluator()
            ),
            asymmetryAnalyzer = DefaultAsymmetryAnalyzer()
        )

        val mockBitmap = mockk<Bitmap>(relaxed = true)
        val report = useCase.analyze(mockBitmap, mockBitmap, heightCm = null)

        assertEquals(3, report.postures.size)
        assertNotNull(report.posture(PostureType.FORWARD_HEAD))
        assertNotNull(report.posture(PostureType.HEAD_TILT))
        assertNotNull(report.posture(PostureType.SCOLIOSIS))
        assertNotNull(report.asymmetry)
    }
}

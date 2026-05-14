package com.pose.poseanalyzer.data.asymmetry

import com.pose.poseanalyzer.domain.asymmetry.AsymmetryAnalyzer
import com.pose.poseanalyzer.domain.model.AsymmetryResult
import com.pose.poseanalyzer.domain.model.JointName
import com.pose.poseanalyzer.domain.model.PoseFrame
import com.pose.poseanalyzer.util.GeometryMath
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

/**
 * 어깨/엉덩이 좌우 비대칭 기본 구현.
 *
 * iOS `DefaultAsymmetryAnalyzer` 1:1 번역.
 *
 * ⚠️ y축 방향 차이:
 * - iOS (Vision): y 큰 쪽 = 위. → `lp.y > rp.y` 이면 LEFT_HIGHER
 * - Android (ML Kit): y 작은 쪽 = 위. → `lp.y < rp.y` 이면 LEFT_HIGHER
 */
@Singleton
class DefaultAsymmetryAnalyzer @Inject constructor() : AsymmetryAnalyzer {

    /** 균형으로 판단할 각도 임계값 (도) */
    private val balancedThreshold = 0.5

    override fun analyze(frontFrame: PoseFrame, heightCm: Double?): AsymmetryResult {
        val shoulder = analyzePair(
            frontFrame, JointName.LEFT_SHOULDER, JointName.RIGHT_SHOULDER, heightCm
        )
        val hip = analyzePair(
            frontFrame, JointName.LEFT_HIP, JointName.RIGHT_HIP, heightCm
        )
        return AsymmetryResult(shoulder = shoulder, hip = hip)
    }

    private fun analyzePair(
        frame: PoseFrame,
        left: JointName,
        right: JointName,
        heightCm: Double?
    ): AsymmetryResult.Difference {
        val lp = frame.point(left)
        val rp = frame.point(right)
        if (lp == null || rp == null) {
            return AsymmetryResult.Difference(
                cm = null, ratio = 0.0, angleDegrees = 0.0,
                direction = AsymmetryResult.Direction.BALANCED
            )
        }

        val angle = GeometryMath.absLineAngleFromHorizontal(lp, rp)
        val referenceWidth = GeometryMath.distance(lp, rp)
        val normalizedYDiff = abs((lp.y - rp.y).toDouble())
        val ratio = if (referenceWidth > 0) normalizedYDiff / referenceWidth else 0.0

        val direction = when {
            angle < balancedThreshold -> AsymmetryResult.Direction.BALANCED
            lp.y < rp.y -> AsymmetryResult.Direction.LEFT_HIGHER
            else -> AsymmetryResult.Direction.RIGHT_HIGHER
        }

        // cm 환산 — 키 있으면 코-발목 정규화 거리로 환산 비율 계산
        var cm: Double? = null
        if (heightCm != null) {
            val nose = frame.point(JointName.NOSE)
            val la = frame.point(JointName.LEFT_ANKLE)
            val ra = frame.point(JointName.RIGHT_ANKLE)
            if (nose != null && la != null && ra != null) {
                val ankleAvgY = (la.y + ra.y).toDouble() / 2.0
                val bodyPixelHeight = abs(nose.y.toDouble() - ankleAvgY)
                if (bodyPixelHeight > 0) {
                    val cmPerNormalized = heightCm / bodyPixelHeight
                    cm = normalizedYDiff * cmPerNormalized
                }
            }
        }

        return AsymmetryResult.Difference(
            cm = cm,
            ratio = ratio,
            angleDegrees = angle,
            direction = direction
        )
    }
}

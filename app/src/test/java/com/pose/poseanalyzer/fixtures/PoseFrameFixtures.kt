package com.pose.poseanalyzer.fixtures

import com.pose.poseanalyzer.domain.model.JointName
import com.pose.poseanalyzer.domain.model.Point2D
import com.pose.poseanalyzer.domain.model.PoseFrame
import com.pose.poseanalyzer.domain.model.SessionView
import kotlin.math.cos
import kotlin.math.sin

/**
 * 단위테스트용 PoseFrame 빌더 헬퍼.
 *
 * 모든 좌표는 정규화 (0~1) 가정. 신뢰도는 명시하지 않으면 0.9.
 */
object PoseFrameFixtures {

    private const val DEFAULT_CONFIDENCE = 0.9f
    private const val IMG_W = 1000
    private const val IMG_H = 1000

    fun frame(
        view: SessionView,
        joints: Map<JointName, Point2D>,
        confidence: Float = DEFAULT_CONFIDENCE,
        imageWidth: Int = IMG_W,
        imageHeight: Int = IMG_H
    ): PoseFrame {
        val jointMap = joints.mapValues { (name, loc) ->
            PoseFrame.Joint(name = name, location = loc, confidence = confidence)
        }
        return PoseFrame(jointMap, view, imageWidth, imageHeight)
    }

    /**
     * vertex 기준 두 점이 [angleDeg]° 각도를 이루도록 좌표 계산.
     *
     * vertex가 원점인 좌표계에서:
     * - p1: (1, 0)
     * - p2: (cos(angle), sin(angle))
     *
     * 그래서 vertex를 임의 위치에 두고 두 점을 offset.
     */
    fun pointsForAngle(
        vertex: Point2D,
        angleDeg: Double,
        armLength: Float = 0.1f
    ): Triple<Point2D, Point2D, Point2D> {
        val rad = Math.toRadians(angleDeg)
        val p1 = Point2D(vertex.x + armLength, vertex.y)
        val p2 = Point2D(
            vertex.x + (armLength * cos(rad)).toFloat(),
            vertex.y + (armLength * sin(rad)).toFloat()
        )
        return Triple(p1, vertex, p2)
    }

    /**
     * 측면 거북목 평가용 frame:
     * 귀-어깨-엉덩이가 주어진 [angleDeg]°를 이루도록 우측 joint들만 셋업.
     */
    fun sideEarShoulderHipAngle(angleDeg: Double, useRight: Boolean = true): PoseFrame {
        val shoulder = Point2D(0.5f, 0.5f)
        val (ear, _, hip) = pointsForAngle(shoulder, angleDeg, armLength = 0.15f)
        val joints = if (useRight) mapOf(
            JointName.RIGHT_EAR to ear,
            JointName.RIGHT_SHOULDER to shoulder,
            JointName.RIGHT_HIP to hip
        ) else mapOf(
            JointName.LEFT_EAR to ear,
            JointName.LEFT_SHOULDER to shoulder,
            JointName.LEFT_HIP to hip
        )
        return frame(SessionView.SIDE, joints)
    }
}

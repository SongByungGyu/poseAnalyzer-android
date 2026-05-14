package com.pose.poseanalyzer.util

import com.pose.poseanalyzer.domain.model.Point2D
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.atan2
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

/**
 * 관절 좌표 기반 기하 계산 유틸 (순수 함수 모음).
 *
 * iOS의 `GeometryMath`와 1:1 대응. 안드로이드 의존 없음 (JVM 단위테스트 가능).
 */
object GeometryMath {

    /**
     * 세 점이 이루는 각도 (vertex 기준 ∠p1·vertex·p2). 단위: 도(0~180).
     * 두 벡터 중 하나라도 길이 0이면 분모 보호로 0 반환.
     */
    fun angleBetween(p1: Point2D, vertex: Point2D, p2: Point2D): Double {
        val v1x = (p1.x - vertex.x).toDouble()
        val v1y = (p1.y - vertex.y).toDouble()
        val v2x = (p2.x - vertex.x).toDouble()
        val v2y = (p2.y - vertex.y).toDouble()
        val mag1 = sqrt(v1x * v1x + v1y * v1y)
        val mag2 = sqrt(v2x * v2x + v2y * v2y)
        if (mag1 == 0.0 || mag2 == 0.0) return 0.0
        val dot = v1x * v2x + v1y * v2y
        var cosTheta = dot / (mag1 * mag2)
        cosTheta = max(-1.0, min(1.0, cosTheta))
        return Math.toDegrees(acos(cosTheta))
    }

    /** 두 점 사이 유클리드 거리 */
    fun distance(a: Point2D, b: Point2D): Double {
        val dx = (a.x - b.x).toDouble()
        val dy = (a.y - b.y).toDouble()
        return sqrt(dx * dx + dy * dy)
    }

    /**
     * 수평선 대비 두 점이 만드는 직선의 기울기 각도. 결과 범위 -180~180.
     * 좌상단 원점 기준: dy 양수 = 아래쪽, dx 양수 = 오른쪽.
     */
    fun lineAngleFromHorizontal(a: Point2D, b: Point2D): Double {
        val dx = (b.x - a.x).toDouble()
        val dy = (b.y - a.y).toDouble()
        return Math.toDegrees(atan2(dy, dx))
    }

    /** 절댓값 기울기 (0~180) */
    fun absLineAngleFromHorizontal(a: Point2D, b: Point2D): Double =
        abs(lineAngleFromHorizontal(a, b))

    /** 두 점의 수평 거리 / 기준 폭 비율 (절댓값). referenceWidth ≤ 0 → 0 반환. */
    fun horizontalGapRatio(from: Point2D, to: Point2D, referenceWidth: Double): Double {
        if (referenceWidth <= 0) return 0.0
        return abs((from.x - to.x).toDouble()) / referenceWidth
    }
}

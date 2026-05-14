package com.pose.poseanalyzer.util

import com.pose.poseanalyzer.domain.model.Point2D
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class GeometryMathTest {

    @Test
    fun `세 점이 직선이면 각도는 180도`() {
        val angle = GeometryMath.angleBetween(
            Point2D(0f, 0f), Point2D(1f, 0f), Point2D(2f, 0f)
        )
        assertEquals(180.0, angle, 0.01)
    }

    @Test
    fun `세 점이 직각이면 각도는 90도`() {
        val angle = GeometryMath.angleBetween(
            Point2D(0f, 1f), Point2D(0f, 0f), Point2D(1f, 0f)
        )
        assertEquals(90.0, angle, 0.01)
    }

    @Test
    fun `세 점이 겹치면 0 반환 (분모 보호)`() {
        val angle = GeometryMath.angleBetween(
            Point2D(0f, 0f), Point2D(0f, 0f), Point2D(1f, 0f)
        )
        assertTrue("유한 값이어야 함", angle.isFinite())
    }

    @Test
    fun `같은 점 사이 거리는 0`() {
        val d = GeometryMath.distance(Point2D(5f, 5f), Point2D(5f, 5f))
        assertEquals(0.0, d, 0.01)
    }

    @Test
    fun `피타고라스 345 거리는 5`() {
        val d = GeometryMath.distance(Point2D(0f, 0f), Point2D(3f, 4f))
        assertEquals(5.0, d, 0.01)
    }

    @Test
    fun `수평 직선 기울기는 0도`() {
        val angle = GeometryMath.lineAngleFromHorizontal(
            Point2D(0f, 5f), Point2D(10f, 5f)
        )
        assertEquals(0.0, angle, 0.01)
    }

    @Test
    fun `수직 직선 기울기는 절댓값 90도`() {
        val angle = GeometryMath.lineAngleFromHorizontal(
            Point2D(5f, 0f), Point2D(5f, 10f)
        )
        assertEquals(90.0, abs(angle), 0.01)
    }

    @Test
    fun `우측 아래로 45도 (좌상단 원점)`() {
        // Android 좌상단 원점이라 y가 클수록 아래
        // (0,0) -> (10,10)이면 우측 아래로 45도
        val angle = GeometryMath.lineAngleFromHorizontal(
            Point2D(0f, 0f), Point2D(10f, 10f)
        )
        assertEquals(45.0, angle, 0.01)
    }

    @Test
    fun `절댓값 기울기 함수`() {
        val angle = GeometryMath.absLineAngleFromHorizontal(
            Point2D(0f, 10f), Point2D(10f, 0f)
        )
        assertEquals(45.0, angle, 0.01)
    }

    @Test
    fun `수평 거리 비율`() {
        val ratio = GeometryMath.horizontalGapRatio(
            Point2D(5f, 0f), Point2D(0f, 0f), referenceWidth = 20.0
        )
        assertEquals(0.25, ratio, 0.01)
    }

    @Test
    fun `referenceWidth 0이면 0 반환 (분모 보호)`() {
        val ratio = GeometryMath.horizontalGapRatio(
            Point2D(5f, 0f), Point2D(0f, 0f), referenceWidth = 0.0
        )
        assertEquals(0.0, ratio, 0.01)
    }
}

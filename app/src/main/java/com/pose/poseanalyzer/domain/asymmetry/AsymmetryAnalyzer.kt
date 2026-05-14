package com.pose.poseanalyzer.domain.asymmetry

import com.pose.poseanalyzer.domain.model.AsymmetryResult
import com.pose.poseanalyzer.domain.model.PoseFrame

/**
 * 정면 사진 기반 좌우 비대칭 분석.
 *
 * @param frontFrame 정면 사진 [PoseFrame]
 * @param heightCm 사용자 키 (옵션, 있으면 cm 환산)
 */
interface AsymmetryAnalyzer {
    fun analyze(frontFrame: PoseFrame, heightCm: Double?): AsymmetryResult
}

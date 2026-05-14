package com.pose.poseanalyzer.domain.model

/**
 * 한 장의 사진에서 추출된 관절 좌표 묶음.
 *
 * 좌표계: 정규화 0~1, 좌상단 원점 (Android/ML Kit 기본).
 * iOS Vision은 좌하단 원점이라 변환이 필요했지만 ML Kit은 좌상단이라 단순.
 */
data class PoseFrame(
    val joints: Map<JointName, Joint>,
    val view: SessionView,
    val imageWidth: Int,
    val imageHeight: Int
) {
    data class Joint(
        val name: JointName,
        val location: Point2D,
        val confidence: Float
    )

    /** 특정 관절의 신뢰도가 임계값 이상인지 */
    fun isReliable(name: JointName, threshold: Float = DEFAULT_CONFIDENCE): Boolean {
        val joint = joints[name] ?: return false
        return joint.confidence >= threshold
    }

    /** 여러 관절이 모두 신뢰 가능한지 */
    fun areReliable(names: List<JointName>, threshold: Float = DEFAULT_CONFIDENCE): Boolean =
        names.all { isReliable(it, threshold) }

    /** 관절 좌표 반환 (신뢰도 무관) */
    fun point(name: JointName): Point2D? = joints[name]?.location

    /** 평균 신뢰도 (관절 미존재 시 0) */
    fun averageConfidence(names: List<JointName>): Double {
        val valid = names.mapNotNull { joints[it] }
        if (valid.isEmpty()) return 0.0
        return valid.map { it.confidence.toDouble() }.average()
    }

    /**
     * iOS의 `.neck` joint 대응 — ML Kit엔 없으므로 양 어깨 중점으로 계산.
     * 양 어깨가 모두 신뢰 가능할 때만 반환.
     */
    val neck: Point2D?
        get() {
            if (!areReliable(listOf(JointName.LEFT_SHOULDER, JointName.RIGHT_SHOULDER))) return null
            val l = point(JointName.LEFT_SHOULDER) ?: return null
            val r = point(JointName.RIGHT_SHOULDER) ?: return null
            return Point2D((l.x + r.x) / 2f, (l.y + r.y) / 2f)
        }

    companion object {
        const val DEFAULT_CONFIDENCE: Float = 0.3f
    }
}

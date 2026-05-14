package com.pose.poseanalyzer.domain.model

/**
 * 측정에 사용하는 관절 이름 (ML Kit [com.google.mlkit.vision.pose.PoseLandmark]의 부분 집합).
 *
 * [mlkitType] 값은 [com.google.mlkit.vision.pose.PoseLandmark]의 상수와 1:1 매핑.
 * iOS Vision의 `.neck` joint는 ML Kit에 없으므로 [PoseFrame.neck]에서 양 어깨 중점으로 계산.
 */
enum class JointName(val mlkitType: Int) {
    NOSE(0),
    LEFT_EYE(2),
    RIGHT_EYE(5),
    LEFT_EAR(7),
    RIGHT_EAR(8),
    LEFT_SHOULDER(11),
    RIGHT_SHOULDER(12),
    LEFT_ELBOW(13),
    RIGHT_ELBOW(14),
    LEFT_WRIST(15),
    RIGHT_WRIST(16),
    LEFT_HIP(23),
    RIGHT_HIP(24),
    LEFT_KNEE(25),
    RIGHT_KNEE(26),
    LEFT_ANKLE(27),
    RIGHT_ANKLE(28);

    companion object {
        private val byMlkit = values().associateBy { it.mlkitType }
        fun fromMlKit(type: Int): JointName? = byMlkit[type]
    }
}

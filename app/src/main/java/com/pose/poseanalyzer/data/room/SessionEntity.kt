package com.pose.poseanalyzer.data.room

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 한 번의 측정 세션 (정면+측면 사진 1세트).
 *
 * iOS `SessionRecord` SwiftData 모델 대응. 비대칭 정보는 normalized된 컬럼으로 저장.
 */
@Entity(
    tableName = "sessions",
    indices = [Index(value = ["measuredAtMs"])]
)
data class SessionEntity(
    @PrimaryKey val id: String,             // UUID.toString()
    val measuredAtMs: Long,                  // epoch millis
    val frontImagePath: String,
    val sideImagePath: String,
    val heightCmAtMeasure: Double?,
    val asymmetryShoulderCm: Double?,
    val asymmetryShoulderRatio: Double,
    val asymmetryShoulderAngle: Double,
    val asymmetryShoulderDirectionRaw: String,
    val asymmetryHipCm: Double?,
    val asymmetryHipRatio: Double,
    val asymmetryHipAngle: Double,
    val asymmetryHipDirectionRaw: String
)

package com.pose.poseanalyzer.data.room

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 한 세션 안의 개별 자세 판정 결과.
 *
 * iOS `PostureRecord` SwiftData 모델 대응. [SessionEntity]와 CASCADE FK.
 */
@Entity(
    tableName = "postures",
    foreignKeys = [
        ForeignKey(
            entity = SessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["sessionId"])]
)
data class PostureEntity(
    @PrimaryKey val id: String,
    val sessionId: String,
    val typeRaw: String,
    val statusRaw: String,
    val primaryMetric: Double,
    val primaryMetricUnitRaw: String,
    val confidence: Double,
    val advice: String?
)

package com.pose.poseanalyzer.data.room

import androidx.room.ColumnInfo
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
    val advice: String?,
    /**
     * 알고리즘 버전 마커. 거북목·라운드숄더는 v1→v2(CVA·FSA) 마이그레이션.
     * 기본값 "v1" — 마이그레이션 1→2 시점에 기존 행은 모두 v1로 채워짐.
     */
    @ColumnInfo(name = "algorithm_version", defaultValue = "v1")
    val algorithmVersion: String = "v1"
)

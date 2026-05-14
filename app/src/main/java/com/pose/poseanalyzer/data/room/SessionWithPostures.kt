package com.pose.poseanalyzer.data.room

import androidx.room.Embedded
import androidx.room.Relation

/**
 * [SessionEntity] + 1:N [PostureEntity] 조합 (Room Relation).
 */
data class SessionWithPostures(
    @Embedded val session: SessionEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "sessionId"
    )
    val postures: List<PostureEntity>
)

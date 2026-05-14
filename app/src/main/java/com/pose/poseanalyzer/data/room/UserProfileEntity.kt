package com.pose.poseanalyzer.data.room

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 사용자 프로필 (앱당 1개 인스턴스, 항상 id=1).
 *
 * iOS의 `UserProfile` SwiftData 모델과 대응.
 */
@Entity(tableName = "user_profiles")
data class UserProfileEntity(
    @PrimaryKey val id: Int = SINGLETON_ID,
    val heightCm: Double?,
    val updatedAtMs: Long
) {
    companion object {
        const val SINGLETON_ID = 1
    }
}

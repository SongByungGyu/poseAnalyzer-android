package com.pose.poseanalyzer.data

import com.pose.poseanalyzer.data.room.UserProfileDao
import com.pose.poseanalyzer.data.room.UserProfileEntity
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 사용자 키 등 단일 프로필 저장/조회 (앱당 1개 레코드만 유지).
 *
 * iOS `UserProfileRepository` 1:1 대응.
 */
@Singleton
class UserProfileRepository @Inject constructor(
    private val dao: UserProfileDao
) {

    suspend fun getHeightCm(): Double? = dao.get()?.heightCm

    suspend fun updateHeightCm(value: Double?) {
        dao.upsert(
            UserProfileEntity(
                id = UserProfileEntity.SINGLETON_ID,
                heightCm = value,
                updatedAtMs = System.currentTimeMillis()
            )
        )
    }
}

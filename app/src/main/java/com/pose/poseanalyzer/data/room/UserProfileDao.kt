package com.pose.poseanalyzer.data.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface UserProfileDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(profile: UserProfileEntity)

    @Query("SELECT * FROM user_profiles WHERE id = :id LIMIT 1")
    suspend fun get(id: Int = UserProfileEntity.SINGLETON_ID): UserProfileEntity?
}

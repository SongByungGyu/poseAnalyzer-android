package com.pose.poseanalyzer.data.room

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        UserProfileEntity::class,
        SessionEntity::class,
        PostureEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun sessionDao(): SessionDao
    abstract fun userProfileDao(): UserProfileDao

    companion object {
        const val NAME = "pose_analyzer.db"
    }
}

package com.pose.poseanalyzer.data.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        UserProfileEntity::class,
        SessionEntity::class,
        PostureEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun sessionDao(): SessionDao
    abstract fun userProfileDao(): UserProfileDao

    companion object {
        const val NAME = "pose_analyzer.db"

        /**
         * v1 → v2 : `postures.algorithm_version` 컬럼 추가.
         * 기존 행은 모두 "v1" 라벨로 보존 (CVA·FSA 도입 전 자체 알고리즘).
         */
        val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE postures ADD COLUMN algorithm_version TEXT NOT NULL DEFAULT 'v1'"
                )
            }
        }
    }
}

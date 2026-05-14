package com.pose.poseanalyzer.data.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface SessionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: SessionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPostures(postures: List<PostureEntity>)

    @Transaction
    suspend fun insertWithPostures(session: SessionEntity, postures: List<PostureEntity>) {
        insertSession(session)
        insertPostures(postures)
    }

    @Transaction
    @Query("SELECT * FROM sessions ORDER BY measuredAtMs DESC")
    suspend fun fetchAll(): List<SessionWithPostures>

    @Transaction
    @Query("SELECT * FROM sessions WHERE id = :id LIMIT 1")
    suspend fun fetchById(id: String): SessionWithPostures?

    @Query("DELETE FROM sessions WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT COUNT(*) FROM sessions")
    suspend fun count(): Int
}

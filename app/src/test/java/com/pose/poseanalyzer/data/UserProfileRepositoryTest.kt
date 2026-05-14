package com.pose.poseanalyzer.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.pose.poseanalyzer.data.room.AppDatabase
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class UserProfileRepositoryTest {

    private lateinit var db: AppDatabase
    private lateinit var repo: UserProfileRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repo = UserProfileRepository(db.userProfileDao())
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun `초기 키는 null`() = runTest {
        assertNull(repo.getHeightCm())
    }

    @Test
    fun `키 저장 후 조회`() = runTest {
        repo.updateHeightCm(170.5)
        assertEquals(170.5, repo.getHeightCm()!!, 0.001)
    }

    @Test
    fun `키 업데이트 시 항상 단일 레코드 유지 (id=1)`() = runTest {
        repo.updateHeightCm(170.0)
        repo.updateHeightCm(175.0)
        assertEquals(175.0, repo.getHeightCm()!!, 0.001)
    }

    @Test
    fun `null 저장 가능`() = runTest {
        repo.updateHeightCm(170.0)
        repo.updateHeightCm(null)
        assertNull(repo.getHeightCm())
    }
}

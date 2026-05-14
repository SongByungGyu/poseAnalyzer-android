package com.pose.poseanalyzer.data

import android.content.Context
import android.graphics.Bitmap
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.pose.poseanalyzer.data.room.AppDatabase
import com.pose.poseanalyzer.domain.model.AsymmetryResult
import com.pose.poseanalyzer.domain.model.JointName
import com.pose.poseanalyzer.domain.model.Point2D
import com.pose.poseanalyzer.domain.model.PoseFrame
import com.pose.poseanalyzer.domain.model.PostureResult
import com.pose.poseanalyzer.domain.model.PostureStatus
import com.pose.poseanalyzer.domain.model.PostureType
import com.pose.poseanalyzer.domain.model.SessionReport
import com.pose.poseanalyzer.domain.model.SessionView
import com.pose.poseanalyzer.domain.model.Thresholds
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.UUID

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class SessionRepositoryTest {

    private lateinit var db: AppDatabase
    private lateinit var imageStore: ImageStore
    private lateinit var repo: SessionRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        imageStore = ImageStore(context)
        repo = SessionRepository(db.sessionDao(), imageStore)
    }

    @After
    fun teardown() {
        db.close()
    }

    private fun makeReport(id: UUID = UUID.randomUUID(), measuredAt: Long = System.currentTimeMillis()): SessionReport {
        val front = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        val side = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        val emptyFrame = PoseFrame(
            joints = mapOf(JointName.NOSE to PoseFrame.Joint(JointName.NOSE, Point2D(0.5f, 0.1f), 0.9f)),
            view = SessionView.FRONT,
            imageWidth = 100, imageHeight = 100
        )
        val thresholds = Thresholds(170.0..360.0, 160.0..170.0, Thresholds.Direction.HIGHER_IS_NORMAL)
        return SessionReport(
            id = id,
            measuredAt = measuredAt,
            frontImage = front,
            sideImage = side,
            frontFrame = emptyFrame,
            sideFrame = emptyFrame,
            postures = listOf(
                PostureResult(
                    type = PostureType.FORWARD_HEAD,
                    status = PostureStatus.NORMAL,
                    primaryMetric = 175.0,
                    primaryMetricUnit = PostureResult.MetricUnit.DEGREE,
                    thresholds = thresholds,
                    usedJointNames = emptyList(),
                    confidence = 0.9,
                    advice = null
                )
            ),
            asymmetry = AsymmetryResult(
                shoulder = AsymmetryResult.Difference(
                    cm = null, ratio = 0.01, angleDegrees = 0.3,
                    direction = AsymmetryResult.Direction.BALANCED
                ),
                hip = AsymmetryResult.Difference(
                    cm = null, ratio = 0.02, angleDegrees = 0.5,
                    direction = AsymmetryResult.Direction.LEFT_HIGHER
                )
            ),
            heightCmAtMeasure = 170.0
        )
    }

    @Test
    fun `save 후 fetchAll 1건`() = runTest {
        val report = makeReport()
        repo.save(report)
        val all = repo.fetchAll()
        assertEquals(1, all.size)
        assertEquals(report.id.toString(), all.first().session.id)
        assertEquals(1, all.first().postures.size)
    }

    @Test
    fun `save 후 fetch by id`() = runTest {
        val report = makeReport()
        repo.save(report)
        val found = repo.fetch(report.id)
        assertNotNull(found)
        assertEquals(report.id.toString(), found!!.session.id)
    }

    @Test
    fun `여러 세션 시간 역순으로 조회`() = runTest {
        repo.save(makeReport(measuredAt = 1000L))
        repo.save(makeReport(measuredAt = 3000L))
        repo.save(makeReport(measuredAt = 2000L))
        val all = repo.fetchAll()
        assertEquals(3, all.size)
        assertEquals(3000L, all[0].session.measuredAtMs)
        assertEquals(2000L, all[1].session.measuredAtMs)
        assertEquals(1000L, all[2].session.measuredAtMs)
    }

    @Test
    fun `delete 후 fetch 결과 없음`() = runTest {
        val report = makeReport()
        repo.save(report)
        repo.delete(report.id)
        assertNull(repo.fetch(report.id))
    }

    @Test
    fun `fetchLatest excludingId`() = runTest {
        val r1 = makeReport(measuredAt = 2000L)
        val r2 = makeReport(measuredAt = 3000L)
        repo.save(r1)
        repo.save(r2)
        val latest = repo.fetchLatest()
        assertEquals(r2.id.toString(), latest!!.session.id)
        val excluded = repo.fetchLatest(excludingId = r2.id)
        assertEquals(r1.id.toString(), excluded!!.session.id)
    }
}

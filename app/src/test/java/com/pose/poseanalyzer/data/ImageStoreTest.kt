package com.pose.poseanalyzer.data

import android.content.Context
import android.graphics.Bitmap
import androidx.test.core.app.ApplicationProvider
import com.pose.poseanalyzer.domain.model.SessionView
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import java.util.UUID

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ImageStoreTest {

    private lateinit var context: Context
    private lateinit var store: ImageStore

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        store = ImageStore(context)
    }

    @Test
    fun `save 후 load 가능`() {
        val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        val sessionId = UUID.randomUUID()
        val path = store.save(bitmap, sessionId, SessionView.FRONT)
        assertTrue(path.startsWith("sessions/$sessionId/"))
        assertTrue(path.endsWith("front.jpg"))

        val file = File(context.filesDir, path)
        assertTrue(file.exists())

        val loaded = store.load(path)
        assertNotNull(loaded)
    }

    @Test
    fun `delete 시 세션 폴더 제거`() {
        val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        val sessionId = UUID.randomUUID()
        store.save(bitmap, sessionId, SessionView.FRONT)
        store.save(bitmap, sessionId, SessionView.SIDE)
        store.delete(sessionId)
        val dir = File(context.filesDir, "sessions/$sessionId")
        assertTrue(!dir.exists())
    }

    @Test
    fun `존재하지 않는 path load null`() {
        assertNull(store.load("sessions/nonexistent/front.jpg"))
    }

    @Test
    fun `큰 이미지 다운스케일 후 저장`() {
        val bitmap = Bitmap.createBitmap(2000, 3000, Bitmap.Config.ARGB_8888)
        val path = store.save(bitmap, UUID.randomUUID(), SessionView.FRONT)
        val loaded = store.load(path)
        assertNotNull(loaded)
        assertTrue("긴 변이 1024 이하여야 함",
            maxOf(loaded!!.width, loaded.height) <= 1024)
    }
}

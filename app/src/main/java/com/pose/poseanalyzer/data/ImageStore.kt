package com.pose.poseanalyzer.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.pose.poseanalyzer.domain.model.SessionView
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 사진 파일을 앱 internal storage에 저장·로드·삭제.
 *
 * iOS `ImageStore` 1:1 대응 (Documents → Android `filesDir`).
 *
 * 경로 구조: `{filesDir}/sessions/{UUID}/{front|side}.jpg`
 * 저장 시 긴 변이 [MAX_DIMENSION]을 넘으면 다운샘플링.
 */
@Singleton
class ImageStore @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val baseDirectory: File
        get() = context.filesDir

    sealed class ImageStoreException(message: String, cause: Throwable? = null) : Exception(message, cause) {
        class EncodingFailed(detail: String) : ImageStoreException("이미지 인코딩 실패: $detail")
        class WriteFailed(detail: String, cause: Throwable) : ImageStoreException("이미지 저장 실패: $detail", cause)
    }

    /**
     * 사진 저장 후 상대 경로 반환 ([baseDirectory] 기준).
     */
    fun save(bitmap: Bitmap, sessionId: UUID, view: SessionView): String {
        val sessionDir = File(baseDirectory, "sessions/$sessionId")
        if (!sessionDir.exists() && !sessionDir.mkdirs()) {
            throw ImageStoreException.EncodingFailed("디렉토리 생성 실패")
        }

        val downsized = bitmap.downscaled(MAX_DIMENSION)
        val filename = "${view.name.lowercase()}.jpg"
        val file = File(sessionDir, filename)

        try {
            FileOutputStream(file).use { out ->
                if (!downsized.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, out)) {
                    throw ImageStoreException.EncodingFailed("compress 반환 false")
                }
            }
        } catch (e: Exception) {
            if (e is ImageStoreException) throw e
            throw ImageStoreException.WriteFailed(file.path, e)
        }

        return "sessions/$sessionId/$filename"
    }

    /** 상대 경로로 사진 로드. 없거나 실패 시 null. */
    fun load(path: String): Bitmap? {
        val file = File(baseDirectory, path)
        if (!file.exists()) return null
        return BitmapFactory.decodeFile(file.absolutePath)
    }

    /** 세션 폴더 전체 삭제 (없으면 무시) */
    fun delete(sessionId: UUID) {
        val sessionDir = File(baseDirectory, "sessions/$sessionId")
        if (sessionDir.exists()) sessionDir.deleteRecursively()
    }

    private fun Bitmap.downscaled(maxDimension: Int): Bitmap {
        val longest = maxOf(width, height)
        if (longest <= maxDimension) return this
        val scale = maxDimension.toFloat() / longest.toFloat()
        val newW = (width * scale).toInt()
        val newH = (height * scale).toInt()
        return Bitmap.createScaledBitmap(this, newW, newH, true)
    }

    companion object {
        private const val MAX_DIMENSION = 1024
        private const val JPEG_QUALITY = 85
    }
}

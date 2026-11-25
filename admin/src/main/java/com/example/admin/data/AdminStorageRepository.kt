package com.example.admin.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.OpenableColumns
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageException
import com.google.firebase.storage.StorageMetadata
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import kotlin.math.max
import kotlin.math.roundToInt

data class UploadedImageAssets(
    val mediaUrl: String,
    val thumbnailUrl: String,
    val storagePath: String,
    val thumbnailStoragePath: String,
    val resolutionLabel: String,
    val fileSizeBytes: Long
)

data class UploadedVideoAssets(
    val mediaUrl: String,
    val thumbnailUrl: String,
    val storagePath: String,
    val thumbnailStoragePath: String,
    val resolutionLabel: String,
    val fileSizeBytes: Long
)

class AdminStorageRepository(private val context: Context) {

    private val storage: FirebaseStorage by lazy { FirebaseStorage.getInstance() }

    suspend fun uploadImageWallpaper(
        wallpaperId: String,
        sourceUri: Uri,
        onProgress: (Float) -> Unit
    ): Result<UploadedImageAssets> = withContext(Dispatchers.IO) {
        try {
            val rawBytes = context.contentResolver.openInputStream(sourceUri)?.use { it.readBytes() }
                ?: return@withContext Result.failure(IllegalStateException("Unable to read selected image."))

            if (rawBytes.size > 25 * 1024 * 1024) {
                return@withContext Result.failure(IllegalStateException("Image exceeds 25 MB limit."))
            }

            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeByteArray(rawBytes, 0, rawBytes.size, bounds)
            val width = bounds.outWidth.coerceAtLeast(1080)
            val height = bounds.outHeight.coerceAtLeast(1920)
            val resLabel = when {
                max(width, height) >= 3840 -> "4K Ultra HD (${width}x$height)"
                max(width, height) >= 2160 -> "2K QHD (${width}x$height)"
                else -> "HD (${width}x$height)"
            }

            val thumbBytes = createThumbnailJpeg(rawBytes)
            val imagePath = "wallpapers/$wallpaperId/image_${System.currentTimeMillis()}.jpg"
            val thumbPath = "wallpapers/$wallpaperId/thumb_${System.currentTimeMillis()}.jpg"

            val mimeType = context.contentResolver.getType(sourceUri) ?: "image/jpeg"
            val imageRef = storage.reference.child(imagePath)
            val imageMeta = StorageMetadata.Builder().setContentType(mimeType).build()
            val uploadTask = imageRef.putBytes(rawBytes, imageMeta)
            uploadTask.addOnProgressListener { snap ->
                if (snap.totalByteCount > 0) {
                    onProgress((snap.bytesTransferred.toFloat() / snap.totalByteCount.toFloat()) * 0.85f)
                }
            }
            uploadTask.await()
            val mediaUrl = imageRef.downloadUrl.await().toString()

            val thumbRef = storage.reference.child(thumbPath)
            val thumbMeta = StorageMetadata.Builder().setContentType("image/jpeg").build()
            thumbRef.putBytes(thumbBytes, thumbMeta).await()
            val thumbnailUrl = thumbRef.downloadUrl.await().toString()
            onProgress(1f)

            Result.success(
                UploadedImageAssets(
                    mediaUrl = mediaUrl,
                    thumbnailUrl = thumbnailUrl,
                    storagePath = imagePath,
                    thumbnailStoragePath = thumbPath,
                    resolutionLabel = resLabel,
                    fileSizeBytes = rawBytes.size.toLong()
                )
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(mapStorageError(e))
        }
    }

    suspend fun extractVideoInfoAndThumbnail(sourceUri: Uri): Result<Pair<Bitmap?, String>> =
        withContext(Dispatchers.IO) {
            val retriever = MediaMetadataRetriever()
            try {
                retriever.setDataSource(context, sourceUri)
                val frame = retriever.getFrameAtTime(500_000L, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                    ?: retriever.frameAtTime
                val widthStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
                val heightStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
                val durationMs = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                    ?.toLongOrNull() ?: 0L
                val resLabel = buildString {
                    if (!widthStr.isNullOrBlank() && !heightStr.isNullOrBlank()) {
                        append("${widthStr}x${heightStr} MP4")
                    } else {
                        append("MP4 Live Loop")
                    }
                    if (durationMs > 0L) {
                        append(" · ${(durationMs / 1000L).coerceAtLeast(1L)}s")
                    }
                }
                Result.success(frame to resLabel)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Result.failure(IllegalStateException("Unable to inspect selected MP4 video: ${e.localizedMessage}"))
            } finally {
                runCatching { retriever.release() }
            }
        }

    suspend fun uploadLiveVideoWallpaper(
        wallpaperId: String,
        sourceUri: Uri,
        onProgress: (Float) -> Unit
    ): Result<UploadedVideoAssets> = withContext(Dispatchers.IO) {
        try {
            val fileSize = queryUriSize(sourceUri)
            if (fileSize > 75L * 1024L * 1024L) {
                return@withContext Result.failure(IllegalStateException("Video exceeds 75 MB limit."))
            }

            val (thumbBitmap, resLabel) = extractVideoInfoAndThumbnail(sourceUri).getOrThrow()
            val thumbBytes = if (thumbBitmap != null) {
                val baos = ByteArrayOutputStream()
                val scaled = scaleBitmapMax(thumbBitmap, 640)
                scaled.compress(Bitmap.CompressFormat.JPEG, 82, baos)
                baos.toByteArray()
            } else {
                createFallbackDarkThumb()
            }

            val videoPath = "live_wallpapers/$wallpaperId/video_${System.currentTimeMillis()}.mp4"
            val thumbPath = "live_wallpapers/$wallpaperId/thumb_${System.currentTimeMillis()}.jpg"

            val videoRef = storage.reference.child(videoPath)
            val videoMeta = StorageMetadata.Builder().setContentType("video/mp4").build()
            val uploadTask = videoRef.putFile(sourceUri, videoMeta)
            uploadTask.addOnProgressListener { snap ->
                if (snap.totalByteCount > 0) {
                    onProgress((snap.bytesTransferred.toFloat() / snap.totalByteCount.toFloat()) * 0.85f)
                }
            }
            uploadTask.await()
            val mediaUrl = videoRef.downloadUrl.await().toString()

            val thumbRef = storage.reference.child(thumbPath)
            val thumbMeta = StorageMetadata.Builder().setContentType("image/jpeg").build()
            thumbRef.putBytes(thumbBytes, thumbMeta).await()
            val thumbnailUrl = thumbRef.downloadUrl.await().toString()
            onProgress(1f)

            Result.success(
                UploadedVideoAssets(
                    mediaUrl = mediaUrl,
                    thumbnailUrl = thumbnailUrl,
                    storagePath = videoPath,
                    thumbnailStoragePath = thumbPath,
                    resolutionLabel = resLabel,
                    fileSizeBytes = fileSize
                )
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(mapStorageError(e))
        }
    }

    suspend fun deleteStorageObjects(paths: List<String>) = withContext(Dispatchers.IO) {
        paths.filter { it.isNotBlank() }.forEach { path ->
            runCatching { storage.reference.child(path).delete().await() }
        }
    }

    private fun queryUriSize(uri: Uri): Long {
        context.contentResolver.query(uri, arrayOf(OpenableColumns.SIZE), null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val idx = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (idx >= 0 && !cursor.isNull(idx)) return cursor.getLong(idx)
            }
        }
        return context.contentResolver.openAssetFileDescriptor(uri, "r")?.use { it.length.coerceAtLeast(0L) } ?: 0L
    }

    private fun createThumbnailJpeg(rawBytes: ByteArray): ByteArray {
        val opts = BitmapFactory.Options().apply { inSampleSize = 2 }
        val decoded = BitmapFactory.decodeByteArray(rawBytes, 0, rawBytes.size, opts)
            ?: return rawBytes.take(150_000).toByteArray()
        val scaled = scaleBitmapMax(decoded, 640)
        val baos = ByteArrayOutputStream()
        scaled.compress(Bitmap.CompressFormat.JPEG, 82, baos)
        return baos.toByteArray()
    }

    private fun scaleBitmapMax(bitmap: Bitmap, maxDim: Int): Bitmap {
        val ratio = maxDim.toFloat() / max(bitmap.width, bitmap.height).toFloat()
        if (ratio >= 1f) return bitmap
        val w = (bitmap.width * ratio).roundToInt().coerceAtLeast(1)
        val h = (bitmap.height * ratio).roundToInt().coerceAtLeast(1)
        return Bitmap.createScaledBitmap(bitmap, w, h, true)
    }

    private fun createFallbackDarkThumb(): ByteArray {
        val bmp = Bitmap.createBitmap(320, 560, Bitmap.Config.ARGB_8888)
        bmp.eraseColor(0xFF10152B.toInt())
        val baos = ByteArrayOutputStream()
        bmp.compress(Bitmap.CompressFormat.JPEG, 80, baos)
        return baos.toByteArray()
    }

    private fun mapStorageError(e: Exception): Exception {
        if (e is StorageException) {
            val detail = when (e.errorCode) {
                StorageException.ERROR_BUCKET_NOT_FOUND,
                StorageException.ERROR_OBJECT_NOT_FOUND ->
                    "Firebase Storage bucket is not initialized yet. Enable Storage in Firebase Console and deploy storage.rules."
                StorageException.ERROR_NOT_AUTHORIZED ->
                    "Storage permission denied. Deploy storage.rules in Firebase Console -> Storage -> Rules."
                else -> e.localizedMessage ?: "Firebase Storage error."
            }
            return IllegalStateException(detail, e)
        }
        return IllegalStateException(e.localizedMessage ?: "Upload failed.", e)
    }
}

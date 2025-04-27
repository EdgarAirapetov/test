package com.numplates.nomera3.presentation.utils

import android.content.Context
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import androidx.exifinterface.media.ExifInterface
import com.meera.application_api.media.MediaFileMetaDataDelegate
import com.meera.application_api.media.model.ImageMetadataModel
import com.meera.application_api.media.model.VideoMetadataModel
import com.meera.core.utils.files.FileManager
import com.meera.core.utils.files.FileUtilsImpl.Companion.MEDIA_TYPE_IMAGE_GIF
import timber.log.Timber
import javax.inject.Inject

class MediaFileMetaDataDelegateImpl @Inject constructor(
    val context: Context?,
    private val fileManager: FileManager
) : MediaFileMetaDataDelegate {
    override fun getImageMetadata(path: String): ImageMetadataModel? {
        val options = getBitmapOptions(path)
        return if (options != null) {
            if (isGifImage(path)) {
                ImageMetadataModel(
                    width = options.outWidth,
                    height = options.outHeight,
                    isRotated = false
                )
            } else {
                val exif = getExif(path)
                if (exif != null) {
                    val rotationDegrees = exif.getRotation()
                    val isRotated = isRotated(rotationDegrees)
                    val width = if (isRotated) options.outHeight else options.outWidth
                    val height = if (isRotated) options.outWidth else options.outHeight
                    ImageMetadataModel(
                        width = width,
                        height = height,
                        isRotated = isRotated,
                        rotationDegrees = rotationDegrees.toFloat()
                    )
                } else {
                    null
                }
            }
        } else {
            null
        }
    }

    override fun getVideoMetadata(uri: Uri): VideoMetadataModel? {
        return try {
            MediaMetadataRetriever().useSafely { retriever ->
                retriever.setDataSource(context, uri)
                val rotation = retriever.getRotation()
                val isRotated = isRotated(rotation)
                val width = if (isRotated) retriever.getHeight() else retriever.getWidth()
                val height = if (isRotated) retriever.getWidth() else retriever.getHeight()
                if (width != null && height != null) {
                    VideoMetadataModel(
                        width = width,
                        height = height,
                        isRotated = isRotated,
                        bitrate = retriever.getBitrate(),
                        duration = retriever.getDuration()
                    )
                } else {
                    null
                }
            }
        } catch (ex: Exception) {
            Timber.d(ex)
            null
        }
    }

    override fun isGifImage(path: String) =
        fileManager.getMediaType(Uri.parse(path)) == MEDIA_TYPE_IMAGE_GIF

    private fun <R> MediaMetadataRetriever.useSafely(block: (MediaMetadataRetriever) -> R): R {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            use(block)
        } else {
            try {
                block(this)
            } finally {
                release()
            }
        }
    }

    private fun MediaMetadataRetriever.getBitrate() = extractMetadata(
        MediaMetadataRetriever.METADATA_KEY_BITRATE
    )?.toInt() ?: 0

    private fun MediaMetadataRetriever.getDuration() = extractMetadata(
        MediaMetadataRetriever.METADATA_KEY_DURATION
    )?.toLong() ?: 0

    private fun MediaMetadataRetriever.getRotation() = extractMetadata(
        MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION
    )?.toInt() ?: 0

    private fun MediaMetadataRetriever.getHeight() = extractMetadata(
        MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT
    )?.toInt()

    private fun MediaMetadataRetriever.getWidth() = extractMetadata(
        MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH
    )?.toInt()

    private fun getBitmapOptions(path: String): BitmapFactory.Options? {
        return Uri.parse(path).path?.let { parsedPath ->
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            BitmapFactory.decodeFile(parsedPath, options)
            options
        }
    }

    private fun getExif(path: String): ExifInterface? {
        return try {
            ExifInterface(path)
        } catch (ex: Exception) {
            Timber.d(ex)
            null
        }
    }

    private fun isRotated(rotation: Int): Boolean {
        return rotation == 90 || rotation == 270
    }

    private fun ExifInterface.getRotation(): Int {
        return when (getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL
        )) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90
            ExifInterface.ORIENTATION_ROTATE_270 -> 270
            else -> 0
        }
    }
}

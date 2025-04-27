package com.numplates.nomera3.modules.upload.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import com.meera.application_api.media.model.ImageMetadataModel
import com.meera.core.utils.files.FileManager
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import kotlin.coroutines.suspendCoroutine

suspend fun compressImage(
    cacheDir: File,
    source: Uri,
    fileManager: FileManager,
    imageMetadataModel: ImageMetadataModel?,
    quality: Int,
    maxWidth: Int
): String {
    return suspendCoroutine { cont ->
        val file: File? = fileManager.duplicateTo(source, cacheDir, 0)
        val compressed: File? = fileManager.duplicateTo(source, cacheDir)
        if (compressed != null && file != null) {
            var bitmap = BitmapFactory.decodeFile(compressed.path)

            val wasScaled = bitmap.width.toFloat() > maxWidth

            bitmap = getScaledImageBitmap(bitmap, imageMetadataModel, maxWidth)
            compressBitmap(
                bitmap = bitmap,
                quality = quality,
                outputStream = FileOutputStream(compressed)
            )

            val path: String
            val pathToDelete: String
            if (!wasScaled && file.size < compressed.size) {
                path = file.path
                pathToDelete = compressed.path
            } else {
                path = compressed.path
                pathToDelete = file.path
            }

            runCatching { fileManager.deleteFile(pathToDelete) }
            cont.resumeWith(Result.success(path))
        } else {
            cont.resumeWith(Result.failure(Exception("File does not exist")))
        }
    }
}

val File.size get() = if (!exists()) 0.0 else length().toDouble()
//val File.sizeInKb get() = size / 1024

private fun getScaledImageBitmap(
    bitmap: Bitmap,
    imageMetadataModel: ImageMetadataModel?,
    maxWidth: Int
): Bitmap {
    var imageWidth = imageMetadataModel?.width?.toFloat() ?: bitmap.width.toFloat()
    var imageHeight = imageMetadataModel?.height?.toFloat() ?: bitmap.height.toFloat()
    val imageRatio = imageWidth / imageHeight

    when {
        imageWidth > maxWidth -> {
            imageWidth = maxWidth.toFloat()
            imageHeight = maxWidth / imageRatio
        }
    }

    return if (imageMetadataModel?.isRotated == true) {
        val matrix = Matrix()
        matrix.postRotate(imageMetadataModel.rotationDegrees)
        return Bitmap.createBitmap(
            bitmap, 0, 0,
            bitmap.width,
            bitmap.height,
            matrix,
            true
        )
    } else {
        Bitmap.createScaledBitmap(
            bitmap,
            imageWidth.toInt(),
            imageHeight.toInt(),
            true
        )
    }
}

private fun compressBitmap(
    bitmap: Bitmap,
    quality: Int,
    outputStream: OutputStream?
) {
    if (outputStream == null) return
    val bytes = ByteArrayOutputStream()
    try {
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, bytes)
        val bitmapData = bytes.toByteArray()
        outputStream.write(bitmapData)
    } catch (e: Exception) {
        e.printStackTrace()
    } finally {
        bytes.flush()
        bytes.close()
        outputStream.flush()
        outputStream.close()
    }
}

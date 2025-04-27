package com.meera.core.utils.imagecompressor

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import java.io.File
import java.io.FileOutputStream
import kotlin.math.min

/**
 * Created on : January 24, 2020
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
private val separator = File.separator

private fun cachePath(context: Context) = "${context.cacheDir.path}${separator}compressor$separator"
private fun cachePath(context: Context, folderName:String) = "${context.cacheDir.path}${separator}${folderName}$separator"

fun File.compressFormat() = when (extension.lowercase()) {
    "png" -> Bitmap.CompressFormat.PNG
    "webp" -> Bitmap.CompressFormat.WEBP
    else -> Bitmap.CompressFormat.JPEG
}

fun Bitmap.CompressFormat.extension() = when (this) {
    Bitmap.CompressFormat.PNG -> "png"
    Bitmap.CompressFormat.WEBP -> "webp"
    else -> "jpg"
}

fun loadBitmap(imageFile: File) = BitmapFactory.decodeFile(imageFile.absolutePath).run {
    determineImageRotation(imageFile, this)
}

fun decodeSampledBitmapFromFile(imageFile: File, reqWidth: Int, reqHeight: Int): Bitmap {
    return BitmapFactory.Options().run {
        inJustDecodeBounds = true
        BitmapFactory.decodeFile(imageFile.absolutePath, this)

        inSampleSize = calculateInSampleSize(this, reqWidth, reqHeight)

        inJustDecodeBounds = false
        BitmapFactory.decodeFile(imageFile.absolutePath, this)
    }
}

fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
    // Raw height and width of image
    val (height: Int, width: Int) = options.run { outHeight to outWidth }
    var inSampleSize = 1

    if (height > reqHeight || width > reqWidth) {

        val halfHeight: Int = height / 2
        val halfWidth: Int = width / 2

        // Calculate the largest inSampleSize value that is a power of 2 and keeps both
        // height and width larger than the requested height and width.
        while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
            inSampleSize *= 2
        }
    }

    return inSampleSize
}

fun resizeImage(minSize: Int, bitmap: Bitmap): Bitmap {
    if (min(bitmap.width, bitmap.height) <= minSize) return bitmap

    val (newWidth, newHeight) = if (bitmap.width < bitmap.height) {
        val scale = minSize / bitmap.width.toFloat()
        val evenHeight = (bitmap.height * scale).toInt() / 2 * 2
        minSize to evenHeight
    } else {
        val scale = minSize / bitmap.height.toFloat()
        val evenWidth = (bitmap.width * scale).toInt() / 2 * 2
        evenWidth to minSize
    }

    return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, false).also {
        bitmap.recycle()
    }
}

fun determineImageRotation(imageFile: File, bitmap: Bitmap): Bitmap {
    val exif = ExifInterface(imageFile.absolutePath)
    val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0)
    val matrix = Matrix()
    when (orientation) {
        6 -> matrix.postRotate(90f)
        3 -> matrix.postRotate(180f)
        8 -> matrix.postRotate(270f)
        else -> return bitmap
    }
    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
}

internal fun copyToCache(context: Context, file: File): File {
    return file.copyTo(File("${cachePath(context)}${file.name}"), true)
}

internal fun copyToCache(context: Context, file: File, folderName: String): File {
    return file.copyTo(File("${cachePath(context, folderName)}${file.name}"), true)
}

fun overWrite(imageFile: File, bitmap: Bitmap, format: Bitmap.CompressFormat = imageFile.compressFormat(), quality: Int = 100): File {
    val result = if (format == imageFile.compressFormat()) {
        imageFile
    } else {
        File("${imageFile.absolutePath.substringBeforeLast(".")}.${format.extension()}")
    }
    imageFile.delete()
    saveBitmap(bitmap, result, format, quality)
    return result
}

fun saveBitmap(bitmap: Bitmap, destination: File, format: Bitmap.CompressFormat = destination.compressFormat(), quality: Int = 100) {
    destination.parentFile?.mkdirs()
    var fileOutputStream: FileOutputStream? = null
    try {
        fileOutputStream = FileOutputStream(destination.absolutePath)
        bitmap.compress(format, quality, fileOutputStream)
    } finally {
        fileOutputStream?.run {
            flush()
            close()
        }
    }
}

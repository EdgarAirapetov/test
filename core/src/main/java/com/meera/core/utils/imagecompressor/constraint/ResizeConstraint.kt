package com.meera.core.utils.imagecompressor.constraint

import android.graphics.Bitmap
import com.meera.core.utils.imagecompressor.decodeSampledBitmapFromFile
import com.meera.core.utils.imagecompressor.determineImageRotation
import com.meera.core.utils.imagecompressor.overWrite
import com.meera.core.utils.imagecompressor.resizeImage
import java.io.File

class ResizeConstraint(
    private val minSize: Int = 1500,
    private val format: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG,
    private val quality: Int = 100
) : Constraint {
    private var isResolved = false

    override fun isSatisfied(imageFile: File): Boolean {
        return isResolved
    }

    override fun satisfy(imageFile: File): File {
        val result = decodeSampledBitmapFromFile(imageFile, minSize, minSize).run {
            resizeImage(minSize, this).run {
                determineImageRotation(imageFile, this).run {
                    overWrite(imageFile, this, format, quality)
                }
            }
        }
        isResolved = true
        return result
    }
}

fun Compression.editorResize(
    minSize: Int = 1500,
    format: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG,
    quality: Int = 100
) {
    constraint(ResizeConstraint(minSize, format, quality))
}

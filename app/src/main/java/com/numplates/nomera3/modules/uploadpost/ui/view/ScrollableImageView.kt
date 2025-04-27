package com.numplates.nomera3.modules.uploadpost.ui.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import com.numplates.nomera3.modules.feed.ui.entity.MediaPositioning

private const val DEFAULT_SHIFT: Float = 0.5f
private const val RELATIVE_SHIFT_DIFF: Float = 0.5f

class ScrollableImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : AppCompatImageView(context, attrs, defStyle) {

    init {
        scaleType = ScaleType.MATRIX
    }

    private var currentShiftY: Float = DEFAULT_SHIFT
    private var currentShiftX: Float = DEFAULT_SHIFT
    private var xScale: Float = 1f
    private var yScale: Float = 1f
    private var lastDx: Float = 0.0f
    private var lastDy: Float = 0.0f
    private var mediaPositioning: MediaPositioning? = null
    private var onImageSet: () -> Unit = {}
    private var xPositioning: Boolean = false
    private var isGifMode = false
    private var isEditMode: Boolean = false
    private var isNeedToFitHorizontal: Boolean = true
    private var id: String? = null

    fun setGifMode() {
        isGifMode = true
    }

    fun setDefaultMode() {
        isGifMode = false
    }

    fun setXPositioning() {
        xPositioning = true
    }

    fun setDefaultPositioning() {
        xPositioning = false
    }

    fun bind(
        onImageSet: () -> Unit = {},
        isEditMode: Boolean = false,
        isNeedToFitHorizontal: Boolean = true,
        id: String? = ""
    ) {
        this.onImageSet = onImageSet
        this.isEditMode = isEditMode
        this.isNeedToFitHorizontal = isNeedToFitHorizontal
        this.id = id
    }

    fun moveByY(distance: Float) {
        if (drawable == null) return

        val matrix = imageMatrix

        lastDy -= distance
        val totalHeight = height - drawable.intrinsicHeight * yScale

        if (lastDy < 0 && lastDy < totalHeight) {
            lastDy = totalHeight
        } else if (lastDy > 0) {
            lastDy = 0f
        }

        matrix.setScale(xScale, yScale)
        matrix.postTranslate(0f, lastDy)

        var shift = lastDy / totalHeight
        if (shift > 1f) {
            shift = 1f
        }
        if (shift < 0f) {
            shift = 0f
        }
        currentShiftY = shift

        imageMatrix = matrix
        invalidate()
    }

    fun moveByX(distance: Float) {
        if (drawable == null) return

        val matrix = imageMatrix

        lastDx -= distance
        val totalWidth = width - drawable.intrinsicWidth * xScale

        if (lastDx < 0 && lastDx < totalWidth) {
            lastDx = totalWidth
        } else if (lastDx > 0) {
            lastDx = 0f
        }

        matrix.setScale(xScale, yScale)
        matrix.postTranslate(lastDx, 0f)

        var shift = lastDx / totalWidth
        if (shift > 1f) {
            shift = 1f
        }
        if (shift < 0f) {
            shift = 0f
        }
        currentShiftX = shift

        imageMatrix = matrix
        invalidate()
    }

    private fun setBoundsForEditMode(shiftY: Float, shiftX: Float) {
        if (drawable == null) return

        val matrix = imageMatrix
        currentShiftY = shiftY
        currentShiftX = shiftX


        var dy = 0f
        var dx = 0f

        if (xPositioning) {
            xScale = height.toFloat() / drawable.intrinsicHeight.toFloat()
            dx = (width - drawable.intrinsicWidth * xScale) * shiftX
            yScale = 1f

        } else {
            yScale = width.toFloat() / drawable.intrinsicWidth.toFloat()
            dy = (height - drawable.intrinsicHeight * yScale) * shiftY
            xScale = 1f

        }

        if (isGifMode) {
            if(xPositioning) {
                yScale = xScale
            } else {
                xScale = yScale
            }
        }

        matrix.setScale(xScale, yScale)
        matrix.postTranslate(dx, dy)

        lastDy = dy
        lastDx = dx

        imageMatrix = matrix
        invalidate()

        onImageSet.invoke()
    }

    private fun setBoundsForViewMode(shiftY: Float, shiftX: Float) {
        if (drawable == null) return

        val matrix = imageMatrix
        currentShiftY = shiftY
        currentShiftX = shiftX

        val isImageHorizontal = drawable.intrinsicWidth >= drawable.intrinsicHeight
        val attachmentNotFitByWidth = drawable.intrinsicHeight * width <= height * drawable.intrinsicWidth

        var dy = 0f
        var dx = 0f

        if ((isImageHorizontal || isNeedToFitHorizontal) && attachmentNotFitByWidth) {
            xScale = height.toFloat() / drawable.intrinsicHeight.toFloat()
            yScale = 1f
            dx = (width - drawable.intrinsicWidth * xScale) * shiftX
        } else {
            yScale = width.toFloat() / drawable.intrinsicWidth.toFloat()
            xScale = 1f
            dy = (height - drawable.intrinsicHeight * yScale) * shiftY
        }

        matrix.setScale(xScale, yScale)
        matrix.postTranslate(dx, dy)

        lastDy = dy
        lastDx = dx

        imageMatrix = matrix
        invalidate()

        onImageSet.invoke()
    }

    fun setMediaPositioning(mediaPositioning: MediaPositioning?) {
        this.mediaPositioning = mediaPositioning
    }

    override fun setImageBitmap(bm: Bitmap?) {
        super.setImageBitmap(bm)
        setImagePosition()
    }

    override fun setImageDrawable(drawable: Drawable?) {
        super.setImageDrawable(drawable)
        setImagePosition()
    }

    private fun setImagePosition() {
        val mediaPositioning = mediaPositioning
        val shiftY = if (mediaPositioning != null) mediaPositioning.y + RELATIVE_SHIFT_DIFF else DEFAULT_SHIFT
        val shiftX = if (mediaPositioning != null) mediaPositioning.x + RELATIVE_SHIFT_DIFF else DEFAULT_SHIFT

        post {
            if (isEditMode) {
                setBoundsForEditMode(shiftY = shiftY.toFloat(), shiftX = shiftX.toFloat())
            } else {
                setBoundsForViewMode(shiftY = shiftY.toFloat(), shiftX = shiftX.toFloat())
            }
        }
    }

    fun getRelativeImageYPosition(): Double {
        return (currentShiftY - RELATIVE_SHIFT_DIFF).toDouble()
    }

    fun getRelativeImageXPosition(): Double {
        return (currentShiftX - RELATIVE_SHIFT_DIFF).toDouble()
    }
}

package com.numplates.nomera3.modules.chat.views

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.Rect
import android.util.AttributeSet
import androidx.core.content.ContextCompat
import com.meera.core.extensions.dpToPx
import com.numplates.nomera3.R
import com.numplates.nomera3.presentation.view.widgets.DetectorSeekBar
import timber.log.Timber

private const val BARS_COLUMN_WIDTH_DP = 4
private const val BARS_HEIGHT_DP = 16
private const val BAR_RADIUS_DP = 2

// TODO: https://nomera.atlassian.net/browse/BR-30771
private const val UIKIT_COLOR_FOREGROUND_PRIMARY_SEMI_TRANSPARENT = "#75262626"


class VoiceMessagePreviewBarsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : DetectorSeekBar(context, attrs, defStyleAttr) {

    private var isInitBars: Boolean = false
    private var listAmplitudes = mutableListOf<Int>()
    private var maxValue = 0f
    private var minValue = 0f
    private var columnWidth = 0f
    private var barWidth = 0f
    private var localWidth = 1
    private var localHeight = 1
    private var baseY: Int = 0
    private var _canvasBitmap: Bitmap? = null
    private var _canvas: Canvas? = null
    private val _paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val radius = dpToPx(BAR_RADIUS_DP).toFloat()
    private val rect = Rect()
    private val barProgressColor = ContextCompat.getColor(context, R.color.uiKitColorForegroundPrimary)
    private val barOriginColor = Color.parseColor(UIKIT_COLOR_FOREGROUND_PRIMARY_SEMI_TRANSPARENT)

    fun showBars(listAmplitudes: List<Int>) {
        try {
            this.listAmplitudes.clear()
            this.listAmplitudes.addAll(listAmplitudes)
            maxValue = listAmplitudes.maxOrNull()?.toFloat() ?: 0.0f
            minValue = listAmplitudes.minOrNull()?.toFloat() ?: 0.0f
            columnWidth = dpToPx(BARS_COLUMN_WIDTH_DP).toFloat()
            barWidth = columnWidth / 4f
            localWidth = ((columnWidth) * listAmplitudes.size).toInt()
            localHeight = dpToPx(BARS_HEIGHT_DP)
            baseY = localHeight / 2
            _canvasBitmap = Bitmap.createBitmap(localWidth, localHeight, Bitmap.Config.ARGB_8888)
            _canvasBitmap?.let { _canvas = Canvas(it) }
            isInitBars = true
            invalidate()
        } catch (e: Exception) {
            Timber.e("ERROR Draw amplitude bars")
        }
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (isInitBars) {
            rect.set(0, 0, localWidth, localHeight)
            _canvasBitmap?.let { bitmap -> canvas.drawBitmap(bitmap, Matrix(), null) }
            drawBars(progress)
        }
    }

    private fun drawBars(position: Int) {
        var tempHeight = ((maxValue - minValue) / localHeight)
        if (tempHeight == 0f) tempHeight = localHeight.toFloat()

        _canvas?.drawColor(0, PorterDuff.Mode.CLEAR)

        listAmplitudes.forEachIndexed { index, _ ->
            if (index < (listAmplitudes.size.toFloat() / this.max * position)) {
                _paint.color = barProgressColor
            } else {
                _paint.color = barOriginColor
            }

            var columnHeight = ((listAmplitudes[index].toFloat() / tempHeight / 2.5) + (localHeight / 20)).toFloat()
            if (columnHeight >= localHeight) columnHeight = localHeight.toFloat()
            val left = index * columnWidth + barWidth
            val right = (index + 1) * columnWidth - barWidth

            _canvas?.drawRoundRect(
                left,
                baseY - columnHeight,
                right,
                baseY + columnHeight,
                radius,
                radius,
                _paint
            )
        }
    }

}

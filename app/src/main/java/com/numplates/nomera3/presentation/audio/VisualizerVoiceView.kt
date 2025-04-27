package com.numplates.nomera3.presentation.audio

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.Rect
import android.util.AttributeSet
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.meera.core.extensions.dpToPx
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.chat.data.MAX_AMPLITUDE_VALUE
import com.numplates.nomera3.modules.chat.data.MIN_AMPLITUDE_VALUE
import com.numplates.nomera3.presentation.view.widgets.DetectorSeekBar

private const val COLUMN_WIDTH_DP = 5
private const val COLUMN_SPACE_MULTIPLIER = 4.5

@Deprecated("Deprecated in app. Use UiKitVisualizerVoiceView instead")
open class VisualizerVoiceView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : DetectorSeekBar(context, attrs, defStyleAttr) {

    private var colorFirst: Int = 0
    private var colorSecond: Int = 0

    private var mBaseY: Int = 0

    private var mCanvas: Canvas? = null
    private var mCanvasBitmap: Bitmap? = null
    private val mRect = Rect()
    private val mPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    var localWidth = 1
    var localHeight = 1

    var radius = dpToPx(2).toFloat()

    private var mColumnWidth: Float = 0.toFloat()
    private var mSpace: Float = 0.toFloat()

    private var listOfColumnsHeight: List<Float> = emptyList()

    private var maxValue = 0f
    private var minValue = 0f

    private var allowedToDraw = true
    fun init(columnsHeight: List<Number>, isIncomingMessage: Boolean) {
        allowedToDraw = true
        initColors(isIncomingMessage)
        listOfColumnsHeight = columnsHeight.map { it.toFloat() }
        maxValue = listOfColumnsHeight.maxOrNull() ?: 0f
        minValue = listOfColumnsHeight.minOrNull() ?: 0f
        validateMaxMinValues()
        mColumnWidth = dpToPx(COLUMN_WIDTH_DP).toFloat()
        mSpace = mColumnWidth / COLUMN_SPACE_MULTIPLIER.toFloat()
        localWidth = ((mColumnWidth) * (listOfColumnsHeight.size + 1)).toInt()
        localHeight = dpToPx(30)
        this.layoutParams = LinearLayout.LayoutParams(localWidth, localHeight)

        mBaseY = localHeight / 2

        mCanvasBitmap = Bitmap.createBitmap(localWidth, localHeight, Bitmap.Config.ARGB_8888)

        mCanvas = Canvas(mCanvasBitmap!!)

        invalidate()
    }

    //Старые клиенты могут прислать значение выше чем MAX_AMPLITUDE_VALUE, на новых это невозможно
    fun validateMaxMinValues() {
        if (maxValue <= MAX_AMPLITUDE_VALUE) {
            maxValue = MAX_AMPLITUDE_VALUE.toFloat()
            minValue = MIN_AMPLITUDE_VALUE.toFloat()
        }
    }

    private fun initColors(isIncomingMessage: Boolean) {
        if (isIncomingMessage) {
            colorFirst = ContextCompat.getColor(context, R.color.ui_purple)
            colorSecond = ContextCompat.getColor(context, R.color.ui_gray)
        } else {
            colorFirst = ContextCompat.getColor(context, R.color.ui_color_chat_send_grey)
            colorSecond = ContextCompat.getColor(context, R.color.ui_gray)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        mRect.set(0, 0, localWidth, localHeight)

        mCanvasBitmap?.let { bitmap -> canvas.drawBitmap(bitmap, Matrix(), null) }

        if (allowedToDraw) {
            drawBar(this.max)
            allowedToDraw = false
        }
    }

    fun drawBar(position: Int) {
        mCanvas?.drawColor(0, PorterDuff.Mode.CLEAR)

        val minColumnHeight = localHeight / 20f

        for (i in listOfColumnsHeight.indices) {

            if (i < (listOfColumnsHeight.size.toFloat() / this.max * position)) {
                mPaint.color = colorFirst
            } else {
                mPaint.color = colorSecond
            }

            val coefficient = listOfColumnsHeight[i] / maxValue
            var columnHeight = minColumnHeight + (coefficient * localHeight)

            if (columnHeight >= localHeight) columnHeight = localHeight.toFloat()

            val left = i * mColumnWidth + mSpace
            val right = (i + 1) * mColumnWidth - mSpace

            mCanvas?.drawRoundRect(left, mBaseY - (columnHeight / 2), right, mBaseY + (columnHeight / 2), radius, radius, mPaint)
        }
        invalidate()
    }

    companion object {
        fun getVoiceAmplitudes(waveForm: List<Int>): List<Int> {
            return waveForm.ifEmpty {
                val listOfColumnsHeight: ArrayList<Int> = arrayListOf()
                repeat(22) {
                    listOfColumnsHeight.add((0..20).random())
                }
                listOfColumnsHeight
            }
        }
    }

}

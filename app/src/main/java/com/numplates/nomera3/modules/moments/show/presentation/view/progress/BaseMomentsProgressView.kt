package com.numplates.nomera3.modules.moments.show.presentation.view.progress

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.content.withStyledAttributes
import com.meera.core.extensions.dpToPx
import com.numplates.nomera3.R
import kotlin.math.min

private const val LOAD_LINE_VIEW_DEFAULT_HEIGHT_DP = 18
private const val LOAD_LINE_VIEW_DEFAULT_HORIZONTAL_PADDING_DP = 11
private const val LOAD_LINE_VIEW_DEFAULT_MARGIN_BETWEEN_BARS_DP = 4
private const val LOAD_LINE_VIEW_DEFAULT_BAR_HEIGHT_DP = 2

private const val LOAD_LINE_VIEWED_ALPHA = 255
private const val LOAD_LINE_NOT_VIEWED_ALPHA = 127

abstract class BaseMomentsProgressView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {

    private val defaultViewHeight = dpToPx(LOAD_LINE_VIEW_DEFAULT_HEIGHT_DP)
    private val defaultHorizontalPadding = dpToPx(LOAD_LINE_VIEW_DEFAULT_HORIZONTAL_PADDING_DP)
    private val defaultMarginBetweenBars = dpToPx(LOAD_LINE_VIEW_DEFAULT_MARGIN_BETWEEN_BARS_DP)
    private val defaultBarHeight = dpToPx(LOAD_LINE_VIEW_DEFAULT_BAR_HEIGHT_DP)

    private val barPaint: Paint = Paint().apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeWidth = defaultBarHeight.toFloat()
        color = ContextCompat.getColor(context, R.color.white)
    }

    var progressBarCount: Int = 1
        set(value) {
            if (value != field) {
                field = value
                invalidate()
            }
        }
    protected var currentBar: Int = 0
    protected var currentBarProgress: Float = 0.0f
    private var disableDefaultPadding: Boolean = false

    init {
        context.withStyledAttributes(attrs, R.styleable.BaseMomentsProgressView) {
            progressBarCount = getInteger(R.styleable.BaseMomentsProgressView_mpvProgressBarCount, 1)
            currentBar = getInteger(R.styleable.BaseMomentsProgressView_mpvCurrentBar, 0)
            currentBarProgress = getFloat(R.styleable.BaseMomentsProgressView_mpvCurrentBarProgress, 0.0f)
            disableDefaultPadding = getBoolean(R.styleable.BaseMomentsProgressView_mpvDisableDefaultPadding, false)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthSpecSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightSpecMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSpecSize = MeasureSpec.getSize(heightMeasureSpec)

        val resolvedHeight = when (heightSpecMode) {
            MeasureSpec.EXACTLY -> heightSpecSize
            MeasureSpec.AT_MOST -> min(defaultViewHeight, heightSpecSize)
            else -> defaultViewHeight
        }

        setMeasuredDimension(
            MeasureSpec.makeMeasureSpec(widthSpecSize, MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(resolvedHeight, MeasureSpec.EXACTLY)
        )
    }

    override fun onDraw(canvas: Canvas) {
        val barWidth = calculateSingleBarWidth()
        val barCenterY = height / 2f
        val marginBetweenBars = getMarginBetweenBars()
        var offset = getStartingOffset()
        for (barIndex in 0 until progressBarCount) {
            if (barIndex == currentBar) {
                drawWithProgressBar(
                    canvas = canvas,
                    startX = offset,
                    barCenterY = barCenterY,
                    endX = offset + barWidth,
                    progress = currentBarProgress
                )
            } else {
                drawNormalBar(
                    canvas = canvas,
                    startX = offset,
                    barCenterY = barCenterY,
                    endX = offset + barWidth,
                    isWatched = barIndex < currentBar
                )
            }
            offset += barWidth + marginBetweenBars
        }
    }

    private fun drawNormalBar(
        canvas: Canvas,
        startX: Float,
        barCenterY: Float,
        endX: Float,
        isWatched: Boolean
    ) {
        barPaint.alpha = if (isWatched) LOAD_LINE_VIEWED_ALPHA else LOAD_LINE_NOT_VIEWED_ALPHA
        canvas.drawLine(startX, barCenterY, endX, barCenterY, barPaint)
    }

    private fun drawWithProgressBar(
        canvas: Canvas,
        startX: Float,
        barCenterY: Float,
        endX: Float,
        progress: Float
    ) {
        barPaint.alpha = LOAD_LINE_NOT_VIEWED_ALPHA
        canvas.drawLine(startX, barCenterY, endX, barCenterY, barPaint)
        if (progress > 0.0f) {
            barPaint.alpha = LOAD_LINE_VIEWED_ALPHA
            canvas.drawLine(
                startX,
                barCenterY,
                startX + ((endX - startX) * progress),
                barCenterY,
                barPaint
            )
        }
    }

    private fun getStartingOffset(): Float {
        return if (disableDefaultPadding) paddingStart.toFloat() else defaultHorizontalPadding.toFloat()
    }

    private fun calculateSingleBarWidth(): Float {
        val widthNoPadding = if (disableDefaultPadding) {
            width - paddingStart - paddingEnd
        } else {
            width - (defaultHorizontalPadding * 2)
        }

        return when {
            progressBarCount == 1 -> widthNoPadding.toFloat()
            progressBarCount > 1 -> {
                val widthWithNoItemMargin =
                    widthNoPadding - (defaultMarginBetweenBars * (progressBarCount - 1))
                widthWithNoItemMargin / progressBarCount.toFloat()
            }
            else -> error("Invalid bar count in MomentsProgressView = $progressBarCount")
        }
    }

    private fun getMarginBetweenBars(): Int {
        return defaultMarginBetweenBars
    }
}

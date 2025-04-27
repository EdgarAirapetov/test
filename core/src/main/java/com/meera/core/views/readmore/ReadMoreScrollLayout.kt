package com.meera.core.views.readmore

import android.content.Context
import android.util.AttributeSet
import androidx.core.content.withStyledAttributes
import androidx.core.view.children
import androidx.core.view.marginBottom
import androidx.core.view.marginTop
import androidx.core.widget.NestedScrollView
import com.meera.core.R
import kotlin.math.min

private const val SCROLL_LAYOUT_MAX_LINES = 13

/**
 * Wrapper layout for [ReadMoreTextView] which enables scrolling behavior.
 *
 * Using this layout instead of [isVerticalScrollBarEnabled] because [ReadMoreTextView] had problems with bottom Fading Edge not working.
 */
class ReadMoreScrollLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = android.R.attr.textViewStyle
) : NestedScrollView(context, attrs, defStyleAttr) {

    private val readMoreTextView by lazy { children.filterIsInstance<ReadMoreTextView>().firstOrNull() ?: requireReadMoreTextView() }

    private var maxLinesForExpandedText = SCROLL_LAYOUT_MAX_LINES

    init {
        context.withStyledAttributes(
            attrs, R.styleable.ReadMoreScrollLayout
        ) {
            maxLinesForExpandedText = getInteger(R.styleable.ReadMoreScrollLayout_rmslExpandedMaxLines, maxLinesForExpandedText)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        measureChildWithMargins(readMoreTextView, widthMeasureSpec, 0, heightMeasureSpec, 0)
        val targetHeight = if (readMoreTextView.isExpanded()) {
            val scrollLayoutExpandedHeight = (readMoreTextView.lineHeight * maxLinesForExpandedText) +
                readMoreTextView.paddingTop + readMoreTextView.paddingBottom +
                readMoreTextView.marginTop + readMoreTextView.marginBottom
            val expandedReadMoreSize = readMoreTextView.measuredHeight
            min(scrollLayoutExpandedHeight, expandedReadMoreSize)
        } else {
            readMoreTextView.measuredHeight
        }
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), targetHeight)
    }

    fun setMaxLinesForExpandedText(linesCount: Int) {
        if (linesCount <= 0) error("ReadMoreScrollLayout. Attempting to set non-positive line count for expanded text. New line count = $linesCount")
        if (maxLinesForExpandedText == linesCount) return
        maxLinesForExpandedText = linesCount
        requestLayout()
    }

    private fun requireReadMoreTextView(): Nothing = error("ReadMoreScrollLayout must contain ReadMoreTextView. Otherwise, what's the point?")

}

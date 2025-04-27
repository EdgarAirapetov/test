package com.meera.core.views.readmore

import android.content.Context
import android.content.res.ColorStateList
import android.content.res.TypedArray
import android.graphics.Typeface
import android.os.Build
import android.text.Layout
import android.text.SpannedString
import android.text.StaticLayout
import android.text.TextPaint
import android.text.TextUtils
import android.text.style.TextAppearanceSpan
import android.util.AttributeSet
import androidx.annotation.StyleableRes
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.content.withStyledAttributes
import androidx.core.text.buildSpannedString
import androidx.core.text.inSpans
import com.meera.core.R
import com.meera.core.extensions.spToPx
import kotlin.text.Typography.ellipsis
import kotlin.text.Typography.nbsp

private const val READ_MORE_COLLAPSED_MAX_LINES = 2
private const val READ_MORE_EXPANDED_MAX_LINES = Integer.MAX_VALUE
private const val READ_MORE_DEFAULT_TEXT_SIZE_SP = 14f

class ReadMoreTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = android.R.attr.textViewStyle
) : AppCompatTextView(context, attrs, defStyleAttr) {

    private var collapsedMaxLines: Int = READ_MORE_COLLAPSED_MAX_LINES
    private var expandedMaxLines: Int = READ_MORE_EXPANDED_MAX_LINES
    private var readMoreText: CharSequence = ""
    private var readMoreTextSize: Int = spToPx(READ_MORE_DEFAULT_TEXT_SIZE_SP).toInt()
    private var readMoreTextColor: ColorStateList? = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.ui_white))
    private var readMoreTextStyle: Int = Typeface.NORMAL
    private var readMoreFontFamily: String? = null

    private var bufferType: BufferType? = null
    private var expanded: Boolean = false
    private var expandable: Boolean = true
    private var expandedText: CharSequence? = null
    private var collapsedText: CharSequence? = null

    private val expandedListeners = mutableListOf<ExpandedListener>()

    fun interface ExpandedListener {
        fun onExpandedStateChanged(isExpanded: Boolean)
    }

    init {
        context.withStyledAttributes(
            attrs, R.styleable.ReadMoreTextView
        ) {
            collapsedMaxLines = getInteger(R.styleable.ReadMoreTextView_rmCollapsedMaxLines, collapsedMaxLines)
            expandedMaxLines = getInteger(R.styleable.ReadMoreTextView_rmExpandedMaxLines, expandedMaxLines)
            readMoreText = getText(R.styleable.ReadMoreTextView_rmReadMoreText) ?: context.getString(R.string.more)

            setReadMoreIndicatorAppearanceFromTextAppearance(this)
            setReadMoreIndicatorAppearanceFromCustomAttributes(this)

            super.setOnClickListener { if (selectionStart == -1 && selectionEnd == -1) toggle() }
        }
    }

    fun toggle() {
        setExpanded(!expanded)
    }

    fun isExpanded(): Boolean {
        return this.expanded
    }

    fun setExpanded(expanded: Boolean) {
        if (expandable.not()) return
        if (this.expanded != expanded) {
            this.expanded = expanded

            invalidateText()

            expandedListeners.forEach { it.onExpandedStateChanged(expanded) }
        }
    }

    fun addExpandedListener(listener: ExpandedListener) {
        expandedListeners.add(listener)
    }

    fun removeExpandedListener(listener: ExpandedListener) {
        expandedListeners.remove(listener)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w != oldw) {
            expandedText?.let { updateCollapsedText(it, w) }
        }
    }

    override fun setText(text: CharSequence?, type: BufferType?) {
        this.expandedText = text
        this.bufferType = type
        text?.let { updateCollapsedText(text, width) }
    }

    private fun setReadMoreIndicatorAppearanceFromTextAppearance(ta: TypedArray) {
        val readMoreTextAppearance = ta.getResourceId(R.styleable.ReadMoreTextView_rmReadMoreTextAppearance, ResourcesCompat.ID_NULL)
        if (readMoreTextAppearance == ResourcesCompat.ID_NULL) return
        context.withStyledAttributes(readMoreTextAppearance, androidx.appcompat.R.styleable.TextAppearance) {
            readMoreTextSize = getDimensionPixelSize(androidx.appcompat.R.styleable.TextAppearance_android_textSize, readMoreTextSize)
            readMoreTextColor = getColorStateList(androidx.appcompat.R.styleable.TextAppearance_android_textColor) ?: readMoreTextColor
            readMoreTextStyle = getInt(androidx.appcompat.R.styleable.TextAppearance_android_textStyle, readMoreTextStyle)
            readMoreFontFamily = getString(androidx.appcompat.R.styleable.TextAppearance_android_fontFamily)
                ?: getFontFamilyFromTypeface(androidx.appcompat.R.styleable.TextAppearance_android_typeface)
        }
    }

    private fun setReadMoreIndicatorAppearanceFromCustomAttributes(ta: TypedArray) = with(ta) {
        readMoreTextSize = getDimensionPixelSize(R.styleable.ReadMoreTextView_rmTextSize, readMoreTextSize)
        readMoreTextColor = getColorStateList(R.styleable.ReadMoreTextView_rmTextColor) ?: readMoreTextColor
        readMoreTextStyle = getInt(R.styleable.ReadMoreTextView_rmTextStyle, readMoreTextStyle)
        readMoreFontFamily = getString(R.styleable.ReadMoreTextView_rmFontFamily)
            ?: getFontFamilyFromTypeface(R.styleable.ReadMoreTextView_rmTypeface) ?: readMoreFontFamily

    }

    private fun updateCollapsedText(text: CharSequence, width: Int) {
        val maximumTextWidth = width - (paddingLeft + paddingRight)
        val collapsedMaxLines = collapsedMaxLines

        if (maximumTextWidth > 0 && collapsedMaxLines > 0) {
            val layout = getStaticLayoutFor(text, maximumTextWidth)
            if (layout.lineCount <= collapsedMaxLines) {
                this.expandable = false
                this.collapsedText = text
            } else {
                this.expandable = true
                this.collapsedText = buildSpannedString {
                    val countUntilMaxLine = layout.getLineVisibleEnd(collapsedMaxLines - 1)
                    if (text.length <= countUntilMaxLine) {
                        append(text)
                    } else {
                        val truncationText = getTruncationTextFor(text)
                        val truncationTextWidth = getStaticLayoutFor(
                            text = truncationText,
                            maximumTextWidth = maximumTextWidth
                        ).getLineWidth(0).toInt()

                        val readMoreAppearanceSpan = getReadMoreAppearanceSpan()
                        val readMoreTextSpanned = getReadMoreSpannedString(readMoreAppearanceSpan)
                        val readMoreTextPaint = TextPaint().apply {
                            set(paint)
                            readMoreAppearanceSpan.updateMeasureState(this)
                        }
                        val readMoreTextWidth = getStaticLayoutFor(
                            text = readMoreTextSpanned,
                            maximumTextWidth = maximumTextWidth,
                            customPaint = readMoreTextPaint
                        ).getLineWidth(0).toInt()

                        val readMoreWidth = truncationTextWidth + readMoreTextWidth

                        val lastVisibleLine = text.substringOf(layout, line = collapsedMaxLines)
                        val sl = getStaticForLastLine(lastVisibleLine, maximumTextWidth - readMoreWidth)
                        val ellipsizedChars = sl.getEllipsisCount(0)
                        append(text.subSequence(0, countUntilMaxLine - ellipsizedChars))
                        append(truncationText)
                        append(readMoreTextSpanned)
                    }
                }
            }
        } else {
            this.collapsedText = text
        }
        invalidateText()
    }

    private fun getTruncationTextFor(text: CharSequence) = buildString {
        append(ellipsis)
        if (text.isNotBlank()) append(nbsp)
    }

    private fun getStaticLayoutFor(text: CharSequence, maximumTextWidth: Int, customPaint: TextPaint? = null): Layout {
        val alignment = Layout.Alignment.ALIGN_NORMAL
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            StaticLayout.Builder
                .obtain(text, 0, text.length, customPaint ?: paint, maximumTextWidth)
                .setAlignment(alignment)
                .setLineSpacing(lineSpacingExtra, lineSpacingMultiplier)
                .build()
        } else {
            StaticLayout(
                text, 0, text.length, customPaint ?: paint, maximumTextWidth,
                alignment,
                lineSpacingExtra, lineSpacingMultiplier,
                includeFontPadding
            )
        }
    }

    private fun getStaticForLastLine(text: CharSequence, maximumTextWidth: Int, customPaint: TextPaint? = null): StaticLayout {
        val alignment = Layout.Alignment.ALIGN_NORMAL
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            StaticLayout.Builder
                .obtain(text, 0, text.length, customPaint ?: paint, maximumTextWidth)
                .setAlignment(alignment)
                .setLineSpacing(lineSpacingExtra, lineSpacingMultiplier)
                .setMaxLines(1)
                .setEllipsize(TextUtils.TruncateAt.END)
                .build()
        } else {
            StaticLayout(
                text, 0, text.length, customPaint ?: paint, maximumTextWidth,
                alignment,
                lineSpacingExtra, lineSpacingMultiplier,
                includeFontPadding
            )
        }
    }

    private fun getReadMoreAppearanceSpan(): TextAppearanceSpan {
        return TextAppearanceSpan(readMoreFontFamily, readMoreTextStyle, readMoreTextSize, readMoreTextColor, null)
    }

    private fun getReadMoreSpannedString(appearance: TextAppearanceSpan): SpannedString = buildSpannedString {
        if (readMoreText.isNotBlank()) inSpans(appearance) { append(readMoreText) }
    }

    private fun TypedArray.getFontFamilyFromTypeface(@StyleableRes index: Int): String? {
        return when (getInt(index, 0)) {
            1 -> "sans"
            2 -> "serif"
            3 -> "monospace"
            else -> null
        }
    }

    private fun CharSequence.substringOf(layout: Layout, line: Int): CharSequence {
        val lastLineStartIndex = layout.getLineStart(line - 1)
        val lastLineEndIndex = layout.getLineEnd(line - 1)
        return subSequence(lastLineStartIndex, lastLineEndIndex)
    }

    private fun invalidateText() {
        if (expanded) {
            super.setText(expandedText, bufferType)
            super.setMaxLines(expandedMaxLines)
        } else {
            super.setText(collapsedText, bufferType)
            super.setMaxLines(collapsedMaxLines)
        }
    }

}

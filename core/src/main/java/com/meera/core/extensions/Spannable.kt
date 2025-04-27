package com.meera.core.extensions

import android.graphics.Color
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextPaint
import android.text.style.AbsoluteSizeSpan
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.view.View
import androidx.annotation.ColorInt
import com.meera.core.utils.ItalicTypefaceSpan

fun SpannableStringBuilder.color(color: String, start: Int, end: Int): SpannableStringBuilder =
    apply {
        setSpan(ForegroundColorSpan(Color.parseColor(color)), start, end, 0)
    }

fun SpannableStringBuilder.color(@ColorInt color: Int, range: IntRange): SpannableStringBuilder =
    apply {
        if (range.first < 0 || range.last > count()) return@apply
        setSpan(ForegroundColorSpan(color), range.first, range.last, 0)
    }

fun SpannableStringBuilder.bold(start: Int, end: Int): SpannableStringBuilder =
    apply {
        setSpan(StyleSpan(Typeface.BOLD), start, end, 0)
    }

fun SpannableStringBuilder.normal(range: IntRange): SpannableStringBuilder =
    apply {
        if (range.first < 0 || range.last > count()) return@apply
        setSpan(StyleSpan(Typeface.NORMAL), range.first, range.last, 0)
    }

fun SpannableStringBuilder.bold(range: IntRange): SpannableStringBuilder =
    apply {
        setSpan(StyleSpan(Typeface.BOLD), range.first, range.last, 0)
    }

fun SpannableStringBuilder.underline(start: Int, end: Int): SpannableStringBuilder =
    apply {
        setSpan(UnderlineSpan(), start, end, 0)
    }

fun SpannableStringBuilder.italic(start: Int, end: Int, typeface: Typeface? = null): SpannableStringBuilder =
    apply {
        if (start < 0 || end > count()) return@apply
        setSpan(if (typeface != null) ItalicTypefaceSpan(typeface) else StyleSpan(Typeface.ITALIC),start, end, 0)
    }

fun SpannableStringBuilder.strike(start: Int, end: Int): SpannableStringBuilder =
    apply {
        setSpan(StrikethroughSpan(), start, end, 0)
    }

fun SpannableStringBuilder.size(textSizePx: Int, start: Int, end: Int): SpannableStringBuilder =
    apply {
        if (start < 0 || end > count()) return@apply
        setSpan(AbsoluteSizeSpan(textSizePx), start, end, 0)
    }

fun SpannableStringBuilder.setSpanExclusive(what: Any, range: IntRange) {
    if (range.first < 0 || range.last > count()) return
    setSpan(what, range.first, range.last, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
}

fun <T> SpannableStringBuilder.addClickWithData(data: T, range: IntRange, @ColorInt color: Int,
                                                onClickListener: (T) -> Unit): SpannableStringBuilder =
    apply {
        if (range.first < 0 || range.last > count()) return@apply
        val clickableSpan =
            object : ClickableSpan() {
                override fun onClick(p0: View) {
                    onClickListener.invoke(data)
                }

                override fun updateDrawState(tp: TextPaint) {
                    tp.isUnderlineText = false
                }
            }

        color(color, range)
        setSpanExclusive(clickableSpan, range)
    }

fun <T> SpannableStringBuilder.addClickWithDataBold(data: T, range: IntRange,
                                                    onClickListener: (T) -> Unit): SpannableStringBuilder =
    apply {
        if (range.first < 0 || range.last > count()) return@apply
        val clickableSpan =
            object : ClickableSpan() {
                override fun onClick(p0: View) {
                    onClickListener.invoke(data)
                }

                override fun updateDrawState(tp: TextPaint) {
                    tp.isUnderlineText = false
                }
            }

        bold(range)
        setSpanExclusive(clickableSpan, range)
    }

fun SpannableStringBuilder.addClickWithDataBoldColored(
    range: IntRange, @ColorInt color: Int, onClickListener: () -> Unit): SpannableStringBuilder =
    apply {
        if (range.first < 0 || range.last > count()) return@apply
        val clickableSpan =
            object : ClickableSpan() {
                override fun onClick(p0: View) {
                    onClickListener.invoke()
                }

                override fun updateDrawState(tp: TextPaint) {
                    tp.isUnderlineText = false
                }
            }

        color(color, range)
        bold(range)
        setSpanExclusive(clickableSpan, range)
    }

fun SpannableStringBuilder.addClickWithDataColored(
    range: IntRange, @ColorInt color: Int, onClickListener: () -> Unit): SpannableStringBuilder =
    apply {
        if (range.first < 0 || range.last > count()) return@apply
        val clickableSpan =
            object : ClickableSpan() {
                override fun onClick(p0: View) {
                    onClickListener.invoke()
                }

                override fun updateDrawState(tp: TextPaint) {
                    tp.isUnderlineText = false
                }
            }

        color(color, range)
        setSpanExclusive(clickableSpan, range)
    }

fun SpannableStringBuilder.getListRangeSpannableColored(
    rangeList: List<IntRange>,
    @ColorInt color: Int,
    onClickListener: () -> Unit
) = apply {
    rangeList.forEach { range ->
        this.addClickWithDataBoldColored(
            range = range,
            color = color
        ) {
            onClickListener.invoke()
        }
    }
}

fun SpannableStringBuilder.addClick(range: IntRange, @ColorInt color: Int, onClickListener: () -> Unit): SpannableStringBuilder =
    apply {
        if (range.first < 0 || range.last > count()) return@apply
        val clickableSpan =
            object : ClickableSpan() {
                override fun onClick(p0: View) {
                    onClickListener()
                }

                override fun updateDrawState(tp: TextPaint) {
                    tp.isUnderlineText = false
                }
            }

        color(color, range)
        bold(range)
        setSpanExclusive(clickableSpan, range)
    }

fun getClickableSpanWithDataBold(
    @ColorInt color: Int,
    onClickListener: (() -> Unit)? = null
): ClickableSpan {
    return object : ClickableSpan() {
        override fun onClick(widget: View) {
            onClickListener?.invoke()
        }

        override fun updateDrawState(ds: TextPaint) {
            ds.isUnderlineText = false
            ds.color = color
            ds.typeface = Typeface.create(ds.typeface, Typeface.BOLD)
        }
    }
}

fun IntRange.makeProperEnd(): IntRange =
    IntRange(start, endInclusive + 1)

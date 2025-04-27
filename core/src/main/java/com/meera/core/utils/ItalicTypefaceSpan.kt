package com.meera.core.utils

import android.graphics.Paint
import android.graphics.Typeface
import android.text.TextPaint
import android.text.style.MetricAffectingSpan

private const val OBLIQUE_TEXT_FACTOR = -0.25f

class ItalicTypefaceSpan(private val typeface: Typeface) : MetricAffectingSpan() {

    override fun updateDrawState(ds: TextPaint) {
        applyCustomTypeFace(ds, typeface)
    }

    override fun updateMeasureState(paint: TextPaint) {
        applyCustomTypeFace(paint, typeface)
    }

    private fun applyCustomTypeFace(paint: Paint, tf: Typeface) {
        paint.textSkewX = OBLIQUE_TEXT_FACTOR
        paint.typeface = tf
    }
}

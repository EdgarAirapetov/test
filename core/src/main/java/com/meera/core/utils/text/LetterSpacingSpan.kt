package com.meera.core.utils.text

import android.text.TextPaint
import android.text.style.MetricAffectingSpan

class LetterSpacingSpan(val letterSpacing: Float): MetricAffectingSpan() {

    override fun updateDrawState(textPaint: TextPaint) {
        setLetterSpacing(textPaint)
    }

    override fun updateMeasureState(textPaint: TextPaint) {
        setLetterSpacing(textPaint)
    }

    private fun setLetterSpacing(textPaint: TextPaint) {
        textPaint.letterSpacing = letterSpacing
    }
}

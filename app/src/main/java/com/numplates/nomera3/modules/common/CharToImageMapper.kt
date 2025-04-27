package com.numplates.nomera3.modules.common

import android.text.Spannable
import android.text.style.ImageSpan
import java.util.regex.Pattern
import javax.inject.Inject

class CharToImageMapper @Inject constructor() {

    fun mapChars(imageProvider: SpanImageProvider, source: CharSequence, mask: String, lineHeight: Int): Spannable {
        val spannable = Spannable.Factory.getInstance().newSpannable(source)
        val pattern = Pattern.compile("[$mask]")
        val matcher = pattern.matcher(spannable)
        while (matcher.find()) {
            var set = true
            for (span in spannable.getSpans(matcher.start(), matcher.end(), ImageSpan::class.java)) {
                if (spannable.getSpanStart(span) >= matcher.start() && spannable.getSpanEnd(span) <= matcher.end()) {
                    spannable.removeSpan(span)
                } else {
                    set = false
                    break
                }
            }
            val char = spannable.subSequence(matcher.start(), matcher.end()).toString().trim { it <= ' ' }
            val drawable = imageProvider.provideByChar(char) ?: continue
            val drawableWidth = drawable.intrinsicWidth.toFloat()
            val drawableHeight = drawable.intrinsicHeight.toFloat()
            val aspectRatio = drawableWidth / drawableHeight
            drawable.setBounds(0, 0, (lineHeight * aspectRatio).toInt(), lineHeight)
            if (set) {
                spannable.setSpan(
                    ImageSpan(drawable),
                    matcher.start(),
                    matcher.end(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }
        return spannable
    }
}

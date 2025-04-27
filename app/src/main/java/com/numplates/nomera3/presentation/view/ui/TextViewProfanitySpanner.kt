package com.numplates.nomera3.presentation.view.ui

import android.content.Context
import android.text.Spannable
import android.text.style.ImageSpan
import androidx.core.content.res.ResourcesCompat
import com.numplates.nomera3.OBSCENE_WORDS_MASK
import com.numplates.nomera3.R
import java.util.regex.Pattern

object TextViewProfanitySpanner {
    private val spannableFactory = Spannable.Factory.getInstance()

    fun getTextWithImages(context: Context, text: CharSequence, height: Float): Spannable {
        val spannable = spannableFactory.newSpannable(text)
        addImages(context, spannable, height)
        return spannable
    }

    private fun addImages(context: Context, spannable: Spannable, height: Float): Boolean {
        val refImg = Pattern.compile("[$OBSCENE_WORDS_MASK]")
        var hasChanges = false
        val matcher = refImg.matcher(spannable)

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
            val resName = spannable.subSequence(matcher.start(0), matcher.end(0)).toString().trim { it <= ' ' }
            val resId = getResourceWithChar(resName)
            val mDrawable = ResourcesCompat.getDrawable(context.resources, resId, null) ?: continue
            val drawableWidth = mDrawable.intrinsicWidth.toFloat()
            val drawableHeight = mDrawable.intrinsicHeight.toFloat()
            val aspectRatio = drawableWidth / drawableHeight
            mDrawable.setBounds(0, 0, (height * aspectRatio).toInt(), height.toInt())
            if (set) {
                hasChanges = true
                spannable.setSpan(
                    ImageSpan(mDrawable),
                    matcher.start(),
                    matcher.end(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }
        return hasChanges
    }

    private fun getResourceWithChar(character: String): Int =
        when (character) {
            "Ǟ" -> R.drawable.text_icon_1
            "Ǡ" -> R.drawable.text_icon_2
            "Ǣ" -> R.drawable.text_icon_3
            "ſ" -> R.drawable.text_icon_4
            "Ǥ" -> R.drawable.text_icon_5
            "Ǩ" -> R.drawable.text_icon_6
            "Ǹ" -> R.drawable.text_icon_7
            "Ǭ" -> R.drawable.text_icon_8
            "Ǯ" -> R.drawable.text_icon_9
            "Ǳ" -> R.drawable.text_icon_10
            "Ǻ" -> R.drawable.text_icon_11
            "Ǽ" -> R.drawable.text_icon_12
            "Ǿ" -> R.drawable.text_icon_13
            "Ȁ" -> R.drawable.text_icon_14
            "Ȃ" -> R.drawable.text_icon_15
            "Ȅ" -> R.drawable.text_icon_16
            "Ȇ" -> R.drawable.text_icon_17
            "Ȉ" -> R.drawable.text_icon_18
            "Ȋ" -> R.drawable.text_icon_19
            "Ȍ" -> R.drawable.text_icon_20
            "Ȏ" -> R.drawable.text_icon_21
            "Ȑ" -> R.drawable.text_icon_22
            "Ȓ" -> R.drawable.text_icon_23
            "Ȕ" -> R.drawable.text_icon_24
            "Ȗ" -> R.drawable.text_icon_25
            "Ȝ" -> R.drawable.text_icon_26
            "Ȟ" -> R.drawable.text_icon_27
            "Ȥ" -> R.drawable.text_icon_28
            "Ȧ" -> R.drawable.text_icon_29
            "Ȩ" -> R.drawable.text_icon_30
            else -> 0
        }
}

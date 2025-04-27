package com.numplates.nomera3.presentation.view.ui

import android.content.Context
import android.text.Spannable
import android.text.style.ImageSpan
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import com.numplates.nomera3.REACTION_SYMBOLS_MASK
import com.numplates.nomera3.modules.reaction.data.ReactionType
import com.numplates.nomera3.modules.reaction.data.ReactionType.Companion.getDrawableFromCharacter
import com.numplates.nomera3.modules.reaction.data.ReactionType.Companion.getDrawableFromUnsupportedCharacter
import com.numplates.nomera3.presentation.view.ui.TextViewWithImages.Companion.getResourceWithChar
import java.util.regex.Pattern

class TextViewWithReactions @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAtr: Int = 0
) : AppCompatTextView(context, attributeSet, defStyleAtr) {

    override fun setText(text: CharSequence, type: BufferType) {
        super.setText(getTextWithImages(context, text, lineHeight.toFloat()), BufferType.SPANNABLE)
    }

    companion object {
        private val spannableFactory = Spannable.Factory.getInstance()

        private val reactionCharacterMask: String by lazy {
            var returnedString = ""

            ReactionType.valuesMap.values.forEach { reaction ->
                returnedString += reaction.characterRepresentation
            }

            returnedString += REACTION_SYMBOLS_MASK

            returnedString
        }

        private fun addImages(context: Context, spannable: Spannable, height: Float): Boolean {
            val refImg = Pattern.compile("[$reactionCharacterMask]")
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

                val resName = spannable.subSequence(matcher.start(), matcher.end()).toString().trim { it <= ' ' }
                // TODO: https://nomera.atlassian.net/jira/software/c/projects/BR/issues/BR-25035?filter=myopenissues выпилить после переезда на тэги в матах
                val resId = if((matcher.end()-1 == spannable.lastIndex) && getResourceWithChar(spannable.lastOrNull()?.toString().orEmpty()) != 0) {
                    getResourceWithChar(spannable.lastOrNull()?.toString().orEmpty())
                } else {
                    getDrawableFromCharacter(resName).takeIf { it != 0 } ?: getDrawableFromUnsupportedCharacter(resName)
                }
                val drawable = ContextCompat.getDrawable(context, resId) ?: return hasChanges
                val drawableWidth = drawable.intrinsicWidth.toFloat()
                val drawableHeight = drawable.intrinsicHeight.toFloat()
                val aspectRatio = drawableWidth / drawableHeight
                drawable.setBounds(0, 0, (height * aspectRatio).toInt(), height.toInt())

                if (set) {
                    hasChanges = true
                    spannable.setSpan(
                        ImageSpan(drawable),
                        matcher.start(),
                        matcher.end(),
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
            }
            return hasChanges
        }

        private fun getTextWithImages(context: Context, text: CharSequence, height: Float): Spannable {
            val spannable = spannableFactory.newSpannable(text)
            addImages(context, spannable, height)
            return spannable
        }
    }
}

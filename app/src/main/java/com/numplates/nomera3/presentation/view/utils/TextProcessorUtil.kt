package com.numplates.nomera3.presentation.view.utils

import android.content.Context
import android.graphics.Typeface
import android.text.Layout
import android.text.SpannableStringBuilder
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.TextView
import com.meera.core.extensions.color
import com.meera.core.extensions.displayWidth
import com.meera.core.extensions.dp
import com.meera.db.models.message.ParsedUniquename
import com.numplates.nomera3.HTTPS_SCHEME
import com.numplates.nomera3.HTTP_SCHEME
import com.numplates.nomera3.R
import com.numplates.nomera3.presentation.view.ui.TextViewWithImages

const val ROAD_POSTS_TEXT_SIZE = 16f
const val ROAD_POSTS_TEXT_FONT = "fonts/sourcesanspro_regular.ttf"
const val ROAD_POSTS_TEXT_PADDING = 16
const val NO_MEDIA_TEXT_MAX_LIMIT = 23
const val NO_MEDIA_TEXT_MIN_LIMIT = 11
const val MEDIA_TEXT_MAX_LIMIT = 11
const val MEDIA_TEXT_MIN_LIMIT = 5
const val SNIPPET_TEXT_MIN_LIMIT = 3

interface TextProcessorUtil {
    fun createLayoutFromText(text: String): Layout
    fun calculateTextLineCount(
        tagSpan: ParsedUniquename?,
        isMedia: Boolean,
        isInSnippet: Boolean,
        isRepost: Boolean = false
    ): ParsedUniquename?
}

class TextProcessorUtilImpl(val context: Context) : TextProcessorUtil {

    private val textView = TextViewWithImages(context.applicationContext)
    private val moreText = context.getString(R.string.more)

    init {
        val lp = ViewGroup.LayoutParams(
            context.displayWidth - (2 * ROAD_POSTS_TEXT_PADDING.dp),
            WRAP_CONTENT
        )
        textView.layoutParams = lp
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, ROAD_POSTS_TEXT_SIZE)
        textView.typeface = Typeface.createFromAsset(context.assets, ROAD_POSTS_TEXT_FONT)
        textView.setTextColor(context.color(R.color.ui_black))
    }

    @Synchronized
    override fun createLayoutFromText(text: String): Layout {
        textView.setText(text, TextView.BufferType.SPANNABLE)
        textView.measure(
            View.MeasureSpec.makeMeasureSpec(
                context.displayWidth - (2 * ROAD_POSTS_TEXT_PADDING.dp),
                View.MeasureSpec.AT_MOST
            ),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )
        return textView.layout
    }

    @Synchronized
    override fun calculateTextLineCount(
        tagSpan: ParsedUniquename?,
        isMedia: Boolean,
        isInSnippet: Boolean,
        isRepost: Boolean
    ): ParsedUniquename? {
        val postText = tagSpan?.text.orEmpty()
        val layout = createLayoutFromText(postText)
        val lineCount = layout.lineCount
        // Пост с медиа контентном

        val maxLines = when {
            isInSnippet -> calculateSnippetMaxLines(lineCount = lineCount)
            isRepost && isMedia -> calculateRepostMaxLines(lineCount = lineCount)
            else -> calculateMaxLines(isMedia = isMedia, lineCount = lineCount)
        }

        if (maxLines == -1) {
            return tagSpan
        } else {
            tagSpan?.showFullText = false
        }

        val end = layout.getLineEnd(maxLines - 1)
        val shortText = SpannableStringBuilder(layout.text)
        shortText.replace(end, shortText.length, "")

        var moreString: String
        var resultShortText = "$shortText"
        do {
            resultShortText = resultShortText.dropLastWhile { it != ' ' && it != '\n' }.trimEnd()
            moreString = "${getEllipsize(resultShortText)}$moreText"
            val moreStringLayout = createLayoutFromText(resultShortText + moreString)
            if (moreStringLayout.lineCount < maxLines) {
                moreString = "${getEllipsize(shortText.toString())}$moreText"
                var droppedCount = 0
                resultShortText = "$shortText".dropLastWhile { droppedCount++ < moreString.length && it != '\n' }
                break
            }
        } while (moreStringLayout.lineCount > maxLines)

        tagSpan?.shortText = resultShortText + moreString
        tagSpan?.lineCount = lineCount
        return tagSpan
    }

    private fun calculateMaxLines(isMedia: Boolean, lineCount: Int): Int =
        if (isMedia) {
            if (lineCount in (MEDIA_TEXT_MIN_LIMIT + 1) until MEDIA_TEXT_MAX_LIMIT) {
                MEDIA_TEXT_MIN_LIMIT
            } else if (lineCount >= MEDIA_TEXT_MAX_LIMIT) {
                MEDIA_TEXT_MIN_LIMIT
            } else {
                -1
            }
        } else {
            if (lineCount in (NO_MEDIA_TEXT_MIN_LIMIT + 1) until NO_MEDIA_TEXT_MAX_LIMIT) {
                NO_MEDIA_TEXT_MIN_LIMIT
            } else if (lineCount >= NO_MEDIA_TEXT_MAX_LIMIT) {
                NO_MEDIA_TEXT_MIN_LIMIT
            } else {
                -1
            }
        }

    private fun calculateSnippetMaxLines(lineCount: Int) =
        if (lineCount >= SNIPPET_TEXT_MIN_LIMIT) {
            SNIPPET_TEXT_MIN_LIMIT
        } else {
            -1
        }

    private fun calculateRepostMaxLines(lineCount: Int) =
        if (lineCount > SNIPPET_TEXT_MIN_LIMIT) {
            SNIPPET_TEXT_MIN_LIMIT
        } else {
            -1
        }


    private fun getEllipsize(shortText: String): String {
        val lastString = shortText.split(" ").last()
        return if (lastString.lowercase().contains(HTTP_SCHEME) || lastString.lowercase()
                .contains(HTTPS_SCHEME)
        ) " ..." else "..."
    }
}

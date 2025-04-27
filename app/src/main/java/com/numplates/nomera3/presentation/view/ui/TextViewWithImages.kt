package com.numplates.nomera3.presentation.view.ui

import android.content.Context
import android.graphics.Canvas
import android.text.Layout
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.StaticLayout
import android.text.TextUtils
import android.text.style.ImageSpan
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.meera.core.extensions.tryCatch
import com.numplates.nomera3.OBSCENE_WORDS_MASK
import com.numplates.nomera3.R
import timber.log.Timber
import java.util.concurrent.CopyOnWriteArrayList
import java.util.regex.Pattern

class TextViewWithImages : AppCompatTextView {

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context) : super(context)

    private val ELLIPSIS = "  ..."

    interface EllipsizeListener {
        fun ellipsizeStateChanged(ellipsized: Boolean)
    }

    private val ellipsizeListeners: CopyOnWriteArrayList<EllipsizeListener> = CopyOnWriteArrayList()

    var strBuilder: SpannableStringBuilder? = null
    private var isEllipsized = false
    private var isStale = false
    private var programmaticChange = false
    private var fullText: String? = null
    private var maxLines = -1
    override fun getLineSpacingMultiplier(): Float = super.getLineSpacingMultiplier()
    private var lineAdditionalVerticalPadding = 0.0f

    var forceResetOff = false
    fun addEllipsizeListener(listener: EllipsizeListener?) {
        if (listener == null) {
            throw NullPointerException("EllipsizeListener is null")
        }
        ellipsizeListeners.add(listener)
    }

    fun removeEllipsizeListener(listener: EllipsizeListener?) {
        ellipsizeListeners.remove(listener)
    }

    fun isEllipsized(): Boolean = isEllipsized

    override fun setMaxLines(maxLines: Int) {
        super.setMaxLines(maxLines)
        this.maxLines = maxLines
        isStale = true
    }

    override fun getMaxLines(): Int = maxLines

    override fun setLineSpacing(add: Float, mult: Float) {
        lineAdditionalVerticalPadding = add
        super.setLineSpacing(add, mult)
    }

    override fun onTextChanged(text: CharSequence, start: Int, before: Int, after: Int) {
        super.onTextChanged(text, start, before, after)
        if (!programmaticChange) {
            fullText = text.toString()
            isStale = true
        }
    }

    override fun onDraw(canvas: Canvas) {
        if (forceResetOff) {
            super.onDraw(canvas)
            return
        }
        if (isStale) {
            super.setEllipsize(null)
            resetText()
        }
        super.onDraw(canvas)
    }

    private fun resetText() {
        tryCatch {
            val maxLines = getMaxLines()
            var workingText: SpannableStringBuilder = strBuilder ?: SpannableStringBuilder(
                fullText
                    ?: ""
            )
            var ellipsized = false
            if (maxLines != -1) {
                val layout: Layout = createWorkingLayout(workingText)
                if (layout.lineCount > maxLines) {
                    val lineEnd = layout.getLineEnd(maxLines - 1)
                    workingText = workingText
                        .subSequence(0, lineEnd)
                        .trim { it <= ' ' } as SpannableStringBuilder

                    while (createWorkingLayout(workingText.append(ELLIPSIS)).lineCount > maxLines) {
                        var lastSpace = workingText.lastIndexOf(' ')

                        if (lastSpace == -1) {
                            break
                        }

                        if (lastSpace > 10) lastSpace -= 10 //удаляем лишние символы элипса из за них может зациклить
                        workingText = workingText.subSequence(0, lastSpace) as SpannableStringBuilder
                    }
//                    workingText = workingText.append(ELLIPSIS)
                    ellipsized = true
                }
            }
            if (workingText != text) {
                programmaticChange = true
                text = try {
                    workingText
                } finally {
                    programmaticChange = false
                }
            }
            isStale = false

            isEllipsized = ellipsized
            val iterator = ellipsizeListeners.iterator()
            while (iterator.hasNext()) {
                iterator.next().ellipsizeStateChanged(ellipsized)
            }
        }
    }

    fun getLineCountForText(workingText: CharSequence): Int {
        val layout = createWorkingLayout(workingText)
        return layout.lineCount
    }

    private fun createWorkingLayout(workingText: CharSequence): Layout =
        StaticLayout(
            workingText, paint, width - paddingLeft - paddingRight,
            Layout.Alignment.ALIGN_NORMAL, lineSpacingMultiplier, lineAdditionalVerticalPadding, false
        )

    override fun setEllipsize(where: TextUtils.TruncateAt?) {
        // Ellipsize settings are not respected
    }

    override fun setText(text: CharSequence, type: BufferType) {
        try {
            super.setText(getTextWithImages(context, text, lineHeight.toFloat()), BufferType.SPANNABLE)
        } catch (error: Throwable) {
            FirebaseCrashlytics.getInstance().apply {
                recordException(error)
                log("Crash TextViewWithImages setText, text size = ${text.length}, " +
                    "view parent = ${parent}, text = ${text}, textIsEmpty = ${text.isEmpty()}")
            }

            Timber.e(error)
        }
    }

    companion object {
        private val spannableFactory = Spannable.Factory.getInstance()

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
                val mDrawable = context.resources.getDrawable(resId)
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

        private fun getTextWithImages(context: Context, text: CharSequence, height: Float): Spannable {
            val spannable = spannableFactory.newSpannable(text)
            addImages(context, spannable, height)
            return spannable
        }

        /**
         * Used with the TextViewWithImages
         *
         * @see com.numplates.nomera3.presentation.view.ui.TextViewWithImages
         */
        fun getResourceWithChar(character: String): Int =
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
}

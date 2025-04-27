package com.numplates.nomera3.modules.moments.show.presentation.view

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.core.content.withStyledAttributes
import androidx.core.view.isVisible
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.reaction.ui.util.ReactionCounterFormatter

class MomentsViewCounterView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = android.R.attr.textViewStyle,
) : AppCompatTextView(context, attrs, defStyleAttr) {

    private val eyeDrawable = ContextCompat.getDrawable(context, R.drawable.ic_eye_view_count)

    private val reactionCounterFormatter = ReactionCounterFormatter(
        thousandLabel = context.getString(R.string.thousand_lowercase_label),
        millionLabel = context.getString(R.string.million_lowercase_label),
        oneAllow = true,
        thousandAllow = false
    )

    private var previousValue: Long? = null
    private var previousFormattedValue: String? = null

    init {
        context.withStyledAttributes(attrs, R.styleable.MomentsViewCounterView) {
            setCompoundDrawablesRelativeWithIntrinsicBounds(eyeDrawable, null, null, null)
            val startingValue = getInteger(R.styleable.MomentsViewCounterView_mvcvStartingCounterValue, -1)
            setViewCounterValue(startingValue.toLong())
        }
    }

    fun setViewCounterValue(viewCount: Long) {
        if (viewCount == previousValue) return
        manageVisibility(viewCount)
        previousValue = viewCount

        if (viewCount < 0) return

        val formattedValue = reactionCounterFormatter.format(viewCount)
        if (formattedValue == previousFormattedValue) return
        previousFormattedValue = formattedValue

        text = formattedValue
    }

    private fun manageVisibility(viewCount: Long) {
        isVisible = viewCount > 0
    }

}

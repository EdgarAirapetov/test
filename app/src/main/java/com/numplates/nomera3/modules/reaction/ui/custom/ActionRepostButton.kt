package com.numplates.nomera3.modules.reaction.ui.custom

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.meera.core.extensions.animateWidth
import com.meera.core.extensions.animateWithColor
import com.meera.core.extensions.setTint
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.ActionRepostButtonBinding
import com.numplates.nomera3.modules.feed.ui.adapter.ContentActionBar
import com.numplates.nomera3.modules.reaction.ui.util.ReactionCounterFormatter

private const val COUNT_ZERO = 0
private const val COLOR_CHANGING_TIME_MILLIS = 200L
private const val TEXT_EXPANDING_TIME_MILLIS = 200L

class ActionRepostButton @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAtr: Int = 0
) : LinearLayout(context, attributeSet, defStyleAtr), ReactionButtonColor {

    private val binding: ActionRepostButtonBinding =
        ActionRepostButtonBinding.inflate(LayoutInflater.from(context), this, true)

    private val reactionCounterFormatter = ReactionCounterFormatter(
        thousandLabel = context.getString(R.string.thousand_lowercase_label),
        millionLabel = context.getString(R.string.million_lowercase_label),
        oneAllow = true,
        thousandAllow = false
    )

    override fun setButtonThemeByContent(contentActionBarType: ContentActionBar.ContentActionBarType) {
        val defaultColor = getDefaultBackgroundTint(context = context, contentActionBarType = contentActionBarType)
        binding.llActionRepostButton.backgroundTintList = ColorStateList.valueOf(defaultColor)
        when (contentActionBarType) {
            ContentActionBar.ContentActionBarType.DEFAULT -> {
                binding.tvActionRepostCount
                    .setTextColor(ContextCompat.getColor(context, R.color.ui_gray_80))
                binding.ivActionRepostIcon.setTint(R.color.ui_gray_80)
            }
            ContentActionBar.ContentActionBarType.DARK,
            ContentActionBar.ContentActionBarType.BLUR -> {
                binding.tvActionRepostCount
                    .setTextColor(ContextCompat.getColor(context, R.color.color_action_bar_light_text_color))
                binding.ivActionRepostIcon.setTint(R.color.color_action_bar_light_text_color)
            }
        }
    }

    fun onClick(contentActionBarType: ContentActionBar.ContentActionBarType, click: (View) -> Unit) {
        setOnClickListener {
            binding.llActionRepostButton.animateWithColor(
                firstColor = getDefaultBackgroundTint(
                    context = context,
                    contentActionBarType = contentActionBarType
                ),
                secondColor = getPressedBackgroundTint(
                    context = context,
                    contentActionBarType = contentActionBarType
                ),
                durationMills = COLOR_CHANGING_TIME_MILLIS
            )
            click(it)
        }
    }

    fun setRepostCount(count: Int?) {
        if (count == null) return
        val currentText = binding.tvActionRepostCount.text.toString()
        val formattedText = reactionCounterFormatter.format(count)
        if (currentText == formattedText) return
        val targetWidth = if (count > COUNT_ZERO) LayoutParams.WRAP_CONTENT else 0
        expandTextWidth(targetWidth) {
            binding.tvActionRepostCount.text = formattedText
        }
    }

    fun setButtonEnabledAlpha(value: Boolean) {
        setButtonEnabled(enabled = value, buttonLinearLayout = binding.llActionRepostButton)
    }

    private fun expandTextWidth(width: Int, onTransitionEnd: (() -> Unit)) {
        binding.tvActionRepostCount.animateWidth(
            newWidth = width,
            duration = TEXT_EXPANDING_TIME_MILLIS
        ) {
            onTransitionEnd.invoke()
        }
    }



}

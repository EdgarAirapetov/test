package com.numplates.nomera3.modules.reaction.ui.custom

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.meera.core.extensions.animateWidth
import com.meera.core.extensions.animateWithColor
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.setTint
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraActionCommentButtonBinding
import com.numplates.nomera3.modules.feed.ui.adapter.MeeraContentActionBar
import com.numplates.nomera3.modules.reaction.ui.util.ReactionCounterFormatter

private const val COUNT_ZERO = 0
private const val COLOR_CHANGING_TIME_MILLIS = 200L
private const val TEXT_EXPANDING_TIME_MILLIS = 200L

class MeeraActionCommentButton @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAtr: Int = 0
) : LinearLayout(context, attributeSet, defStyleAtr), MeeraReactionButtonColor {

    private val binding: MeeraActionCommentButtonBinding =
        MeeraActionCommentButtonBinding.inflate(LayoutInflater.from(context), this, true)

    private val reactionCounterFormatter = ReactionCounterFormatter(
        thousandLabel = context.getString(R.string.thousand_lowercase_label),
        millionLabel = context.getString(R.string.million_lowercase_label),
        oneAllow = true,
        thousandAllow = false
    )

    override fun setButtonThemeByContent(contentActionBarType: MeeraContentActionBar.ContentActionBarType) {
        val defaultColor = getDefaultBackgroundTint(context = context, contentActionBarType = contentActionBarType)
        binding.llActionCommentButton.backgroundTintList = ColorStateList.valueOf(defaultColor)
        when (contentActionBarType) {
            MeeraContentActionBar.ContentActionBarType.DEFAULT -> {
                binding.tvActionCommentCount
                    .setTextColor(ContextCompat.getColor(context, R.color.uiKitColorLegacySecondary))
                binding.ivActionCommentIcon.setTint(R.color.uiKitColorLegacySecondary)
            }
            MeeraContentActionBar.ContentActionBarType.DARK,
            MeeraContentActionBar.ContentActionBarType.BLUR -> {
                binding.tvActionCommentCount
                    .setTextColor(
                        ContextCompat.getColor(
                            context,
                            R.color.uiKitColorForegroundInvers
                        )
                    )
                binding.ivActionCommentIcon.setTint(R.color.uiKitColorForegroundInvers)
            }
        }
    }

    fun onClick(contentActionBarType: MeeraContentActionBar.ContentActionBarType, click: () -> Unit) {
        setThrottledClickListener {
            binding.llActionCommentButton.animateWithColor(
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
            click()
        }
    }

    fun setCommentCount(count: Int?) {
        if (count == null) return
        val currentText = binding.tvActionCommentCount.text.toString()
        val formattedText = reactionCounterFormatter.format(count)
        if (currentText == formattedText) return
        val targetWidth = if (count > COUNT_ZERO) LayoutParams.WRAP_CONTENT else 0
        expandTextWidth(targetWidth) {
            binding.tvActionCommentCount.text = formattedText
        }
    }

    fun setButtonEnabledAlpha(value: Boolean) {
        setButtonEnabled(enabled = value, buttonLinearLayout = binding.llActionCommentButton)
    }

    fun clearResources() {
        setOnClickListener(null)
        binding.tvActionCommentCount.clearAnimation()
    }

    private fun expandTextWidth(width: Int, onTransitionEnd: (() -> Unit)) {
        binding.tvActionCommentCount.animateWidth(
            newWidth = width,
            duration = TEXT_EXPANDING_TIME_MILLIS
        ) {
            onTransitionEnd.invoke()
        }
    }
}

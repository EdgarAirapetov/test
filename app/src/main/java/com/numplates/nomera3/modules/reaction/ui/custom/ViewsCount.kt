package com.numplates.nomera3.modules.reaction.ui.custom

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.setTint
import com.meera.core.extensions.setVisible
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.ViewsCountViewBinding
import com.numplates.nomera3.modules.feed.ui.adapter.ContentActionBar
import com.numplates.nomera3.modules.reaction.ui.util.ReactionCounterFormatter

class ViewsCount @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAtr: Int = 0
) : LinearLayout(context, attributeSet, defStyleAtr), ReactionButtonColor {

    private val binding: ViewsCountViewBinding =
        ViewsCountViewBinding.inflate(LayoutInflater.from(context), this, true)

    private var listener: (() -> Unit)? = null

    private val reactionCounterFormatter = ReactionCounterFormatter(
        thousandLabel = context.getString(R.string.thousand_lowercase_label),
        millionLabel = context.getString(R.string.million_lowercase_label),
        oneAllow = true,
        thousandAllow = false
    )

    init {
        binding.llViewsCount.setThrottledClickListener {
            listener?.invoke()
        }
    }

    fun setClickListener(listener: () -> Unit) {
        this.listener = listener
    }

    override fun setButtonThemeByContent(contentActionBarType: ContentActionBar.ContentActionBarType) {
        when (contentActionBarType) {
            ContentActionBar.ContentActionBarType.DEFAULT -> {
                binding.tvViewsCount
                    .setTextColor(ContextCompat.getColor(context, R.color.ui_gray_80))
                binding.ivViewsIcon.setTint(R.color.ui_gray_80)
            }
            ContentActionBar.ContentActionBarType.DARK,
            ContentActionBar.ContentActionBarType.BLUR -> {
                binding.tvViewsCount
                    .setTextColor(ContextCompat.getColor(context, R.color.color_action_bar_light_text_color))
                binding.ivViewsIcon.setTint(R.color.color_action_bar_light_text_color)
            }
        }
    }

    fun setViewsCount(count: Long) {
        this.setVisible(count > 0)
        binding.tvViewsCount.text = reactionCounterFormatter.format(count)
    }
}

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
import com.numplates.nomera3.databinding.MeeraViewsCountViewBinding
import com.numplates.nomera3.modules.feed.ui.adapter.MeeraContentActionBar
import com.numplates.nomera3.modules.reaction.ui.util.ReactionCounterFormatter

class MeeraViewsCount @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAtr: Int = 0
) : LinearLayout(context, attributeSet, defStyleAtr), MeeraReactionButtonColor {

    private val binding: MeeraViewsCountViewBinding =
        MeeraViewsCountViewBinding.inflate(LayoutInflater.from(context), this, true)

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

    fun clearResources(){
        listener = null
    }

    override fun setButtonThemeByContent(contentActionBarType: MeeraContentActionBar.ContentActionBarType) {
        when (contentActionBarType) {
            MeeraContentActionBar.ContentActionBarType.DEFAULT -> {
                binding.tvViewsCount
                    .setTextColor(ContextCompat.getColor(context, R.color.uiKitColorForegroundInvers))
                binding.ivViewsIcon.setTint(R.color.uiKitColorForegroundInvers)
            }
            MeeraContentActionBar.ContentActionBarType.DARK,
            MeeraContentActionBar.ContentActionBarType.BLUR -> {
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

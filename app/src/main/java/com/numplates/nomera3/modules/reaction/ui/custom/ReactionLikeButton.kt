package com.numplates.nomera3.modules.reaction.ui.custom

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import com.meera.core.extensions.animateWithColor
import com.meera.core.extensions.gone
import com.meera.core.extensions.setTextStyle
import com.meera.core.extensions.setTint
import com.meera.core.extensions.visible
import com.meera.core.utils.IS_APP_REDESIGNED
import com.meera.core.utils.checkAppRedesigned
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.ReactionLikeButtonBinding
import com.numplates.nomera3.modules.feed.ui.adapter.ContentActionBar
import com.numplates.nomera3.modules.reaction.data.ReactionType
import com.numplates.nomera3.modules.reaction.data.net.ReactionEntity
import com.numplates.nomera3.modules.reaction.data.net.isMine

private const val COLOR_CHANGING_TIME_MILLIS = 200L

class ReactionLikeButton @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAtr: Int = 0
) : LinearLayout(context, attributeSet, defStyleAtr), ReactionButtonColor {

    private val binding: ReactionLikeButtonBinding =
        ReactionLikeButtonBinding.inflate(LayoutInflater.from(context), this, true)

    fun onClick(contentActionBarType: ContentActionBar.ContentActionBarType, click: (View) -> Unit) {
        setOnClickListener {
            if (!IS_APP_REDESIGNED) {
                binding.llReactionLikeButton.animateWithColor(
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
            }
            click(it)
        }
    }

    fun resetAnimation() {
        binding.lottieReactionLikeIcon.progress = 0F
    }

    fun getLikeButtonCollapsed(): Boolean {
        return binding.tvReactionLikeCount.isGone
    }

    fun setLikeButtonCollapsed(collapsed: Boolean) {
        if (collapsed) {
            binding.tvReactionLikeCount.gone()
        } else {
            binding.tvReactionLikeCount.visible()
        }
    }

    fun setButtonEnabledAlpha(value: Boolean) {
        if (!IS_APP_REDESIGNED) {
            setButtonEnabled(enabled = value, buttonLinearLayout = binding.llReactionLikeButton)
        }
    }

    /**
     * Добавляет / удаляет пользовательскую реакцию.
     */
    fun setLikeButtonState(
        contentActionBarType: ContentActionBar.ContentActionBarType,
        reactionEntities: List<ReactionEntity>,
        animate: Boolean
    ) {
        resetButtonState(contentActionBarType)
        for (reactionEntity in reactionEntities) {
            if (reactionEntity.isMine()) {
                setButtonState(reactionEntity)
                if (animate) {
                    binding.lottieReactionLikeIcon.playAnimation()
                }
            }
        }
    }

    private fun resetButtonState(contentActionBarType: ContentActionBar.ContentActionBarType) {
        binding.ivReactionLikeIcon.visible()
        binding.lottieReactionLikeIcon.gone()
        checkAppRedesigned(
            isRedesigned = {
                binding.tvReactionLikeCount.text = ""
            },
            isNotRedesigned = {
                binding.tvReactionLikeCount.text = context.getString(R.string.reaction_green_light)
            }
        )
        setButtonThemeByContent(contentActionBarType)
    }

    override fun setButtonThemeByContent(contentActionBarType: ContentActionBar.ContentActionBarType) {
        if (!IS_APP_REDESIGNED){
            val defaultColor = getDefaultBackgroundTint(context = context, contentActionBarType = contentActionBarType)
            binding.llReactionLikeButton.backgroundTintList = ColorStateList.valueOf(defaultColor)
        }
        when (contentActionBarType) {
            ContentActionBar.ContentActionBarType.DEFAULT -> {
                binding.tvReactionLikeCount
                    .setTextColor(ContextCompat.getColor(context, R.color.ui_gray_80))
                binding.ivReactionLikeIcon.setTint(R.color.ui_gray_80)
            }
            ContentActionBar.ContentActionBarType.DARK,
            ContentActionBar.ContentActionBarType.BLUR -> {
                checkAppRedesigned(
                    isRedesigned = {
                        binding.ivReactionLikeIcon.setImageResource(R.drawable.ic_outlined_like_m)
                        binding.ivReactionLikeIcon.setTint(R.color.ui_white)
                        binding.llReactionLikeButton.setBackgroundResource(R.color.transparent)
                        binding.tvReactionLikeCount.setTextStyle(R.style.UiKit_Body_Normal)
                    },
                    isNotRedesigned = {
                        binding.tvReactionLikeCount
                            .setTextColor(ContextCompat.getColor(context, R.color.color_action_bar_light_text_color))
                        binding.ivReactionLikeIcon.setTint(R.color.color_action_bar_light_text_color)
                    }
                )
            }
        }
    }

    private fun setButtonState(reactionEntity: ReactionEntity) {
        val reactionType = ReactionType.getByString(reactionEntity.reactionType) ?: return
        binding.ivReactionLikeIcon.gone()
        binding.lottieReactionLikeIcon.visible()
        binding.lottieReactionLikeIcon.setAnimation(reactionType.resourceNoBorder)
        binding.lottieReactionLikeIcon.playAnimation()
        binding.tvReactionLikeCount.text = context.getString(reactionType.resourceName)
        if (IS_APP_REDESIGNED) {
            binding.llReactionLikeButton.setBackgroundResource(R.drawable.meera_background_content_action_bar_button)
        }

        binding.tvReactionLikeCount
            .setTextColor(ContextCompat.getColor(context, reactionType.resourceColor))
    }
}

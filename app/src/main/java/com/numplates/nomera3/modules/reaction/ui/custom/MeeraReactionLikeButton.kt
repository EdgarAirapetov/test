package com.numplates.nomera3.modules.reaction.ui.custom

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import com.meera.core.extensions.dp
import com.meera.core.extensions.gone
import com.meera.core.extensions.setPaddingEnd
import com.meera.core.extensions.setPaddingStart
import com.meera.core.extensions.setTint
import com.meera.core.extensions.visible
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraReactionLikeButtonBinding
import com.numplates.nomera3.modules.feed.ui.adapter.MeeraContentActionBar
import com.numplates.nomera3.modules.reaction.data.ReactionType
import com.numplates.nomera3.modules.reaction.data.net.ReactionEntity
import com.numplates.nomera3.modules.reaction.data.net.isMine

class MeeraReactionLikeButton @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAtr: Int = 0
) : LinearLayout(context, attributeSet, defStyleAtr), MeeraReactionButtonColor {

    private var commentType = false
    private var buttonHorizontalPadding = 6.dp
    private var buttonLikeHalfSize = 10.dp

    private val binding: MeeraReactionLikeButtonBinding =
        MeeraReactionLikeButtonBinding.inflate(LayoutInflater.from(context), this, true)

    private var screenAnimationShowListener:
        ((reactionEntity: ReactionEntity, anchorViewLocation: Pair<Int, Int>) -> Unit)? = null

    fun onScreenAnimationShowListener(
        listener: ((reactionEntity: ReactionEntity, anchorViewLocation: Pair<Int, Int>) -> Unit)
    ) {
        screenAnimationShowListener = listener
    }

    fun clearResources() {
        binding.lottieReactionLikeIcon.clearAnimation()
        setOnClickListener(null)
        screenAnimationShowListener = null
    }

    fun setCommentType() {
        commentType = true
    }

    fun onClick(click: (View) -> Unit) {
        setOnClickListener {
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

    /**
     * Добавляет / удаляет пользовательскую реакцию.
     */
    fun setLikeButtonState(
        contentActionBarType: MeeraContentActionBar.ContentActionBarType,
        reactionEntities: List<ReactionEntity>,
        animate: Boolean
    ) {
        resetButtonState(contentActionBarType)
        for (reactionEntity in reactionEntities) {
            if (reactionEntity.isMine()) {
                setButtonState(reactionEntity)
                if (animate) {
                    screenAnimationShowListener?.invoke(reactionEntity, getAnchorViewCenterCoordinates())
                    binding.lottieReactionLikeIcon.playAnimation()
                }
            }
        }
    }

    private fun getAnchorViewCenterCoordinates(): Pair<Int, Int> {
        binding.lottieReactionLikeIcon.let { view ->
            val viewLocation = IntArray(2)
            view.getLocationOnScreen(viewLocation)
            val centerX = viewLocation[0] + buttonLikeHalfSize
            val centerY = viewLocation[1] + this.height / 2
            return Pair(centerX, centerY)
        }
    }

    private fun resetButtonState(contentActionBarType: MeeraContentActionBar.ContentActionBarType) {
        if(commentType) {
            binding.ivReactionLikeIcon.gone()
            binding.tvReactionTitle.visible()
            removeButtonPadding()
        } else {
            binding.ivReactionLikeIcon.visible()
            setButtonDefaultPaddingState()
        }

        binding.lottieReactionLikeIcon.gone()
        binding.llReactionLikeButton.setBackgroundResource(0)
        binding.tvReactionLikeCount.text = ""
        setButtonThemeByContent(contentActionBarType)
    }

    override fun setButtonThemeByContent(contentActionBarType: MeeraContentActionBar.ContentActionBarType) {
        when (contentActionBarType) {
            MeeraContentActionBar.ContentActionBarType.DEFAULT -> {
                binding.tvReactionLikeCount
                    .setTextColor(ContextCompat.getColor(context, R.color.uiKitColorLegacySecondary))
                binding.ivReactionLikeIcon.setTint(R.color.uiKitColorLegacySecondary)
            }

            MeeraContentActionBar.ContentActionBarType.DARK,
            MeeraContentActionBar.ContentActionBarType.BLUR -> {
                binding.llReactionLikeButton.setBackgroundResource(R.color.transparent)
                binding.ivReactionLikeIcon.setTint(R.color.uiKitColorForegroundInvers)
            }
        }
    }

    private fun setButtonState(reactionEntity: ReactionEntity) {
        val reactionType = ReactionType.getByString(reactionEntity.reactionType) ?: return
        binding.ivReactionLikeIcon.gone()
        setButtonDefaultPaddingState()
        binding.tvReactionTitle.gone()
        binding.lottieReactionLikeIcon.visible()
        binding.lottieReactionLikeIcon.setAnimation(reactionType.resourceNoBorder)

        if(reactionType.maxFrame!= 0){
            binding.lottieReactionLikeIcon.setMaxFrame(reactionType.maxFrame)
        }

        binding.lottieReactionLikeIcon.playAnimation()
        binding.tvReactionLikeCount.text = context.getString(reactionType.resourceName)
        binding.llReactionLikeButton.setBackgroundResource(reactionType.resourceBackground)

        binding.tvReactionLikeCount
            .setTextColor(ContextCompat.getColor(context, reactionType.resourceColor))
    }

    private fun setButtonDefaultPaddingState() {
        binding.llReactionLikeButton.setPaddingStart(buttonHorizontalPadding)
        binding.llReactionLikeButton.setPaddingEnd(buttonHorizontalPadding)
    }

    private fun removeButtonPadding(){
        binding.llReactionLikeButton.setPaddingStart(0)
        binding.llReactionLikeButton.setPaddingEnd(0)
    }
}

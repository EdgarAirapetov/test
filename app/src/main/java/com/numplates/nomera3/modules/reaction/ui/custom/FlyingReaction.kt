package com.numplates.nomera3.modules.reaction.ui.custom

import android.content.Context
import android.graphics.Point
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.airbnb.lottie.LottieAnimationView
import com.meera.core.extensions.animateFading
import com.meera.core.extensions.animateFlying
import com.meera.core.extensions.animateScale
import com.numplates.nomera3.databinding.ReactionFlyingBinding
import com.numplates.nomera3.modules.reaction.data.ReactionType

private const val FLYING_ANIMATION_DURATION = 700L
private const val FLYING_ANIMATION_HALF_DURATION = FLYING_ANIMATION_DURATION / 2

class FlyingReaction @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAtr: Int = 0
) : FrameLayout(context, attributeSet, defStyleAtr) {

    private val binding: ReactionFlyingBinding =
        ReactionFlyingBinding.inflate(LayoutInflater.from(context), this, true)

    private var currentReactionType: ReactionType? = null
    private var flyingAnimationPlayListener: FlyingAnimationPlayListener? = null

    fun setReactionType(reactionType: ReactionType) {
        currentReactionType = reactionType
    }

    fun startAnimationFlying() {
        val reactionType = currentReactionType ?: return
        binding.lavFlyingReaction.apply {
            disableClipOnParents(binding.lavFlyingReaction)
            setAnimation(reactionType.resourceNoBorder)
            binding.lavFlyingReaction
                .cloneReaction()
                .animateFirstStep()
        }
    }

    fun setFlyingAnimationPlayListener(flyingAnimationPlayListener: FlyingAnimationPlayListener?) {
        this.flyingAnimationPlayListener = flyingAnimationPlayListener
    }

    fun setViewLayoutParams(position: Point) {
        layoutParams = LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(position.x, position.y, 0, 0)
        }
    }

    fun clearResources() {
        binding.lavFlyingReaction.clearAnimation()
        currentReactionType = null
        flyingAnimationPlayListener = null
    }

    // Documentation: https://docs.google.com/spreadsheets/d/1Z85xRDgMKREcdBO3aUL15cRzE9_nTiTXfhqGc0xp7u8/edit#gid=658484253
    private fun LottieAnimationView.animateFirstStep() {
        animateReactionVisibility()
        animateScale(
            durationMills = FLYING_ANIMATION_DURATION,
            startScale = 1f,
            endScale = 0.7f
        )
        animateFlying(
            leftDirection = true,
            durationMills = FLYING_ANIMATION_DURATION,
            durationMillsToStartNext = 66L,
            startNextCallback = {
                binding.lavFlyingReaction
                    .cloneReaction()
                    .animateSecondStep()
            },
            onAnimationEnd = {
                binding.root.removeView(this)
            }
        )
    }

    private fun LottieAnimationView.animateSecondStep() {
        animateReactionVisibility()
        animateScale(
            durationMills = FLYING_ANIMATION_HALF_DURATION,
            startScale = 0.9f,
            endScale = 1.2f,
            onAnimationEnd = {
                animateScale(
                    durationMills = FLYING_ANIMATION_HALF_DURATION,
                    startScale = 1.2f,
                    endScale = 1.0f
                )
            }
        )
        animateFlying(
            leftDirection = false,
            durationMills = FLYING_ANIMATION_DURATION,
            durationMillsToStartNext = 33L,
            startNextCallback = {
                binding.lavFlyingReaction
                    .cloneReaction()
                    .animateThirdStep()
            },
            onAnimationEnd = {
                binding.root.removeView(this)
            }
        )
    }

    private fun LottieAnimationView.animateThirdStep() {
        animateReactionVisibility()
        animateScale(
            durationMills = FLYING_ANIMATION_DURATION,
            startScale = 0.7f,
            endScale = 0.65f
        )
        animateFlying(
            leftDirection = true,
            durationMills = FLYING_ANIMATION_DURATION,
            durationMillsToStartNext = 99L,
            startNextCallback = {
                binding.lavFlyingReaction
                    .cloneReaction()
                    .animateFourthStep()
            },
            onAnimationEnd = {
                binding.root.removeView(this)
            }
        )
    }

    private fun LottieAnimationView.animateFourthStep() {
        animateReactionVisibility()
        animateScale(
            durationMills = FLYING_ANIMATION_DURATION,
            startScale = 0.6f,
            endScale = 0.72f
        )
        animateFlying(
            leftDirection = false,
            durationMills = FLYING_ANIMATION_DURATION,
            onAnimationEnd = {
                binding.root.removeView(this)
                flyingAnimationPlayListener?.onFlyingAnimationPlayed(this@FlyingReaction)
            }
        )
    }

    private fun LottieAnimationView.cloneReaction(): LottieAnimationView {
        val clone = LottieAnimationView(context)
        clone.layoutParams = layoutParams
        clone.alpha = alpha
        currentReactionType?.let { clone.setAnimation(it.resourceNoBorder) }
        binding.root.addView(clone)
        return clone
    }

    private fun disableClipOnParents(view: View) {
        if (view.parent == null) return
        if (view is ViewGroup) {
            view.clipChildren = false
            view.clipToPadding = false
        }
        (view.parent as? View)?.let(::disableClipOnParents)
    }

    private fun LottieAnimationView.animateReactionVisibility() {
        animateFading(
            durationMills = FLYING_ANIMATION_HALF_DURATION,
            endAlpha = 1f,
            reverse = true
        )
    }
}

interface FlyingAnimationPlayListener {
    fun onFlyingAnimationPlayed(playedFlyingReaction: FlyingReaction)
}

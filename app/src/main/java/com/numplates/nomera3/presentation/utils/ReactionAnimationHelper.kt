package com.numplates.nomera3.presentation.utils

import android.animation.Animator
import android.content.Context
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import com.airbnb.lottie.LottieAnimationView
import com.meera.core.extensions.dp
import com.numplates.nomera3.modules.reaction.data.ReactionType

class ReactionAnimationHelper {

    private val LOTTIE_POPUP_HAFT_WEIGHT_VALUE = 104.dp
    private val LOTTIE_POPUP_INITIAL_OFFSET = 34.dp
    private var recyclerView: RecyclerView? = null
    private var parent: ViewGroup? = null
    private var context: Context? = null

    fun playLottieAtPosition(
        recyclerView: RecyclerView?,
        context: Context,
        parent: ViewGroup?,
        reactionType: ReactionType,
        x: Float,
        y: Float
    ) {
        this.recyclerView = recyclerView
        this.parent = parent
        this.context = context
        if (y == 0f) return
        val popupAnimation = reactionType.resourcePopupAnimation ?: return

        val parentLocation = IntArray(2)
        parent?.getLocationOnScreen(parentLocation)
        val parentYOffset = parentLocation[1]

        val totalY = y - parentYOffset - LOTTIE_POPUP_HAFT_WEIGHT_VALUE
        val totalX = x - LOTTIE_POPUP_INITIAL_OFFSET + reactionType.reactionXTranslation

        val lottieView = LottieAnimationView(context).apply {
            setAnimation(popupAnimation)
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            repeatCount = 0
            translationX = totalX
            translationY = totalY
        }

        val scrollListener = object: OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val viewY = lottieView.y
                lottieView.y = viewY - dy
            }
        }

        parent?.addView(lottieView)

        lottieView.addAnimatorListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) = Unit
            override fun onAnimationEnd(animation: Animator) {
                parent?.removeView(lottieView)
            }

            override fun onAnimationCancel(animation: Animator) {
                recyclerView?.removeOnScrollListener(scrollListener)
                parent?.removeView(lottieView)
            }

            override fun onAnimationRepeat(animation: Animator) = Unit
        })

        recyclerView?.addOnScrollListener(scrollListener)
        lottieView.playAnimation()
    }

    fun clearData() {
        recyclerView = null
        parent = null
        context = null
    }
}

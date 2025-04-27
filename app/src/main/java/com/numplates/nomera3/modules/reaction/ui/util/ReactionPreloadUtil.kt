package com.numplates.nomera3.modules.reaction.ui.util

import android.content.Context
import com.airbnb.lottie.LottieAnimationView
import com.numplates.nomera3.modules.reaction.data.ReactionType

object ReactionPreloadUtil {

    fun preloadReactionsResources(context: Context) {
        preloadResourceWithoutBorder(context)
    }

    private fun preloadResourceWithoutBorder(context: Context) {
        ReactionType.values().forEach { reactionType ->
            LottieAnimationView(context).apply {
                setAnimation(reactionType.resourceNoBorder)
                setCacheComposition(true)
            }
        }
    }
}

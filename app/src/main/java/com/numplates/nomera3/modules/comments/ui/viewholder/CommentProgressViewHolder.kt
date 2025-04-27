package com.numplates.nomera3.modules.comments.ui.viewholder

import android.view.ViewGroup
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieDrawable
import com.numplates.nomera3.LOTTIE_LOADER_ANIMATION
import com.numplates.nomera3.LOTTIE_LOADER_SPEED
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.notifications.ui.viewholder.BaseViewHolder

class CommentProgressViewHolder(
    viewGroup: ViewGroup
) : BaseViewHolder(viewGroup, R.layout.item_comment_progress) {

    private val imageProgress: LottieAnimationView = itemView.findViewById(R.id.lav_progress_indicator)

    fun bind() {
        imageProgress.setAnimation(LOTTIE_LOADER_ANIMATION)
        imageProgress.speed = LOTTIE_LOADER_SPEED
        imageProgress.repeatCount = LottieDrawable.INFINITE
        imageProgress.playAnimation()
    }
}

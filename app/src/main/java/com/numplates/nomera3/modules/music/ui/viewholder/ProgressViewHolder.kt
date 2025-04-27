package com.numplates.nomera3.modules.music.ui.viewholder

import android.view.ViewGroup
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieDrawable
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.baseCore.BaseViewHolder

class ProgressViewHolder(parent: ViewGroup) : BaseViewHolder(parent, R.layout.progress_view) {
    private val progress: LottieAnimationView = itemView.findViewById(R.id.lottie_imgProgress)

    fun bind(){
        startProgress()
    }

    private fun startProgress() {
        progress.repeatCount = LottieDrawable.INFINITE
        progress.playAnimation()
    }
}

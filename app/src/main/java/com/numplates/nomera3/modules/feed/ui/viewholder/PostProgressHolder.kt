package com.numplates.nomera3.modules.feed.ui.viewholder

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieDrawable
import com.numplates.nomera3.LOTTIE_LOADER_ANIMATION
import com.numplates.nomera3.LOTTIE_LOADER_SPEED
import com.numplates.nomera3.R
import timber.log.Timber

class PostProgressHolder(private val view: View): RecyclerView.ViewHolder(view) {

    private val lottieImgProgress: LottieAnimationView? = view.findViewById(R.id.lottie_imgProgress)

    fun bind() {
        Timber.d("BIND Post Progress holder")
        lottieImgProgress?.setAnimation(LOTTIE_LOADER_ANIMATION)
        lottieImgProgress?.speed = LOTTIE_LOADER_SPEED
        lottieImgProgress?.repeatCount = LottieDrawable.INFINITE
        lottieImgProgress?.playAnimation()
    }

}
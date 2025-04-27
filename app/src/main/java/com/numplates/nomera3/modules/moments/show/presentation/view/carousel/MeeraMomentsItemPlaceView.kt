package com.numplates.nomera3.modules.moments.show.presentation.view.carousel

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.bumptech.glide.Glide
import com.numplates.nomera3.databinding.ItemMomentPlaceBinding

class MeeraMomentsItemPlaceView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding = ItemMomentPlaceBinding.inflate(LayoutInflater.from(context), this, true)

    fun setName(name: String) {
        binding.tvMomentItemPlaceName.text = name
    }

    fun setDistance(distance: String) {
        binding.tvMomentItemPlaceDistance.text = distance
    }

    fun setBackground(url: String) {
        Glide.with(context)
            .load(url)
            .centerCrop()
            .into(binding.ivMomentItemBackground)
    }

    fun setWatchedStatus(isWatched: Boolean) {
        binding.ivMomentItemBackground.alpha = if (isWatched) 0.5f else 1.0f
    }
}


package com.numplates.nomera3.modules.feed.ui.viewholder

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.numplates.nomera3.databinding.MomentCarouselShimmerBinding
import com.numplates.nomera3.modules.moments.show.data.entity.MomentInfoCarouselUiModel
import com.numplates.nomera3.modules.moments.show.presentation.data.MomentCarouselItem

class ShimmerMomentViewHolder(val binding: MomentCarouselShimmerBinding) : RecyclerView.ViewHolder(binding.root) {

    companion object {
        private val shimmerModel = MomentInfoCarouselUiModel(
            momentsCarouselList = listOf(
                MomentCarouselItem.BlankShimmer,
                MomentCarouselItem.BlankShimmer,
                MomentCarouselItem.BlankShimmer,
                MomentCarouselItem.BlankShimmer
            )
        )
    }

    init {
        binding.rvMomentsBlankShimmer.apply {
            layoutManager = object : LinearLayoutManager(context, HORIZONTAL, false) { override fun canScrollHorizontally() = false }
            isNestedScrollingEnabled = false
            submitMoments(shimmerModel)
        }
    }

    fun clearResource() {
        binding.rvMomentsBlankShimmer.clearResources()
    }
}

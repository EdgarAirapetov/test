package com.numplates.nomera3.modules.feed.ui.viewholder

import androidx.recyclerview.widget.RecyclerView
import com.meera.core.extensions.setThrottledClickListener
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.ItemFeedPostsViewedBinding
import com.numplates.nomera3.modules.feed.ui.MeeraPostCallback
import com.numplates.nomera3.modules.feed.ui.adapter.FeedType
import com.numplates.nomera3.modules.feed.ui.util.divider.IDividedPost

class MeeraPostsViewedViewHolder(
    private val binding: ItemFeedPostsViewedBinding
) : RecyclerView.ViewHolder(binding.root), IDividedPost, PostCallbackHolder {

    var feedType: FeedType? = null
    private var postCallback : MeeraPostCallback? = null

    override fun isVip() = feedType == FeedType.POSTS_VIEWED_PROFILE_VIP

    override fun initCallback(meeraPostCallback: MeeraPostCallback?) {
        this.postCallback = meeraPostCallback
    }

    fun clearResources() {
        binding.btnFindPeople.setOnClickListener(null)
        postCallback = null
    }

    fun bind(type: FeedType) {
        this.feedType = type
        binding.btnFindPeople.setThrottledClickListener { postCallback?.onFindPeoplesClicked() }
        setupText(type)
    }

    private fun setupText(type: FeedType) {
        when (type) {
            FeedType.POSTS_VIEWED_PROFILE, FeedType.POSTS_VIEWED_PROFILE_VIP -> {
                binding.tvHeader.text = itemView.context.getString(R.string.all_posts_viewed)
                binding.tvDescription.text = itemView.context.getString(R.string.posts_viewed_profile_description)
            }
            FeedType.POSTS_VIEWED_ROAD -> {
                binding.tvHeader.text = itemView.context.getString(R.string.all_new_posts_viewed)
                binding.tvDescription.text = itemView.context.getString(R.string.posts_viewed_road_description)
            }
            else -> Unit
        }
    }
}

package com.numplates.nomera3.modules.feed.ui.viewholder

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.meera.core.extensions.dp
import com.meera.core.extensions.setThrottledClickListener
import com.numplates.nomera3.databinding.MeeraItemProfileSuggestionsFloorBinding
import com.numplates.nomera3.modules.feed.ui.MeeraPostCallback
import com.numplates.nomera3.modules.feed.ui.entity.PostUIEntity
import com.numplates.nomera3.modules.feed.ui.util.divider.IDividedPost
import com.numplates.nomera3.modules.userprofile.ui.adapter.MeeraProfileSuggestionsAdapter
import com.numplates.nomera3.modules.userprofile.ui.model.UserProfileUIAction

private const val HORIZONTAL_ITEM_PADDING_DP = 8

class MeeraRoadSuggestionsViewHolder(
    val binding: MeeraItemProfileSuggestionsFloorBinding
) : RecyclerView.ViewHolder(binding.root), IDividedPost, PostCallbackHolder {

    override fun isVip() = false
    private var postCallback: MeeraPostCallback? = null
    private var profileSuggestionItemDecoration: RecyclerView.ItemDecoration? = null

    private var adapter: MeeraProfileSuggestionsAdapter? = null

    private val profileUIActionHandler: (UserProfileUIAction) -> Unit = { action ->
        when (action) {
            is UserProfileUIAction.OnSuggestionUserClicked -> {
                postCallback?.onSuggestedUserClicked(
                    action.isTopContentMaker,
                    action.isApproved,
                    action.hasMutualFriends,
                    action.isSubscribed,
                    action.toUserId
                )
            }
            is UserProfileUIAction.RemoveFriendSuggestion -> {
                postCallback?.onRemoveFriendSuggestedUserClicked(action.userId)
            }
            is UserProfileUIAction.UnsubscribeSuggestion -> {
                postCallback?.onUnsubscribeSuggestedUserClicked(
                    action.userId,
                    action.isApprovedUser,
                    action.topContentMaker
                )
            }
            is UserProfileUIAction.AddFriendSuggestion -> {
                postCallback?.onAddFriendSuggestedUserClicked(
                    action.userId,
                    action.isApprovedUser,
                    action.topContentMaker
                )
            }
            is UserProfileUIAction.SubscribeSuggestion -> {
                postCallback?.onSubscribeSuggestedUserClicked(
                    action.userId,
                    action.isApprovedUser,
                    action.topContentMaker
                )
            }
            is UserProfileUIAction.BlockSuggestionById -> {
                postCallback?.onHideSuggestedUserClicked(action.userId)
            }
            else -> Unit
        }
    }

    init {
        adapter = MeeraProfileSuggestionsAdapter(profileUIActionHandler)
        val layoutManager =
            LinearLayoutManager(itemView.context, LinearLayoutManager.HORIZONTAL, false)
        binding.rvProfileSuggestions.adapter = adapter
        binding.rvProfileSuggestions.layoutManager = layoutManager
        binding.rvProfileSuggestions.isNestedScrollingEnabled = false

        profileSuggestionItemDecoration = object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(
                outRect: Rect,
                view: View,
                parent: RecyclerView,
                state: RecyclerView.State
            ) = with(outRect) {
                left = HORIZONTAL_ITEM_PADDING_DP.dp
            }
        }.also { decoration ->
            binding.rvProfileSuggestions.addItemDecoration(decoration)
        }

        binding.btnShowMore.setThrottledClickListener {
            postCallback?.onShowMoreSuggestionsClicked()
        }
    }

    override fun initCallback(meeraPostCallback: MeeraPostCallback?) {
        this.postCallback = meeraPostCallback
    }

    fun clearResources() {
        binding.apply {
            profileSuggestionItemDecoration?.let {
                rvProfileSuggestions.removeItemDecoration(it)
                profileSuggestionItemDecoration = null
            }
            adapter?.clearResources()
            adapter = null
            rvProfileSuggestions.adapter = null
            rvProfileSuggestions.layoutManager = null
            postCallback = null
        }
    }

    fun bind(item: PostUIEntity) {
        item.featureData?.suggestions?.let { adapter?.submitList(ArrayList(it)) }
    }

}

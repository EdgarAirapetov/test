package com.numplates.nomera3.modules.feed.ui.viewholder

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.meera.core.extensions.dp
import com.meera.core.extensions.setThrottledClickListener
import com.numplates.nomera3.databinding.ItemRoadSuggestionsBinding
import com.numplates.nomera3.modules.feed.ui.PostCallback
import com.numplates.nomera3.modules.feed.ui.entity.PostUIEntity
import com.numplates.nomera3.modules.feed.ui.util.divider.IDividedPost
import com.numplates.nomera3.modules.userprofile.ui.adapter.ProfileSuggestionsAdapter
import com.numplates.nomera3.modules.userprofile.ui.model.UserProfileUIAction

private const val HORIZONTAL_ITEM_PADDING_DP = 8

class RoadSuggestionsViewHolder(
    binding: ItemRoadSuggestionsBinding,
    private val callback: PostCallback
) : ViewHolder(binding.root), IDividedPost {

    override fun isVip() = false

    private var adapter: ProfileSuggestionsAdapter? = null

    private val profileUIActionHandler: (UserProfileUIAction) -> Unit = { action ->
        when (action) {
            is UserProfileUIAction.OnSuggestionUserClicked -> {
                callback.onSuggestedUserClicked(
                    action.isTopContentMaker,
                    action.isApproved,
                    action.hasMutualFriends,
                    action.isSubscribed,
                    action.toUserId
                )
            }
            is UserProfileUIAction.RemoveFriendSuggestion -> {
                callback.onRemoveFriendSuggestedUserClicked(action.userId)
            }
            is UserProfileUIAction.UnsubscribeSuggestion -> {
                callback.onUnsubscribeSuggestedUserClicked(
                    action.userId,
                    action.isApprovedUser,
                    action.topContentMaker
                )
            }
            is UserProfileUIAction.AddFriendSuggestion -> {
                callback.onAddFriendSuggestedUserClicked(
                    action.userId,
                    action.isApprovedUser,
                    action.topContentMaker
                )
            }
            is UserProfileUIAction.SubscribeSuggestion -> {
                callback.onSubscribeSuggestedUserClicked(
                    action.userId,
                    action.isApprovedUser,
                    action.topContentMaker
                )
            }
            is UserProfileUIAction.BlockSuggestionById -> {
                callback.onHideSuggestedUserClicked(action.userId)
            }
            else -> Unit
        }
    }

    init {
        adapter = ProfileSuggestionsAdapter(profileUIActionHandler)
        val layoutManager = LinearLayoutManager(itemView.context, LinearLayoutManager.HORIZONTAL, false)
        binding.rvProfileSuggestions.adapter = adapter
        binding.rvProfileSuggestions.layoutManager = layoutManager
        binding.rvProfileSuggestions.isNestedScrollingEnabled = false
        binding.rvProfileSuggestions.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(
                outRect: Rect,
                view: View,
                parent: RecyclerView,
                state: RecyclerView.State
            ) = with(outRect) {
                left = HORIZONTAL_ITEM_PADDING_DP.dp
            }
        })
        binding.tvShowMore.setThrottledClickListener {
            callback.onShowMoreSuggestionsClicked()
        }
    }

    fun bind(item: PostUIEntity) {
        item.featureData?.suggestions?.let { adapter?.submitList(ArrayList(it)) }
    }

}

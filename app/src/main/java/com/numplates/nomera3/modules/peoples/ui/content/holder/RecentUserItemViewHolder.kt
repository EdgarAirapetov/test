package com.numplates.nomera3.modules.peoples.ui.content.holder

import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.meera.core.extensions.clickAnimate
import com.meera.core.extensions.setThrottledClickListener
import com.numplates.nomera3.databinding.SearchRecentUserItemBinding
import com.numplates.nomera3.modules.peoples.ui.content.action.FriendsContentActions
import com.numplates.nomera3.modules.peoples.ui.content.entity.RecentUserUiModel

class RecentUserItemViewHolder(
    private val binding: SearchRecentUserItemBinding,
    private val actionListener: (FriendsContentActions) -> Unit
) : ViewHolder(binding.root) {

    fun bind(item: RecentUserUiModel) {
        binding.apply {
            root.setThrottledClickListener {
                searchRecentItemAvatarView.clickAnimate()
                actionListener.invoke(FriendsContentActions.SelectRecentItemUiAction(item))
            }
            searchRecentItemNameText.text = item.name
            searchRecentItemAvatarView.setUp(
                context = itemView.context,
                avatarLink = item.image,
                accountType = item.accountType.value,
                frameColor = item.accountColor,
                hasMoments = item.hasMoments,
                hasNewMoments = item.hasNewMoments
            )
        }
    }
}

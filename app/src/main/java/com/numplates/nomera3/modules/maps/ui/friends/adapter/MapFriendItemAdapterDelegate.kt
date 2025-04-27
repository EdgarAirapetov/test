package com.numplates.nomera3.modules.maps.ui.friends.adapter

import androidx.core.view.isVisible
import com.hannesdorfmann.adapterdelegates4.dsl.adapterDelegate
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.utils.enableApprovedIcon
import com.numplates.nomera3.R
import com.numplates.nomera3.data.network.core.INetworkValues
import com.numplates.nomera3.databinding.ItemMapEventFriendBinding
import com.numplates.nomera3.modules.maps.ui.friends.model.MapFriendListItem
import com.numplates.nomera3.modules.maps.ui.friends.model.MapFriendsListUiAction

fun eventItemAdapterDelegate(itemActionListener: (MapFriendsListUiAction) -> Unit) =
    adapterDelegate<MapFriendListItem.MapFriendUiModel, MapFriendListItem>(R.layout.item_map_event_friend) {
        val binding = ItemMapEventFriendBinding.bind(itemView)
        bind { _ ->
            EventItemBinder(
                binding = binding,
                itemUiModel = item,
                itemActionListener = itemActionListener,
                position = bindingAdapterPosition
            ).bind()
        }
    }

private class EventItemBinder(
    private val binding: ItemMapEventFriendBinding,
    private val itemUiModel: MapFriendListItem.MapFriendUiModel,
    private val itemActionListener: (MapFriendsListUiAction) -> Unit,
    private val position: Int
) {
    private val context = binding.root.context

    fun bind() {
        binding.tvItemMapFriendTitle.text = itemUiModel.name
        binding.tvItemMapFriendSubtitle.text = itemUiModel.uniqueName
        val context = binding.root.context
        binding.tvItemMapFriendTitle.enableApprovedIcon(
            enabled = itemUiModel.approved == 1,
            isVip = itemUiModel.accountType != INetworkValues.ACCOUNT_TYPE_REGULAR,
            topContentMaker = itemUiModel.topContentMaker
        )
        binding.vvMapFriendAvatar.setUp(
            context = context,
            avatarLink = itemUiModel.avatarUrl,
            accountType = itemUiModel.accountType,
            frameColor = itemUiModel.accountColor,
            hasShadow = false,
            hasMoments = itemUiModel.moments?.hasMoments ?: false,
            hasNewMoments = itemUiModel.moments?.hasNewMoments ?: false,
        )
        binding.llContainer.setThrottledClickListener {
            itemActionListener.invoke(
                MapFriendsListUiAction.ParticipantClicked(
                    itemUiModel,
                    binding.vvMapFriendAvatar,
                    position
                )
            )
        }

        binding.vvMapFriendAvatar.setThrottledClickListener {
            itemActionListener.invoke(
                MapFriendsListUiAction.ParticipantClicked(
                    itemUiModel,
                    binding.vvMapFriendAvatar,
                    position,
                    true
                )
            )
        }

        binding.ivMessage.isVisible = itemUiModel.iCanChat
        if (itemUiModel.iCanChat) {
            binding.ivMessage.setThrottledClickListener {
                itemActionListener.invoke(
                    MapFriendsListUiAction.SendMessageClicked(
                        itemUiModel, position
                    )
                )
            }
        }
    }
}

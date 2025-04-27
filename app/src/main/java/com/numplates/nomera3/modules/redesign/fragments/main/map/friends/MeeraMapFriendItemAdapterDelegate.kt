package com.numplates.nomera3.modules.redesign.fragments.main.map.friends

import android.widget.ImageView
import com.hannesdorfmann.adapterdelegates4.dsl.adapterDelegate
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.toBoolean
import com.meera.uikit.widgets.people.TopAuthorApprovedUserModel
import com.meera.uikit.widgets.userpic.UserpicUiModel
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraItemMapEventFriendBinding
import com.numplates.nomera3.modules.maps.ui.friends.model.MapFriendListItem
import com.numplates.nomera3.modules.maps.ui.friends.model.MapFriendsListUiAction

fun meeraEventItemAdapterDelegate(itemActionListener: (MapFriendsListUiAction) -> Unit) =
    adapterDelegate<MapFriendListItem.MapFriendUiModel, MapFriendListItem>(R.layout.meera_item_map_event_friend) {
        val binding = MeeraItemMapEventFriendBinding.bind(itemView)
        bind { _ ->
            MeeraEventItemBinder(
                binding = binding,
                itemUiModel = item,
                itemActionListener = itemActionListener,
                position = bindingAdapterPosition
            ).bind()
        }
    }

private class MeeraEventItemBinder(
    private val binding: MeeraItemMapEventFriendBinding,
    private val itemUiModel: MapFriendListItem.MapFriendUiModel,
    private val itemActionListener: (MapFriendsListUiAction) -> Unit,
    private val position: Int
) {

    fun bind() {
        binding.eventFriendItem.apply {
            setLeftUserPicConfig(
                UserpicUiModel(
                    userAvatarUrl = itemUiModel.avatarUrl,
                    scaleType = ImageView.ScaleType.CENTER_CROP
                )
            )
            setTitleValue(itemUiModel.name)
            cellCityText = true
            setCityValue(itemUiModel.uniqueName)
            enableTopContentAuthorApprovedUser(
                TopAuthorApprovedUserModel(
                    approved = itemUiModel.approved.toBoolean(),
                    interestingAuthor = itemUiModel.topContentMaker,
                )
            )
            setThrottledClickListener {
                itemActionListener.invoke(
                    MapFriendsListUiAction.ParticipantClicked(
                        itemUiModel,
                        binding.root,
                        position
                    )
                )
            }
            cellRightIconClickListener = {
                if (itemUiModel.iCanChat) {
                    itemActionListener.invoke(
                        MapFriendsListUiAction.SendMessageClicked(
                            itemUiModel, position
                        )
                    )
                }
            }
        }
    }
}

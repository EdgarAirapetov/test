package com.numplates.nomera3.modules.redesign.fragments.main.map.friends

import androidx.recyclerview.widget.DiffUtil
import com.hannesdorfmann.adapterdelegates4.AsyncListDifferDelegationAdapter
import com.numplates.nomera3.modules.maps.ui.friends.adapter.searchEmptyItemAdapterDelegate
import com.numplates.nomera3.modules.maps.ui.friends.adapter.stubItemAdapterDelegate
import com.numplates.nomera3.modules.maps.ui.friends.model.MapFriendListItem
import com.numplates.nomera3.modules.maps.ui.friends.model.MapFriendsListUiAction

class MeeraEventFriendsListItemAdapter(
    onItemSelected: (MapFriendsListUiAction) -> Unit
) : AsyncListDifferDelegationAdapter<MapFriendListItem>(
    DiffCallback(),
    meeraEventItemAdapterDelegate(onItemSelected),
    stubItemAdapterDelegate(),
    meeraEmptyItemAdapterDelegate(),
    searchEmptyItemAdapterDelegate(),
    noFriendAdapterDelegate(onItemSelected)
) {

    private class DiffCallback : DiffUtil.ItemCallback<MapFriendListItem>() {
        override fun areItemsTheSame(
            oldItem: MapFriendListItem,
            newItem: MapFriendListItem
        ): Boolean {
            return oldItem.isTheSame(newItem)
        }

        override fun areContentsTheSame(
            oldItem: MapFriendListItem,
            newItem: MapFriendListItem
        ): Boolean  {
            return oldItem.isContentTheSame(newItem)
        }
    }
}


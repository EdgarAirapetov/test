package com.numplates.nomera3.modules.maps.ui.friends.adapter

import androidx.recyclerview.widget.DiffUtil
import com.hannesdorfmann.adapterdelegates4.AsyncListDifferDelegationAdapter
import com.numplates.nomera3.modules.maps.ui.friends.model.MapFriendListItem
import com.numplates.nomera3.modules.maps.ui.friends.model.MapFriendsListUiAction

class EventFriendsListItemAdapter(
    onItemSelected: (MapFriendsListUiAction) -> Unit
) : AsyncListDifferDelegationAdapter<MapFriendListItem>(
    DiffCallback(),
    eventItemAdapterDelegate(onItemSelected),
    stubItemAdapterDelegate(),
    emptyItemAdapterDelegate(),
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


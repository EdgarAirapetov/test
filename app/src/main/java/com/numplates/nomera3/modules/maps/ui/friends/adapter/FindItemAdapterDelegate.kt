package com.numplates.nomera3.modules.maps.ui.friends.adapter

import com.hannesdorfmann.adapterdelegates4.dsl.adapterDelegate
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.ItemMapNoFriendBinding
import com.numplates.nomera3.modules.maps.ui.friends.model.MapFriendListItem
import com.numplates.nomera3.modules.maps.ui.friends.model.MapFriendsListUiAction

fun noFriendAdapterDelegate(onItemSelected: (MapFriendsListUiAction) -> Unit) =
    adapterDelegate<MapFriendListItem.FindFriendItemUiModel, MapFriendListItem>(R.layout.item_map_no_friend) {
        val binding = ItemMapNoFriendBinding.bind(itemView)
        bind { _ ->
            NoFriendItemBinder(
                binding = binding,
                itemActionListener = onItemSelected
            ).bind()
        }
    }

private class NoFriendItemBinder(
    private val binding: ItemMapNoFriendBinding,
    private val itemActionListener: (MapFriendsListUiAction) -> Unit
) {

    fun bind() {
        binding.tvFindFriends.setOnClickListener {
            itemActionListener.invoke(
                MapFriendsListUiAction.OpenPeople
            )
        }
    }
}


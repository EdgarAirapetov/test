package com.numplates.nomera3.modules.redesign.fragments.main.map.friends

import com.hannesdorfmann.adapterdelegates4.dsl.adapterDelegate
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraItemMapNoFriendBinding
import com.numplates.nomera3.modules.maps.ui.friends.model.MapFriendListItem
import com.numplates.nomera3.modules.maps.ui.friends.model.MapFriendsListUiAction

fun noFriendAdapterDelegate(onItemSelected: (MapFriendsListUiAction) -> Unit) =
    adapterDelegate<MapFriendListItem.FindFriendItemUiModel, MapFriendListItem>(R.layout.meera_item_map_no_friend) {
        val binding = MeeraItemMapNoFriendBinding.bind(itemView)
        bind { _ ->
            NoFriendItemBinder(
                binding = binding,
                itemActionListener = onItemSelected
            ).bind()
        }
    }

private class NoFriendItemBinder(
    private val binding: MeeraItemMapNoFriendBinding,
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


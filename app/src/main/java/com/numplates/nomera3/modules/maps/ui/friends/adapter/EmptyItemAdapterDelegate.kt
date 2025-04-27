package com.numplates.nomera3.modules.maps.ui.friends.adapter

import com.hannesdorfmann.adapterdelegates4.dsl.adapterDelegate
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.maps.ui.friends.model.MapFriendListItem

fun emptyItemAdapterDelegate() =
    adapterDelegate<MapFriendListItem.EmptyItemUiModel, MapFriendListItem>(R.layout.item_map_friend_empty) {}


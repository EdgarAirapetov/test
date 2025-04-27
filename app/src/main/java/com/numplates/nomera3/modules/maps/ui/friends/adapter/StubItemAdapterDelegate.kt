package com.numplates.nomera3.modules.maps.ui.friends.adapter

import com.hannesdorfmann.adapterdelegates4.dsl.adapterDelegate
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.maps.ui.friends.model.MapFriendListItem

fun stubItemAdapterDelegate() =
    adapterDelegate<MapFriendListItem.StubItemUiModel, MapFriendListItem>(R.layout.item_map_friends_list_stub) {}


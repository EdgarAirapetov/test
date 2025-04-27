package com.numplates.nomera3.modules.maps.ui.friends.adapter

import com.hannesdorfmann.adapterdelegates4.dsl.adapterDelegate
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.maps.ui.friends.model.MapFriendListItem

fun searchEmptyItemAdapterDelegate() =
    adapterDelegate<MapFriendListItem.EmptySearchItemUiModel, MapFriendListItem>(R.layout.map_friends_item_search_empty) {}


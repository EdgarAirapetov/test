package com.numplates.nomera3.modules.redesign.fragments.main.map.friends

import com.hannesdorfmann.adapterdelegates4.dsl.adapterDelegate
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.maps.ui.friends.model.MapFriendListItem

fun meeraSearchEmptyItemAdapterDelegate() =
    adapterDelegate<MapFriendListItem.EmptySearchItemUiModel, MapFriendListItem>(R.layout.map_friends_item_search_empty) {}


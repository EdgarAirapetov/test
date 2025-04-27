package com.numplates.nomera3.modules.redesign.fragments.main.map.friends

import com.hannesdorfmann.adapterdelegates4.dsl.adapterDelegate
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.maps.ui.friends.model.MapFriendListItem

fun meeraEmptyItemAdapterDelegate() =
    adapterDelegate<MapFriendListItem.EmptyItemUiModel, MapFriendListItem>(R.layout.meera_item_map_friend_empty) {}


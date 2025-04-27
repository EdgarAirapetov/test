package com.numplates.nomera3.modules.maps.ui.friends.model

data class MapFriendsListUiModel(
    val items: List<MapFriendListItem>,
    val isLoadingNextPage: Boolean,
    val isLastPage: Boolean,
    val updatePosition: Boolean
)

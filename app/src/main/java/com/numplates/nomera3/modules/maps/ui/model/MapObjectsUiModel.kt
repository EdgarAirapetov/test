package com.numplates.nomera3.modules.maps.ui.model

data class MapObjectsUiModel(
    val users: List<MapUserUiModel>,
    val clusters: List<MapClusterUiModel>,
    val nearestFriend: NearestFriendUiModel?
)

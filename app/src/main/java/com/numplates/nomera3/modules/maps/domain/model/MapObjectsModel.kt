package com.numplates.nomera3.modules.maps.domain.model

data class MapObjectsModel(
    val users: List<MapUserModel>,
    val clusters: List<MapClusterModel>,
    val nearestFriend: NearestFriendModel?
)

package com.numplates.nomera3.data.network

import com.google.gson.annotations.SerializedName
import com.numplates.nomera3.modules.maps.data.model.NearestFriendDto

data class MapObjectsDto(
    @SerializedName("clusters") var clusters: List<MapClusterDto>,
    @SerializedName("users") var users: List<MapUserDto>,
    @SerializedName("nearest_friend") val nearestFriend: NearestFriendDto?,
)

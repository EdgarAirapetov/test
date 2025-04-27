package com.numplates.nomera3.modules.maps.domain.events.model

data class GetMapFriendsParamsModel(
    val offset: Int,
    val limit: Int,
    val search: String?,
)

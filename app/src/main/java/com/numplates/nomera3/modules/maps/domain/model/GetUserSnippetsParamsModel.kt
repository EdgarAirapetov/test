package com.numplates.nomera3.modules.maps.domain.model

data class GetUserSnippetsParamsModel(
    val selectedUserId: Long,
    val excludedUserIds: List<Long>,
    val lat: Double,
    val lon: Double,
    val limit: Int
)

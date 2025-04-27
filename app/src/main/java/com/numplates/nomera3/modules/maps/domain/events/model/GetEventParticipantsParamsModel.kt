package com.numplates.nomera3.modules.maps.domain.events.model

data class GetEventParticipantsParamsModel(
    val eventId: Long,
    val offset: Int,
    val limit: Int
)

package com.numplates.nomera3.modules.maps.domain.events.model

import com.meera.db.models.message.ParsedUniquename

data class EventModel(
    val id: Long,
    val title: String,
    val tagSpan: ParsedUniquename?,
    val address: AddressModel,
    val timestampMs: Long,
    val timeZoneId: String,
    val eventType: EventType,
    val participantAvatars: List<String?>,
    val participation: ParticipationModel,
)

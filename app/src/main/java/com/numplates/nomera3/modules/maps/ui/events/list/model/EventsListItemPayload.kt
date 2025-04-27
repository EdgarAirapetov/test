package com.numplates.nomera3.modules.maps.ui.events.list.model

import com.numplates.nomera3.modules.maps.ui.events.participants.view.model.EventParticipantsUiModel

sealed interface EventsListItemPayload {
    data class EventItemParticipation(val eventParticipants: EventParticipantsUiModel) : EventsListItemPayload
}

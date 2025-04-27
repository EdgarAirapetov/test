package com.numplates.nomera3.modules.maps.ui.events.participants.view.model

sealed interface EventParticipantsUiAction {
    object JoinEvent : EventParticipantsUiAction
    object LeaveEvent : EventParticipantsUiAction
    object ShowEventOnMap : EventParticipantsUiAction
    object ShowEventCreator : EventParticipantsUiAction
    object ShowEventParticipants : EventParticipantsUiAction
    object NavigateToEvent : EventParticipantsUiAction
    object HandleJoinAnimationFinished : EventParticipantsUiAction
}

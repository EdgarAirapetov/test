package com.numplates.nomera3.modules.maps.ui.events.participants.list.model

sealed interface EventParticipantsListUiAction {
    data class ParticipantClicked(val userId: Long) : EventParticipantsListUiAction
    object LeaveEventClicked : EventParticipantsListUiAction
    data class RemoveParticipantClicked(val userId: Long) : EventParticipantsListUiAction
    data class ParticipantOptionsClicked(
        val item: EventParticipantsListItemUiModel
    ) : EventParticipantsListUiAction
    data class ViewInitialized(
        val params: EventParticipantsParamsUiModel
    ) : EventParticipantsListUiAction
    object RefreshRequested : EventParticipantsListUiAction
    object LoadNextPageRequested : EventParticipantsListUiAction
}

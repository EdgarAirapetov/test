package com.numplates.nomera3.modules.maps.ui.events.participants.list.model

sealed interface EventParticipantsListUiEffect {
    data class OpenUserProfile(val userId: Long) : EventParticipantsListUiEffect
    data class ShowParticipantMenu(
        val userId: Long,
        val removeOption: ParticipantRemoveOption
    ) : EventParticipantsListUiEffect
}

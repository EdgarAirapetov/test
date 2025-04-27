package com.numplates.nomera3.modules.maps.ui.events.participants.list.model

sealed interface ParticipantRemoveOption {
    object RemoveNotAvailable : ParticipantRemoveOption
    object CanRemove : ParticipantRemoveOption
    object CanLeave : ParticipantRemoveOption
}

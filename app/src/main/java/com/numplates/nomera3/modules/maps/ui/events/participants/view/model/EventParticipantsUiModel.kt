package com.numplates.nomera3.modules.maps.ui.events.participants.view.model

import com.numplates.nomera3.modules.maps.domain.events.model.ParticipationModel

data class EventParticipantsUiModel(
    val hostAvatar: String?,
    val participantsAvatars: List<String?>,
    val participation: ParticipationModel,
    val showMap: Boolean,
    val isCompact: Boolean,
    val isVip: Boolean,
    val isFinished: Boolean
)

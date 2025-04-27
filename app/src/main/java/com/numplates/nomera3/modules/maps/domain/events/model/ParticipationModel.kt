package com.numplates.nomera3.modules.maps.domain.events.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ParticipationModel(
    val participantsCount: Int,
    val isHost: Boolean,
    val isParticipant: Boolean,
    val newParticipants: Int
) : Parcelable

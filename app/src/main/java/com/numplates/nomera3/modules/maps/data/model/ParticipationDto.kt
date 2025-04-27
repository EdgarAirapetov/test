package com.numplates.nomera3.modules.maps.data.model

import com.google.gson.annotations.SerializedName

data class ParticipationDto(
    @SerializedName("members_count") val participantsCount: Int,
    @SerializedName("owner_membership") val isHost: Int,
    @SerializedName("user_membership") val isParticipant: Int,
    @SerializedName("newly_applied") val newlyApplied: Int,
)

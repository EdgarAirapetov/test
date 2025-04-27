package com.numplates.nomera3.modules.maps.data.model

import com.google.gson.annotations.SerializedName

data class JoinEventBodyDto(
    @SerializedName("event_id") val eventId: Long,
)

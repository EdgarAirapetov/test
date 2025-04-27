package com.numplates.nomera3.modules.maps.data.model

import com.google.gson.annotations.SerializedName

data class GetEventResultDto(
    @SerializedName("event") val event: EventDto
)

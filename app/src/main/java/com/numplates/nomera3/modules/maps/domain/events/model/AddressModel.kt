package com.numplates.nomera3.modules.maps.domain.events.model

import com.numplates.nomera3.modules.baseCore.domain.model.CoordinatesModel

data class AddressModel(
    val name: String,
    val addressString: String,
    val location: CoordinatesModel,
    val timeZoneId: String
)

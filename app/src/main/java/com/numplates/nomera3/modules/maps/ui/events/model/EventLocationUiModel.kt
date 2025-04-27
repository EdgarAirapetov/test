package com.numplates.nomera3.modules.maps.ui.events.model

import com.numplates.nomera3.modules.baseCore.domain.model.CoordinatesModel

data class EventLocationUiModel(
    val location: CoordinatesModel,
    val name: String
)

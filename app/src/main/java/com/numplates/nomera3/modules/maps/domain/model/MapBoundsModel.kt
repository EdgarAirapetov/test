package com.numplates.nomera3.modules.maps.domain.model

import com.numplates.nomera3.modules.baseCore.domain.model.CoordinatesModel

data class MapBoundsModel(
    val southWest: CoordinatesModel,
    val northEast: CoordinatesModel
)

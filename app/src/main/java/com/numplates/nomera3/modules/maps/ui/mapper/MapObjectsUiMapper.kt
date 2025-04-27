package com.numplates.nomera3.modules.maps.ui.mapper

import com.numplates.nomera3.modules.maps.domain.model.MapObjectsModel
import com.numplates.nomera3.modules.maps.ui.model.MapObjectsUiModel

interface MapObjectsUiMapper {
    fun mapMapObjects(mapObjectsModel: MapObjectsModel): MapObjectsUiModel
}

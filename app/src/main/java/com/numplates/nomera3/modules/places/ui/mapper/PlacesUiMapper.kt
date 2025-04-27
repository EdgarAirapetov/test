package com.numplates.nomera3.modules.places.ui.mapper

import com.numplates.nomera3.modules.places.domain.model.PlaceModel
import com.numplates.nomera3.modules.places.ui.model.PlaceItemUiModel

interface PlacesUiMapper {
    fun mapPlaceItem(placeModel: PlaceModel): PlaceItemUiModel
}

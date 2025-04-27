package com.numplates.nomera3.modules.places.ui.mapper

import com.numplates.nomera3.modules.places.domain.model.PlaceModel
import com.numplates.nomera3.modules.places.ui.model.PlaceItemUiModel
import javax.inject.Inject

class PlacesUiMapperImpl @Inject constructor() : PlacesUiMapper {

    override fun mapPlaceItem(placeModel: PlaceModel): PlaceItemUiModel = PlaceItemUiModel(
        title = placeModel.name,
        address = placeModel.addressString,
        place = placeModel
    )
}

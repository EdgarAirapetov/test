package com.numplates.nomera3.modules.places.domain

import com.numplates.nomera3.modules.baseCore.domain.model.CoordinatesModel
import com.numplates.nomera3.modules.places.domain.model.PlaceModel

interface PlacesRepository {
    suspend fun getPlacesByLocation(coordinatesModel: CoordinatesModel): List<PlaceModel>
    suspend fun searchPlacesByText(text: String): List<PlaceModel>
}

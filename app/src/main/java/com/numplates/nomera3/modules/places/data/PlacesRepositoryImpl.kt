package com.numplates.nomera3.modules.places.data

import com.numplates.nomera3.data.network.ApiMain
import com.numplates.nomera3.modules.baseCore.domain.model.CoordinatesModel
import com.numplates.nomera3.modules.places.data.mapper.PlacesDataMapper
import com.numplates.nomera3.modules.places.domain.PlacesRepository
import com.numplates.nomera3.modules.places.domain.model.PlaceModel
import javax.inject.Inject

class PlacesRepositoryImpl @Inject constructor(
    private val apiMain: ApiMain,
    private val mapper: PlacesDataMapper
) : PlacesRepository {
    override suspend fun getPlacesByLocation(coordinatesModel: CoordinatesModel): List<PlaceModel> = apiMain
        .getPlaces(
            lat = coordinatesModel.lat,
            lon = coordinatesModel.lon
        )
        .data
        .map(mapper::mapPlaceModel)

    override suspend fun searchPlacesByText(text: String): List<PlaceModel> = apiMain
        .getPlaces(keyword = text)
        .data
        .map(mapper::mapPlaceModel)
}

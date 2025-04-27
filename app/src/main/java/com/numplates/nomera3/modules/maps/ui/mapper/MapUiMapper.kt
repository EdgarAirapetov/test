package com.numplates.nomera3.modules.maps.ui.mapper

import com.numplates.nomera3.modules.maps.ui.events.mapper.MapEventsUiMapper
import com.numplates.nomera3.modules.maps.ui.events.mapper.MapEventsUiMapperImpl
import com.numplates.nomera3.modules.maps.ui.model.MapMode
import com.numplates.nomera3.modules.maps.ui.model.MapUiState
import com.numplates.nomera3.modules.maps.ui.model.MapUiValuesUiModel
import com.numplates.nomera3.modules.places.ui.mapper.PlacesUiMapper
import com.numplates.nomera3.modules.places.ui.mapper.PlacesUiMapperImpl
import javax.inject.Inject

class MapUiMapper @Inject constructor(
    private val objectsUiMapperImpl: MapObjectsUiMapperImpl,
    private val locationUiMapperImpl: LocationUiMapperImpl,
    private val eventsUiMapperImpl: MapEventsUiMapperImpl,
    private val placesUiMapperImpl: PlacesUiMapperImpl,
    private val mapAnalyticsMapperImpl: MapAnalyticsMapperImpl
) : MapObjectsUiMapper by objectsUiMapperImpl,
    LocationUiMapper by locationUiMapperImpl,
    MapEventsUiMapper by eventsUiMapperImpl,
    PlacesUiMapper by placesUiMapperImpl,
    MapAnalyticsMapper by mapAnalyticsMapperImpl {

    fun mapUiState(
        mapMode: MapMode,
        mapUiValues: MapUiValuesUiModel,
        nonDefaultLayersSettings: Boolean
    ): MapUiState =
        MapUiState(
            mapMode = mapMode,
            mapUiValues = mapUiValues,
            nonDefaultLayersSettings = nonDefaultLayersSettings
        )
}

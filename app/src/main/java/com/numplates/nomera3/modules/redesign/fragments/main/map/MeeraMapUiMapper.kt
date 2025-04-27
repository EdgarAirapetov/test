package com.numplates.nomera3.modules.redesign.fragments.main.map

import com.numplates.nomera3.modules.maps.ui.mapper.LocationUiMapper
import com.numplates.nomera3.modules.maps.ui.mapper.LocationUiMapperImpl
import com.numplates.nomera3.modules.maps.ui.mapper.MapAnalyticsMapper
import com.numplates.nomera3.modules.maps.ui.mapper.MapAnalyticsMapperImpl
import com.numplates.nomera3.modules.maps.ui.mapper.MapObjectsUiMapper
import com.numplates.nomera3.modules.maps.ui.mapper.MapObjectsUiMapperImpl
import com.numplates.nomera3.modules.maps.ui.model.MapMode
import com.numplates.nomera3.modules.maps.ui.model.MapUiState
import com.numplates.nomera3.modules.maps.ui.model.MapUiValuesUiModel
import com.numplates.nomera3.modules.places.ui.mapper.PlacesUiMapper
import com.numplates.nomera3.modules.places.ui.mapper.PlacesUiMapperImpl
import com.numplates.nomera3.modules.redesign.fragments.main.map.configuration.MeeraMapEventsUiMapper
import com.numplates.nomera3.modules.redesign.fragments.main.map.configuration.MeeraMapEventsUiMapperImpl
import javax.inject.Inject

class MeeraMapUiMapper @Inject constructor(
    private val objectsUiMapperImpl: MapObjectsUiMapperImpl,
    private val locationUiMapperImpl: LocationUiMapperImpl,
    private val eventsUiMapperImpl: MeeraMapEventsUiMapperImpl,
    private val placesUiMapperImpl: PlacesUiMapperImpl,
    private val mapAnalyticsMapperImpl: MapAnalyticsMapperImpl
) : MapObjectsUiMapper by objectsUiMapperImpl,
    LocationUiMapper by locationUiMapperImpl,
    MeeraMapEventsUiMapper by eventsUiMapperImpl,
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

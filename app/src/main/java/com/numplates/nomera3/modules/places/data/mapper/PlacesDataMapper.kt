package com.numplates.nomera3.modules.places.data.mapper

import com.numplates.nomera3.modules.baseCore.domain.model.CoordinatesModel
import com.numplates.nomera3.modules.places.data.model.PlaceDto
import com.numplates.nomera3.modules.places.domain.model.PlaceModel
import java.time.ZoneId
import java.util.TimeZone
import javax.inject.Inject

class PlacesDataMapper @Inject constructor() {

    fun mapPlaceModel(dto: PlaceDto): PlaceModel {
        val timeZone = TimeZone.getTimeZone(ZoneId.of(dto.timeZone))
        val location = CoordinatesModel(
            lat = dto.location.lat,
            lon = dto.location.lon
        )
        return PlaceModel(
            addressString = dto.addressString,
            location = location,
            name = dto.name,
            timeZone = timeZone,
            placeId = dto.placeId
        )
    }
}

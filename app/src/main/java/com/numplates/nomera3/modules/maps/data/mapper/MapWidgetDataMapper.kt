package com.numplates.nomera3.modules.maps.data.mapper

import com.numplates.nomera3.modules.maps.data.model.MapWidgetPlaceDto
import com.numplates.nomera3.modules.maps.data.model.MapWidgetPointInfoDto
import com.numplates.nomera3.modules.maps.data.model.MapWidgetWeatherDto
import com.numplates.nomera3.modules.maps.domain.widget.model.MapWidgetPlaceModel
import com.numplates.nomera3.modules.maps.domain.widget.model.MapWidgetPointInfoModel
import com.numplates.nomera3.modules.maps.domain.widget.model.MapWidgetWeatherModel
import java.io.File
import java.time.DateTimeException
import java.time.ZoneId
import java.util.TimeZone
import javax.inject.Inject

class MapWidgetDataMapper @Inject constructor(
) {
    @Suppress("detekt:SwallowedException")
    fun mapWidgetPointInfoModel(dto: MapWidgetPointInfoDto, animationFile: File?): MapWidgetPointInfoModel {
        val place = mapWidgetPlace(dto.place)
        val timeZone = try {
            TimeZone.getTimeZone(ZoneId.of(dto.timeZone))
        } catch (e: DateTimeException) {
            TimeZone.getDefault()
        }
        val weather = if (dto.weather != null) {
            mapWidgetWeather(dto = dto.weather, animationFile = animationFile)
        } else {
            null
        }
        return MapWidgetPointInfoModel(
            place = place,
            timeZone = timeZone,
            weather = weather
        )
    }

    private fun mapWidgetPlace(dto: MapWidgetPlaceDto): MapWidgetPlaceModel =
        MapWidgetPlaceModel(
            country = dto.country,
            state = dto.state,
            county = dto.county,
            district = dto.district,
            city = dto.city,
            street = dto.street,
            house = dto.house
        )

    private fun mapWidgetWeather(dto: MapWidgetWeatherDto, animationFile: File?): MapWidgetWeatherModel =
        MapWidgetWeatherModel(
            temperatureFahrenheit = dto.temperatureFahrenheit,
            temperatureCelsius = dto.temperatureCelsius,
            description = dto.description,
            animationFile = animationFile
        )
}

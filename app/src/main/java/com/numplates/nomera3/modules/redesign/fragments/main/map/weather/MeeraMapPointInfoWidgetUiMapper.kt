package com.numplates.nomera3.modules.redesign.fragments.main.map.weather

import android.content.Context
import android.text.format.DateFormat
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.maps.domain.widget.model.MapWidgetPlaceModel
import com.numplates.nomera3.modules.maps.domain.widget.model.MapWidgetWeatherModel
import com.numplates.nomera3.modules.maps.ui.widget.model.AllowedPointInfoWidgetVisibility
import com.numplates.nomera3.modules.maps.ui.widget.model.MapPointInfoWidgetState
import com.numplates.nomera3.modules.maps.ui.widget.model.MapPointInfoWidgetUiModel
import com.numplates.nomera3.modules.maps.ui.widget.model.PointInfoExtendedUiModel
import com.numplates.nomera3.modules.maps.ui.widget.model.WeatherUiModel
import com.numplates.nomera3.modules.redesign.util.NavigationManager
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.Arrays
import java.util.Locale
import javax.inject.Inject
import kotlin.math.roundToInt

class MeeraMapPointInfoWidgetUiMapper @Inject constructor(
    private val context: Context,
) {

    //    private val topLevelHierarchyString = context.getString(R.string.map_point_info_hierarchy_top_level)
    private val formatter = mapTimeFormatter(context)
    private val randomPrimaryNames = mutableSetOf<String>()
    private val temperatureUnit = mapTemperatureUnit(Locale.getDefault())
    private val addressIntervals = listOf(
        AddressInterval(
            maxZoom = LEVEL_1_MAX_ZOOM,
            primaryIndex = HIERARCHY_INDEX_TOP_LEVEL,
            secondaryIndex = HIERARCHY_INDEX_COUNTRY
        ),
        AddressInterval(
            maxZoom = LEVEL_2_MAX_ZOOM,
            primaryIndex = HIERARCHY_INDEX_COUNTRY,
            secondaryIndex = HIERARCHY_INDEX_STATE
        ),
        AddressInterval(
            maxZoom = LEVEL_3_MAX_ZOOM,
            primaryIndex = HIERARCHY_INDEX_STATE,
            secondaryIndex = HIERARCHY_INDEX_COUNTRY
        ),
        AddressInterval(
            maxZoom = LEVEL_4_MAX_ZOOM,
            primaryIndex = HIERARCHY_INDEX_COUNTY,
            secondaryIndex = HIERARCHY_INDEX_STATE
        ),
        AddressInterval(
            maxZoom = LEVEL_5_MAX_ZOOM,
            primaryIndex = HIERARCHY_INDEX_CITY,
            secondaryIndex = HIERARCHY_INDEX_COUNTY
        ),
        AddressInterval(
            maxZoom = LEVEL_6_MAX_ZOOM,
            primaryIndex = HIERARCHY_INDEX_CITY,
            secondaryIndex = HIERARCHY_INDEX_DISTRICT
        ),
        AddressInterval(
            maxZoom = LEVEL_7_MAX_ZOOM,
            primaryIndex = HIERARCHY_INDEX_DISTRICT,
            secondaryIndex = HIERARCHY_INDEX_CITY
        ),
        AddressInterval(
            maxZoom = LEVEL_8_MAX_ZOOM,
            primaryIndex = HIERARCHY_INDEX_STREET,
            secondaryIndex = HIERARCHY_INDEX_DISTRICT
        )
    )

    fun mapUiModel(
        pointInfoExtended: PointInfoExtendedUiModel?,
        visibility: AllowedPointInfoWidgetVisibility,
        timestampMs: Long,
        isConntected: Boolean
    ): MapPointInfoWidgetUiModel {

        if (!isConntected) {
            val state = MapPointInfoWidgetState.Hidden
            return MapPointInfoWidgetUiModel(state)
        }
//        if (visibility == AllowedPointInfoWidgetVisibility.NONE || pointInfoExtended == null) {
        if ( pointInfoExtended == null || !NavigationManager.getManager().isMapMode) {
            return MapPointInfoWidgetUiModel(MapPointInfoWidgetState.Hidden)
        }
        val pointInfoModel = pointInfoExtended.pointInfo
        val addressStrings = mapAddressStrings(
            place = pointInfoModel.place,
            zoom = pointInfoExtended.mapTarget.zoom
        )

        if (addressStrings.primary.isNullOrEmpty()) return MapPointInfoWidgetUiModel(MapPointInfoWidgetState.Hidden)
        val pointTime = Instant.ofEpochMilli(timestampMs).atZone(pointInfoModel.timeZone.toZoneId())
        val timeString = formatter.format(pointTime.toLocalTime())
        val weather = pointInfoModel.weather?.let(::mapWeather)
        if (visibility == AllowedPointInfoWidgetVisibility.COLLAPSED) {
            val state = MapPointInfoWidgetState.Shown.Collapsed(
                primaryAddress = addressStrings.primary,
                weather = weather,
                timeString = timeString,
                withMeeraLogo = true,
            )
            return MapPointInfoWidgetUiModel(state)
        }

        if (pointInfoExtended.mapTarget.zoom <= LEVEL_1_MAX_ZOOM) {
            return MapPointInfoWidgetUiModel(MapPointInfoWidgetState.Shown.ExtendedGeneral(addressStrings.primary))
        }

        val state = MapPointInfoWidgetState.Shown.ExtendedDetailed(
            primaryAddress = addressStrings.primary,
            secondaryAddress = addressStrings.secondary.orEmpty(),
            weather = weather,
            timeString = timeString,
            withMeeraLogo = true,
            showSecondaryAddress = pointInfoExtended.mapTarget.zoom >= LEVEL_SHOW_SECONDARY_ADDRESS,
        )
        return MapPointInfoWidgetUiModel(state)
    }

    fun mapGetWeather(zoom: Float): Boolean = zoom >= GET_WEATHER_MIN_ZOOM

    private fun mapWeather(weather: MapWidgetWeatherModel): WeatherUiModel? {
        if (weather.animationFile == null) return null
        val temperature = when (temperatureUnit) {
            TemperatureUnit.CELSIUS -> weather.temperatureCelsius
            TemperatureUnit.FAHRENHEIT -> weather.temperatureFahrenheit
        }
        val temperatureString = "${temperature.roundToInt()}${temperatureUnit.value}"
        val weatherString = "${weather.description} $temperatureString"
        return WeatherUiModel(
            animation = weather.animationFile,
            description = weatherString,
            temperature = temperatureString
        )
    }

    private fun mapAddressStrings(place: MapWidgetPlaceModel, zoom: Float): WidgetAddressStrings {
        val addressHierarchy = listOf(
            mapStreetString(place),
            place.district,
            place.city,
            place.county,
            place.state,
            place.country,
//            topLevelHierarchyString
        )

        val interval = addressIntervals.first { zoom <= it.maxZoom }
        var primaryString: String? = null
        var primaryIndex = interval.primaryIndex
        for (i in interval.primaryIndex until addressHierarchy.size) {
            if (addressHierarchy[i].isNullOrEmpty().not()) {
                primaryIndex = i
                primaryString = addressHierarchy[i]
                break
            }
        }
        var secondaryString: String? = null
        if (primaryString != null) {
            for (i in interval.secondaryIndex until addressHierarchy.size) {
                if (addressHierarchy[i].isNullOrEmpty().not() && i != primaryIndex) {
                    secondaryString = addressHierarchy[i]
                    break
                }
            }
        }
        if (zoom <= LEVEL_1_MAX_ZOOM) {
            return WidgetAddressStrings(primary = generatePrimaryAddress(), null)
        } else if (zoom <= LEVEL_SHOW_SECONDARY_ADDRESS) {
            return WidgetAddressStrings(primary = generatePrimaryAddress(), null)
        }
        return WidgetAddressStrings(primary = primaryString, secondary = secondaryString)
    }

    private fun mapStreetString(place: MapWidgetPlaceModel): String? {
        if (place.street.isNullOrEmpty()) return null
        return if (place.house.isNullOrEmpty()) {
            place.street
        } else {
            "${place.street}, ${place.house}"
        }
    }

    private fun mapTemperatureUnit(locale: Locale): TemperatureUnit {
        return if (Arrays.binarySearch(WEATHER_FAHRENHEIT_COUNTRIES, locale.country) >= 0) {
            TemperatureUnit.FAHRENHEIT
        } else {
            TemperatureUnit.CELSIUS
        }
    }

    private fun generatePrimaryAddress(): String {
        val randomString = setOf(
            context.getString(R.string.map_widget_suggest1),
            context.getString(R.string.map_widget_suggest2),
            context.getString(R.string.map_widget_suggest3),
            context.getString(R.string.map_widget_suggest4),
            context.getString(R.string.map_widget_suggest5),
        ).random()
        if (randomPrimaryNames.add(randomString) && randomPrimaryNames.size == 4) {
            randomPrimaryNames.clear()
        }
        return randomString
    }

    private fun mapTimeFormatter(context: Context): DateTimeFormatter {
        val pattern = if (DateFormat.is24HourFormat(context)) "HH:mm" else "hh:mm a"
        return DateTimeFormatter.ofPattern(pattern, Locale.US)
    }

    private class AddressInterval(
        val maxZoom: Float,
        val primaryIndex: Int,
        val secondaryIndex: Int
    )

    private class WidgetAddressStrings(
        val primary: String?,
        val secondary: String?
    )

    private enum class TemperatureUnit(val value: String) {
        CELSIUS("ºC"),
        FAHRENHEIT("ºF")
    }

    companion object {
        /** From https://developer.android.com/reference/androidx/core/text/util/LocalePreferences implementation which
         * requires target sdk 34 */
        private val WEATHER_FAHRENHEIT_COUNTRIES = arrayOf("BS", "BZ", "KY", "PR", "PW", "US")

        private const val GET_WEATHER_MIN_ZOOM = 2f
        private const val LEVEL_1_MAX_ZOOM = 3f
        private const val LEVEL_2_MAX_ZOOM = 7f
        private const val LEVEL_3_MAX_ZOOM = 9f
        private const val LEVEL_4_MAX_ZOOM = 11f
        private const val LEVEL_5_MAX_ZOOM = 12f
        private const val LEVEL_6_MAX_ZOOM = 13f
        private const val LEVEL_7_MAX_ZOOM = 15f
        private const val LEVEL_8_MAX_ZOOM = 22f
        private const val LEVEL_SHOW_SECONDARY_ADDRESS = 4f
        private const val HIERARCHY_INDEX_STREET = 0
        private const val HIERARCHY_INDEX_DISTRICT = 1
        private const val HIERARCHY_INDEX_CITY = 2
        private const val HIERARCHY_INDEX_COUNTY = 3
        private const val HIERARCHY_INDEX_STATE = 4
        private const val HIERARCHY_INDEX_COUNTRY = 5
        private const val HIERARCHY_INDEX_TOP_LEVEL = 6


    }
}

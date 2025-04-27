package com.numplates.nomera3.modules.newroads.util

import com.meera.core.extensions.empty
import com.numplates.nomera3.data.network.core.INetworkValues
import com.numplates.nomera3.modules.newroads.data.entities.FilterSettings
import com.numplates.nomera3.presentation.view.fragments.bottomfragment.roadfilter.CountryFilterItem
import com.numplates.nomera3.presentation.view.fragments.bottomfragment.roadfilter.getCountryId

class FilterSettingsMapper {

    fun mapToAnalyticCityData(filterSettings: FilterSettings): String {
        val cities = filterSettings.data.filterCities
        var result = String.empty()

        cities.forEach { city ->
            result += "${city.title_}, "
        }

        return result.dropLast(2)
    }

    fun mapToAnalyticCountryData(filterSettings: FilterSettings): String {
        var countries = String.empty()

        if (filterSettings.data.allCountries) {
            return "All countries"
        } else {
            if (filterSettings.hasCountryFilter(CountryFilterItem.RUSSIA.getCountryId())) countries += "Russia, "
            if (filterSettings.hasCountryFilter(CountryFilterItem.ARMENIA.getCountryId())) countries += "Armenia, "
            if (filterSettings.hasCountryFilter(CountryFilterItem.BELARUS.getCountryId())) countries += "Belarus, "
            if (filterSettings.hasCountryFilter(CountryFilterItem.GEORGIA.getCountryId())) countries += "Georgia, "
            if (filterSettings.hasCountryFilter(CountryFilterItem.KAZAKHSTAN.getCountryId())) countries +=
                "Kazachstan, "
            if (filterSettings.hasCountryFilter(CountryFilterItem.UKRAINE.getCountryId())) countries += "Ukraine, "
        }

        return countries.dropLast(2)
    }

    fun mapToCityIds(filterSettings: FilterSettings): String {
        val citiesList = filterSettings.data.filterCities
        var cityIds = ""
        citiesList.forEach {
            cityIds = cityIds + it.cityId + ","
        }

        cityIds = if (cityIds.length > 1) {
            cityIds.substring(0, cityIds.length - 1)
        } else {
            "0"
        }
        return cityIds
    }

    fun mapToParams(filterSettings: FilterSettings): String {
        var parameters = ""

        if (filterSettings.hasCountryFilter(CountryFilterItem.ARMENIA.getCountryId())) {
            parameters = parameters + INetworkValues.ROAD_TO_ARMENIA + ","
        }
        if (filterSettings.hasCountryFilter(CountryFilterItem.GEORGIA.getCountryId())) {
            parameters = parameters + INetworkValues.ROAD_TO_GEORGIA + ","
        }
        if (filterSettings.hasCountryFilter(CountryFilterItem.BELARUS.getCountryId())) {
            parameters = parameters + INetworkValues.ROAD_TO_BELORUS + ","
        }
        if (filterSettings.hasCountryFilter(CountryFilterItem.RUSSIA.getCountryId())) {
            parameters = parameters + INetworkValues.ROAD_TO_RUSSIA + ","
        }
        if (filterSettings.hasCountryFilter(CountryFilterItem.KAZAKHSTAN.getCountryId())) {
            parameters = parameters + INetworkValues.ROAD_TO_KAZAKHSTAN + ","
        }
        if (filterSettings.hasCountryFilter(CountryFilterItem.UKRAINE.getCountryId())) {
            parameters = parameters + INetworkValues.ROAD_TO_UKRAINE + ","
        }
        if (filterSettings.hasCountryFilter(CountryFilterItem.AZERBAIJAN.getCountryId())) {
            parameters = parameters + INetworkValues.ROAD_TO_AZERBAIJAN + ","
        }
        if (filterSettings.hasCountryFilter(CountryFilterItem.KYRGYZSTAN.getCountryId())) {
            parameters = parameters + INetworkValues.ROAD_TO_KYRGYZSTAN + ","
        }
        if (filterSettings.hasCountryFilter(CountryFilterItem.MOLDOVA.getCountryId())) {
            parameters = parameters + INetworkValues.ROAD_TO_MOLDOVA + ","
        }
        if (filterSettings.hasCountryFilter(CountryFilterItem.TAJIKISTAN.getCountryId())) {
            parameters = parameters + INetworkValues.ROAD_TO_TAJIKISTAN + ","
        }
        if (filterSettings.hasCountryFilter(CountryFilterItem.TURKMENISTAN.getCountryId())) {
            parameters = parameters + INetworkValues.ROAD_TO_TURKMENISTAN + ","
        }
        if (filterSettings.hasCountryFilter(CountryFilterItem.UZBEKISTAN.getCountryId())) {
            parameters = parameters + INetworkValues.ROAD_TO_UZBEKISTAN + ","
        }
        if (filterSettings.hasCountryFilter(CountryFilterItem.TURKEY.getCountryId())) {
            parameters = parameters + INetworkValues.ROAD_TO_TURKEY + ","
        }
        if (filterSettings.hasCountryFilter(CountryFilterItem.OAE.getCountryId())) {
            parameters = parameters + INetworkValues.ROAD_TO_OAE + ","
        }
        if (filterSettings.hasCountryFilter(CountryFilterItem.THAILAND.getCountryId())) {
            parameters = parameters + INetworkValues.ROAD_TO_THAILAND + ","
        }

        parameters = if (parameters.length > 1) {
            parameters.substring(0, parameters.length - 1)
        } else {
            "0"
        }

        return parameters
    }
}

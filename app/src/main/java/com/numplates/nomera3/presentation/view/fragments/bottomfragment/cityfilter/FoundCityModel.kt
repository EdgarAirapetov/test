package com.numplates.nomera3.presentation.view.fragments.bottomfragment.cityfilter

data class FoundCityModel(
        // from City
        val cityId: Int,
        val countryId: Int,
        val cityName: String,
        val countryName: String,
        val title: String,
        // for search result list
        var isSelected: Boolean
)
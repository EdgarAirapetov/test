package com.numplates.nomera3.modules.userprofile.domain.model.usermain

data class LocationModel(
    val latitude: Double?,
    val longitude: Double?,
    val cityName: String,
    val countryName: String,
    val cityId: Long?,
    val countryId: Long?,
)

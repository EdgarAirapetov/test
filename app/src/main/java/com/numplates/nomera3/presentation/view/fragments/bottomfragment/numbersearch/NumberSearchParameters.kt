package com.numplates.nomera3.presentation.view.fragments.bottomfragment.numbersearch

data class NumberSearchParameters(
    val number: String,
    val vehicleTypeId: Int,
    val countryId: Long,

    val countryName: String? = null
)
package com.numplates.nomera3.presentation.view.fragments.dialogs

import com.numplates.nomera3.data.network.City

interface CityPickerDialogCallback {
    fun onCityClicked(city: City?)
}

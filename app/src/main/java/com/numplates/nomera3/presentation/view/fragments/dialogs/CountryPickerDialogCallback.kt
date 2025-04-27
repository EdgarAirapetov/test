package com.numplates.nomera3.presentation.view.fragments.dialogs

import com.numplates.nomera3.data.network.Country

interface CountryPickerDialogCallback {
    fun onCountryClicked(country: Country?)
}

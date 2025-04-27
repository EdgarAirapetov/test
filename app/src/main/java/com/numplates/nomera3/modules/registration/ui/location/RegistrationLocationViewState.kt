package com.numplates.nomera3.modules.registration.ui.location

import com.numplates.nomera3.data.network.City
import com.numplates.nomera3.data.network.Country

sealed class RegistrationLocationViewState {
    object CheckLocation: RegistrationLocationViewState()
    data class ShowCountriesSelector(val selectedCountry: Country?) : RegistrationLocationViewState()
    data class ShowCitiesSelector(val selectedCity: City?) : RegistrationLocationViewState()
    data class Address(val countryName: String?, val cityName: String?) :
        RegistrationLocationViewState()
    object None: RegistrationLocationViewState()
}

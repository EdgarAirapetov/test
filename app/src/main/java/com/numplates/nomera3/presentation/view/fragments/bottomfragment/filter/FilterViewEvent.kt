package com.numplates.nomera3.presentation.view.fragments.bottomfragment.filter

import com.numplates.nomera3.data.network.City
import com.numplates.nomera3.modules.registration.domain.model.RegistrationCountryModel

sealed class FilterViewEvent {
    data class Countries(val countries: MutableSet<RegistrationCountryModel>): FilterViewEvent()
    data class Country(val country: Pair<RegistrationCountryModel, Boolean>): FilterViewEvent()
    data class Cities(val cities: MutableSet<City>): FilterViewEvent()
    data class ResetButtonState(val enabled: Boolean): FilterViewEvent()
    object Initialize: FilterViewEvent()
}

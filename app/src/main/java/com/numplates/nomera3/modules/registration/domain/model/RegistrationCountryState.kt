package com.numplates.nomera3.modules.registration.domain.model

sealed class RegistrationCountryState {

    class RegistrationCountryList(
        val countries: List<RegistrationCountryModel>
    ) : RegistrationCountryState()

}

package com.numplates.nomera3.modules.registration.domain

import com.numplates.nomera3.modules.registration.data.repository.RegistrationCountriesRepository
import com.numplates.nomera3.modules.registration.domain.mapper.RegistrationCountriesMapper
import javax.inject.Inject

class LoadSignupCountriesUseCase @Inject constructor(
    private val repository: RegistrationCountriesRepository,
    private val registrationCountriesMapper: RegistrationCountriesMapper
) {

    suspend fun invoke() {
        val countries = repository.loadSignupCountries()
        repository.setSignupCountries(countries.map(registrationCountriesMapper::mapDtoToDbModel))
    }

}

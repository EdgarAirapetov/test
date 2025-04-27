package com.numplates.nomera3.modules.registration.domain

import com.numplates.nomera3.modules.registration.domain.model.RegistrationCountryModel
import com.numplates.nomera3.modules.registration.data.repository.RegistrationCountriesRepository
import com.numplates.nomera3.modules.registration.domain.mapper.RegistrationCountriesMapper
import javax.inject.Inject

class GetCountriesUseCase @Inject constructor(
    private val registrationCountriesRepository: RegistrationCountriesRepository,
    private val registrationCountriesMapper: RegistrationCountriesMapper
) {

    suspend fun invoke(): List<RegistrationCountryModel> {
        return registrationCountriesRepository.getCountries().map(registrationCountriesMapper::mapCountryToUiModel)
    }

}

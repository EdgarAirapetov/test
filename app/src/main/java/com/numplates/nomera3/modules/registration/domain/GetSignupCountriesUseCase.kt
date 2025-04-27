package com.numplates.nomera3.modules.registration.domain

import com.numplates.nomera3.modules.registration.domain.model.RegistrationCountryModel
import com.numplates.nomera3.modules.registration.data.repository.RegistrationCountriesRepository
import com.numplates.nomera3.modules.registration.domain.mapper.RegistrationCountriesMapper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetSignupCountriesUseCase @Inject constructor(
    private val registrationLocationRepository: RegistrationCountriesRepository,
    private val registrationCountriesMapper: RegistrationCountriesMapper
) {

    fun invoke(): Flow<List<RegistrationCountryModel>> {
        return registrationLocationRepository.getSignupCountries()
            .map { countriesList ->
                return@map countriesList.map(registrationCountriesMapper::mapDbModelToUiModel)
            }
    }

}

package com.numplates.nomera3.modules.registration.domain.mapper

import com.meera.core.extensions.empty
import com.meera.db.models.RegistrationCountryDbModel
import com.numplates.nomera3.data.network.Country
import com.numplates.nomera3.modules.registration.data.entity.RegistrationCountryDto
import com.numplates.nomera3.modules.registration.domain.model.RegistrationCountryModel
import javax.inject.Inject

class RegistrationCountriesMapper @Inject constructor() {

    fun mapDtoToUiModel(src: RegistrationCountryDto): RegistrationCountryModel {
        return RegistrationCountryModel(
            name = src.name,
            code = src.code,
            mask = src.mask,
            flag = src.flag
        )
    }

    fun mapDtoToDbModel(src: RegistrationCountryDto): RegistrationCountryDbModel {
        return RegistrationCountryDbModel(
            name = src.name,
            code = src.code,
            mask = src.mask,
            flag = src.flag
        )
    }

    fun mapDbModelToUiModel(src: RegistrationCountryDbModel): RegistrationCountryModel {
        return RegistrationCountryModel(
            name = src.name,
            code = src.code,
            mask = src.mask,
            flag = src.flag
        )
    }

    fun mapCountryToUiModel(src: Country): RegistrationCountryModel {
        return RegistrationCountryModel(
            name = src.name ?: String.empty(),
            flag = src.flag ?: String.empty(),
            id = src.countryId
        )
    }

}

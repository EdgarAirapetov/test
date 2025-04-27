package com.numplates.nomera3.modules.registration.data.repository

import com.numplates.nomera3.data.network.City
import com.numplates.nomera3.data.network.Country

interface RegistrationLocationRepository {

    suspend fun getCountries(success: (List<Country>) -> Unit, fail: (Exception) -> Unit)

    suspend fun getCitiesSuggestion(
        countryId: Long?,
        query: String?,
        success: (List<City>) -> Unit,
        fail: (Exception) -> Unit
    )
}

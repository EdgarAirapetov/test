package com.numplates.nomera3.modules.registration.data.repository

import com.numplates.nomera3.data.network.City
import com.numplates.nomera3.data.network.Country
import com.numplates.nomera3.modules.registration.data.CitiesNotFoundException
import com.numplates.nomera3.modules.registration.data.CountriesNotFoundException
import com.numplates.nomera3.modules.registration.data.RegistrationApi

class RegistrationLocationRepositoryImpl(
    private val api: RegistrationApi
): RegistrationLocationRepository {

    override suspend fun getCountries(success: (List<Country>) -> Unit, fail: (Exception) -> Unit) {
        try {
            val countries = api.getCountries().data
            if (!countries.countries.isNullOrEmpty()){
                countries.countries?.also { success(it) }
            } else {
                fail(CountriesNotFoundException())
            }
        } catch (e: Exception) {
            fail(e)
        }
    }

    override suspend fun getCitiesSuggestion(
        countryId: Long?,
        query: String?,
        success: (List<City>) -> Unit,
        fail: (java.lang.Exception) -> Unit
    ) {
        try {
            val cities = api.getCitiesSuggestion(countryId, query).data
            if (!cities.isNullOrEmpty()) {
                success(cities)
            } else {
                fail(CitiesNotFoundException())
            }
        } catch (e: Exception) {
            fail(e)
        }
    }
}

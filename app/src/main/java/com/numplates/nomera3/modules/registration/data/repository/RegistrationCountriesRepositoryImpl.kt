package com.numplates.nomera3.modules.registration.data.repository

import com.meera.core.di.scopes.AppScope
import com.meera.db.dao.RegistrationCountriesDao
import com.meera.db.models.RegistrationCountryDbModel
import com.numplates.nomera3.data.network.ApiMain
import com.numplates.nomera3.data.network.Country
import com.numplates.nomera3.modules.registration.data.CountriesNotFoundException
import com.numplates.nomera3.modules.registration.data.entity.RegistrationCountryDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AppScope
class RegistrationCountriesRepositoryImpl @Inject constructor(
    private val api: ApiMain,
    private val dao: RegistrationCountriesDao
) : RegistrationCountriesRepository {

    private var countries: List<Country>? = null

    override suspend fun getCountries(): List<Country> {
        return withContext(Dispatchers.IO) {
            val alreadyLoadedCountries = countries
            if (alreadyLoadedCountries != null) {
                alreadyLoadedCountries
            } else {
                val countries = api.getCountries().data
                if (countries == null || countries.countries.isNullOrEmpty()) throw CountriesNotFoundException()
                val loadedCountries = countries.countries ?: emptyList()
                this@RegistrationCountriesRepositoryImpl.countries = loadedCountries
                loadedCountries
            }
        }
    }

    override suspend fun loadSignupCountries(): List<RegistrationCountryDto> {
        val countries = api.getSignupCountries().data
        if (countries.isNullOrEmpty()) throw CountriesNotFoundException()
        return countries
    }

    override fun getSignupCountries(): Flow<List<RegistrationCountryDbModel>> {
        return dao.getAllRegistrationCountries()
    }

    override suspend fun setSignupCountries(countries: List<RegistrationCountryDbModel>) {
        dao.deleteAllRegistrationCountries()
        dao.insertRegistrationCountries(countries)
    }
}

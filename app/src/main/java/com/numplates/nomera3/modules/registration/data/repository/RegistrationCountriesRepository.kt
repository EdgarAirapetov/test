package com.numplates.nomera3.modules.registration.data.repository

import com.numplates.nomera3.data.network.Country
import com.meera.db.models.RegistrationCountryDbModel
import com.numplates.nomera3.modules.registration.data.entity.RegistrationCountryDto
import kotlinx.coroutines.flow.Flow

interface RegistrationCountriesRepository {

    suspend fun getCountries(): List<Country>

    suspend fun loadSignupCountries(): List<RegistrationCountryDto>

    fun getSignupCountries(): Flow<List<RegistrationCountryDbModel>>

    suspend fun setSignupCountries(countries: List<RegistrationCountryDbModel>)

}

package com.numplates.nomera3.domain.interactornew

import com.numplates.nomera3.data.network.ApiHiWay
import com.numplates.nomera3.data.network.Countries
import com.numplates.nomera3.data.network.Country
import com.numplates.nomera3.data.network.core.ResponseWrapper
import io.reactivex.Flowable

class GetCountriesUseCase(private val repository: ApiHiWay?) {

    fun getCountries() = repository?.countries

    fun getCountriesOrEmptyList(): Flowable<List<Country>>? {
        return repository
                ?.countries
                ?.map { responseWrapper: ResponseWrapper<Countries> ->
                    responseWrapper
                            .data
                            ?.countries
                            ?.filterNotNull()
                            ?: listOf()
                }
    }
}
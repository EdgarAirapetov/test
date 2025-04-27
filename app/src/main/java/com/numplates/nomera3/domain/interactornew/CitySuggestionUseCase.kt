package com.numplates.nomera3.domain.interactornew

import com.numplates.nomera3.data.network.Api
import com.numplates.nomera3.data.network.City
import com.numplates.nomera3.data.network.core.ResponseWrapper
import io.reactivex.Flowable

class CitySuggestionUseCase(private val repository: Api?) {

    fun getCitySuggestion(countryId: Long, query: String? = null): Flowable<ResponseWrapper<MutableList<City>>>? {
        if (countryId == 0L) {
            return if (query != null)
                repository?.citiesSuggestion(countryId, query)
            else
                repository?.citiesSuggestion(countryId)
        }

        return if (query == null) {
            repository?.citiesSuggestion(countryId)
        } else repository?.citiesSuggestion(countryId, query)
    }

    fun getCitySuggestion(query: String? = null): Flowable<ResponseWrapper<MutableList<City>>>? {
        return if (query == null) {
            repository?.citiesSuggestion(0)
        } else repository?.citiesSuggestion(query)
    }

    // todo should be suspended fun
    fun searchCity(citySearchPhrase: String): Flowable<ResponseWrapper<MutableList<City>>>? {
        return repository?.citiesSuggestion(citySearchPhrase)
    }

    fun getCitySuggestionOrEmptyList(countryId: Long, query: String? = null): Flowable<MutableList<City>> {
        val citiesSuggestion = if (query == null) {
            repository?.citiesSuggestion(countryId)
        } else {
            repository?.citiesSuggestion(countryId, query)
        }

        return citiesSuggestion
                ?.map { it.data ?: mutableListOf() }
                ?: Flowable.just(mutableListOf())
    }
}

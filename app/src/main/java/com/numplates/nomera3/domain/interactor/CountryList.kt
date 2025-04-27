package com.numplates.nomera3.domain.interactor

import com.numplates.nomera3.data.network.ApiHiWay
import com.numplates.nomera3.data.network.Countries
import com.numplates.nomera3.data.network.core.ResponseWrapper
import io.reactivex.Flowable

/**
 * Created by abelov.
 */
class CountryList(private val api: ApiHiWay) : UseCase<ResponseWrapper<Countries>>() {

    override fun buildUseCaseObservable(): Flowable<ResponseWrapper<Countries>> {
        return api.countries
    }

}

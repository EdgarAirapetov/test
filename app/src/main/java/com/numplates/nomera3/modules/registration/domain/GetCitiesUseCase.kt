package com.numplates.nomera3.modules.registration.domain

import com.numplates.nomera3.data.network.City
import com.numplates.nomera3.modules.baseCore.BaseUseCaseCoroutine
import com.numplates.nomera3.modules.baseCore.DefParams
import com.numplates.nomera3.modules.registration.data.repository.RegistrationLocationRepository
import javax.inject.Inject

class GetCitiesUseCase @Inject constructor(
    private val repository: RegistrationLocationRepository
): BaseUseCaseCoroutine<GetCitiesParams, List<City>> {
    override suspend fun execute(
        params: GetCitiesParams,
        success: (List<City>) -> Unit,
        fail: (Throwable) -> Unit
    ) {
        repository.getCitiesSuggestion(
            countryId = params.countryId,
            query = params.query,
            success = success,
            fail = fail
        )
    }
}

data class GetCitiesParams(val countryId: Long?, val query: String?): DefParams()

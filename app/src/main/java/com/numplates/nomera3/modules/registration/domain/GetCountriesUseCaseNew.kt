package com.numplates.nomera3.modules.registration.domain

import com.numplates.nomera3.data.network.Country
import com.numplates.nomera3.modules.baseCore.BaseUseCaseCoroutine
import com.numplates.nomera3.modules.baseCore.DefParams
import com.numplates.nomera3.modules.registration.data.repository.RegistrationLocationRepository
import javax.inject.Inject

class GetCountriesUseCaseNew @Inject constructor(
    private val repository: RegistrationLocationRepository
): BaseUseCaseCoroutine<DefParams, List<Country>> {

    override suspend fun execute(
        params: DefParams,
        success: (List<Country>) -> Unit,
        fail: (Throwable) -> Unit
    ) {
        repository.getCountries(success, fail)
    }
}

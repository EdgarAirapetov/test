package com.numplates.nomera3.modules.search.domain.usecase

import com.numplates.nomera3.modules.search.data.repository.SearchRepository
import javax.inject.Inject

class GetNumberSearchParamsFlowUseCase @Inject constructor(
    private val searchRepository: SearchRepository
) {

    fun invoke() = searchRepository.observeNumberSearchParamsChange()

}

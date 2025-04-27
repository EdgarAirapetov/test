package com.numplates.nomera3.modules.search.domain.usecase

import com.numplates.nomera3.modules.search.data.repository.SearchRepository
import com.numplates.nomera3.presentation.view.fragments.bottomfragment.numbersearch.NumberSearchParameters
import javax.inject.Inject

class SetNumberSearchParamsUseCase @Inject constructor(
    private val searchRepository: SearchRepository
) {

    fun invoke(numberSearchParameters: NumberSearchParameters) {
        searchRepository.setNumberSearchParams(numberSearchParameters)
    }

}

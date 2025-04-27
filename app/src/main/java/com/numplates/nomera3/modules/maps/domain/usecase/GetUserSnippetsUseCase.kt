package com.numplates.nomera3.modules.maps.domain.usecase

import com.numplates.nomera3.modules.maps.domain.model.GetUserSnippetsParamsModel
import com.numplates.nomera3.modules.maps.domain.model.UserSnippetModel
import com.numplates.nomera3.modules.maps.domain.repository.MapDataRepository
import javax.inject.Inject

class GetUserSnippetsUseCase @Inject constructor(
    private val mapDataRepository: MapDataRepository
) {

    suspend fun invoke(paramsModel: GetUserSnippetsParamsModel): List<UserSnippetModel> {
        return mapDataRepository.getUserSnippets(paramsModel)
    }
}
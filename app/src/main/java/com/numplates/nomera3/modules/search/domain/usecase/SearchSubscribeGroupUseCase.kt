package com.numplates.nomera3.modules.search.domain.usecase

import com.numplates.nomera3.modules.search.data.repository.SearchRepositoryImpl
import com.numplates.nomera3.modules.search.domain.BaseUseCaseCoroutine
import com.numplates.nomera3.modules.search.domain.DefParams
import javax.inject.Inject

class SearchSubscribeGroupUseCase @Inject constructor(private val repository: SearchRepositoryImpl) :
    BaseUseCaseCoroutine<SearchSubscribeGroupParams, List<*>> {

    override suspend fun execute(
        params: SearchSubscribeGroupParams,
        success: (List<*>) -> Unit,
        fail: (Exception) -> Unit
    ) {
        repository.requestSubscribeGroup(params.groupId, success, fail)
    }
}

class SearchSubscribeGroupParams(
    val groupId: Int
) : DefParams()
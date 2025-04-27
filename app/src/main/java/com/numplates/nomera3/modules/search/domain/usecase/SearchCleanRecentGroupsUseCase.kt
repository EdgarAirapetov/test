package com.numplates.nomera3.modules.search.domain.usecase

import com.numplates.nomera3.modules.search.data.repository.SearchRepositoryImpl
import com.numplates.nomera3.modules.search.domain.BaseUseCaseCoroutine
import com.numplates.nomera3.modules.search.domain.DefParams
import javax.inject.Inject

class SearchCleanRecentGroupsUseCase @Inject constructor(private val repository: SearchRepositoryImpl) :
    BaseUseCaseCoroutine<SearchCleanRecentGroupsParams, Boolean> {

    override suspend fun execute(
        params: SearchCleanRecentGroupsParams,
        success: (Boolean) -> Unit,
        fail: (Exception) -> Unit
    ) {
        repository.requestCleanRecentGroups({ success(true) }, fail)
    }
}

class SearchCleanRecentGroupsParams : DefParams()
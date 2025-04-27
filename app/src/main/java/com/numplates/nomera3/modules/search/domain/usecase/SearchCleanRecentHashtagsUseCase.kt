package com.numplates.nomera3.modules.search.domain.usecase

import com.numplates.nomera3.modules.search.data.repository.SearchRepositoryImpl
import com.numplates.nomera3.modules.search.domain.BaseUseCaseCoroutine
import com.numplates.nomera3.modules.search.domain.DefParams
import javax.inject.Inject

class SearchCleanRecentHashtagsUseCase @Inject constructor(
    private val repository: SearchRepositoryImpl
) : BaseUseCaseCoroutine<SearchCleanRecentHashtagsParams, Boolean> {

    override suspend fun execute(
        params: SearchCleanRecentHashtagsParams,
        success: (Boolean) -> Unit,
        fail: (Exception) -> Unit
    ) {
        repository.requestCleanRecentHashtags({ success(true) }, fail)
    }
}

class SearchCleanRecentHashtagsParams : DefParams()
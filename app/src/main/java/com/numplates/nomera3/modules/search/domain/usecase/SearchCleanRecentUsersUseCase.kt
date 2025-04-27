package com.numplates.nomera3.modules.search.domain.usecase

import com.numplates.nomera3.modules.search.data.repository.SearchRepositoryImpl
import com.numplates.nomera3.modules.search.domain.BaseUseCaseCoroutine
import com.numplates.nomera3.modules.search.domain.DefParams
import javax.inject.Inject

class SearchCleanRecentUsersUseCase @Inject constructor(
    private val repository: SearchRepositoryImpl
) : BaseUseCaseCoroutine<SearchCleanRecentUsersParams, Boolean> {

    override suspend fun execute(
        params: SearchCleanRecentUsersParams,
        success: (Boolean) -> Unit,
        fail: (Exception) -> Unit
    ) {
        repository.requestCleanRecentUsers({ success.invoke(true) }, fail)
    }
}

class SearchCleanRecentUsersParams : DefParams()
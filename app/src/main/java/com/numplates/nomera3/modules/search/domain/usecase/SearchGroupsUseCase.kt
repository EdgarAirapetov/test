package com.numplates.nomera3.modules.search.domain.usecase

import com.numplates.nomera3.modules.search.data.entity.SearchGroupEntityResponse
import com.numplates.nomera3.modules.search.data.repository.SearchRepository
import com.numplates.nomera3.modules.search.domain.BaseUseCaseCoroutine
import com.numplates.nomera3.modules.search.domain.DefParams
import javax.inject.Inject

class SearchGroupsUseCase @Inject constructor(
        private val repository: SearchRepository
): BaseUseCaseCoroutine<SearchGroupsParams, SearchGroupEntityResponse> {

    override suspend fun execute(params: SearchGroupsParams,
                                 success: (SearchGroupEntityResponse) -> Unit,
                                 fail: (Exception) -> Unit) {
        repository.requestSearchGroups(
                query = params.query,
                limit = params.limit,
                offset = params.offset,
                success = success,
                fail = fail
        )
    }
}

class SearchGroupsParams(
        val query: String,
        val limit: Int,
        val offset: Int
): DefParams()
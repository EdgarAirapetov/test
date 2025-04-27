package com.numplates.nomera3.modules.search.domain.usecase

import com.numplates.nomera3.modules.search.data.repository.SearchRepository
import com.numplates.nomera3.modules.search.domain.BaseUseCaseCoroutine
import com.numplates.nomera3.modules.search.domain.DefParams
import com.numplates.nomera3.modules.tags.data.entity.HashtagTagListModel
import javax.inject.Inject

class SearchHashTagUseCase @Inject constructor(
    private val repository: SearchRepository
) : BaseUseCaseCoroutine<SearchHashTagParams, HashtagTagListModel> {

    override suspend fun execute(params: SearchHashTagParams,
                                 success: (HashtagTagListModel) -> Unit,
                                 fail: (Exception) -> Unit) {
        repository.requestSearchHashTags(
            query = params.query,
            limit = params.limit,
            offset = params.offset,
            success = success,
            fail = fail
        )
    }
}

class SearchHashTagParams(
    val query: String,
    val limit: Int,
    val offset: Int
) : DefParams()
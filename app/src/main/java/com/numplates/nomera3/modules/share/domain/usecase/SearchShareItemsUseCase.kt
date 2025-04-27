package com.numplates.nomera3.modules.share.domain.usecase

import com.numplates.nomera3.modules.baseCore.BaseUseCaseCoroutine
import com.numplates.nomera3.modules.baseCore.DefParams
import com.numplates.nomera3.modules.share.data.entity.ResponseShareItem
import com.numplates.nomera3.modules.share.data.repository.RoomsRepository
import javax.inject.Inject

class SearchShareItemsUseCase @Inject constructor(
    private val repository: RoomsRepository
): BaseUseCaseCoroutine<SearchShareItemsParams, List<ResponseShareItem>> {

    override suspend fun execute(
        params: SearchShareItemsParams,
        success: (List<ResponseShareItem>) -> Unit,
        fail: (Throwable) -> Unit
    ) {
        repository.searchShareItems(
            success = success,
            fail = fail,
            lastContactId = params.lastId,
            query = params.query
        )
    }
}

class SearchShareItemsParams(
    val lastId: String?,
    val query: String,
) : DefParams()

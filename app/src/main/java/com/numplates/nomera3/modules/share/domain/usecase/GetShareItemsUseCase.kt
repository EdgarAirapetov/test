package com.numplates.nomera3.modules.share.domain.usecase

import com.numplates.nomera3.modules.baseCore.BaseUseCaseCoroutine
import com.numplates.nomera3.modules.baseCore.DefParams
import com.numplates.nomera3.modules.share.data.entity.ResponseShareItem
import com.numplates.nomera3.modules.share.data.repository.RoomsRepository
import javax.inject.Inject

class GetShareItemsUseCase @Inject constructor(
    private val repository: RoomsRepository
) : BaseUseCaseCoroutine<GetShareItemsParams, List<ResponseShareItem>> {

    override suspend fun execute(
        params: GetShareItemsParams,
        success: (List<ResponseShareItem>) -> Unit,
        fail: (Throwable) -> Unit
    ) {
        repository.getShareItems(
            success = success,
            fail = fail,
            lastContactId = params.lastId,
            selectedUserId = params.selectUsedId
        )
    }
}

class GetShareItemsParams(
    val lastId: String?,
    val selectUsedId: Long?
) : DefParams()

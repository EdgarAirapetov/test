package com.numplates.nomera3.modules.share.domain.usecase

import com.numplates.nomera3.data.network.core.ResponseWrapper
import com.numplates.nomera3.modules.baseCore.BaseUseCaseCoroutine
import com.numplates.nomera3.modules.baseCore.DefParams
import com.numplates.nomera3.modules.share.data.repository.ShareRepositoryImpl
import javax.inject.Inject

class ShareCommunityUseCase @Inject constructor(
    private val repository: ShareRepositoryImpl
) : BaseUseCaseCoroutine<ShareCommunityParams, ResponseWrapper<Any>> {

    override suspend fun execute(
        params: ShareCommunityParams,
        success: (ResponseWrapper<Any>) -> Unit,
        fail: (Throwable) -> Unit
    ) {
        repository.shareCommunity(
            groupId = params.groupId,
            userIds = params.userIds,
            roomIds = params.roomIds,
            comment = params.comment,
            success = success,
            fail = fail
        )
    }

}

class ShareCommunityParams(
    val groupId: Int,
    val userIds: List<Long>,
    val roomIds: List<Long>,
    val comment: String
) : DefParams()

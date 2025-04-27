package com.numplates.nomera3.modules.share.domain.usecase

import com.numplates.nomera3.data.network.core.ResponseWrapper
import com.numplates.nomera3.modules.baseCore.BaseUseCaseCoroutine
import com.numplates.nomera3.modules.baseCore.DefParams
import com.numplates.nomera3.modules.share.data.repository.ShareRepositoryImpl
import javax.inject.Inject

class ShareUserProfileUseCase @Inject constructor(
    private val repository: ShareRepositoryImpl
) : BaseUseCaseCoroutine<ShareUserProfileParams, ResponseWrapper<Any>> {

    override suspend fun execute(
        params: ShareUserProfileParams,
        success: (ResponseWrapper<Any>) -> Unit,
        fail: (Throwable) -> Unit
    ) {
        repository.shareUserProfile(
            params.userId,
            params.userIds,
            params.roomIds,
            params.comment,
            success,
            fail
        )
    }
}

class ShareUserProfileParams(
    val userId: Long,
    val userIds: List<Long>,
    val roomIds: List<Long>,
    val comment: String
): DefParams()

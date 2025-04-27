package com.numplates.nomera3.modules.share.domain.usecase

import com.numplates.nomera3.modules.baseCore.BaseUseCaseCoroutine
import com.numplates.nomera3.modules.baseCore.DefParams
import com.numplates.nomera3.modules.share.data.entity.LinkResponse
import com.numplates.nomera3.modules.share.data.repository.ShareRepositoryImpl
import javax.inject.Inject

class GetPostLinkUseCase @Inject constructor(
    private val repository: ShareRepositoryImpl
) : BaseUseCaseCoroutine<GetPostLinkParams, LinkResponse> {

    override suspend fun execute(
        params: GetPostLinkParams,
        success: (LinkResponse) -> Unit,
        fail: (Throwable) -> Unit
    ) {
        repository.getPostLink(params.postId, success, fail)
    }

    suspend fun invoke(params: GetPostLinkParams): String {
        return repository.getPostLink(params.postId).deeplinkUrl
    }

}

class GetPostLinkParams(val postId: Long) : DefParams()

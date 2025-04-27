package com.numplates.nomera3.modules.share.domain.usecase

import com.numplates.nomera3.modules.baseCore.BaseUseCaseCoroutine
import com.numplates.nomera3.modules.baseCore.DefParams
import com.numplates.nomera3.modules.share.data.entity.LinkResponse
import com.numplates.nomera3.modules.share.data.repository.ShareRepositoryImpl
import javax.inject.Inject

class GetCommunityLinkUseCase @Inject constructor(
    private val repository: ShareRepositoryImpl
) : BaseUseCaseCoroutine<GetCommunityLinkParams, LinkResponse> {

    override suspend fun execute(
        params: GetCommunityLinkParams,
        success: (LinkResponse) -> Unit,
        fail: (Throwable) -> Unit
    ) {
        repository.getCommunityLink(params.groupId, success, fail)
    }

}

class GetCommunityLinkParams(val groupId: Int) : DefParams()

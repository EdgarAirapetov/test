package com.numplates.nomera3.modules.communities.domain.usecase

import com.numplates.nomera3.modules.communities.data.entity.Community
import com.numplates.nomera3.modules.communities.data.repository.CommunityInformationRepository
import javax.inject.Inject

class GetCommunityInformationUseCase @Inject constructor(
    private val repository: CommunityInformationRepository
) : BaseUseCaseCoroutine<GetCommunityInformationUseCaseParams, Community?> {

    override suspend fun execute(
        params: GetCommunityInformationUseCaseParams,
        success: (Community?) -> Unit,
        fail: (Exception) -> Unit
    ) {
        repository.getCommunityInformationById(params.communityId, success, fail)
    }
}

